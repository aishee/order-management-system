package com.walmart.oms.integration.listeners;

import com.walmart.common.domain.event.processing.EventGeneratorService;
import com.walmart.common.domain.messaging.DomainEvent;
import com.walmart.common.domain.messaging.DomainEventListener;
import com.walmart.common.domain.type.Tenant;
import com.walmart.common.domain.type.Vertical;
import com.walmart.marketplace.order.domain.valueobject.mappers.CancellationDetailsValueObjectMapper;
import com.walmart.oms.commands.DeliveredOrderCommand;
import com.walmart.oms.commands.OmsCancelOrderCommand;
import com.walmart.oms.commands.OrderConfirmationCommand;
import com.walmart.oms.commands.PickCompleteCommand;
import com.walmart.oms.commands.mappers.FmsOrderValueObjectToPickCommandMapper;
import com.walmart.oms.domain.event.messages.OrderCancelledDomainEventMessage;
import com.walmart.oms.domain.event.messages.PickCompleteDomainEventMessage;
import com.walmart.oms.domain.mapper.OmsDomainToEventMessageMapper;
import com.walmart.oms.eventprocessors.OmsCancelOrderCommandService;
import com.walmart.oms.eventprocessors.OmsDeliveredCommandService;
import com.walmart.oms.eventprocessors.OmsOrderConfirmationCommandService;
import com.walmart.oms.eventprocessors.OmsPickCompleteCommandService;
import com.walmart.oms.integration.exception.DomainEventListenerException;
import com.walmart.oms.order.aggregateroot.OmsOrder;
import com.walmart.oms.order.valueobject.events.FmsOrderValueObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class FmsOrderUpdatesListener implements DomainEventListener {

  private static final String EMPTY_PAYLOAD_ERROR = "Empty/Invalid Payload received";

  @Autowired
  private OmsOrderConfirmationCommandService orderConfirmationCommandService;
  @Autowired
  private OmsDeliveredCommandService omsDeliveredCommandService;
  @Autowired
  private OmsCancelOrderCommandService omsCancelOrderCommandService;
  @Autowired
  private OmsPickCompleteCommandService omsPickCompleteCommandService;
  @Autowired
  private EventGeneratorService eventGeneratorService;

  @JmsListener(
      destination = "FMS_ORDER_UPDATES",
      containerFactory = "defaultConnectionFactory",
      concurrency = "5-10")
  public void listen(DomainEvent event) {
    log.info("fms order update event received to OMS:: {}", event.getMessage());

    switch (event.getName()) {
      case FMS_ORDER_CONFIRM:
        orderConfirmationCommandService.orderConfirmedAtStore(
            createOrderConfirmCommandFromEvent(event));
        break;

      case FMS_ORDER_DELIVERED:
        omsDeliveredCommandService.deliverOrder(createDeliveredOrderCommandFromEvent(event));
        break;

      case FMS_ORDER_CANCELLED:
        OmsOrder cancelledOrder =
            omsCancelOrderCommandService.cancelOrder(
                createStoreCancelledOrderCommandFromEvent(event));
        OrderCancelledDomainEventMessage orderCancelledDomainEventMessage =
            OmsDomainToEventMessageMapper.mapToOrderCancelledDomainEventMessage(cancelledOrder);
        eventGeneratorService.publishApplicationEvent(orderCancelledDomainEventMessage);
        break;

      case FMS_ORDER_PICK_COMPLETE:
        OmsOrder omsOrder =
            omsPickCompleteCommandService.pickCompleteOrder(
                createPickCompleteCommandFromEvent(event));
        // Spring application event generator for executing side effects in same domain.
        PickCompleteDomainEventMessage pickCompleteDomainEventMessage =
            OmsDomainToEventMessageMapper.mapToPickCompleteDomainEventMessage(omsOrder);
        eventGeneratorService.publishApplicationEvent(pickCompleteDomainEventMessage);
        break;

      default:
        log.info("No matching event name");
        break;
    }
  }

  private PickCompleteCommand createPickCompleteCommandFromEvent(DomainEvent event) {

    return event
        .createObjectFromJson(FmsOrderValueObject.class)
        .filter(FmsOrderValueObject::isValid)
        .map(FmsOrderValueObjectToPickCommandMapper.INSTANCE::convertToCommand)
        .orElseThrow(() -> new DomainEventListenerException(EMPTY_PAYLOAD_ERROR));
  }

  private OmsCancelOrderCommand createStoreCancelledOrderCommandFromEvent(DomainEvent event) {

    return event
        .createObjectFromJson(FmsOrderValueObject.class)
        .filter(FmsOrderValueObject::isValid)
        .filter(FmsOrderValueObject::hasValidCancellationDetails)
        .map(
            valueObject ->
                OmsCancelOrderCommand.builder()
                    .sourceOrderId(valueObject.getSourceOrderId())
                    .cancellationDetails(CancellationDetailsValueObjectMapper.INSTANCE
                        .modelToDomainObject(valueObject.getCancellationDetails()))
                    .tenant(valueObject.getTenant())
                    .vertical(valueObject.getVertical())
                    .build())
        .orElseThrow(() -> new DomainEventListenerException(EMPTY_PAYLOAD_ERROR));
  }

  private DeliveredOrderCommand createDeliveredOrderCommandFromEvent(DomainEvent event) {

    return event
        .createObjectFromJson(FmsOrderValueObject.class)
        .filter(FmsOrderValueObject::isValid)
        .map(
            valueObject ->
                DeliveredOrderCommand.builder()
                    .data(
                        DeliveredOrderCommand.DeliveredOrderCommandData.builder()
                            .sourceOrderId(valueObject.getSourceOrderId())
                            .storeId(valueObject.getStoreId())
                            .tenant(valueObject.getTenant())
                            .vertical(valueObject.getVertical())
                            .build())
                    .build())
        .orElseThrow(() -> new DomainEventListenerException(EMPTY_PAYLOAD_ERROR));
  }

  private OrderConfirmationCommand createOrderConfirmCommandFromEvent(DomainEvent event) {

    return event
        .createObjectFromJson(FmsOrderValueObject.class)
        .filter(FmsOrderValueObject::isValid)
        .map(
            valueObject ->
                OrderConfirmationCommand.builder()
                    .sourceOrderId(valueObject.getSourceOrderId())
                    .tenant(Tenant.ASDA)
                    .vertical(Vertical.MARKETPLACE)
                    .build())
        .orElseThrow(() -> new DomainEventListenerException(EMPTY_PAYLOAD_ERROR));
  }
}
