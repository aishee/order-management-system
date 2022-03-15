package com.walmart.marketplace.repository;

import com.walmart.marketplace.order.domain.entity.MarketPlaceEvent;
import com.walmart.marketplace.order.repository.IMarketPlaceEventRepository;
import com.walmart.marketplace.repository.infrastructure.mssql.IMarketPlaceEventSqlServerRepository;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class MarketPlaceEventRepository implements IMarketPlaceEventRepository {

  @Autowired private IMarketPlaceEventSqlServerRepository marketPlaceEventSqlServerRepository;

  @Override
  public MarketPlaceEvent save(MarketPlaceEvent event) {
    return marketPlaceEventSqlServerRepository.save(event);
  }

  @Override
  public MarketPlaceEvent get(String externalEventId) {
    return marketPlaceEventSqlServerRepository.findBySourceEventId(externalEventId);
  }

  @Override
  public String getNextIdentity() {
    return UUID.randomUUID().toString().toUpperCase();
  }
}
