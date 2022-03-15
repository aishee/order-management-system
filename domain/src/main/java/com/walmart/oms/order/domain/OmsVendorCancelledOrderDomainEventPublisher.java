package com.walmart.oms.order.domain;

import com.walmart.common.domain.messaging.DomainEvent;
import com.walmart.common.domain.messaging.DomainEventPublisher;
import com.walmart.common.domain.type.Domain;
import com.walmart.common.domain.type.DomainEventType;
import com.walmart.oms.order.aggregateroot.OmsOrder;
import com.walmart.oms.order.valueobject.mappers.OMSToFMSValueObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class OmsVendorCancelledOrderDomainEventPublisher implements OmsOrderCancelDomainEventPublisher {

  private final DomainEventPublisher omsDomainEventPublisher;
  private static final String OMS_CANCEL_DESTINATION = "OMS_ORDER_CANCELLED";
  private static final String OMS_ORDER_CANCELLED_DESCRIPTION = "An order cancellation is initiated by vendor";

  /**
   * Cancellation of OmsOrder from Marketplace.
   *
   * @param omsOrder OmsOrder object
   */
  @Override
  public void sendCancelOrderEvent(OmsOrder omsOrder) {

    log.info(
        "Order cancellation from vendor received.Sending cancellation event to FMS for order {}",
        omsOrder.getStoreOrderId());
    omsDomainEventPublisher.publish(
        new DomainEvent.EventBuilder(DomainEventType.OMS_ORDER_CANCELLED, OMS_ORDER_CANCELLED_DESCRIPTION)
            .from(Domain.OMS)
            .to(Domain.FMS)
            .addMessage(
                OMSToFMSValueObjectMapper.INSTANCE.convertOMSOrderToFMSValueObject(omsOrder))
            .build(),
        OMS_CANCEL_DESTINATION);
  }
}
