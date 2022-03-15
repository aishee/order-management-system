package com.walmart.fms.order.domain.entity;

import static java.util.Objects.isNull;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.walmart.common.domain.BaseEntity;
import com.walmart.common.domain.type.SubstitutionOption;
import com.walmart.fms.order.aggregateroot.FmsOrder;
import com.walmart.fms.order.converter.ItemUpcInfoConverter;
import com.walmart.fms.order.valueobject.ItemCatalogInfo;
import com.walmart.fms.order.valueobject.ItemPriceInfo;
import com.walmart.fms.order.valueobject.ItemUpcInfo;
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
import javax.persistence.OneToOne;
import javax.persistence.Table;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@ToString(exclude = "fmsOrder")
@EqualsAndHashCode(exclude = "fmsOrder")
@Entity
@Getter
@NoArgsConstructor
@Table(name = "OMSCORE.FULFILLMENT_ORDER_LINE")
public class FmsOrderItem extends BaseEntity {

  @Column(name = "ITEM_ID")
  private String itemId;

  @Column(name = "CONSUMER_ITEM_NUMBER")
  private String consumerItemNumber;

  @Column(name = "QUANTITY")
  private long quantity;

  @Column(name = "WEIGHT")
  private double weight;

  @Column(name = "SUBSTITUTION_OPTION")
  @Enumerated(EnumType.STRING)
  private SubstitutionOption substitutionOption;

  @Column(name = "UPC_INFO")
  @Convert(converter = ItemUpcInfoConverter.class)
  private ItemUpcInfo upcInfo;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "ORDER_RECORD_ID")
  private FmsOrder fmsOrder;

  @Embedded private ItemPriceInfo itemPriceInfo;
  @Embedded private ItemCatalogInfo catalogInfo;

  @OneToOne(
      fetch = FetchType.LAZY,
      mappedBy = "fmsOrderItem",
      cascade = CascadeType.ALL,
      orphanRemoval = true)
  private FmsPickedItem pickedItem;

  @Builder
  public FmsOrderItem(
      String id,
      String itemId,
      String consumerItemNumber,
      long quantity,
      double weight,
      FmsOrder fmsOrder,
      ItemPriceInfo itemPriceInfo,
      SubstitutionOption substitutionOption) {
    super(id);

    this.assertArgumentTrue(quantity > 0, "Quantity should be non zero");
    this.assertArgumentNotEmpty(consumerItemNumber, "CIN number cannot be null or empty");

    this.itemId = itemId;
    this.consumerItemNumber = consumerItemNumber;
    this.quantity = quantity;
    this.weight = weight;
    this.fmsOrder = fmsOrder;
    this.itemPriceInfo = itemPriceInfo;
    this.substitutionOption = substitutionOption;
  }

  public List<String> getOrderItemUpcs() {
    return upcInfo != null ? upcInfo.getUpcNumbers() : Collections.emptyList();
  }

  public void enrichPickedInfoWithPickedItem(FmsPickedItem pickedItem) {

    this.pickedItem = Objects.requireNonNull(pickedItem);
  }

  public void addUpcInfo(ItemUpcInfo upcInfo) {
    this.upcInfo = upcInfo;
  }

  public void addCatalogInfo(ItemCatalogInfo catalogInfo) {
    this.catalogInfo = catalogInfo;
  }

  @JsonIgnore
  public long getPickedItemQuantity() {
    return this.getPickedItem() != null ? this.getPickedItem().getQuantity() : 0;
  }

  @JsonIgnore
  public long getNilPickQuantity() {
    return this.getPickedItemQuantity() > 0
        ? Math.abs(this.getQuantity() - this.getPickedItemQuantity())
        : this.getQuantity();
  }

  @JsonIgnore
  public String getImageUrl() {
    return !isNull(this.getCatalogInfo()) ? this.getCatalogInfo().getImageUrl() : null;
  }

  @JsonIgnore
  public String getItemDescription() {
    return !isNull(this.getCatalogInfo()) ? this.getCatalogInfo().getPickerItemDescription() : null;
  }

  @JsonIgnore
  public String getSalesUnit() {
    return !isNull(this.getCatalogInfo()) ? this.getCatalogInfo().getSalesUnit() : null;
  }

  @JsonIgnore
  public BigDecimal getUnitPrice() {
    return !isNull(this.getItemPriceInfo())
        ? this.getItemPriceInfo().getUnitPriceAmount()
        : BigDecimal.ZERO;
  }

  @JsonIgnore
  public String getUnitOfMeasurement() {
    return !isNull(this.getCatalogInfo()) ? this.getCatalogInfo().getUnitOfMeasurement() : null;
  }

  @JsonIgnore
  public Integer getMaxIdealDayValue() {
    return !isNull(this.getCatalogInfo()) ? this.getCatalogInfo().getMaxIdealDays() : null;
  }

  @JsonIgnore
  public Integer getMinIdealDayValue() {
    return !isNull(this.getCatalogInfo()) ? this.getCatalogInfo().getMinIdealDays() : null;
  }

  @JsonIgnore
  public String getTemparatureZone() {
    return !isNull(this.getCatalogInfo()) ? this.getCatalogInfo().getUnitOfMeasurement() : null;
  }

  @JsonIgnore
  public boolean isSellbyDateRequired() {
    return !isNull(this.getCatalogInfo()) && this.getCatalogInfo().isSellbyDateRequired();
  }

  @JsonIgnore
  public List<String> getUpcNumbersList() {
    return !isNull(getUpcInfo()) ? this.getUpcInfo().getUpcNumbers() : Collections.emptyList();
  }

  @JsonIgnore
  public float getItemUnitPriceAmountValue() {

    return Optional.ofNullable(getItemPriceInfo())
        .map(ItemPriceInfo::getUnitPriceAmount)
        .map(BigDecimal::floatValue)
        .orElse(0f);
  }

  @JsonIgnore
  public SubstitutionOption getSubstitutionOption() {
    return Optional.ofNullable(substitutionOption).orElse(SubstitutionOption.DO_NOT_SUBSTITUTE);
  }

  @JsonIgnore
  public boolean isSubstitutionAllowed() {
    return getSubstitutionOption() == SubstitutionOption.SUBSTITUTE;
  }

  @JsonIgnore
  public boolean isFmsOrderItemNilPicked() {
    return Optional.ofNullable(this.getPickedItem()).map(FmsPickedItem::isNilPicked).orElse(false);
  }

  @JsonIgnore
  public boolean isSubstitutionOptionCancelOrder() {
    return this.getSubstitutionOption().isSubstitutionOptionCancelOrder();
  }

  @JsonIgnore
  public boolean isCancellationValid() {
    return this.isSubstitutionOptionCancelOrder() && this.isFmsOrderItemNilPicked();
  }
}
