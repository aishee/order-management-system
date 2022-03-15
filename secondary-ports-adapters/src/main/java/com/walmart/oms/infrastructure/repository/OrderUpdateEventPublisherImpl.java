package com.walmart.oms.infrastructure.repository;

import com.walmart.common.domain.event.processing.EventGeneratorService;
import com.walmart.oms.domain.event.messages.DwhOrderEventMessage;
import com.walmart.oms.domain.mapper.OmsDomainToEventMessageMapper;
import com.walmart.oms.infrastructure.configuration.OmsOrderConfig;
import com.walmart.oms.infrastructure.gateway.orderservice.OmsToOrderServiceModelMapper;
import com.walmart.oms.order.aggregateroot.OmsOrder;
import com.walmart.oms.order.gateway.orderservice.OrdersEvent;
import io.strati.configuration.annotation.ManagedConfiguration;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class OrderUpdateEventPublisherImpl implements OrderUpdateEventPublisher {

  @Autowired private EventGeneratorService eventGeneratorService;

  @ManagedConfiguration private OmsOrderConfig omsOrderConfig;

  @Override
  public void emitOrderUpdateEvent(OmsOrder omsOrder) {
    if (!omsOrderConfig.isPublishOrderUpdateEvent()) {
      log.warn(
          "Publishing Order update events to Kafka is Disabled. Skipping DWH publish for Order : {}",
          omsOrder.getStoreOrderId());
      return;
    }
    try {
      log.info(
          "Emitting message to Spring Event Listener for order:: {}", omsOrder.getStoreOrderId());
      OrdersEvent<com.walmart.services.oms.order.common.model.OmsOrder> ordersEventOrdersEvent =
          OmsToOrderServiceModelMapper.INSTANCE.generateOrderEvent(omsOrder);
      DwhOrderEventMessage dwhOrderEventMessage =
          OmsDomainToEventMessageMapper.mapToDwhOrderEventMessage(
              ordersEventOrdersEvent, omsOrder.getStoreOrderId());
      eventGeneratorService.publishApplicationEvent(dwhOrderEventMessage);
    } catch (Exception ex) {
      String message =
          String.format(
              "Error while sending message to order update topic for order %s",
              omsOrder.getStoreOrderId());
      log.error(message, ex);
    }
  }
}
