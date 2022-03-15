package com.walmart.oms.infrastructure.repository;

import com.walmart.oms.order.aggregateroot.OmsOrder;

public interface OrderUpdateEventPublisher {

  void emitOrderUpdateEvent(OmsOrder omsOrder);
}
