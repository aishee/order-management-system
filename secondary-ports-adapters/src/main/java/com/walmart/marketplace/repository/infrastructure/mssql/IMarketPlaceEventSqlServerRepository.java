package com.walmart.marketplace.repository.infrastructure.mssql;

import com.walmart.marketplace.order.domain.entity.MarketPlaceEvent;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IMarketPlaceEventSqlServerRepository
    extends JpaRepository<MarketPlaceEvent, UUID> {

  public MarketPlaceEvent findBySourceEventId(String soruceEventId);
}
