package com.walmart.fms.order.repository;

import com.walmart.fms.order.aggregateroot.FmsOrder;

public interface IFmsGateWay {

  FmsOrder getOrder(String vendorOrderId, String resourceUrl);
}
