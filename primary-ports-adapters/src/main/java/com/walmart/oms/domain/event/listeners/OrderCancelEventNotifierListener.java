package com.walmart.oms.domain.event.listeners;

import com.walmart.common.domain.event.processing.MessageListener;
import com.walmart.common.domain.type.CancellationSource;
import com.walmart.oms.domain.event.messages.OrderCancelledDomainEventMessage;
import com.walmart.oms.order.aggregateroot.OmsOrder;
import com.walmart.oms.order.domain.factory.OmsOrderCancelDomainEventPublisherFactory;
import com.walmart.oms.order.factory.OmsOrderFactory;
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
public class OrderCancelEventNotifierListener
    implements MessageListener<OrderCancelledDomainEventMessage> {
  private final OmsOrderCancelDomainEventPublisherFactory omsOrderCancelDomainEventPublisherFactory;
  private final OmsOrderFactory omsOrderFactory;


  @Async
  @EventListener
  @Override
  @Transactional
  public void listen(OrderCancelledDomainEventMessage message) {
    log.info(
        "event received at OrderCancelEventNotifierListener for order:{}",
        message.getSourceOrderId());
    OmsOrder omsOrder =
        omsOrderFactory.getOmsOrderBySourceOrder(
            message.getSourceOrderId(),
            message.getTenant(),
            message.getVertical());
    omsOrderCancelDomainEventPublisherFactory
        .getOrderCancelDomainEventPublisher(omsOrder.getCancellationSource()
            .orElse(CancellationSource.OMS))
        .sendCancelOrderEvent(omsOrder);

    if (message.isCancelOrder()) {
      omsOrderCancelDomainEventPublisherFactory
          .getOrderCancelDomainEventPublisher(CancellationSource.VENDOR)
          .sendCancelOrderEvent(omsOrder);
    }
  }
}
