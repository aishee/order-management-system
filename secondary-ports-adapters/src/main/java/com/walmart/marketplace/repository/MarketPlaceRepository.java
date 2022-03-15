package com.walmart.marketplace.repository;

import com.walmart.marketplace.order.aggregateroot.MarketPlaceOrder;
import com.walmart.marketplace.order.repository.IMarketPlaceRepository;
import com.walmart.marketplace.repository.infrastructure.mssql.IMarketPlaceSqlServerRepository;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class MarketPlaceRepository implements IMarketPlaceRepository {

  @Autowired IMarketPlaceSqlServerRepository marketPlaceSqlServerRepository;

  @Override
  public MarketPlaceOrder get(String orderId) {

    return marketPlaceSqlServerRepository.findByVendorOrderId(orderId);
  }

  @Override
  public MarketPlaceOrder save(MarketPlaceOrder order) {

    return marketPlaceSqlServerRepository.save(order);
  }

  @Override
  public int getInProgressOrderCount(List<String> orderStates, String storeId) {
    return marketPlaceSqlServerRepository
        .countMarketPlaceOrdersByOrderStateInAndStoreIdAndOrderDueTimeGreaterThanEqual(
            orderStates,
            storeId,
            Date.from(LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant()));
  }

  @Override
  public String getNextIdentity() {
    return UUID.randomUUID().toString().toUpperCase();
  }

  @Override
  public Optional<MarketPlaceOrder> getById(String id) {
    return marketPlaceSqlServerRepository.findById(id);
  }
}
