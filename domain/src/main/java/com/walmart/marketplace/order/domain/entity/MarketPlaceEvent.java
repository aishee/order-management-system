package com.walmart.marketplace.order.domain.entity;

import com.walmart.common.domain.BaseEntity;
import com.walmart.marketplace.order.domain.entity.type.Vendor;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

// Introduce a lambok getter, setter, data, toString and annotate with Entiry
@Entity
@Table(name = "OMSCORE.MARKET_PLACE_EVENTS")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MarketPlaceEvent extends BaseEntity {

  @Column(name = "EVENT_TYPE")
  private String eventType;

  @Column(name = "EXTERNAL_ORDER_ID")
  private String externalOrderId;

  @Column(name = "RESOURCE_URL")
  private String resourceURL;

  @Column(name = "SOURCE_EVENT_ID")
  private String sourceEventId;

  @Column(name = "VENDOR")
  @Enumerated(EnumType.STRING)
  private Vendor vendor;

  public MarketPlaceEvent(
      String id,
      String externalOrderId,
      String resourceURL,
      String eventType,
      String sourceEventId,
      Vendor vendor) {
    super(id);
    this.externalOrderId = externalOrderId;
    this.resourceURL = resourceURL;
    this.eventType = eventType;
    this.sourceEventId = sourceEventId;
    this.vendor = vendor;
  }
}
