package com.walmart.fms.order.domain.entity;

import com.walmart.common.domain.BaseEntity;
import com.walmart.fms.order.valueobject.Money;
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
@Table(name = "OMSCORE.FULFILLMENT_ORDER_PICK_LINE_UPC")
@NoArgsConstructor
public class FmsPickedItemUpc extends BaseEntity {

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "REF_RECORD_ID")
  private FmsPickedItem pickedItem;

  @Builder
  public FmsPickedItemUpc(
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

  @Column(name = "UNIT_OF_MEASUREMENT")
  private String uom;

  @Column(name = "WALMART_ITEM_NUMBER")
  private String win;

  @Column(name = "QUANTITY")
  private long quantity;

  @Column(name = "WEIGHT")
  private double weight;

  @Embedded
  @AttributeOverride(name = "amount", column = @Column(name = "STORE_UNIT_PRICE"))
  private Money storeUnitPrice;

  public void addPickedItem(FmsPickedItem pickedItem) {
    this.pickedItem = pickedItem;
  }
}
