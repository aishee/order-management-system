package com.walmart.oms.integration.listeners;

import com.walmart.common.domain.event.processing.EventGeneratorService;
import com.walmart.common.domain.messaging.DomainEvent;
import com.walmart.common.domain.messaging.DomainEventListener;
import com.walmart.common.domain.type.Tenant;
import com.walmart.common.domain.type.Vertical;
import com.walmart.marketplace.order.domain.valueobject.MarketPlaceOrderValueObject;
import com.walmart.marketplace.order.domain.valueobject.mappers.CancellationDetailsValueObjectMapper;
import com.walmart.oms.commands.OmsCancelOrderCommand;
import com.walmart.oms.domain.event.messages.OrderCancelledDomainEventMessage;
import com.walmart.oms.domain.mapper.OmsDomainToEventMessageMapper;
import com.walmart.oms.eventprocessors.OmsCancelOrderCommandService;
import com.walmart.oms.integration.exception.DomainEventListenerException;
import com.walmart.oms.order.aggregateroot.OmsOrder;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

@Component("externalOrderCancellationEventListener")
@Slf4j
public class OrderCancellationEventListener implements DomainEventListener {

  @Autowired
  private OmsCancelOrderCommandService omsCancelOrderCommandService;
  @Autowired
  private EventGeneratorService eventGeneratorService;

  @JmsListener(
      destination = "ORDER_CANCELLED",
      containerFactory = "defaultConnectionFactory",
      concurrency = "2-5")
  public void listen(DomainEvent event) {
    log.info("Order cancellation event received to OMS:: {}", event.getMessage());
    if (event.isInitiatedByMarketPlace()) {
      OmsOrder cancelledOrder =
          omsCancelOrderCommandService.cancelOrder(
              createOmsOrderCommandFromMarketPlaceOrderValueObject(event));
      OrderCancelledDomainEventMessage orderCancelledDomainEventMessage =
          OmsDomainToEventMessageMapper.mapToOrderCancelledDomainEventMessage(cancelledOrder);
      eventGeneratorService.publishApplicationEvent(orderCancelledDomainEventMessage);
    }
  }

  private OmsCancelOrderCommand createOmsOrderCommandFromMarketPlaceOrderValueObject(
      DomainEvent event) {
    Optional<MarketPlaceOrderValueObject> valueObjectOptional =
        event.createObjectFromJson(MarketPlaceOrderValueObject.class);
    if (valueObjectOptional.isPresent() && valueObjectOptional.get().getVendorOrderId() != null) {
      MarketPlaceOrderValueObject marketPlaceOrderValueObject = valueObjectOptional.get();
      return OmsCancelOrderCommand.builder()
          .sourceOrderId(marketPlaceOrderValueObject.getSourceOrderId())
          .vertical(Vertical.MARKETPLACE)
          .tenant(Tenant.ASDA)
          .cancellationDetails(
              CancellationDetailsValueObjectMapper.INSTANCE
                  .modelToDomainObject(marketPlaceOrderValueObject.getCancellationDetails()))
          .build();
    } else {
      throw new DomainEventListenerException("Empty payload received");
    }
  }
}