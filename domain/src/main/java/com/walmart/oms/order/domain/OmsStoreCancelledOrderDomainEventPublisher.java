package com.walmart.oms.order.domain;

import com.walmart.common.domain.messaging.DomainEvent;
import com.walmart.common.domain.messaging.DomainEventPublisher;
import com.walmart.common.domain.type.Domain;
import com.walmart.common.domain.type.DomainEventType;
import com.walmart.oms.order.aggregateroot.OmsOrder;
import com.walmart.oms.order.valueobject.mappers.OMSOrderToMarketPlaceOrderValueObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class OmsStoreCancelledOrderDomainEventPublisher implements OmsOrderCancelDomainEventPublisher {

  private final DomainEventPublisher omsDomainEventPublisher;
  private static final String MARKETPLACE_DESTINATION = "OMS_ORDER_UPDATES";
  private static final String OMS_STORE_CANCELLED_DESCRIPTION = "An order cancellation is initiated by Store";

  /**
   * Cancellation of OmsOrder from Store.
   *
   * @param omsOrder OmsOrder object
   */
  @Override
  public void sendCancelOrderEvent(OmsOrder omsOrder) {
    log.info(
        "Order cancellation from store received sending cancellation event to marketplace for order {}",
        omsOrder.getStoreOrderId());
    omsDomainEventPublisher.publish(
        new DomainEvent.EventBuilder(DomainEventType.OMS_ORDER_CANCELLED, OMS_STORE_CANCELLED_DESCRIPTION)
            .from(Domain.OMS)
            .to(Domain.MARKETPLACE)
            .addMessage(
                OMSOrderToMarketPlaceOrderValueObjectMapper.INSTANCE
                    .convertOmsOrderToMarketPlaceOrderValueObject(omsOrder))
            .build(),
        MARKETPLACE_DESTINATION);
  }
}
