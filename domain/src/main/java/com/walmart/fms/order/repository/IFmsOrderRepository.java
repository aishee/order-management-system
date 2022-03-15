package com.walmart.fms.order.repository;

import com.walmart.common.domain.type.Tenant;
import com.walmart.common.domain.type.Vertical;
import com.walmart.fms.order.aggregateroot.FmsOrder;

public interface IFmsOrderRepository {

  public FmsOrder getOrderByMarketPlaceId(
      String marketPlaceOrderId, Tenant tenant, Vertical vertical);

  public FmsOrder getOrderByStoreOrderId(String storeOrderId);

  public FmsOrder save(FmsOrder fmsOrder);

  public FmsOrder getOrder(String orderId, Tenant tenant, Vertical vertical);

  public String getNextIdentity();
}
