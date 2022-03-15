package com.walmart.oms.infrastructure.repository.infrastructure.mssql;

import com.walmart.common.domain.type.Tenant;
import com.walmart.common.domain.type.Vertical;
import com.walmart.oms.order.aggregateroot.OmsOrder;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IOmsOrderSqlServerRepository extends JpaRepository<OmsOrder, UUID> {

  OmsOrder findBySourceOrderIdAndTenantAndVertical(
      String sourceOrderId, Tenant tenant, Vertical vertical);

  OmsOrder findByMarketPlaceInfo_VendorOrderIdAndTenantAndVertical(
      String vendorOrderId, Tenant tenant, Vertical vertical);
}
