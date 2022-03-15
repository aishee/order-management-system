package com.walmart.fms.order.domain.entity;

import static java.util.Objects.isNull;

import com.walmart.common.domain.BaseEntity;
import com.walmart.common.domain.type.Currency;
import com.walmart.fms.order.valueobject.Money;
import com.walmart.fms.order.valueobject.PickedItemPriceInfo;
import com.walmart.fms.order.valueobject.Picker;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "OMSCORE.FULFILLMENT_ORDER_PICKED_LINE")
@NoArgsConstructor
public class FmsPickedItem extends BaseEntity {

  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "ORDERED_ITEM_RECORD_ID")
  private FmsOrderItem fmsOrderItem;

  @Column(name = "DEPARTMENT_ID")
  private String departmentID;

  @Column(name = "CONSUMER_ITEM_NUMBER")
  private String cin;

  @Column(name = "WALMART_ITEM_NUMBER")
  private String walmartItemNumber;

  @Column(name = "QUANTITY")
  private long quantity;

  @Column(name = "WEIGHT")
  private double weight;

  @Column(name = "DESCRIPTION")
  private String pickedItemDescription;

  @Column(name = "SELL_BY_DATE")
  private Date sellByDate;

  @Embedded private Picker picker;

  @Embedded private PickedItemPriceInfo pickedItemPriceInfo;

  @OneToMany(fetch = FetchType.LAZY, mappedBy = "pickedItem", cascade = CascadeType.ALL)
  private List<FmsPickedItemUpc> pickedItemUpcList;

  @OneToMany(
      fetch = FetchType.LAZY,
      mappedBy = "pickedItem",
      cascade = CascadeType.ALL,
      orphanRemoval = true)
  private List<FmsSubstitutedItem> substitutedItems;

  @Builder
  public FmsPickedItem(
      FmsOrderItem fmsOrderItem,
      String id,
      String departmentID,
      String cin,
      long quantity,
      String pickedItemDescription,
      Picker picker) {
    super(id);

    this.assertArgumentTrue(quantity >= 0, "Quantity has to be O or greater than 0");

    this.departmentID = departmentID;
    this.cin = Objects.requireNonNull(cin);
    this.fmsOrderItem = fmsOrderItem;
    this.quantity = quantity;
    this.pickedItemDescription = pickedItemDescription;
    this.picker = picker;

    this.pickedItemPriceInfo = new PickedItemPriceInfo(new Money(BigDecimal.ZERO, Currency.GBP));
  }

  private void updatePickedItemFromUpc(FmsPickedItemUpc pickedItemUpc) {

    incrementQuantityFromUpc(pickedItemUpc.getQuantity());

    incrementWeightFromUpc(pickedItemUpc.getWeight(), pickedItemUpc.getUom());

    incrementUnitPriceFromUpc(pickedItemUpc.getStoreUnitPrice());
  }

  private void incrementUnitPriceFromUpc(Money storeUnitPrice) {

    if (!isNull(this.pickedItemPriceInfo.getUnitPrice())
        && this.pickedItemPriceInfo.getUnitPrice().compareTo(storeUnitPrice) < 0) {
      this.pickedItemPriceInfo = new PickedItemPriceInfo(storeUnitPrice);
    }
  }

  private void incrementWeightFromUpc(double weight, String uom) {
    this.weight = uom.equalsIgnoreCase("K") ? weight : 0.0;
  }

  private void incrementQuantityFromUpc(long quantityFromUPC) {
    this.assertArgumentTrue(
        quantityFromUPC >= 0, "Quantity should be either 0 or greater than zero");
    this.quantity = this.quantity + quantityFromUPC;
  }

  public void updateOrderItem(FmsOrderItem orderItem) {
    this.fmsOrderItem = orderItem;
  }

  public void updateSubstitutedItems(List<FmsSubstitutedItem> substitutedItems) {
    this.substitutedItems = substitutedItems;
    this.substitutedItems.forEach(fmsSubstitutedItem -> fmsSubstitutedItem.addPickedItem(this));
  }

  public void addPickedItemUpcList(List<FmsPickedItemUpc> pickedItemUpcs) {
    pickedItemUpcs.forEach(fmsPickedItemUpc -> fmsPickedItemUpc.addPickedItem(this));
    this.pickedItemUpcList = pickedItemUpcs;
    this.pickedItemUpcList.forEach(this::updatePickedItemFromUpc);
  }

  public boolean isNilPicked() {
    return this.getQuantity() == 0;
  }
}
