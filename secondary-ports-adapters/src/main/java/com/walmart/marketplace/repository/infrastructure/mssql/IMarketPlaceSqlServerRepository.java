package com.walmart.marketplace.repository.infrastructure.mssql;

import com.walmart.marketplace.order.aggregateroot.MarketPlaceOrder;
import java.util.Collection;
import java.util.Date;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IMarketPlaceSqlServerRepository extends JpaRepository<MarketPlaceOrder, String> {

  MarketPlaceOrder findByVendorOrderId(String vendorOrderId);

  int countMarketPlaceOrdersByOrderStateInAndStoreIdAndOrderDueTimeGreaterThanEqual(
      Collection<String> orderStates, String storeId, Date deliveryDate);
}
