package com.walmart.marketplace.order.repository;

import com.walmart.marketplace.order.aggregateroot.MarketPlaceOrder;
import java.util.List;
import java.util.Optional;

public interface IMarketPlaceRepository {

  MarketPlaceOrder get(String orderId);

  MarketPlaceOrder save(MarketPlaceOrder order);

  int getInProgressOrderCount(List<String> orderStates, String storeId);

  String getNextIdentity();

  Optional<MarketPlaceOrder> getById(String id);
}
