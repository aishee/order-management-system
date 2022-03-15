package com.walmart.marketplace.integration.listeners;

import com.walmart.common.domain.messaging.DomainEvent;
import com.walmart.common.domain.messaging.DomainEventListener;
import com.walmart.common.domain.valueobject.CancellationDetails;
import com.walmart.marketplace.commands.CancelMarketPlaceOrderCommand;
import com.walmart.marketplace.commands.MarketPlaceDeliveredOrderCommand;
import com.walmart.marketplace.commands.MarketPlaceOrderConfirmationCommand;
import com.walmart.marketplace.commands.MarketPlacePickCompleteCommand;
import com.walmart.marketplace.eventprocessors.MarketPlaceDeliveredCommandService;
import com.walmart.marketplace.eventprocessors.MarketPlaceOrderConfirmationService;
import com.walmart.marketplace.eventprocessors.MarketPlacePickCompleteCommandService;
import com.walmart.marketplace.eventprocessors.MarketPlaceStoreCancelCommandService;
import com.walmart.marketplace.order.domain.valueobject.MarketPlaceOrderValueObject;
import com.walmart.marketplace.order.domain.valueobject.mappers.CancellationDetailsValueObjectMapper;
import com.walmart.oms.integration.exception.DomainEventListenerException;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class OmsOrderUpdatesListener implements DomainEventListener {

  private static final String EMPTY_PAYLOAD_ERROR = "Empty/Invalid Payload received";

  @Autowired
  private MarketPlaceOrderConfirmationService marketPlaceOrderConfirmationService;
  @Autowired
  private MarketPlaceDeliveredCommandService marketPlaceDeliveredCommandService;
  @Autowired
  private MarketPlaceStoreCancelCommandService marketPlaceStoreCancelCommandService;
  @Autowired
  private MarketPlacePickCompleteCommandService marketPlacePickCompleteCommandService;

  @Override
  @JmsListener(
      destination = "OMS_ORDER_UPDATES",
      containerFactory = "defaultConnectionFactory",
      concurrency = "5-10")
  public void listen(DomainEvent event) {
    log.info("Oms order update event received in marketplace:: {}", event.getMessage());

    switch (event.getName()) {
      case OMS_ORDER_CONFIRM:
        marketPlaceOrderConfirmationService.orderConfirmedAtStore(
            createOrderConfirmCommandFromEvent(event));
        break;

      case OMS_ORDER_DELIVERED:
        marketPlaceDeliveredCommandService.deliverOrder(
            createDeliveredOrderCommandFromEvent(event));
        break;

      case OMS_ORDER_CANCELLED:
        marketPlaceStoreCancelCommandService.cancelOrder(
            createStoreCancelledOrderCommandFromEvent(event));
        break;

      case OMS_ORDER_PICK_COMPLETE:
        marketPlacePickCompleteCommandService.pickCompleteOrder(
            createPickCompleteCommandFromEvent(event));
        break;

      default:
        log.info("No matching event name");
        break;
    }
  }

  private MarketPlacePickCompleteCommand createPickCompleteCommandFromEvent(DomainEvent event) {
    return event
        .createObjectFromJson(MarketPlaceOrderValueObject.class)
        .filter(MarketPlaceOrderValueObject::isValid)
        .map(MarketPlacePickCompleteCommand::buildMarketPlacePickCompleteCommand)
        .orElseThrow(() -> new DomainEventListenerException(EMPTY_PAYLOAD_ERROR));
  }

  private CancelMarketPlaceOrderCommand createStoreCancelledOrderCommandFromEvent(
      DomainEvent event) {

    Optional<MarketPlaceOrderValueObject> valueObjectOptional =
        event.createObjectFromJson(MarketPlaceOrderValueObject.class);
    if (valueObjectOptional.isPresent()
        && MarketPlaceOrderValueObject.isValid(valueObjectOptional.get())) {
      MarketPlaceOrderValueObject valueObject = valueObjectOptional.get();
      return CancelMarketPlaceOrderCommand.builder()
          .sourceOrderId(valueObject.getSourceOrderId())
          .cancellationDetails(buildCancellationDetailsForSourceStore(valueObject))
          .build();

    } else {
      throw new DomainEventListenerException(EMPTY_PAYLOAD_ERROR);
    }
  }

  public CancellationDetails buildCancellationDetailsForSourceStore(MarketPlaceOrderValueObject valueObject) {
    return CancellationDetailsValueObjectMapper.INSTANCE.modelToDomainObject(valueObject.getCancellationDetails());
  }

  private MarketPlaceDeliveredOrderCommand createDeliveredOrderCommandFromEvent(DomainEvent event) {

    Optional<MarketPlaceOrderValueObject> valueObjectOptional =
        event.createObjectFromJson(MarketPlaceOrderValueObject.class);
    if (valueObjectOptional.isPresent()
        && MarketPlaceOrderValueObject.isValid(valueObjectOptional.get())) {
      return MarketPlaceDeliveredOrderCommand.builder()
          .data(
              MarketPlaceDeliveredOrderCommand.MarketPlaceDeliveredOrderCommandData.builder()
                  .sourceOrderId(valueObjectOptional.get().getSourceOrderId())
                  .build())
          .build();
    } else {
      throw new DomainEventListenerException(EMPTY_PAYLOAD_ERROR);
    }
  }

  private MarketPlaceOrderConfirmationCommand createOrderConfirmCommandFromEvent(
      DomainEvent event) {

    Optional<MarketPlaceOrderValueObject> valueObjectOptional =
        event.createObjectFromJson(MarketPlaceOrderValueObject.class);
    if (valueObjectOptional.isPresent()
        && MarketPlaceOrderValueObject.isValid(valueObjectOptional.get())) {
      return MarketPlaceOrderConfirmationCommand.builder()
          .data(
              MarketPlaceOrderConfirmationCommand.MarketPlaceOrderConfirmationCommandData.builder()
                  .sourceOrderId(valueObjectOptional.get().getSourceOrderId())
                  .build())
          .build();
    } else {
      throw new DomainEventListenerException(EMPTY_PAYLOAD_ERROR);
    }
  }
}