package com.walmart.oms.domain.event.listeners;

import com.walmart.common.domain.event.processing.MessageListener;
import com.walmart.oms.domain.event.messages.PickCompleteDomainEventMessage;
import com.walmart.oms.order.aggregateroot.OmsOrder;
import com.walmart.oms.order.domain.OmsOrderPickCompleteDomainService;
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
public class PickCompletePricingEventListener
    implements MessageListener<PickCompleteDomainEventMessage> {

  private final OmsOrderPickCompleteDomainService omsOrderPickCompleteDomainService;
  private final OmsOrderFactory omsOrderFactory;

  @Async
  @EventListener
  @Override
  @Transactional
  public void listen(PickCompleteDomainEventMessage pickCompleteDomainEventMessage) {

    log.info(
        "pick complete event listener to execute Pricing for order:{}",
        pickCompleteDomainEventMessage.getSourceOrderId());

    OmsOrder omsOrder =
        omsOrderFactory.getOmsOrderBySourceOrder(
            pickCompleteDomainEventMessage.getSourceOrderId(),
            pickCompleteDomainEventMessage.getTenant(),
            pickCompleteDomainEventMessage.getVertical());

    omsOrderPickCompleteDomainService.performPricingOnTheOrder(omsOrder);
  }
}
