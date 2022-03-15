package com.walmart.fms.eventprocessors;

import com.walmart.common.domain.messaging.DomainEvent;
import com.walmart.common.domain.messaging.DomainEventPublisher;
import com.walmart.common.domain.type.Domain;
import com.walmart.common.domain.type.DomainEventType;
import com.walmart.fms.commands.FmsDeliveredOrderCommand;
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
public class FmsDeliveredCommandService {

  public static final String DESTINATION = "FMS_ORDER_UPDATES";
  public static final String DESCRIPTION = "An order has reached to store.";

  @Autowired
  private DomainEventPublisher fmsDomainEventPublisher;

  @Autowired
  private FmsOrderFactory fmsOrderFactory;

  @Autowired
  private IFmsOrderRepository fmsOrderRepository;

  @Transactional
  public FmsOrder deliverOrder(FmsDeliveredOrderCommand deliveredOrderCommand) {

    FmsOrder fmsOrder =
        fmsOrderFactory.getFmsOrderByStoreOrder(deliveredOrderCommand.getData().getStoreOrderId());

    if (fmsOrder != null && !fmsOrder.isTransientState()) {
      if (fmsOrder.isValidOrderStatusSequence(FmsOrder.OrderStatus.DELIVERED.getName())) {
        fmsOrder.markOrderAsDelivered();
        fmsOrderRepository.save(fmsOrder);
        fmsDomainEventPublisher.publish(
            new DomainEvent.EventBuilder(DomainEventType.FMS_ORDER_DELIVERED, DESCRIPTION)
                .from(Domain.FMS)
                .to(Domain.OMS)
                .addMessage(
                    FMSOrderToFmsOrderValueObjectMapper.INSTANCE
                        .convertFmsOrderToFmsOrderValueObject(fmsOrder))
                .build(),
            DESTINATION);
      } else {
        throw new FMSBadRequestException(
            "Received DeliverOrder Event But Order:"
                + fmsOrder.getStoreOrderId()
                + " already in: "
                + fmsOrder.getOrderState());
      }
    } else {
      log.error(
          "Order doesn't exist with source order id :{}",
          deliveredOrderCommand.getData().getStoreOrderId());
      throw new FMSBadRequestException(
          "Order doesn't exist with source order id"
              + deliveredOrderCommand.getData().getStoreOrderId());
    }
    return fmsOrder;
  }
}
