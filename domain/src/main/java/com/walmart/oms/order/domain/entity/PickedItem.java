package com.walmart.oms.order.domain.entity;

import static java.util.Objects.isNull;
import static org.springframework.util.CollectionUtils.isEmpty;

import com.walmart.common.constants.CommonConstants;
import com.walmart.common.domain.BaseEntity;
import com.walmart.common.domain.type.Currency;
import com.walmart.oms.order.valueobject.Money;
import com.walmart.oms.order.valueobject.PickedItemPriceInfo;
import com.walmart.oms.order.valueobject.Picker;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
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
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;

@Entity
@Getter
@Table(name = "OMSCORE.OMS_PICKED_ITEM")
@NoArgsConstructor
@Slf4j
public class PickedItem extends BaseEntity {

  @OneToOne
  @JoinColumn(name = "ORDER_ITEM_RECORD_ID")
  private OmsOrderItem omsOrderItem;

  @Column(name = "DEPARTMENT_ID")
  private String departmentID;

  @Column(name = "ORDERED_CIN")
  private String orderedCin;

  @Column(name = "QUANTITY")
  private long quantity;

  @Column(name = "WEIGHT")
  private double weight;

  @Column(name = "PICKED_ITEM_DESCRIPTION")
  private String pickedItemDescription;

  @Embedded private Picker picker;

  @Embedded private PickedItemPriceInfo pickedItemPriceInfo;

  @OneToMany(fetch = FetchType.LAZY, mappedBy = "pickedItem", cascade = CascadeType.ALL)
  private List<PickedItemUpc> pickedItemUpcList;

  @OneToMany(
      fetch = FetchType.LAZY,
      mappedBy = "pickedItem",
      cascade = CascadeType.ALL,
      orphanRemoval = true)
  private List<SubstitutedItem> substitutedItems;

  @Builder
  public PickedItem(
      OmsOrderItem omsOrderItem,
      String id,
      String departmentID,
      String orderedCin,
      long quantity,
      String pickedItemDescription,
      Picker picker,
      List<PickedItemUpc> pickedItemUpcList) {
    super(id);

    this.assertArgumentTrue(quantity >= 0, "Quantity has to be O or greater than 0");
    this.assertArgumentNotNull(pickedItemUpcList, "picked item upcs cannot be null");

    this.departmentID = departmentID;
    this.orderedCin = Objects.requireNonNull(orderedCin);
    this.omsOrderItem = omsOrderItem;
    this.quantity = quantity;
    this.pickedItemDescription = pickedItemDescription;
    this.picker = picker;
    setPickedItemUpcList(pickedItemUpcList);
    this.pickedItemPriceInfo = new PickedItemPriceInfo(new Money(BigDecimal.ZERO, Currency.GBP));
    this.pickedItemUpcList.forEach(this::updatePickedItemFromUpc);
  }

  private void updatePickedItemFromUpc(PickedItemUpc pickedItemUpc) {

    incrementQuantityFromUpc(pickedItemUpc.getQuantity());

    incrementWeightFromUpc(pickedItemUpc.getWeight(), pickedItemUpc.getUom());

    incrementUnitPriceFromUpc(pickedItemUpc.getStoreUnitPrice());
  }

  private void incrementUnitPriceFromUpc(Money storeUnitPrice) {
    if (storeUnitPrice != null && getUnitPriceAmount().compareTo(storeUnitPrice.getAmount()) < 0) {
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

  public void updateOrderItem(OmsOrderItem omsOrderitem) {
    this.omsOrderItem = omsOrderitem;
  }

  /**
   * Added this method for a conditional check in case of nil picks. Returning an empty list as we
   * do not support substitutions yet
   *
   * @return
   */
  public List<String> getSubstitutionList() {
    return Collections.emptyList();
  }

  public List<PickedItemUpc> getPickedItemUpcList() {
    if (isNull(this.pickedItemUpcList)) {
      return Collections.emptyList();
    }
    return this.pickedItemUpcList;
  }

  private void setPickedItemUpcList(List<PickedItemUpc> pickedItemUpcList) {

    if (CollectionUtils.isNotEmpty(pickedItemUpcList)) {
      pickedItemUpcList.forEach(pickedItemUpc -> pickedItemUpc.addPickedItem(this));
    }
    this.pickedItemUpcList = pickedItemUpcList;
  }

  public Optional<String> getPickedByUser() {
    return Optional.ofNullable(this.picker).map(Picker::getPickerUserName);
  }

  public boolean isNilPicked() {
    return isPickedQuantityZero() && isEmpty(this.getSubstitutionList());
  }

  public boolean isPickedQuantityZero() {
    return this.getQuantity() == 0;
  }

  /**
   * For Async Methods, Inner lazy loaded objects are not fetched and sometimes new thread is not
   * part of a transaction. We can call this method to eagerly fetch all the inner items.
   */
  public void initializeInnerEntitiesEagerly() {
    log.debug("PickedItemUpcList Size : {}", this.getPickedItemUpcList().size());
  }

  public BigDecimal getUnitPriceAmount() {
    return Optional.ofNullable(this.pickedItemPriceInfo)
        .map(PickedItemPriceInfo::getUnitPriceAmount)
        .orElse(BigDecimal.ZERO);
  }

  public String getPickedItemUpc() {
    return this.getPickedItemUpcList().stream().findFirst().map(PickedItemUpc::getUpc).orElse(null);
  }

  public BigDecimal getTotalPrice() {
    return getUnitPriceAmount()
        .multiply(BigDecimal.valueOf(this.getQuantity()))
        .setScale(CommonConstants.SCALE, CommonConstants.ROUNDING_MODE);
  }

  public void updateSubstitutedItems(List<SubstitutedItem> substitutedItems) {
    this.substitutedItems = substitutedItems;
    this.substitutedItems.forEach(omsSubstitutedItem -> omsSubstitutedItem.addPickedItem(this));
  }

  public Optional<SubstitutedItem> getSubstitutedItem() {
    return Optional.ofNullable(this.getSubstitutedItems())
        .flatMap(items -> items.stream().findFirst());
  }

  public Optional<BigDecimal> getSubstitutedItemVendorPrice() {
    return getSubstitutedItem().flatMap(SubstitutedItem::getSubstitutedItemVendorUnitPrice);
  }

  public boolean isSubstituted() {
    return CollectionUtils.isNotEmpty(getSubstitutedItems());
  }

  public Optional<Long> getTotalSubstitutedItemsQuantity() {
    return getSubstitutedItem().map(SubstitutedItem::getQuantity);
  }
}
