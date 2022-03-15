package com.walmart.marketplace.infrastructure.gateway.uber;

import static java.util.Objects.requireNonNull;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.walmart.common.domain.valueobject.MetricsValueObject;
import com.walmart.common.infrastructure.ApiAction;
import com.walmart.common.infrastructure.config.Resilience4jConfig;
import com.walmart.common.infrastructure.webclient.Oauth2BaseWebClient;
import com.walmart.common.metrics.MetricConstants;
import com.walmart.common.metrics.MetricConstants.MetricCounters;
import com.walmart.common.metrics.MetricConstants.MetricTypes;
import com.walmart.marketplace.infrastructure.gateway.uber.dto.request.UberAcceptOrderRequest;
import com.walmart.marketplace.infrastructure.gateway.uber.dto.request.UberCancelOrderRequest;
import com.walmart.marketplace.infrastructure.gateway.uber.dto.request.UberCancelReason;
import com.walmart.marketplace.infrastructure.gateway.uber.dto.request.UberDenyOrderRequest;
import com.walmart.marketplace.infrastructure.gateway.uber.dto.request.UberPatchCartRequest;
import com.walmart.marketplace.infrastructure.gateway.uber.dto.request.UberUpdateItemRequest;
import com.walmart.marketplace.infrastructure.gateway.uber.dto.response.UberOrder;
import com.walmart.marketplace.infrastructure.gateway.uber.dto.response.UberStore;
import com.walmart.marketplace.infrastructure.gateway.uber.report.dto.UberReportReq;
import com.walmart.marketplace.order.domain.entity.UpdateItemInfo;
import com.walmart.marketplace.order.domain.uber.PatchCartInfo;
import com.walmart.oms.domain.error.exception.OMSBadRequestException;
import com.walmart.oms.domain.error.exception.OMSThirdPartyException;
import io.strati.configuration.annotation.ManagedConfiguration;
import java.net.URI;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.function.Function;
import java.util.function.Supplier;
import javax.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.WebClient.ResponseSpec;
import org.springframework.web.util.UriBuilder;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

/**
 * This class uses Webclient to integrate with Uber Order APIs and implements Oauth2BaseWebClient
 */
@Component
@Slf4j
public class UberWebClient extends Oauth2BaseWebClient {

  private static final String UBER = "UBER";
  private static final String LOG_PREFIX = "%s UberOrderId=%s";
  private static final String THREAD_FACTORY_NAME = "Uber-thread-pool-%d";

  @ManagedConfiguration private UberServiceConfiguration uberConfig;
  @ManagedConfiguration private Resilience4jConfig resilience4jConfig;

  @PostConstruct
  @Override
  public void initialize() {
    if (uberConfig != null) {
      super.initialize();
    }
    initialiseScheduledThreadPool();
  }

  private void initialiseScheduledThreadPool() {
    log.info(
        "Initialising scheduledExecutorService with {} threads for client {}",
        resilience4jConfig.getRetryScheduledThreadPoolSize(),
        getClientName());
    this.retryScheduledExecutorService =
        Executors.newScheduledThreadPool(
            resilience4jConfig.getRetryScheduledThreadPoolSize(),
            new ThreadFactoryBuilder().setNameFormat("UberEats-Web-Client-Thread-%d").build());
  }

  private MultiValueMap<String, String> getStoreQueryParams() {
    MultiValueMap<String, String> storeQueryParams = new LinkedMultiValueMap<>();
    storeQueryParams.add("limit", String.valueOf(uberConfig.getUberStoreQueryLimit()));
    return storeQueryParams;
  }

  @Override
  protected String getClientName() {
    return UBER;
  }

  @Override
  protected ThreadFactory getThreadFactory() {
    return new BasicThreadFactory.Builder().namingPattern(THREAD_FACTORY_NAME).build();
  }

  @Override
  protected int getThreadPoolSize() {
    return uberConfig.getThreadPoolSize();
  }

  @Override
  public String getMetricsExceptionCounterName() {
    return MetricCounters.UBER_EXCEPTION.getCounter();
  }

  @Override
  public String getMetricsCounterName() {
    return MetricConstants.MetricCounters.UBER_INVOCATION.getCounter();
  }

  /**
   * Accept an uber order
   *
   * @param uberOrderId
   * @param reason
   * @return
   */
  public boolean acceptUberOrder(String uberOrderId, String reason) {
    try {
      return executeApiWithRetries(
          acceptUberOrderCallable(uberOrderId, reason), ApiAction.ACCEPT_ORDER);
    } catch (Exception exception) {
      incrementMetricCounter(MetricTypes.UBER_ACCEPT_ORDER.getType(), exception);
      throw exception;
    }
  }

  private Callable<Boolean> acceptUberOrderCallable(String uberOrderId, String reason) {
    return () -> {
      logRequest(String.format(LOG_PREFIX, uberConfig.getUberAcceptOrderUri(), uberOrderId));
      UberAcceptOrderRequest acceptOrderReq = new UberAcceptOrderRequest(reason);
      ResponseSpec responseSpec =
          webClient
              .post()
              .uri(acceptOrderUriBuilderFunc(uberOrderId))
              .body(Mono.just(acceptOrderReq), UberAcceptOrderRequest.class)
              .retrieve();
      ResponseEntity<String> responseEntity =
          captureMetrics(
              responseSpec,
              String.class,
              MetricTypes.UBER_ACCEPT_ORDER.getType(),
              uberConfig.getUberAcceptOrderUri());
      logResponse(
          String.format(LOG_PREFIX, uberConfig.getUberAcceptOrderUri(), uberOrderId),
          responseEntity);
      return isResponseSuccess(responseEntity, HttpStatus.NO_CONTENT);
    };
  }

  /**
   * Get an Uber Order
   *
   * @param uberOrderId
   * @return
   */
  public UberOrder getUberOrder(String uberOrderId) {
    try {
      return executeApiWithRetries(getUberOrderCallable(uberOrderId), ApiAction.GET_ORDER);
    } catch (Exception exception) {
      incrementMetricCounter(MetricTypes.UBER_GET_ORDER.getType(), exception);
      throw exception;
    }
  }

  private Callable<UberOrder> getUberOrderCallable(String uberOrderId) {
    return () -> {
      logRequest(String.format(LOG_PREFIX, uberConfig.getUberGetOrderUri(), uberOrderId));
      ResponseSpec responseSpec =
          webClient.get().uri(getOrderUriBuilderFunc(uberOrderId)).retrieve();
      ResponseEntity<UberOrder> uberOrderResponseEntity =
          captureMetrics(
              responseSpec,
              UberOrder.class,
              MetricTypes.UBER_GET_ORDER.getType(),
              uberConfig.getUberGetOrderUri());
      return getOrderResponseHandling(uberOrderResponseEntity, uberOrderId);
    };
  }

  private UberOrder getOrderResponseHandling(
      ResponseEntity<UberOrder> uberOrderResponseEntity, String uberOrderId) {
    UberOrder uberOrder = requireNonNull(uberOrderResponseEntity).getBody();
    if (uberOrder == null) {
      String errorMsg =
          String.format(
              "Uber GET ORDER API returned with null response for vendorOrderId : %s", uberOrderId);
      log.error(errorMsg);
      throw new OMSThirdPartyException(errorMsg);
    }
    return uberOrder;
  }

  /**
   * Cancel a uber order
   *
   * @param uberOrderId
   * @param reason
   * @param reasonDetails
   * @return
   */
  public boolean cancelUberOrder(String uberOrderId, String reason, String reasonDetails) {
    try {
      return executeApiWithRetries(
          cancelUberOrderCallable(uberOrderId, reason, reasonDetails), ApiAction.CANCEL_ORDER);
    } catch (Exception exception) {
      incrementMetricCounter(MetricTypes.UBER_CANCEL_ORDER.getType(), exception);
      throw exception;
    }
  }

  private Callable<Boolean> cancelUberOrderCallable(
      String uberOrderId, String reason, String reasonDetails) {
    return () -> {
      logRequest(String.format(LOG_PREFIX, uberConfig.getUberCancelOrderUri(), uberOrderId));
      UberCancelOrderRequest cancelOrderRequest =
          new UberCancelOrderRequest(UberCancelReason.getEnumFromStr(reason), reasonDetails);
      ResponseSpec responseSpec =
          webClient
              .post()
              .uri(cancelOrderUriBuilderFunc(uberOrderId))
              .body(Mono.just(cancelOrderRequest), UberCancelOrderRequest.class)
              .retrieve();
      ResponseEntity<String> responseEntity =
          captureMetrics(
              responseSpec,
              String.class,
              MetricTypes.UBER_CANCEL_ORDER.getType(),
              uberConfig.getUberCancelOrderUri());
      logResponse(
          String.format(LOG_PREFIX, uberConfig.getUberCancelOrderUri(), uberOrderId),
          responseEntity);
      return isResponseSuccess(responseEntity, HttpStatus.OK);
    };
  }

  /**
   * Deny a uber order
   *
   * @param uberOrderId
   * @param denialReason
   * @param invalidItems
   * @param outOfStockItems
   * @return
   */
  public boolean denyUberOrder(
      String uberOrderId,
      String denialReason,
      List<String> invalidItems,
      List<String> outOfStockItems) {
    try {
      return executeApiWithRetries(
          denyUberOrderCallable(uberOrderId, denialReason, invalidItems, outOfStockItems),
          ApiAction.DENY_ORDER);
    } catch (Exception exception) {
      incrementMetricCounter(MetricTypes.UBER_DENY_ORDER.getType(), exception);
      throw exception;
    }
  }

  private Callable<Boolean> denyUberOrderCallable(
      String uberOrderId,
      String denialReason,
      List<String> invalidItems,
      List<String> outOfStockItems) {
    return () -> {
      logRequest(String.format(LOG_PREFIX, uberConfig.getUberDenyOrderUri(), uberOrderId));
      UberDenyOrderRequest uberDenyOrderRequest =
          UberRequestBuilder.buildDenyOrderRequest(denialReason, invalidItems, outOfStockItems);
      ResponseSpec responseSpec =
          webClient
              .post()
              .uri(denyOrderUriBuilderFunc(uberOrderId))
              .body(Mono.just(uberDenyOrderRequest), UberDenyOrderRequest.class)
              .retrieve();
      ResponseEntity<String> responseEntity =
          captureMetrics(
              responseSpec,
              String.class,
              MetricTypes.UBER_DENY_ORDER.getType(),
              uberConfig.getUberDenyOrderUri());
      logResponse(
          String.format(LOG_PREFIX, uberConfig.getUberDenyOrderUri(), uberOrderId), responseEntity);
      return isResponseSuccess(responseEntity, HttpStatus.NO_CONTENT);
    };
  }

  /**
   * Invoke Uber's Patch cart endpoint
   *
   * @param patchCartInfo
   * @return
   */
  public CompletableFuture<Boolean> patchCart(PatchCartInfo patchCartInfo) {
    final Supplier<CompletionStage<Boolean>> circuitBreakerSupplier =
        () -> invokeUberPatchCart(patchCartInfo);
    return executeApiWithRetriesNonBlocking(
        circuitBreakerSupplier, MetricTypes.UBER_PATCH_CART.getType(), ApiAction.PATCH_CART);
  }

  protected CompletableFuture<Boolean> invokeUberPatchCart(PatchCartInfo patchCartInfo) {
    UberPatchCartRequest uberPatchCartRequest;
    logRequest(
        String.format(
            LOG_PREFIX, uberConfig.getUberPatchCartUri(), patchCartInfo.getVendorOrderId()));
    uberPatchCartRequest = UberRequestBuilder.buildUberPatchCart(patchCartInfo);
    long startTime = System.currentTimeMillis();
    return webClient
        .patch()
        .uri(patchCartUriBuilderFunc(patchCartInfo.getVendorOrderId()))
        .body(Mono.just(uberPatchCartRequest), UberPatchCartRequest.class)
        .retrieve()
        .toEntity(String.class)
        .onErrorResume(
            ex ->
                handleTimeOutForWebClientAPI(
                    String.format(
                        LOG_PREFIX,
                        uberConfig.getUberPatchCartUri(),
                        patchCartInfo.getVendorOrderId()),
                    ex))
        .map(responseEntity -> getPatchCartResponse(patchCartInfo, responseEntity))
        .subscribeOn(Schedulers.fromExecutor(executorService))
        .toFuture()
        .whenComplete(
            (response, exception) -> {
              long duration = System.currentTimeMillis() - startTime;
              processPatchCartResponse(patchCartInfo.getVendorOrderId(), duration, exception);
            });
  }

  private void processPatchCartResponse(String vendorOrderId, long duration, Throwable exception) {
    HttpStatus status = HttpStatus.OK;
    if (exception != null) {
      status = getHttpStatusFromException(exception);
      log.error("Patch Cart Uber call failed for vendorOrderId : {}", vendorOrderId);
    } else {
      log.info("Patch Cart Uber call was successful for vendorOrderId : {}", vendorOrderId);
    }
    MetricsValueObject metricsValueObject =
        getMetricsValueObject(
            MetricTypes.UBER_PATCH_CART.getType(), duration, String.valueOf(status.value()));
    log.info(
        "action=UberPatchCart Completed, {}",
        metricService.recordExecutionTime(metricsValueObject));
  }

  private HttpStatus getHttpStatusFromException(Throwable exception) {
    HttpStatus status;
    if (exception instanceof OMSBadRequestException) {
      status = HttpStatus.BAD_REQUEST;
    } else {
      status = HttpStatus.INTERNAL_SERVER_ERROR;
    }
    return status;
  }

  private Boolean getPatchCartResponse(
      PatchCartInfo patchCartInfo, ResponseEntity<String> responseEntity) {
    logResponse(
        String.format(
            LOG_PREFIX, uberConfig.getUberPatchCartUri(), patchCartInfo.getVendorOrderId()),
        responseEntity);
    return isResponseSuccess(responseEntity, HttpStatus.NO_CONTENT);
  }

  /**
   * Invoke Uber's update item endpoint to mark the nil picked and partial picked items as
   * OUT_OF_STOCK for the same day
   *
   * @param updateItemInfo
   * @return
   */
  public CompletableFuture<Boolean> updateItem(
      UpdateItemInfo updateItemInfo, String externalItemId) {
    final Supplier<CompletionStage<Boolean>> circuitBreakerSupplier =
        () -> invokeUberUpdateItem(updateItemInfo, externalItemId);
    return executeApiWithRetriesNonBlocking(
        circuitBreakerSupplier, MetricTypes.UBER_UPDATE_ITEM.getType(), ApiAction.UPDATE_ITEM);
  }

  protected CompletableFuture<Boolean> invokeUberUpdateItem(
      UpdateItemInfo updateItemInfo, String externalItemId) {
    UberUpdateItemRequest uberUpdateItemRequest;
    logRequest(
        String.format(
            LOG_PREFIX, uberConfig.getUberUpdateItemUri(), updateItemInfo.getVendorOrderId()));
    uberUpdateItemRequest = UberRequestBuilder.buildUberUpdateItemRequest(updateItemInfo);
    long startTime = System.currentTimeMillis();
    return webClient
        .post()
        .uri(updateItemUriBuilderFunc(updateItemInfo.getVendorStoreId(), externalItemId))
        .body(Mono.just(uberUpdateItemRequest), UberUpdateItemRequest.class)
        .retrieve()
        .toEntity(String.class)
        .onErrorResume(
            ex ->
                handleTimeOutForWebClientAPI(
                    String.format(
                        LOG_PREFIX,
                        uberConfig.getUberUpdateItemUri(),
                        updateItemInfo.getVendorOrderId()),
                    ex))
        .map(responseEntity -> getUpdateItemResponse(updateItemInfo, responseEntity))
        .subscribeOn(Schedulers.fromExecutor(executorService))
        .toFuture()
        .whenComplete(
            (response, exception) -> {
              long duration = System.currentTimeMillis() - startTime;
              HttpStatus status = HttpStatus.OK;
              if (exception != null) {
                status = getHttpStatusFromException(exception);
                log.error(
                    "Update Item Uber call failed for item : {}, vendorOrderId : {}",
                    externalItemId,
                    updateItemInfo.getVendorOrderId());
              } else {
                log.info(
                    "Update Item Uber call was successful for item : {}, vendorOrderId : {}",
                    externalItemId,
                    updateItemInfo.getVendorOrderId());
              }
              addMetricsForUpdateItem(duration, status);
            });
  }

  private Boolean getUpdateItemResponse(
      UpdateItemInfo updateItemInfo, ResponseEntity<String> responseEntity) {
    logResponse(
        String.format(
            LOG_PREFIX, uberConfig.getUberUpdateItemUri(), updateItemInfo.getVendorOrderId()),
        responseEntity);
    return isResponseSuccess(responseEntity, HttpStatus.NO_CONTENT);
  }

  private void addMetricsForUpdateItem(long duration, HttpStatus status) {
    MetricsValueObject metricsValueObject =
        getMetricsValueObject(
            MetricTypes.UBER_UPDATE_ITEM.getType(), duration, String.valueOf(status.value()));
    log.info(
        "action=UberUpdateItem Completed, {}",
        metricService.recordExecutionTime(metricsValueObject));
  }

  /**
   * Get All Uber Stores
   *
   * @return {@link UberStore}
   */
  public UberStore getUberStore() {
    try {
      return executeApiWithRetries(getUberStoreCallable(), ApiAction.STORE_API);
    } catch (Exception exception) {
      incrementMetricCounter(MetricTypes.UBER_GET_STORES.getType(), exception);
      throw exception;
    }
  }

  private Callable<UberStore> getUberStoreCallable() {
    MultiValueMap<String, String> storeQueryParams = getStoreQueryParams();
    return () -> {
      logRequest(
          String.format(
              "Get Store Api: %s, queryParam: %s", uberConfig.getUberStoreUri(), storeQueryParams));
      ResponseSpec responseSpec =
          webClient
              .get()
              .uri(
                  getUriBuilderFunctionFromQueryParam(
                      uberConfig.getUberStoreUri(), storeQueryParams))
              .retrieve();
      ResponseEntity<UberStore> storeResponseEntity =
          captureMetrics(
              responseSpec,
              UberStore.class,
              MetricTypes.UBER_GET_STORES.getType(),
              uberConfig.getUberStoreUri());
      logResponse(uberConfig.getUberStoreUri(), storeResponseEntity);
      return storeResponseHandling(storeResponseEntity);
    };
  }

  /**
   * Invoke Uber Report API
   *
   * @param uberReportReq {@link UberReportReq}
   * @return {@link String}
   */
  public String invokeUberReport(UberReportReq uberReportReq) {
    try {
      return executeApiWithRetries(getUberReportCallable(uberReportReq), ApiAction.REPORT_API);
    } catch (Exception exception) {
      incrementMetricCounter(MetricTypes.UBER_GET_REPORT.getType(), exception);
      throw exception;
    }
  }

  private Callable<String> getUberReportCallable(UberReportReq uberReportReq) {
    return () -> {
      String errMsg =
          String.format(
              "Uber Web Client Custom Exception Filter was not able to handle this exception for report reportType:%s, storeUUIDs:%s, startDate:%s, endDate:%s",
              uberReportReq.getReportType(),
              uberReportReq.getStoreUUIDs(),
              uberReportReq.getStartDate(),
              uberReportReq.getEndDate());
      logRequest(uberConfig.getUberReportUri(), uberReportReq);
      // This blocking call will be non-blocking in next changes.
      ResponseSpec responseSpec =
          webClient
              .post()
              .uri(getGenerateReportUriBuilderFunc())
              .body(Mono.just(uberReportReq), UberReportReq.class)
              .retrieve();
      ResponseEntity<String> responseEntity =
          captureMetrics(responseSpec, String.class, MetricTypes.UBER_GET_REPORT.getType(), errMsg);
      logResponse(uberConfig.getUberReportUri(), responseEntity);
      return reportResponseHandling(responseEntity, uberReportReq);
    };
  }

  @Override
  public int getWriteTimeout() {
    return uberConfig.getWriteTimeout();
  }

  @Override
  public int getMaxConnectionCount() {
    return uberConfig.getMaxConnectionCount();
  }

  @Override
  public int getPendingAcquireTimeoutMs() {
    return uberConfig.getPendingAcquireTimeoutMs();
  }

  @Override
  public int getIdleTimeoutMs() {
    return uberConfig.getIdleTimeoutMs();
  }

  @Override
  public String getClientId() {
    return uberConfig.getClientId();
  }

  @Override
  public String getClientSecret() {
    return uberConfig.getClientSecret();
  }

  @Override
  public List<String> getScopes() {
    return uberConfig.getScopes();
  }

  @Override
  public String getAccessTokenUri() {
    return uberConfig.getAccessTokenUri();
  }

  @Override
  public String getResourceBaseUri() {
    return uberConfig.getUberBaseUri();
  }

  @Override
  protected int getReadTimeout() {
    return uberConfig.getReadTimeout();
  }

  @Override
  protected int getConnTimeout() {
    return uberConfig.getConnTimeout();
  }

  @Override
  public boolean isLogEnabled() {
    return uberConfig.isLoggingEnabled();
  }

  @Override
  public String getOauth2ClientRegistrationId() {
    return uberConfig.getOauth2ClientRegistrationId();
  }

  private Function<UriBuilder, URI> cancelOrderUriBuilderFunc(String uberOrderId) {
    return getUriBuilderFunction(uberConfig.getUberCancelOrderUri(), uberOrderId);
  }

  private Function<UriBuilder, URI> acceptOrderUriBuilderFunc(String uberOrderId) {
    return getUriBuilderFunction(uberConfig.getUberAcceptOrderUri(), uberOrderId);
  }

  private Function<UriBuilder, URI> getOrderUriBuilderFunc(String uberOrderId) {
    return getUriBuilderFunction(uberConfig.getUberGetOrderUri(), uberOrderId);
  }

  private Function<UriBuilder, URI> denyOrderUriBuilderFunc(String uberOrderId) {
    return getUriBuilderFunction(uberConfig.getUberDenyOrderUri(), uberOrderId);
  }

  private Function<UriBuilder, URI> patchCartUriBuilderFunc(String uberOrderId) {
    return getUriBuilderFunction(uberConfig.getUberPatchCartUri(), uberOrderId);
  }

  private Function<UriBuilder, URI> updateItemUriBuilderFunc(String storeId, String itemId) {
    return getUriBuilderFunction(uberConfig.getUberUpdateItemUri(), storeId, itemId);
  }

  private Function<UriBuilder, URI> getGenerateReportUriBuilderFunc() {
    return uriBuilder -> uriBuilder.path(uberConfig.getUberReportUri()).build();
  }

  private String reportResponseHandling(
      ResponseEntity<String> reportResponseEntity, UberReportReq uberReportReq) {
    String workFlowId = requireNonNull(reportResponseEntity).getBody();
    if (workFlowId == null) {
      return emptyResponseHandling(uberReportReq);
    }
    return workFlowId;
  }

  private UberStore storeResponseHandling(ResponseEntity<UberStore> uberStoreResponseEntity) {
    UberStore uberStore = requireNonNull(uberStoreResponseEntity).getBody();
    if (uberStore == null) {
      String errorMsg = "Uber Store API returned with null store response.";
      log.error(errorMsg);
      throw new OMSThirdPartyException(errorMsg);
    }
    List<String> storeIds = uberStore.getStoreIds();
    if (storeIds.isEmpty()) {
      String errorMsg = String.format("No Store is configured at Uber, stores:%s", uberStore);
      log.error(errorMsg);
      throw new OMSBadRequestException(errorMsg);
    }
    log.info("Report Request Being Invoked for Store list:{}", storeIds);
    return uberStore;
  }

  private String emptyResponseHandling(UberReportReq uberReportReq) {
    String errorMessage =
        String.format(
            "Report API returned with Empty Response for reportType: %s, storeUUIDs:%s, startDate:%s, endDate:%s",
            uberReportReq.getReportType(),
            uberReportReq.getStoreUUIDs(),
            uberReportReq.getStartDate(),
            uberReportReq.getEndDate());
    log.error(errorMessage);
    throw new OMSThirdPartyException(errorMessage);
  }
}
