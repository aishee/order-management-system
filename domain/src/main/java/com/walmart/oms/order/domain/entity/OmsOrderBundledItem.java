package com.walmart.oms.order.domain.entity;

import com.walmart.common.domain.BaseEntity;
import com.walmart.common.utils.NumberUtils;
import java.math.BigDecimal;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Entity
@Getter
@Table(name = "OMSCORE.OMS_ORDER_BUNDLED_ITEM")
@NoArgsConstructor
@Slf4j
public class OmsOrderBundledItem extends BaseEntity {

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "ITEM_RECORD_ID")
  private OmsOrderItem omsOrderItem;

  @Column(name = "BUNDLE_QUANTITY")
  private long bundleQuantity;

  @Column(name = "ITEM_QUANTITY")
  private long itemQuantity;

  @Column(name = "BUNDLE_SKU_ID")
  private String bundleSkuId;

  @Column(name = "BUNDLE_INSTANCE_ID")
  private String bundleInstanceId;

  @Column(name = "BUNDLE_DESCRIPTION")
  private String bundleDescription;

  @Builder
  public OmsOrderBundledItem(OmsOrderItem omsOrderItem,
                             String id,
                             long bundleQuantity,
                             long itemQuantity,
                             String bundleSkuId,
                             String bundleInstanceId,
                             String bundleDescription) {
    super(id);
    this.omsOrderItem = omsOrderItem;
    this.bundleQuantity = bundleQuantity;
    this.itemQuantity = itemQuantity;
    this.bundleSkuId = bundleSkuId;
    this.bundleInstanceId = bundleInstanceId;
    this.bundleDescription = bundleDescription;
  }

  public String getOrderItemSkuId() {
    return omsOrderItem.getSkuId();
  }

  public BigDecimal getBundleItemTotalPrice() {
    return NumberUtils.getRoundedBigDecimal(
        omsOrderItem.getOrderedItemUnitPriceAmount()
            .multiply(BigDecimal.valueOf(
                getTotalItemQuantityInBundle())));
  }

  public long getTotalItemQuantityInBundle() {
    return itemQuantity * bundleQuantity;
  }

  public void initializeInnerEntitiesEagerly() {
    log.debug("omsOrderItem for bundleId :  {} {}", getBundleInstanceId(), this.getOmsOrderItem());
  }
}
