package com.walmart.fms.repository;

import com.walmart.common.domain.type.Tenant;
import com.walmart.common.domain.type.Vertical;
import com.walmart.fms.domain.error.exception.FMSBadRequestException;
import com.walmart.fms.order.aggregateroot.FmsOrder;
import com.walmart.fms.order.repository.IFmsOrderRepository;
import com.walmart.fms.repository.mssql.IFmsOrderSqlServerRepository;
import java.util.Objects;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class FmsOrderRepository implements IFmsOrderRepository {

  @Autowired IFmsOrderSqlServerRepository fmsOrderSqlServerRepository;

  @Override
  public FmsOrder getOrderByMarketPlaceId(
      String marketPlaceOrderId, Tenant tenant, Vertical vertical) {

    return fmsOrderSqlServerRepository.findByMarketPlaceInfo_VendorOrderIdAndTenantAndVertical(
        marketPlaceOrderId, tenant, vertical);
  }

  @Override
  public FmsOrder getOrderByStoreOrderId(String storeOrderId) {
    return fmsOrderSqlServerRepository.findByStoreOrderId(storeOrderId);
  }

  @Override
  public FmsOrder getOrder(String orderId, Tenant tenant, Vertical vertical) {

    FmsOrder existingOrder =
        fmsOrderSqlServerRepository.findBySourceOrderIdAndTenantAndVertical(
            orderId, tenant, vertical);
    if (Objects.isNull(existingOrder)) {
      throw new FMSBadRequestException("Unable to find order for id:" + orderId);
    }
    return existingOrder;
  }

  @Override
  public FmsOrder save(FmsOrder order) {

    return fmsOrderSqlServerRepository.save(order);
  }

  @Override
  public String getNextIdentity() {
    return UUID.randomUUID().toString().toUpperCase();
  }
}
