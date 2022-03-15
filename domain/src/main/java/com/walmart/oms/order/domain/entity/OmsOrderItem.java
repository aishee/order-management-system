package com.walmart.oms.order.domain.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.walmart.common.constants.CommonConstants;
import com.walmart.common.domain.BaseEntity;
import com.walmart.common.domain.type.Currency;
import com.walmart.common.domain.type.SubstitutionOption;
import com.walmart.common.utils.NumberUtils;
import com.walmart.oms.order.aggregateroot.OmsOrder;
import com.walmart.oms.order.converter.CatalogItemConverter;
import com.walmart.oms.order.valueobject.CatalogItem;
import com.walmart.oms.order.valueobject.ItemPriceInfo;
import com.walmart.oms.order.valueobject.Money;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;

@Entity
@Getter
@Table(name = "OMSCORE.OMS_ORDER_ITEM")
@NoArgsConstructor
@Slf4j
public class OmsOrderItem extends BaseEntity {

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "ORDER_RECORD_ID")
  private OmsOrder order;

  @Column(name = "CONSUMER_ITEM_NUM")
  private String cin;

  @Embedded private ItemPriceInfo itemPriceInfo;

  @Column(name = "QUANTITY")
  private long quantity;

  @Column(name = "WEIGHT")
  private double weight;

  @Column(name = "ITEM_DESCRIPTION")
  private String itemDescription;

  @Column(name = "SKU_ID")
  private String skuId;

  @Column(name = "SALES_UNIT")
  private String salesUnit;

  @Column(name = "UOM")
  private String uom;

  @Column(name = "CATALOG_INFO")
  @Convert(converter = CatalogItemConverter.class)
  private CatalogItem catalogItem;

  @Column(name = "SUBSTITUTION_OPTION")
  @Enumerated(EnumType.STRING)
  private SubstitutionOption substitutionOption;

  @OneToOne(mappedBy = "omsOrderItem", cascade = CascadeType.ALL, orphanRemoval = true)
  private PickedItem pickedItem;

  @OneToMany(fetch = FetchType.LAZY, mappedBy = "omsOrderItem", cascade = CascadeType.ALL)
  private List<OmsOrderBundledItem> bundledItemList;

  @Builder
  private OmsOrderItem(
      String id,
      OmsOrder omsOrder,
      String cin,
      ItemPriceInfo itemPriceInfo,
      long quantity,
      double weight,
      String itemDescription,
      String skuId,
      String salesUnit,
      String uom,
      List<OmsOrderBundledItem> bundledItemList,
      SubstitutionOption substitutionOption) {

    super(id);

    this.assertArgumentTrue(quantity > 0, "Quantity should be non zero");
    this.assertArgumentNotEmpty(cin, "Cin number cannot be null or empty");

    this.order = Objects.requireNonNull(omsOrder, "Order cannot be null");
    this.cin = cin;
    this.itemPriceInfo = itemPriceInfo;
    this.quantity = quantity;
    this.weight = weight;
    this.itemDescription = itemDescription;
    this.skuId = skuId;
    this.salesUnit = salesUnit;
    this.uom = uom;
    this.bundledItemList = bundledItemList;
    this.substitutionOption = substitutionOption;
  }

  public SubstitutionOption getSubstitutionOption() {
    return Optional.ofNullable(substitutionOption).orElse(SubstitutionOption.DO_NOT_SUBSTITUTE);
  }

  public boolean isSubstitutionAllowed() {
    return getSubstitutionOption() == SubstitutionOption.SUBSTITUTE;
  }

  public List<String> getOrderItemUpcs() {

    return catalogItem != null ? catalogItem.getUpcNumbers() : null;
  }

  public void enrichItemWithCatalogItemData(CatalogItem catalogItem) {

    this.catalogItem = Objects.requireNonNull(catalogItem, "Catalog item cannot be null");

    this.skuId = catalogItem.getSkuId();
    this.itemPriceInfo.withUnitPriceFromCatalog(
        new Money(catalogItem.getUnformattedUnitPrice(), Currency.GBP));
    this.salesUnit = catalogItem.getSalesUnit();
    this.itemDescription = catalogItem.getPickerDesc();
  }

  public void enrichPickedInfoWithPickedItem(PickedItem pickedItem) {

    this.pickedItem = Objects.requireNonNull(pickedItem);
  }

  public String getImageUrl() {

    if (Objects.nonNull(catalogItem)) {
      if (catalogItem.getLargeImageURL() != null) {
        return catalogItem.getLargeImageURL();
      } else {
        return catalogItem.getSmallImageURL();
      }
    }

    return null;
  }

  public void enrichPickedItemWithPostPricingData(
      Money adjustedPriceExVat,
      Money adjustedPrice,
      Money webAdjustedPrice,
      Money displayPrice,
      Money vatAmount) {

    if (this.pickedItem != null && this.pickedItem.getPickedItemPriceInfo() != null) {

      this.pickedItem
          .getPickedItemPriceInfo()
          .addPostPricingPrices(
              adjustedPriceExVat, adjustedPrice, webAdjustedPrice, displayPrice, vatAmount);
    }
  }

  public boolean hasPositivePrice() {
    return this.getOrderedItemUnitPriceAmount().compareTo(BigDecimal.ZERO) > 0;
  }

  public boolean isPicked() {
    return this.pickedItem != null && pickedItem.getQuantity() > 0;
  }

  public boolean isSubstituted() {
    return getPickedItemOptional().map(PickedItem::isSubstituted).orElse(false);
  }

  public String getOrderedCin() {
    return getPickedItemOptional().map(PickedItem::getOrderedCin).orElse(null);
  }

  public int getPickedItemQuantity() {
    return getPickedItemOptional().map(PickedItem::getQuantity).map(Long::intValue).orElse(0);
  }

  /**
   * This method will check if the item has been partially picked
   *
   * @return boolean
   */
  public boolean isPartialPicked() {
    return ((this.getPickedItemQuantity() > 0)
        && (this.getPickedItemQuantity() < this.getQuantity()));
  }

  /**
   * Check if the item has been nil picked
   *
   * @return boolean
   */
  public boolean isNilPicked() {
    return getPickedItemOptional().map(PickedItem::isNilPicked).orElse(false);
  }

  public List<OmsOrderBundledItem> getBundledItemList() {
    return Optional.ofNullable(bundledItemList).orElse(Collections.emptyList());
  }

  /**
   * For Async Methods, Inner lazy loaded objects are not fetched and sometimes new thread is not
   * part of a transaction. We can call this method to eagerly fetch all the inner items.
   */
  public void initializeInnerEntitiesEagerly() {
    this.getPickedItemOptional().ifPresent(PickedItem::initializeInnerEntitiesEagerly);
    getBundledItemList().forEach(OmsOrderBundledItem::initializeInnerEntitiesEagerly);
  }

  public BigDecimal getOrderedItemUnitPriceAmount() {
    return Optional.ofNullable(this.itemPriceInfo)
        .map(ItemPriceInfo::getUnitPriceAmount)
        .map(NumberUtils::getRoundedBigDecimal)
        .orElse(BigDecimal.ZERO);
  }

  public Optional<BigDecimal> getSubstitutedItemVendorPrice() {
    return Optional.ofNullable(this.pickedItem)
        .flatMap(PickedItem::getSubstitutedItemVendorPrice);
  }

  public BigDecimal getPickedItemUnitPriceAmount() {
    return Optional.ofNullable(this.pickedItem)
        .map(PickedItem::getUnitPriceAmount)
        .map(NumberUtils::getRoundedBigDecimal)
        .orElse(BigDecimal.ZERO);
  }

  public String getPickedItemDepartmentId() {
    return getPickedItemOptional().map(PickedItem::getDepartmentID).orElse(null);
  }

  public List<PickedItemUpc> getPickedItemUpcList() {
    return getPickedItemOptional()
        .map(PickedItem::getPickedItemUpcList)
        .orElse(Collections.emptyList());
  }

  public BigDecimal getVendorUnitPriceAmount() {
    return Optional.ofNullable(this.itemPriceInfo)
        .map(ItemPriceInfo::getVendorUnitPriceAmount)
        .map(NumberUtils::getRoundedBigDecimal)
        .orElse(BigDecimal.ZERO);
  }

  public BigDecimal getVendorTotalPriceAmount() {
    return Optional.ofNullable(this.itemPriceInfo)
        .map(ItemPriceInfo::getVendorTotalPriceAmount)
        .map(NumberUtils::getRoundedBigDecimal)
        .orElse(BigDecimal.ZERO);
  }

  public BigDecimal getOrderedOrPickedItemUnitPrice() {
    return hasPositivePrice()
        ? this.getOrderedItemUnitPriceAmount()
        : this.getPickedItemUnitPriceAmount();
  }

  public BigDecimal getFinalPriceOfOrderedItem() {
    return hasPositiveOrderedOrPickedItemPrice()
        ? this.getOrderedOrPickedItemUnitPrice()
            .multiply(BigDecimal.valueOf(this.getQuantity()))
            .setScale(CommonConstants.SCALE, CommonConstants.ROUNDING_MODE)
        : BigDecimal.ZERO;
  }

  public Optional<PickedItem> getPickedItemOptional() {
    return Optional.ofNullable(pickedItem);
  }

  public boolean hasPositiveOrderedOrPickedItemPrice() {
    return this.getOrderedOrPickedItemUnitPrice().compareTo(BigDecimal.ZERO) > 0;
  }

  public boolean isZeroPricedNilPick() {
    return !hasPositivePrice() && isNilPicked();
  }

  public boolean hasBundle() {
    return !CollectionUtils.isEmpty(bundledItemList);
  }

  public void addBundleItems(List<OmsOrderBundledItem> bundledItemList) {
    this.bundledItemList = bundledItemList;
  }

  public List<SubstitutedItem> getSubstitutedItems() {
    return getPickedItemOptional()
        .map(PickedItem::getSubstitutedItems)
        .orElse(Collections.emptyList());
  }

  @JsonIgnore
  public boolean isSubstitutionOptionCancelOrder() {
    return this.getSubstitutionOption().isSubstitutionOptionCancelOrder();
  }

  @JsonIgnore
  public boolean isOmsOrderItemNilPicked() {
    return this.getPickedItemOptional().map(PickedItem::isPickedQuantityZero).orElse(false);
  }

  @JsonIgnore
  public boolean isCancellationValid() {
    return this.isSubstitutionOptionCancelOrder() && this.isOmsOrderItemNilPicked();
  }
}
