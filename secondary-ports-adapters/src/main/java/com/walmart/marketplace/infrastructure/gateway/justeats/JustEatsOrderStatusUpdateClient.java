package com.walmart.marketplace.infrastructure.gateway.justeats;

import com.walmart.common.infrastructure.ApiAction;
import com.walmart.common.metrics.MetricConstants;
import com.walmart.marketplace.infrastructure.gateway.justeats.dto.request.DenialErrorCode;
import com.walmart.marketplace.infrastructure.gateway.justeats.dto.request.OrderStatusUpdateRequest;
import java.net.URI;
import java.time.LocalDateTime;
import java.util.concurrent.Callable;
import java.util.concurrent.ThreadFactory;
import java.util.function.Function;
import javax.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriBuilder;
import reactor.core.publisher.Mono;

@Component
@Slf4j
public class JustEatsOrderStatusUpdateClient extends JustEatsBaseWebClient {

  @PostConstruct
  @Override
  public void initialize() {
    if (justEatsServiceConfiguration != null) {
      super.initialize();
    }
  }

  /**
   * Accept order.
   *
   * @param vendorOrderId order id of just eats.
   * @return success/failure.
   */
  public boolean acceptOrder(String vendorOrderId) {
    try {
      return executeApiWithRetries(acceptOrderCallable(vendorOrderId), ApiAction.JUST_EATS_API);
    } catch (Exception exception) {
      incrementMetricCounter(
          MetricConstants.MetricTypes.JUST_EATS_ACCEPT_ORDER.getType(), exception);
      throw exception;
    }
  }

  private Callable<Boolean> acceptOrderCallable(String vendorOrderId) {
    return () -> {
      logRequest(
          String.format(
              LOG_PREFIX, justEatsServiceConfiguration.getAcceptOrderApiUrl(), vendorOrderId));
      OrderStatusUpdateRequest orderStatusUpdateRequest =
          OrderStatusUpdateRequest.builder().happenedAt(LocalDateTime.now()).build();
      WebClient.ResponseSpec responseSpec =
          webClient
              .post()
              .uri(acceptOrderUriBuilderFunc(vendorOrderId))
              .header(API_KEY_HEADER, justEatsServiceConfiguration.getOrderStatusUpdateApiKey())
              .body(Mono.just(orderStatusUpdateRequest), OrderStatusUpdateRequest.class)
              .retrieve();
      ResponseEntity<String> responseEntity =
          captureMetrics(
              responseSpec,
              String.class,
              MetricConstants.MetricTypes.JUST_EATS_ACCEPT_ORDER.getType(),
              justEatsServiceConfiguration.getAcceptOrderApiUrl());
      logResponse(
          String.format(
              LOG_PREFIX, justEatsServiceConfiguration.getAcceptOrderApiUrl(), vendorOrderId),
          responseEntity);
      return isResponseSuccess(responseEntity, HttpStatus.NO_CONTENT);
    };
  }

  /**
   * Reject order.
   *
   * @param orderId just eats order id.
   * @return true or false based on success failure
   */
  public boolean rejectOrder(String orderId, DenialErrorCode justEatsDenialErrorCode) {
    try {
      return executeApiWithRetries(
          rejectOrderCallable(orderId, justEatsDenialErrorCode), ApiAction.JUST_EATS_API);
    } catch (Exception exception) {
      incrementMetricCounter(MetricConstants.MetricTypes.JUST_EATS_DENY_ORDER.getType(), exception);
      throw exception;
    }
  }

  private Callable<Boolean> rejectOrderCallable(
      String orderId, DenialErrorCode justEatsDenialErrorCode) {
    return () -> {
      logRequest(
          String.format(LOG_PREFIX, justEatsServiceConfiguration.getDenyOrderApiUrl(), orderId));
      OrderStatusUpdateRequest orderStatusUpdateRequest =
          OrderStatusUpdateRequest.builder()
              .happenedAt(LocalDateTime.now())
              .errorCode(justEatsDenialErrorCode)
              .errorMessage("Store is overloaded with orders. Please try after some time.")
              .build();
      WebClient.ResponseSpec responseSpec =
          webClient
              .post()
              .uri(denyOrderUriBuilderFunc(orderId))
              .header(API_KEY_HEADER, justEatsServiceConfiguration.getOrderStatusUpdateApiKey())
              .body(Mono.just(orderStatusUpdateRequest), OrderStatusUpdateRequest.class)
              .retrieve();
      ResponseEntity<String> responseEntity =
          captureMetrics(
              responseSpec,
              String.class,
              MetricConstants.MetricTypes.JUST_EATS_DENY_ORDER.getType(),
              justEatsServiceConfiguration.getAcceptOrderApiUrl());
      logResponse(
          String.format(LOG_PREFIX, justEatsServiceConfiguration.getAcceptOrderApiUrl(), orderId),
          responseEntity);
      return isResponseSuccess(responseEntity, HttpStatus.NO_CONTENT);
    };
  }

  @Override
  protected String getResourceBaseUri() {
    return justEatsServiceConfiguration.getOrderStatusUpdateBaseUri();
  }

  @Override
  protected ThreadFactory getThreadFactory() {
    return new BasicThreadFactory.Builder().namingPattern(THREAD_FACTORY_NAME).build();
  }

  @Override
  protected int getThreadPoolSize() {
    return justEatsServiceConfiguration.getThreadPoolSize();
  }

  private Function<UriBuilder, URI> acceptOrderUriBuilderFunc(String orderId) {
    return getUriBuilderFunction(justEatsServiceConfiguration.getAcceptOrderApiUrl(), orderId);
  }

  private Function<UriBuilder, URI> denyOrderUriBuilderFunc(String orderId) {
    return getUriBuilderFunction(justEatsServiceConfiguration.getDenyOrderApiUrl(), orderId);
  }
}
