package com.walmart.oms.domain.event.listeners;

import com.walmart.common.domain.event.processing.EventGeneratorService;
import com.walmart.common.domain.event.processing.MessageListener;
import com.walmart.oms.converter.OmsCancelOrderCommandMapper;
import com.walmart.oms.domain.event.messages.OmsOrderEnrichmentFailureEventMessage;
import com.walmart.oms.domain.event.messages.OrderCancelledDomainEventMessage;
import com.walmart.oms.domain.mapper.OmsDomainToEventMessageMapper;
import com.walmart.oms.eventprocessors.OmsCancelOrderCommandService;
import com.walmart.oms.order.aggregateroot.OmsOrder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class OmsOrderEnrichmentFailureEventListener
    implements MessageListener<OmsOrderEnrichmentFailureEventMessage> {

  private final OmsCancelOrderCommandService omsCancelOrderCommandService;
  private final EventGeneratorService eventGeneratorService;

  @Async
  @EventListener
  @Override
  public void listen(OmsOrderEnrichmentFailureEventMessage omsOrderEnrichmentFailureEventMessage) {
    log.info(
        "event received at omsOrder enrichment failure event listener for order:{}",
        omsOrderEnrichmentFailureEventMessage.getSourceOrderId());

    OmsOrder omsOrder =
        omsCancelOrderCommandService.cancelOrder(
            OmsCancelOrderCommandMapper.INSTANCE.mapToOmsCancelOrderCommand(
                omsOrderEnrichmentFailureEventMessage));

    if (omsOrder.isValidOrder()) {
      OrderCancelledDomainEventMessage orderCancelledDomainEventMessage =
          OmsDomainToEventMessageMapper.mapToOrderCancelledDomainEventMessage(omsOrder);
      eventGeneratorService.publishApplicationEvent(orderCancelledDomainEventMessage);
    } else {
      String errorMessage =
          String.format(
              "Not publishing OrderCancelledDomainEventMessage, missing storeOrderId or StoreId fields for vendorOrderId: %s",
              omsOrder.getVendorOrderId());
      log.error(errorMessage);
    }
  }
}
