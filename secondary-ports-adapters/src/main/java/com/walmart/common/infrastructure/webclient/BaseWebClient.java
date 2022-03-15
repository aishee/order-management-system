package com.walmart.common.infrastructure.webclient;

import com.walmart.common.domain.valueobject.MetricsValueObject;
import com.walmart.common.infrastructure.ApiAction;
import com.walmart.common.metrics.MetricService;
import com.walmart.oms.domain.error.exception.OMSBadRequestException;
import com.walmart.oms.domain.error.exception.OMSThirdPartyException;
import io.github.resilience4j.bulkhead.BulkheadRegistry;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.decorators.Decorators;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryRegistry;
import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import java.io.IOException;
import java.net.URI;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.function.Supplier;
import javax.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriBuilder;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.netty.http.client.HttpClient;
import reactor.netty.tcp.TcpClient;

@Slf4j
public abstract class BaseWebClient extends WebClientExecutor implements WebClientMetrics {

  protected WebClient webClient;

  @Autowired protected CircuitBreakerRegistry circuitBreakerRegistry;
  @Autowired protected RetryRegistry retryRegistry;
  @Autowired protected BulkheadRegistry bulkheadRegistry;
  @Autowired protected MetricService metricService;

  protected static boolean isServerError(HttpStatus statusCode) {
    return HttpStatus.Series.SERVER_ERROR.equals(statusCode.series());
  }

  /**
   * Creates a Function that returns a UriBuilder from the path and uriVariables
   *
   * @param path {@code API path}
   * @param uriVariables {@code path variables}
   * @return {@link Function}
   */
  protected static Function<UriBuilder, URI> getUriBuilderFunction(
      String path, Object... uriVariables) {
    return uriBuilder -> uriBuilder.path(path).build(uriVariables);
  }

  /**
   * Creates a Function that returns a UriBuilder from the path and query parameters.
   *
   * @param path {@code API path}
   * @param queryParams {@code query parameters}
   * @return {@link Function}
   */
  protected static Function<UriBuilder, URI> getUriBuilderFunctionFromQueryParam(
      String path, MultiValueMap<String, String> queryParams) {
    return uriBuilder -> uriBuilder.path(path).queryParams(queryParams).build();
  }

  protected abstract String getResourceBaseUri();

  protected abstract int getReadTimeout();

  protected abstract int getConnTimeout();

  protected abstract String getClientName();

  protected void initialize() {
    webClient =
        WebClient.builder()
            .baseUrl(getResourceBaseUri())
            .filters(
                // Response Filter to check for Error.
                exchangeFilterFunctions -> exchangeFilterFunctions.add(getCustomExceptionFilter()))
            .clientConnector(new ReactorClientHttpConnector(HttpClient.from(getTCPClient())))
            .build();
    initExecutor();
  }

  /** Method to get TCPClient. */
  private TcpClient getTCPClient() {
    return TcpClient.create()
        .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, getConnTimeout())
        .doOnConnected(
            connection ->
                connection.addHandlerLast(
                    new ReadTimeoutHandler(getReadTimeout(), TimeUnit.MILLISECONDS)));
  }

  /**
   * Generate customer exception filter.
   *
   * @return
   */
  protected ExchangeFilterFunction getCustomExceptionFilter() {
    return ExchangeFilterFunction.ofResponseProcessor(
        clientResponse -> {
          HttpStatus statusCode = clientResponse.statusCode();
          if (statusCode.isError()) {

            // for 5XX series exception, throw ThirdPartyException. (Retry)
            if (isServerError(statusCode)) {
              return getResponseProcessorForThirdPartyException(clientResponse);
            } else {
              // For 4XX series exception,throw BadRequestException (Non-Retryable)
              return getResponseProcessorForBadRequestException(clientResponse);
            }
          }
          return Mono.just(clientResponse);
        });
  }

  protected boolean isResponseSuccess(
      ResponseEntity<String> responseEntity, HttpStatus successStatus) {
    return responseEntity != null && successStatus.equals(responseEntity.getStatusCode());
  }

  protected Mono<ClientResponse> getResponseProcessorForThirdPartyException(
      ClientResponse clientResponse) {
    HttpStatus statusCode = clientResponse.statusCode();
    return clientResponse
        .bodyToMono(String.class)
        .flatMap(
            errorBody -> {
              String errorMessage =
                  String.format(
                      "Error Response httpStatus:%s responseBody :%s URI:%s",
                      statusCode, errorBody, getResourceBaseUri());
              log.error(errorMessage);
              return Mono.error(
                  new OMSThirdPartyException(
                      "httpStatus:" + statusCode + ",responseBody:" + errorBody));
            });
  }

  protected Mono<ClientResponse> getResponseProcessorForBadRequestException(
      ClientResponse clientResponse) {
    HttpStatus statusCode = clientResponse.statusCode();
    return clientResponse
        .bodyToMono(String.class)
        .flatMap(
            errorBody -> {
              String errorMessage =
                  String.format(
                      "Error Response httpStatus:%s responseBody :%s URI:%s",
                      statusCode, errorBody, getResourceBaseUri());
              log.error(errorMessage);
              return Mono.error(new OMSBadRequestException(errorMessage));
            });
  }

  protected <T> Mono<T> handleTimeOutForWebClientAPI(String message, Throwable ex) {
    if (ex instanceof IOException) {
      String errorMessage = String.format("Connection timeout exception %s", message);
      log.error(errorMessage, ex);
      return Mono.error(new OMSThirdPartyException(errorMessage));
    }
    return Mono.error(ex);
  }

  protected void incrementMetricCounter(String type, Exception exception) {
    String exceptionType = exception.getClass().getSimpleName();
    metricService.incrementExceptionCounterByType(
        getMetricsExceptionCounterName(), type, exceptionType);
  }

  @PreDestroy
  private void destroy() {
    if (retryScheduledExecutorService != null) {
      log.info("Shutting down retryScheduledExecutorService");
      retryScheduledExecutorService.shutdown();
    }
  }

  /**
   * Decorates the http webClient Callable with Retries
   *
   * @param apiCallable
   * @param <T>
   * @return api call response of type T
   */
  public <T> T executeApiWithRetries(Callable<T> apiCallable, ApiAction apiAction) {

    final String ERROR_AFTER_RETRIES_COMPLETED = "Error after all retries completed ";
    final String ERROR_AFTER_BAD_REQUEST = "OMS Bad Request Exception 400 ";

    try {

      return Decorators.ofCallable(apiCallable)
          .withCircuitBreaker(
              circuitBreakerRegistry.circuitBreaker(apiAction.getCircuitBreakerName()))
          .withBulkhead(bulkheadRegistry.bulkhead(getClientName()))
          .withRetry(retryRegistry.retry(getClientName()))
          .decorate()
          .call();

    } catch (OMSThirdPartyException e) {
      log.error(ERROR_AFTER_RETRIES_COMPLETED, e);
      throw e;
    } catch (OMSBadRequestException e) {
      log.error(ERROR_AFTER_BAD_REQUEST, e);
      throw e;
    } catch (Exception e) {
      log.error(ERROR_AFTER_RETRIES_COMPLETED, e);
      throw new OMSThirdPartyException("Exception after retries : " + e.getMessage());
    }
  }

  /**
   * Decorates the API call with Retries
   *
   * @param circuitBreakerSupplier
   * @return
   */
  protected <T> CompletableFuture<T> executeApiWithRetriesNonBlocking(
      Supplier<CompletionStage<T>> circuitBreakerSupplier, String type, ApiAction action) {

    final String ERROR_AFTER_RETRIES_COMPLETED = "Error after all retries completed ";
    final String ERROR_AFTER_BAD_REQUEST = "OMS Bad Request Exception 400 ";
    final Supplier<CompletionStage<T>> retryDecoratedSupplier =
        Retry.decorateCompletionStage(
            retryRegistry.retry(getClientName()),
            retryScheduledExecutorService,
            io.github.resilience4j.bulkhead.Bulkhead.decorateSupplier(
                bulkheadRegistry.bulkhead(getClientName()), circuitBreakerSupplier));
    return circuitBreakerRegistry
        .circuitBreaker(action.getCircuitBreakerName())
        .decorateCompletionStage(retryDecoratedSupplier)
        .get()
        .toCompletableFuture()
        .handle(
            (response, exception) -> {
              if (exception != null) {
                if (exception instanceof OMSBadRequestException) {
                  OMSBadRequestException omsBadRequestException =
                      new OMSBadRequestException(ERROR_AFTER_BAD_REQUEST, exception);
                  log.error(ERROR_AFTER_BAD_REQUEST, exception);
                  incrementMetricCounter(type, omsBadRequestException);
                  throw omsBadRequestException;
                } else {
                  OMSThirdPartyException omsThirdPartyException =
                      new OMSThirdPartyException(ERROR_AFTER_RETRIES_COMPLETED, exception);
                  log.error(ERROR_AFTER_RETRIES_COMPLETED, exception);
                  incrementMetricCounter(type, omsThirdPartyException);
                  throw omsThirdPartyException;
                }
              }
              return response;
            });
  }

  protected <T> ResponseEntity<T> captureMetrics(
      WebClient.ResponseSpec responseSpec,
      Class<T> type,
      String metricsType,
      String timeoutMessage) {
    HttpStatus status = HttpStatus.OK;
    long startTime = System.currentTimeMillis();
    try {
      return responseSpec
          .toEntity(type)
          .onErrorResume(ex -> handleTimeOutForWebClientAPI(timeoutMessage, ex))
          .subscribeOn(Schedulers.fromExecutor(executorService))
          .block();
    } catch (OMSBadRequestException omsBadRequestException) {
      status = HttpStatus.BAD_REQUEST;
      throw omsBadRequestException;
    } catch (Exception exception) {
      status = HttpStatus.INTERNAL_SERVER_ERROR;
      throw exception;
    } finally {
      long duration = System.currentTimeMillis() - startTime;
      MetricsValueObject metricsValueObject =
          getMetricsValueObject(metricsType, duration, String.valueOf(status.value()));
      String message =
          String.format(
              "action=%s Completed, %s",
              metricsType, metricService.recordExecutionTime(metricsValueObject));
      log.info(message);
    }
  }
}
