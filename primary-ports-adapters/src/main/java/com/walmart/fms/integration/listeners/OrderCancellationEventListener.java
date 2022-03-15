package com.walmart.fms.integration.listeners;

import com.walmart.common.domain.messaging.DomainEvent;
import com.walmart.common.domain.messaging.DomainEventListener;
import com.walmart.common.domain.type.CancellationSource;
import com.walmart.fms.commands.FmsCancelOrderCommand;
import com.walmart.fms.eventprocessors.FmsCancelledCommandService;
import com.walmart.fms.order.valueobject.events.FmsOrderValueObject;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

@Component("omsOrderCancellationEventListener")
@Slf4j
public class OrderCancellationEventListener implements DomainEventListener {

  @Autowired
  private FmsCancelledCommandService fmsCancelledCommandService;

  @JmsListener(
      destination = "OMS_ORDER_CANCELLED",
      containerFactory = "defaultConnectionFactory",
      concurrency = "2-5")
  @Override
  public void listen(DomainEvent event) {

    if (event.isInitiatedByOMS()) {
      log.info("Received order cancellation event from OMS to FMS :{}", event.getMessage());
      Optional<FmsOrderValueObject> fmsOrderValueObject =
          event.createObjectFromJson(FmsOrderValueObject.class);
      fmsOrderValueObject.ifPresent(
          fmsOrderValueObject1 ->
              fmsCancelledCommandService.cancelOrder(
                  convertVoToCancelCommand(fmsOrderValueObject1)));
    }
  }

  private FmsCancelOrderCommand convertVoToCancelCommand(FmsOrderValueObject fmsOrderValueObject) {

    return FmsCancelOrderCommand.builder()
        .data(
            FmsCancelOrderCommand.FmsCancelOrderCommandData.builder()
                .storeOrderId(fmsOrderValueObject.getStoreOrderId())
                .cancelledReasonCode("VENDOR")
                .cancelledReasonDescription("cancelled by vendor")
                .cancellationSource(CancellationSource.VENDOR)
                .build())
        .build();
  }
}
