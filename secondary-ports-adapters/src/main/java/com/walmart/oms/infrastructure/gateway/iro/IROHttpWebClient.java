package com.walmart.oms.infrastructure.gateway.iro;

import static java.util.Collections.singletonList;
import static java.util.Objects.requireNonNull;

import com.google.common.collect.Lists;
import com.walmart.common.infrastructure.webclient.BaseWebClient;
import com.walmart.common.metrics.MetricConstants;
import com.walmart.common.metrics.MetricConstants.MetricCounters;
import com.walmart.oms.domain.error.exception.OMSThirdPartyException;
import com.walmart.oms.infrastructure.gateway.iro.dto.request.IRORequest;
import com.walmart.oms.infrastructure.gateway.iro.dto.response.IROResponse;
import com.walmart.oms.order.valueobject.CatalogItemInfoQuery;
import io.github.resilience4j.retry.Retry;
import io.strati.configuration.annotation.ManagedConfiguration;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ThreadFactory;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
@Slf4j
public class IROHttpWebClient extends BaseWebClient {

  private static final String IRO = "IRO";
  private static final String THREAD_FACTORY_NAME = "IRO-thread-pool-%d";
  private static final ThreadLocal<SimpleDateFormat> SIMPLE_DATE_FORMAT =
      ThreadLocal.withInitial(() -> new SimpleDateFormat("yyyy-MM-dd"));
  @ManagedConfiguration private IROServiceConfiguration iroServiceConfiguration;

  @PostConstruct
  @Override
  public void initialize() {
    super.initialize();
  }

  @Override
  public String getMetricsCounterName() {
    return MetricCounters.SECONDARY_PORT_INVOCATION.getCounter();
  }

  public List<IROResponse> retrieveCatalogData(CatalogItemInfoQuery catalogItemInfoQuery) {

    // if item size is less and batching is not required
    if (catalogItemInfoQuery.getItemIds().size() <= iroServiceConfiguration.getIroItemBatchSize()) {
      return singletonList(
          executeIROCall(
              catalogItemInfoQuery.getItemIds(),
              catalogItemInfoQuery.getItemType(),
              catalogItemInfoQuery.getStoreId(),
              catalogItemInfoQuery.getShipOnDate(),
              catalogItemInfoQuery.getStoreOrderId()));
    }

    // If itemId is more than IroItemBatchSize, then we batch requests in parallel
    List<List<String>> itemBatches =
        Lists.partition(
            catalogItemInfoQuery.getItemIds(), iroServiceConfiguration.getIroItemBatchSize());

    return itemBatches.stream()
        .map(
            itemBatch ->
                executeIROCall(
                    itemBatch,
                    catalogItemInfoQuery.getItemType(),
                    catalogItemInfoQuery.getStoreId(),
                    catalogItemInfoQuery.getShipOnDate(),
                    catalogItemInfoQuery.getStoreOrderId()))
        .collect(Collectors.toList());
  }

  // IRO call with CircuitBreaker and Retry mechanism
  private IROResponse executeIROCall(
      List<String> itemIds, String itemType, String storeId, Date shipOnDate, String storeOrderId) {

    // create supplier for IRO http call.
    final Supplier<IROResponse> iroWebClientSupplier =
        () -> executeIroApi(itemIds, itemType, storeId, shipOnDate, storeOrderId);

    // Decorate with retry
    final Supplier<IROResponse> retryDecoratedSupplier =
        Retry.decorateSupplier(retryRegistry.retry(IRO), iroWebClientSupplier);

    // decorate with Circuit breaker.
    return circuitBreakerRegistry
        .circuitBreaker(IRO)
        .decorateSupplier(retryDecoratedSupplier)
        .get();
  }

  private IROResponse executeIroApi(
      List<String> itemIds, String itemType, String storeId, Date shipOnDate, String storeOrderId) {

    // build IRO request
    IRORequest iroRequest = buildIRORequest(itemIds, itemType, storeId, shipOnDate);
    String requestLogMessage =
        String.format(
            "Catalog API Request: %s for Store Order Id : %s",
            iroServiceConfiguration.getIroServiceUri(), storeOrderId);
    // logging request
    logRequest(requestLogMessage, iroRequest);

    // Web Client API call
    WebClient.ResponseSpec responseSpec =
        webClient.post().body(Mono.just(iroRequest), IRORequest.class).retrieve();

    ResponseEntity<IROResponse> iroResponseEntity =
        captureMetrics(
            responseSpec,
            IROResponse.class,
            MetricConstants.MetricTypes.IRO_GET_ITEM.getType(),
            requestLogMessage);

    // printing logs if needed.
    logResponse(
        String.format("Catalog API Response for Store Order Id : %s", storeOrderId),
        iroResponseEntity);

    return iroResponseHandling(storeOrderId, iroResponseEntity);
  }

  private IROResponse iroResponseHandling(
      String storeOrderId, ResponseEntity<IROResponse> iroResponseEntity) {

    IROResponse iroResponse = requireNonNull(iroResponseEntity).getBody();
    // If response comes as Null, retry.
    if (iroResponse == null) {
      return emptyResponseHandling(storeOrderId);
    } else if (iroResponse.containsInvalidItems()) {
      // if IRO returns with Invalid item id Retry. If it is invalid price it will be put as 0.0
      invalidItemIdsHandling(storeOrderId, iroResponse);
    }
    return iroResponse;
  }

  private void invalidItemIdsHandling(String storeOrderId, IROResponse iroResponse) {
    String errorMessage =
        String.format(
            "Catalog API returned with Invalid Items:%s for Store Order Id :%s",
            iroResponse.getInvalidItems(), storeOrderId);
    log.error(errorMessage);
    throw new OMSThirdPartyException(errorMessage);
  }

  private IROResponse emptyResponseHandling(String storeOrderId) {
    String errorMessage =
        String.format(
            "Catalog API returned with Empty Response for Store Order Id :%s", storeOrderId);
    log.error(errorMessage);
    throw new OMSThirdPartyException(errorMessage);
  }

  private IRORequest buildIRORequest(
      List<String> itemIds, String itemType, String storeId, Date shipOnDate) {
    return IRORequest.builder()
        .itemIds(itemIds)
        .itemIdType(itemType)
        .storeId(storeId)
        .shipOnDate(SIMPLE_DATE_FORMAT.get().format(shipOnDate))
        .consumerContract(iroServiceConfiguration.getIroConsumerContract())
        .requestOrigin(iroServiceConfiguration.getIroRequestOrigin())
        .build();
  }

  @Override
  protected int getReadTimeout() {
    return iroServiceConfiguration.getReadTimeout();
  }

  @Override
  protected int getConnTimeout() {
    return iroServiceConfiguration.getConnTimeout();
  }

  @Override
  public boolean isLogEnabled() {
    return iroServiceConfiguration.isLoggingEnabled();
  }

  @Override
  protected String getClientName() {
    return IRO;
  }

  @Override
  protected ThreadFactory getThreadFactory() {
    return new BasicThreadFactory.Builder().namingPattern(THREAD_FACTORY_NAME).build();
  }

  @Override
  protected int getThreadPoolSize() {
    return iroServiceConfiguration.getThreadPoolSize();
  }

  @Override
  public String getMetricsExceptionCounterName() {
    return MetricCounters.IRO_EXCEPTION.getCounter();
  }

  @Override
  protected String getResourceBaseUri() {
    return iroServiceConfiguration.getIroServiceUri();
  }
}
