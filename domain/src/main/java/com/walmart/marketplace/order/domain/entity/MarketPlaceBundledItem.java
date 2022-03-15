package com.walmart.marketplace.order.domain.entity;

import com.walmart.common.domain.BaseEntity;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "OMSCORE.MARKETPLACE_BUNDLED_ITEM")
@NoArgsConstructor
@AllArgsConstructor
public class MarketPlaceBundledItem extends BaseEntity {

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "ITEM_RECORD_ID")
  private MarketPlaceItem marketPlaceItem;

  @Column(name = "BUNDLE_QUANTITY")
  private long bundleQuantity;

  @Column(name = "BUNDLE_SKU_ID")
  private String bundleSkuId;

  @Column(name = "BUNDLE_INSTANCE_ID")
  private String bundleInstanceId;

  @Column(name = "BUNDLE_DESCRIPTION")
  private String bundleDescription;

  @Column(name = "ITEM_QUANTITY")
  private long itemQuantity;

  @Builder
  public MarketPlaceBundledItem(MarketPlaceItem marketPlaceItem,
                                String id,
                                String bundleDescription,
                                long bundleQuantity,
                                long itemQuantity,
                                String bundleSkuId,
                                String bundleInstanceId) {
    super(id);
    this.marketPlaceItem = marketPlaceItem;
    this.bundleQuantity = bundleQuantity;
    this.itemQuantity = itemQuantity;
    this.bundleSkuId = bundleSkuId;
    this.bundleInstanceId = bundleInstanceId;
    this.bundleDescription = bundleDescription;
  }

  public long getTotalBundleItemQuantity() {
    return itemQuantity * bundleQuantity;
  }
}
