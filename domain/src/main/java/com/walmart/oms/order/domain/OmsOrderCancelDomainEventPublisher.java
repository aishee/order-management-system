package com.walmart.oms.order.domain;

import com.walmart.oms.order.aggregateroot.OmsOrder;

public interface OmsOrderCancelDomainEventPublisher {

  void sendCancelOrderEvent(OmsOrder omsOrder);

}
