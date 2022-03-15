package com.walmart.common.infrastructure.webclient;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeUnit;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.security.oauth2.client.AuthorizedClientServiceReactiveOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.InMemoryReactiveOAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.InMemoryReactiveClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.reactive.function.client.ServerOAuth2AuthorizedClientExchangeFilterFunction;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;
import reactor.netty.resources.ConnectionProvider;
import reactor.netty.tcp.TcpClient;

@Slf4j
@Getter
public abstract class Oauth2BaseWebClient extends BaseWebClient {

  @Override
  protected void initialize() {
    initExecutor();
    this.webClient = buildOauth2WebClient();
  }

  public abstract int getWriteTimeout();

  public abstract int getMaxConnectionCount();

  public abstract int getPendingAcquireTimeoutMs();

  public abstract int getIdleTimeoutMs();

  public abstract String getClientId();

  public abstract String getClientSecret();

  public abstract List<String> getScopes();

  public abstract String getAccessTokenUri();

  public abstract String getOauth2ClientRegistrationId();

  /**
   * Build the Oauth2 WebClient
   *
   * @return
   */
  protected WebClient buildOauth2WebClient() {

    ReactiveClientRegistrationRepository clientRegistrations = getRegistration();
    ReactiveOAuth2AuthorizedClientService clientService =
        new InMemoryReactiveOAuth2AuthorizedClientService(clientRegistrations);
    ReactiveOAuth2AuthorizedClientManager authorizedClientManager =
        new AuthorizedClientServiceReactiveOAuth2AuthorizedClientManager(
            clientRegistrations, clientService);
    ServerOAuth2AuthorizedClientExchangeFilterFunction oauthFilter =
        new ServerOAuth2AuthorizedClientExchangeFilterFunction(authorizedClientManager);
    oauthFilter.setDefaultClientRegistrationId(getOauth2ClientRegistrationId());

    return WebClient.builder()
        .filters(
            exchangeFilterFunctions -> {
              exchangeFilterFunctions.add(oauthFilter);
              exchangeFilterFunctions.add(getCustomExceptionFilter(clientService));
            })
        .clientConnector(new ReactorClientHttpConnector(HttpClient.from(createTcpClient())))
        .baseUrl(getResourceBaseUri())
        .build();
  }

  /**
   * Create the Client Registration object
   *
   * @return
   */
  protected ReactiveClientRegistrationRepository getRegistration() {

    ClientRegistration registration =
        ClientRegistration.withRegistrationId(getOauth2ClientRegistrationId())
            .tokenUri(getAccessTokenUri())
            .clientId(getClientId())
            .clientSecret(getClientSecret())
            .authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
            .scope(getScopes())
            .build();
    return new InMemoryReactiveClientRegistrationRepository(registration);
  }

  /**
   * Generate customer exception filter
   *
   * @return
   */
  protected ExchangeFilterFunction getCustomExceptionFilter(
      ReactiveOAuth2AuthorizedClientService clientService) {
    return ExchangeFilterFunction.ofResponseProcessor(
        clientResponse -> {
          HttpStatus statusCode;
          if (clientResponse != null
              && clientResponse.statusCode() != null
              && clientResponse.statusCode().isError()) {

            statusCode = clientResponse.statusCode();

            if (isAuthnOrAuthzErrorCode(statusCode)) {
              log.error("Removing Authorized Client for statusCode :{}", statusCode);
              clientService
                  .removeAuthorizedClient(getOauth2ClientRegistrationId(), "anonymousUser")
                  .subscribe();
            }

            if (isServerError(statusCode) || isAuthnOrAuthzErrorCode(statusCode)) {
              return getResponseProcessorForThirdPartyException(clientResponse);
            } else {
              return getResponseProcessorForBadRequestException(clientResponse);
            }
          }
          return Mono.just(clientResponse);
        });
  }

  private boolean isAuthnOrAuthzErrorCode(HttpStatus statusCode) {
    return HttpStatus.UNAUTHORIZED.equals(statusCode) || HttpStatus.FORBIDDEN.equals(statusCode);
  }

  /**
   * Create a tcp client
   *
   * @return
   */
  protected TcpClient createTcpClient() {

    ConnectionProvider provider =
        ConnectionProvider.builder("fixed")
            .maxConnections(getMaxConnectionCount())
            .pendingAcquireTimeout(Duration.ofMillis(getPendingAcquireTimeoutMs()))
            .maxIdleTime(Duration.ofMillis(getIdleTimeoutMs()))
            .build();

    return TcpClient.create(provider)
        .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, getConnTimeout())
        // .wiretap(true) //uncomment  debug in log4j2 xml for TcpClient
        .doOnConnected(
            connection -> {
              connection.addHandlerLast(
                  new ReadTimeoutHandler(getReadTimeout(), TimeUnit.MILLISECONDS));
              connection.addHandlerLast(
                  new WriteTimeoutHandler(getWriteTimeout(), TimeUnit.MILLISECONDS));
            });
  }
}
