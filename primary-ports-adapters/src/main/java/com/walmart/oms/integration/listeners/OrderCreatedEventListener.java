package com.walmart.oms.integration.listeners;

import com.walmart.common.domain.messaging.DomainEvent;
import com.walmart.common.domain.messaging.DomainEventListener;
import com.walmart.common.domain.type.Tenant;
import com.walmart.common.domain.type.Vertical;
import com.walmart.marketplace.order.domain.valueobject.MarketPlaceOrderValueObject;
import com.walmart.oms.OmsOrderApplicationService;
import com.walmart.oms.commands.CreateOmsOrderCommand;
import com.walmart.oms.commands.mappers.MarketPlaceVoToOMSCommandMapper;
import com.walmart.oms.integration.exception.DomainEventListenerException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

@Component("externalOrderCreationEventListener")
@Slf4j
public class OrderCreatedEventListener implements DomainEventListener {

  public static final String OSN = "OSN";

  @Autowired
  private OmsOrderApplicationService omsOrderApplicationService;

  @JmsListener(
      destination = "ORDER_CREATED",
      containerFactory = "defaultConnectionFactory",
      concurrency = "5-10")
  public void listen(DomainEvent event) {
    log.info("Order creation event received to OMS:: {}", event.getMessage());
    if (event.isInitiatedByMarketPlace()) {
      omsOrderApplicationService.createOmsOrderFromCommand(
          createOmsOrderCommandFromMarketPlaceOrderValueObject(event));
    }
  }

  private CreateOmsOrderCommand createOmsOrderCommandFromMarketPlaceOrderValueObject(
      DomainEvent event) {

    return event
        .createObjectFromJson(MarketPlaceOrderValueObject.class)
        .filter(
            marketPlaceOrderValueObject -> marketPlaceOrderValueObject.getVendorOrderId() != null)
        .map(
            marketPlaceOrderValueObject ->
                MarketPlaceVoToOMSCommandMapper.INSTANCE.convertToCommand(
                    marketPlaceOrderValueObject,
                    Tenant.ASDA,
                    Vertical.MARKETPLACE,
                    extractOsnFromHeader(event)))
        .orElseThrow(() -> new DomainEventListenerException("Empty payload received"));
  }

  private String extractOsnFromHeader(DomainEvent event) {
    return event.getHeaderValueForKey(OSN, String.class).orElse(null);
  }
}
