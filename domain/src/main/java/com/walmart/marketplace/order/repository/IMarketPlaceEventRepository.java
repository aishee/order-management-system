package com.walmart.marketplace.order.repository;

import com.walmart.marketplace.order.domain.entity.MarketPlaceEvent;

public interface IMarketPlaceEventRepository {

  public MarketPlaceEvent save(MarketPlaceEvent event);

  public MarketPlaceEvent get(String externalEventId);

  public String getNextIdentity();
}
