package com.walmart.marketplace.infrastructure.gateway.justeats;

import com.walmart.common.infrastructure.ApiAction;
import com.walmart.common.metrics.MetricConstants;
import com.walmart.marketplace.infrastructure.gateway.justeats.dto.request.ItemUpdateEventType;
import com.walmart.marketplace.infrastructure.gateway.justeats.dto.request.ItemUpdateRequest;
import com.walmart.marketplace.order.domain.entity.UpdateItemInfo;
import java.net.URI;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
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
public class JustEatsItemAvailabilityUpdateClient extends JustEatsBaseWebClient {

  @PostConstruct
  @Override
  public void initialize() {
    if (justEatsServiceConfiguration != null) {
      super.initialize();
    }
  }

  public List<Boolean> updateItemInfo(UpdateItemInfo updateItemInfo) {
    try {
      return Collections.singletonList(
          executeApiWithRetries(updateItemInfoCallable(updateItemInfo), ApiAction.JUST_EATS_API));
    } catch (Exception exception) {
      incrementMetricCounter(
          MetricConstants.MetricTypes.JUST_EATS_UPDATE_ITEM.getType(), exception);
      throw exception;
    }
  }

  private Callable<Boolean> updateItemInfoCallable(UpdateItemInfo updateItemInfo) {
    return () -> {
      ItemUpdateRequest itemUpdateRequest = buildItemUpdateRequest(updateItemInfo);
      logRequest(
          String.format(
              LOG_PREFIX,
              justEatsServiceConfiguration.getItemAvailabilityApiUrl(),
              updateItemInfo.getVendorOrderId()),
          itemUpdateRequest);
      WebClient.ResponseSpec responseSpec =
          webClient
              .post()
              .uri(updateItemUriBuilderFunc())
              .header(API_KEY_HEADER, justEatsServiceConfiguration.getItemAvailabilityApiKey())
              .body(Mono.just(itemUpdateRequest), ItemUpdateRequest.class)
              .retrieve();
      ResponseEntity<String> responseEntity =
          captureMetrics(
              responseSpec,
              String.class,
              MetricConstants.MetricTypes.JUST_EATS_UPDATE_ITEM.getType(),
              justEatsServiceConfiguration.getItemAvailabilityApiUrl());
      logResponse(
          String.format(
              LOG_PREFIX,
              justEatsServiceConfiguration.getItemAvailabilityApiUrl(),
              updateItemInfo.getVendorOrderId()),
          responseEntity);
      return isResponseSuccess(responseEntity, HttpStatus.ACCEPTED);
    };
  }

  private ItemUpdateRequest buildItemUpdateRequest(UpdateItemInfo updateItemInfo) {
    return ItemUpdateRequest.builder()
        .restaurant(updateItemInfo.getVendorStoreId())
        .happenedAt(ZonedDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME))
        .itemReferences(updateItemInfo.getOutOfStockItemIds())
        .event(ItemUpdateEventType.UNAVAILABLE)
        .nextAvailableAt(
            Instant.ofEpochSecond(updateItemInfo.getSuspendUntil())
                .atZone(ZoneId.systemDefault())
                .format(DateTimeFormatter.ISO_OFFSET_DATE_TIME))
        .build();
  }

  @Override
  protected String getResourceBaseUri() {
    return justEatsServiceConfiguration.getItemUpdateBaseUri();
  }

  @Override
  protected ThreadFactory getThreadFactory() {
    return new BasicThreadFactory.Builder().namingPattern(THREAD_FACTORY_NAME).build();
  }

  @Override
  protected int getThreadPoolSize() {
    return justEatsServiceConfiguration.getThreadPoolSize();
  }

  private Function<UriBuilder, URI> updateItemUriBuilderFunc() {
    return getUriBuilderFunction(justEatsServiceConfiguration.getItemAvailabilityApiUrl());
  }
}
