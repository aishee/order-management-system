package com.walmart.fms.integration.listeners;

import com.walmart.common.domain.messaging.DomainEvent;
import com.walmart.common.domain.messaging.DomainEventListener;
import com.walmart.fms.FmsOrderApplicationService;
import com.walmart.fms.commands.converters.FMSValueObjectToCreateCmdConverter;
import com.walmart.oms.order.valueobject.events.FmsOrderValueObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

@Component("omsOrderCreatedEventListener")
@Slf4j
public class OrderCreatedEventListener implements DomainEventListener {

  @Autowired
  FmsOrderApplicationService fmsOrderApplicationService;

  @JmsListener(
      destination = "OMS_ORDER_CREATED",
      containerFactory = "defaultConnectionFactory",
      concurrency = "5-10")
  @Override
  public void listen(DomainEvent event) {
    if (event.isInitiatedByOMS()) {
      log.info("Received order creation event from OMS to FMS :{}", event.getMessage());
      event
          .createObjectFromJson(FmsOrderValueObject.class)
          .ifPresent(
              orderValueObject ->
                  fmsOrderApplicationService.createAndProcessFulfillmentOrder(
                      FMSValueObjectToCreateCmdConverter.INSTANCE.convertVoToCommand(
                          orderValueObject)));
    }
  }
}
