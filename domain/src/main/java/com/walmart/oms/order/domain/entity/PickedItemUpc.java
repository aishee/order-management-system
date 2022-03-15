package com.walmart.oms.order.domain.entity;

import com.walmart.common.domain.BaseEntity;
import com.walmart.oms.order.valueobject.Money;
import java.math.BigDecimal;
import java.util.Optional;
import javax.persistence.AttributeOverride;
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
@Getter
@Table(name = "OMSCORE.OMS_PICKED_ITEM_UPC")
@NoArgsConstructor
public class PickedItemUpc extends BaseEntity {

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "PICKED_ITEM_RECORD_ID")
  private PickedItem pickedItem;

  @Builder
  public PickedItemUpc(
      String id,
      String upc,
      String uom,
      String win,
      long quantity,
      double weight,
      Money storeUnitPrice) {
    super(id);

    this.assertArgumentNotEmpty(uom, "UOM cannot be empty or null");
    this.assertArgumentNotEmpty(upc, "UPC cannot be null");
    this.assertArgumentNotNull(win, "win cannot be empty");
    this.upc = upc;
    this.uom = uom;
    this.win = win;
    this.quantity = quantity;
    this.weight = weight;
    this.storeUnitPrice = storeUnitPrice;
  }

  @Column(name = "UPC")
  private String upc;

  @Column(name = "UOM")
  private String uom;

  @Column(name = "WALMART_ITEM_NUM")
  private String win;

  @Column(name = "QUANTITY")
  private long quantity;

  @Column(name = "WEIGHT")
  private double weight;

  @Embedded
  @AttributeOverride(name = "amount", column = @Column(name = "STORE_UNIT_PRICE"))
  private Money storeUnitPrice;

  public void addPickedItem(PickedItem pickedItem) {
    this.pickedItem = pickedItem;
  }

  public Optional<BigDecimal> getUnitPriceAmount() {
    return Optional.ofNullable(this.storeUnitPrice).map(Money::getAmount);
  }
}
