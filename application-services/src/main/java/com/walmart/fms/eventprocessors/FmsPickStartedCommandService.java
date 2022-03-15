package com.walmart.fms.eventprocessors;

import com.walmart.common.domain.messaging.DomainEvent;
import com.walmart.common.domain.messaging.DomainEventPublisher;
import com.walmart.common.domain.type.Domain;
import com.walmart.common.domain.type.DomainEventType;
import com.walmart.fms.commands.FmsPickStartedOrderCommand;
import com.walmart.fms.domain.error.exception.FMSBadRequestException;
import com.walmart.fms.order.aggregateroot.FmsOrder;
import com.walmart.fms.order.factory.FmsOrderFactory;
import com.walmart.fms.order.repository.IFmsOrderRepository;
import com.walmart.fms.order.valueobject.mappers.FMSOrderToFmsOrderValueObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
public class FmsPickStartedCommandService {

  private static final String DESCRIPTION = "An order has reached to store.";
  private static final String DESTINATION = "FMS_ORDER_UPDATES";

  @Autowired
  private FmsOrderFactory fmsOrderFactory;

  @Autowired
  private IFmsOrderRepository fmsOrderRepository;

  @Autowired
  private DomainEventPublisher fmsDomainEventPublisher;

  @Transactional
  public FmsOrder orderPickStartedStore(FmsPickStartedOrderCommand fmsPickStartedOrderCommand) {
    FmsOrder fmsOrder =
        fmsOrderFactory.getFmsOrderByStoreOrder(fmsPickStartedOrderCommand.getStoreOrderId());
    if (fmsOrder.isValid() && !fmsOrder.isTransientState()) {
      if (fmsOrder.isValidOrderStatusSequence(FmsOrder.OrderStatus.PICKING_STARTED.getName())) {
        fmsOrder.markOrderAsPickStarted();
        fmsOrderRepository.save(fmsOrder);
        fmsDomainEventPublisher.publish(
            new DomainEvent.EventBuilder(DomainEventType.FMS_ORDER_PICK_STARTED, DESCRIPTION)
                .from(Domain.FMS)
                .to(Domain.OMS)
                .addMessage(
                    FMSOrderToFmsOrderValueObjectMapper.INSTANCE
                        .convertFmsOrderToFmsOrderValueObject(fmsOrder))
                .build(),
            DESTINATION);
      } else {
        log.error(
            "Received PickStarted Event Order: {} already in: {}",
            fmsOrder.getSourceOrderId(),
            fmsOrder.getOrderState());
        throw new FMSBadRequestException(
            "Received PickStarted Event Order:"
                + fmsOrder.getSourceOrderId()
                + " already in: "
                + fmsOrder.getOrderState());
      }

    } else {
      log.error("Order doesn't exist with source order id :{}", fmsOrder.getSourceOrderId());
      throw new FMSBadRequestException("Order doesn't exist with source order id");
    }
    return fmsOrder;
  }
}
