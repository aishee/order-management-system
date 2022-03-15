package com.walmart.oms.order.repository;

import com.walmart.common.domain.type.Tenant;
import com.walmart.common.domain.type.Vertical;
import com.walmart.oms.order.aggregateroot.OmsOrder;
import com.walmart.oms.order.domain.model.CreateDateSearchQuery;
import java.util.List;
import org.springframework.stereotype.Repository;

@Repository
public interface IOmsOrderRepository {

  OmsOrder getOrderByMarketPlaceId(String marketPlaceOrderId, Tenant tenant, Vertical vertical);

  OmsOrder getOrder(String orderId, Tenant tenant, Vertical vertical);

  OmsOrder save(OmsOrder omsOrder);

  List<OmsOrder> findAllOrderByCreatedDateRange(CreateDateSearchQuery searchQuery);

  String getNextIdentity();
}
