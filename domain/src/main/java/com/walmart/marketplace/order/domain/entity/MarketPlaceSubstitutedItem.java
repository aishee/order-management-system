package com.walmart.marketplace.order.domain.entity;

import com.walmart.common.domain.BaseEntity;
import com.walmart.marketplace.order.domain.valueobject.MarketPlaceSubstitutedItemPriceInfo;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "OMSCORE.MARKETPLACE_SUBSTITUTED_ITEM")
@NoArgsConstructor
@Getter
public class MarketPlaceSubstitutedItem extends BaseEntity {

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "ITEM_RECORD_ID")
  private MarketPlaceItem marketPlaceItem;

  @Embedded
  private MarketPlaceSubstitutedItemPriceInfo substitutedItemPriceInfo;

  @Column(name = "EXTERNAL_ITEM_ID")
  private String externalItemId;

  @Column(name = "ITEM_DESCRIPTION")
  private String description;

  @Column(name = "ITEM_QUANTITY")
  private Long quantity;

  @Builder
  public MarketPlaceSubstitutedItem(MarketPlaceItem marketPlaceItem,
                                    String id,
                                    String description,
                                    long quantity,
                                    String externalItemId,
                                    MarketPlaceSubstitutedItemPriceInfo substitutedItemPriceInfo) {
    super(id);
    this.marketPlaceItem = marketPlaceItem;
    this.quantity = quantity;
    this.description = description;
    this.externalItemId = externalItemId;
    this.substitutedItemPriceInfo = substitutedItemPriceInfo;
  }

}
