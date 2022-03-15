package com.walmart.oms.order.domain;

import com.walmart.common.domain.event.processing.EventGeneratorService;
import com.walmart.oms.domain.event.messages.OrderCreatedDomainEventMessage;
import com.walmart.oms.domain.mapper.OmsDomainToEventMessageMapper;
import com.walmart.oms.order.aggregateroot.OmsOrder;
import com.walmart.oms.order.repository.IOmsOrderRepository;
import com.walmart.oms.order.valueobject.CancelDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class OmsOrderDomainService {

  private final IOmsOrderRepository omsOrderRepository;

  private final EventGeneratorService eventGeneratorService;

  /**
   * Create, save and raise order created event.
   *
   * @param omsOrder OmsOrder object creation.
   * @return
   */
  @Transactional
  public OmsOrder processOmsOrder(OmsOrder omsOrder) {
    omsOrder.created();
    if (omsOrder.isValid()) {
      omsOrderRepository.save(omsOrder);
    } else {
      log.info(
          "Order received is invalid as either of sourceOrderId, deliveryDate, "
              + "sourceOrderId or orderItemList is missing for order:{}, ",
          omsOrder.getSourceOrderId());
    }
    return omsOrder;
  }

  public void publishOrderCreatedDomainEvent(OmsOrder omsOrder) {
    OrderCreatedDomainEventMessage orderCreatedDomainEventMessage =
        OmsDomainToEventMessageMapper.mapToOrderCreatedDomainEventMessage(omsOrder);
    eventGeneratorService.publishApplicationEvent(orderCreatedDomainEventMessage);
  }

  public void cancelOrderByCancellationSource(
      OmsOrder omsOrder, CancelDetails cancelDetails) {
    markOrderAsCancel(omsOrder, cancelDetails);
  }

  private void markOrderAsCancel(OmsOrder omsOrder, CancelDetails cancelDetails) {
    omsOrder.cancelOrder(cancelDetails);
    omsOrderRepository.save(omsOrder);
  }


}
