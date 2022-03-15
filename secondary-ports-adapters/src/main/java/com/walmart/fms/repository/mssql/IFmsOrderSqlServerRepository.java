package com.walmart.fms.repository.mssql;

import com.walmart.common.domain.type.Tenant;
import com.walmart.common.domain.type.Vertical;
import com.walmart.fms.order.aggregateroot.FmsOrder;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IFmsOrderSqlServerRepository extends JpaRepository<FmsOrder, UUID> {

  FmsOrder findBySourceOrderIdAndTenantAndVertical(
      String sourceOrderId, Tenant tenant, Vertical vertical);

  FmsOrder findByStoreOrderId(String storeOrderId);

  FmsOrder findByMarketPlaceInfo_VendorOrderIdAndTenantAndVertical(
      String vendorOrderId, Tenant tenant, Vertical vertical);
}
