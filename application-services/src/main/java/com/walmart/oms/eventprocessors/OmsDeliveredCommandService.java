package com.walmart.oms.eventprocessors;

import com.walmart.common.domain.messaging.DomainEvent;
import com.walmart.common.domain.messaging.DomainEventPublisher;
import com.walmart.common.domain.type.Domain;
import com.walmart.common.domain.type.DomainEventType;
import com.walmart.oms.commands.DeliveredOrderCommand;
import com.walmart.oms.domain.error.exception.OMSBadRequestException;
import com.walmart.oms.order.aggregateroot.OmsOrder;
import com.walmart.oms.order.factory.OmsOrderFactory;
import com.walmart.oms.order.repository.IOmsOrderRepository;
import com.walmart.oms.order.valueobject.mappers.OMSOrderToMarketPlaceOrderValueObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Slf4j
public class OmsDeliveredCommandService {

  private static final String DESCRIPTION = "An order has been delivred to customer.";
  private static final String DESTINATION = "OMS_ORDER_UPDATES";

  @Autowired
  private DomainEventPublisher omsDomainEventPublisher;

  @Autowired
  private OmsOrderFactory omsOrderFactory;

  @Autowired
  private IOmsOrderRepository omsOrderRepository;

  @Transactional
  public OmsOrder deliverOrder(DeliveredOrderCommand deliveredOrderCommand) {

    OmsOrder omsOrder =
        omsOrderFactory.getOmsOrderBySourceOrder(
            deliveredOrderCommand.getSourceOrderId(),
            deliveredOrderCommand.getTenant(),
            deliveredOrderCommand.getVertical());

    if (omsOrder != null && !omsOrder.isTransientState()) {
      if (omsOrder.isOrderStatusUpdatable(OmsOrder.OrderStatus.DELIVERED.getName())) {
        omsOrder.markOrderAsDelivered();
        omsOrderRepository.save(omsOrder);
        omsDomainEventPublisher.publish(
            new DomainEvent.EventBuilder(DomainEventType.OMS_ORDER_DELIVERED, DESCRIPTION)
                .from(Domain.OMS)
                .to(Domain.MARKETPLACE)
                .addMessage(
                    OMSOrderToMarketPlaceOrderValueObjectMapper.INSTANCE
                        .convertOmsOrderToMarketPlaceOrderValueObject(omsOrder))
                .build(),
            DESTINATION);
      } else {
        String message =
            String.format(
                "Received Delivered Order Event but Order: %s already in: %s",
                omsOrder.getSourceOrderId(), omsOrder.getOrderState());
        log.error(message);
        throw new OMSBadRequestException(message);
      }
    } else {
      String message =
          String.format(
              "Order doesn't exist with source order id : %s",
              deliveredOrderCommand.getSourceOrderId());
      log.error(message);
    }

    return omsOrder;
  }
}
