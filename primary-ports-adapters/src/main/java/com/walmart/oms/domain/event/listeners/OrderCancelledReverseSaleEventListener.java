package com.walmart.oms.domain.event.listeners;

import com.walmart.common.domain.event.processing.MessageListener;
import com.walmart.oms.domain.error.exception.OMSBadRequestException;
import com.walmart.oms.domain.event.messages.OrderCancelledDomainEventMessage;
import com.walmart.oms.order.aggregateroot.OmsOrder;
import com.walmart.oms.order.factory.OmsOrderFactory;
import com.walmart.oms.order.gateway.IPricingGateway;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class OrderCancelledReverseSaleEventListener
    implements MessageListener<OrderCancelledDomainEventMessage> {

  private final OmsOrderFactory omsOrderFactory;
  private final IPricingGateway pricingGateway;

  @Async
  @EventListener
  @Override
  @Transactional
  public void listen(OrderCancelledDomainEventMessage orderCancelledDomainEventMessage) {
    log.info(
        "event received at order cancelled reverse sale event listener for order:{}",
        orderCancelledDomainEventMessage.getVendorOrderId());

    OmsOrder omsOrder =
        omsOrderFactory.getOmsOrderBySourceOrder(
            orderCancelledDomainEventMessage.getSourceOrderId(),
            orderCancelledDomainEventMessage.getTenant(),
            orderCancelledDomainEventMessage.getVertical());

    if (omsOrder.isReverseSaleApplicable()) {
      pricingGateway.reverseSale(orderCancelledDomainEventMessage);
    } else {
      String message =
          String.format(
              "Error while performing reverse sale as order: %s already in: %s",
              omsOrder.getSourceOrderId(), omsOrder.getOrderState());
      log.error(message);
      throw new OMSBadRequestException(message);
    }
  }
}
