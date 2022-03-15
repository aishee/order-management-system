package com.walmart.fms.infrastructure.integration.gateway.store;

import static com.walmart.fms.order.gateway.StoreEvents.PFO;
import static com.walmart.fms.order.gateway.StoreEvents.UFO;

import com.walmart.common.domain.event.processing.EgressEvent;
import com.walmart.common.domain.event.processing.Interactor;
import com.walmart.common.infrastructure.integration.events.processing.IdempotentEgressEventProcessor;
import com.walmart.fms.infrastructure.integration.gateway.store.dto.forceordercancellation.UpdateFulfillmentOrderRequest;
import com.walmart.fms.infrastructure.integration.gateway.store.dto.orderdownload.PlaceFulfillmentOrderRequest;
import com.walmart.fms.infrastructure.integration.jms.config.JmsProducerEndpointConfig;
import com.walmart.fms.mapper.FmsOrderToPFORequestMapper;
import com.walmart.fms.mapper.FmsOrderToUFORequestMapper;
import com.walmart.fms.order.aggregateroot.FmsOrder;
import com.walmart.fms.order.gateway.StoreGateway;
import io.strati.configuration.annotation.ManagedConfiguration;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import javax.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

/**
 * A concrete implementation of StoreGateway for GIF fulfillment app.
 *
 * @see StoreGateway
 */
@Slf4j
@Component
public class GifStoreGateway extends IdempotentEgressEventProcessor implements StoreGateway {

  public static final String FMS = "FMS";
  public static final String ORDER_CONFIRMATION_MESSAGE_TO_STORE =
      "Order Confirmation message to store";
  public static final String ORDER_CANCELLATION_MESSAGE_TO_STORE =
      "Order Cancellation message to store";

  @ManagedConfiguration JmsProducerEndpointConfig jmsProducerEndpointConfig;

  @Autowired Interactor camelJmsProducer;

  Map<String, Consumer<FmsOrder>> methodCodeMap = new HashMap<>();

  @Override
  public void sendOrderConfirmation(FmsOrder fmsOrder) {
    Assert.notNull(fmsOrder, "Order cannot be null !!!");
    log.info(
        "Sending order confirmation to GIF. [sourceOrderId={},storeId={}]",
        fmsOrder.getSourceOrderId(),
        fmsOrder.getStoreId());
    processAsync(buildConfirmationEvent(fmsOrder));
  }

  @Override
  public void sendOrderCancellation(FmsOrder fmsOrder) {
    Assert.notNull(fmsOrder, "Order cannot be null !!!");
    log.info(
        "Sending order cancellation to GIF. [sourceOrderId={},storeId={}]",
        fmsOrder.getSourceOrderId(),
        fmsOrder.getStoreId());
    processAsync(buildCancellationEvent(fmsOrder));
  }

  @Override
  public Optional<Consumer<FmsOrder>> getActionByCode(String eventName) {
    if (methodCodeMap.isEmpty()) {
      buildActionMap();
    }
    return Optional.ofNullable(methodCodeMap.get(eventName));
  }

  @Override
  public Interactor getInteractor() {
    return camelJmsProducer;
  }

  @Override
  public int maxPoolSize() {
    return 10;
  }

  protected EgressEvent<FmsOrder, UpdateFulfillmentOrderRequest> buildCancellationEvent(
      FmsOrder fmsOrder) {
    return new EgressEvent.Builder<FmsOrder, UpdateFulfillmentOrderRequest>(
            UFO.name(), ORDER_CANCELLATION_MESSAGE_TO_STORE)
        .producedFrom(FMS)
        .toDestination(jmsProducerEndpointConfig.getGifForceOrderCancelConfig().getEndpointUrl())
        .withModel(fmsOrder)
        .withMapper(FmsOrderToUFORequestMapper::map)
        .asXml()
        .build();
  }

  protected EgressEvent<FmsOrder, PlaceFulfillmentOrderRequest> buildConfirmationEvent(
      FmsOrder fmsOrder) {
    return new EgressEvent.Builder<FmsOrder, PlaceFulfillmentOrderRequest>(
            PFO.name(), ORDER_CONFIRMATION_MESSAGE_TO_STORE)
        .producedFrom(FMS)
        .toDestination(jmsProducerEndpointConfig.getgifOrderDownloadConfig().getEndpointUrl())
        .withModel(fmsOrder)
        .withMapper(FmsOrderToPFORequestMapper::map)
        .asXml()
        .build();
  }

  @PostConstruct
  protected void buildActionMap() {
    methodCodeMap.put(UFO.name(), this::sendOrderCancellation);
    methodCodeMap.put(PFO.name(), this::sendOrderConfirmation);
  }
}
