package com.walmart.fms.eventprocessors;

import com.walmart.common.domain.messaging.DomainEvent;
import com.walmart.common.domain.messaging.DomainEventPublisher;
import com.walmart.common.domain.type.Domain;
import com.walmart.common.domain.type.DomainEventType;
import com.walmart.fms.commands.FmsOrderConfirmationCommand;
import com.walmart.fms.domain.error.exception.FMSBadRequestException;
import com.walmart.fms.order.aggregateroot.FmsOrder;
import com.walmart.fms.order.factory.FmsOrderFactory;
import com.walmart.fms.order.repository.IFmsOrderRepository;
import com.walmart.fms.order.valueobject.mappers.FMSOrderToFmsOrderValueObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Slf4j
public class FmsOrderConfirmationCommandService {

  private static final String DESCRIPTION = "An order has reached to store.";
  private static final String DESTINATION = "FMS_ORDER_UPDATES";

  @Autowired private IFmsOrderRepository fmsOrderRepository;

  @Autowired private FmsOrderFactory fmsOrderFactory;

  @Autowired private DomainEventPublisher fmsDomainEventPublisher;

  @Transactional
  public FmsOrder orderConfirmedAtStore(FmsOrderConfirmationCommand orderConfirmationCommand) {

    FmsOrder fmsOrder =
        fmsOrderFactory.getFmsOrderByStoreOrder(orderConfirmationCommand.getStoreOrderId());

    if (fmsOrder != null && !fmsOrder.isTransientState()) {
      if (fmsOrder.isValidOrderStatusSequence(FmsOrder.OrderStatus.RECEIVED_AT_STORE.getName())) {
        fmsOrder.markOrderAsReceivedAtStore();
        fmsOrderRepository.save(fmsOrder);

        fmsDomainEventPublisher.publish(
            new DomainEvent.EventBuilder(DomainEventType.FMS_ORDER_CONFIRM, DESCRIPTION)
                .from(Domain.FMS)
                .to(Domain.OMS)
                .addMessage(
                    FMSOrderToFmsOrderValueObjectMapper.INSTANCE
                        .convertFmsOrderToFmsOrderValueObject(fmsOrder))
                .build(),
            DESTINATION);
      } else {
        throw new FMSBadRequestException(
            "Received OrderConfirm Event But Order:"
                + fmsOrder.getStoreOrderId()
                + " already in: "
                + fmsOrder.getOrderState());
      }

    } else {
      log.error(
          "Order doesn't exist with source order id :{}",
          orderConfirmationCommand.getStoreOrderId());
      throw new FMSBadRequestException("Order doesn't exist with source order id");
    }
    return fmsOrder;
  }
}
