package com.walmart.oms.domain.event.listeners;

import com.walmart.common.domain.event.processing.MessageListener;
import com.walmart.oms.domain.event.messages.OrderCreatedDomainEventMessage;
import com.walmart.oms.order.aggregateroot.OmsOrder;
import com.walmart.oms.order.domain.OmsOrderCreatedDomainService;
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
public class OrderCreatedCatalogEnrichEventListener
    implements MessageListener<OrderCreatedDomainEventMessage> {

  private final OmsOrderFactory omsOrderFactory;
  private final OmsOrderCreatedDomainService omsOrderCreatedDomainService;

  @Async
  @EventListener
  @Override
  @Transactional
  public void listen(OrderCreatedDomainEventMessage orderCreatedDomainEventMessage) {

    log.info(
        "event received at order created event listener for order:{}",
        orderCreatedDomainEventMessage.getSourceOrderId());
    OmsOrder omsOrder =
        omsOrderFactory.getOmsOrderBySourceOrder(
            orderCreatedDomainEventMessage.getSourceOrderId(),
            orderCreatedDomainEventMessage.getTenant(),
            orderCreatedDomainEventMessage.getVertical());

    omsOrderCreatedDomainService.enrichSaveAndPublishCreatedOmsOrderToFms(omsOrder);
  }
}
