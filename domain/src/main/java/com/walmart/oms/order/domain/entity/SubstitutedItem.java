package com.walmart.oms.order.domain.entity;

import com.walmart.common.domain.BaseEntity;
import com.walmart.oms.order.valueobject.SubstitutedItemPriceInfo;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Entity
@Getter
@Table(name = "OMSCORE.OMS_SUBSTITUTED_ITEM")
@NoArgsConstructor
@Slf4j
public class SubstitutedItem extends BaseEntity {

  @Column(name = "ITEM_DESCRIPTION")
  private String description;

  @Column(name = "WALMART_ITEM_NUM")
  private String walmartItemNumber;

  @Column(name = "CONSUMER_ITEM_NUM")
  private String consumerItemNumber;

  @Column(name = "DEPARTMENT_ID")
  private String department;

  @Column(name = "QUANTITY")
  private Long quantity;

  @Column(name = "WEIGHT")
  private Double weight;

  @Embedded private SubstitutedItemPriceInfo substitutedItemPriceInfo;

  @OneToMany(
      fetch = FetchType.LAZY,
      cascade = CascadeType.ALL,
      mappedBy = "substitutedItem",
      orphanRemoval = true)
  private List<SubstitutedItemUpc> upcs;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "PICKED_ITEM_RECORD_ID")
  private PickedItem pickedItem;

  @Builder
  public SubstitutedItem(
      String id,
      String description,
      String department,
      String consumerItemNumber,
      String walmartItemNumber,
      Long quantity,
      Double weight,
      SubstitutedItemPriceInfo substitutedItemPriceInfo,
      List<SubstitutedItemUpc> upcs) {
    super(id);
    this.assertArgumentNotNull(consumerItemNumber, "cin cannot be empty");
    this.assertArgumentNotEmpty(description, "Description cannot be empty or null");
    this.assertArgumentNotNull(upcs, "upcs cannot be empty");
    this.assertArgumentNotEmpty(department, "department cannot be null");

    this.description = description;
    this.department = department;
    this.weight = weight;
    this.substitutedItemPriceInfo = substitutedItemPriceInfo;
    this.upcs = upcs;
    this.walmartItemNumber = walmartItemNumber;
    this.quantity = quantity;
    this.consumerItemNumber = consumerItemNumber;
  }

  public void addPickedItem(PickedItem pickedItem) {
    this.pickedItem = pickedItem;
  }

  public BigDecimal getSubstitutedItemUnitPrice() {
    return this.substitutedItemPriceInfo.getUnitPrice();
  }

  public BigDecimal getSubstitutedItemTotalPrice() {
    return this.substitutedItemPriceInfo.getTotalPrice();
  }

  public Optional<BigDecimal> getSubstitutedItemAdjustedPriceExVat() {
    return this.substitutedItemPriceInfo.getAdjustedPriceExVat();
  }

  public Optional<BigDecimal> getSubstitutedItemAdjustedPrice() {
    return this.substitutedItemPriceInfo.getAdjustedPrice();
  }

  public Optional<BigDecimal> getSubstitutedItemWebAdjustedPrice() {
    return this.substitutedItemPriceInfo.getWebAdjustedPrice();
  }

  public Optional<BigDecimal> getSubstitutedItemVatAmount() {
    return this.substitutedItemPriceInfo.getVatAmount();
  }

  public Optional<BigDecimal> getSubstitutedItemVendorUnitPrice() {
    return this.substitutedItemPriceInfo.getVendorUnitPrice();
  }

  public Optional<SubstitutedItemUpc> getSubstitutedItemUpc() {
    return Optional.ofNullable(this.getUpcs()).flatMap(upcList -> upcList.stream().findFirst());
  }

  public Optional<String> getSubstitutedItemUom() {
    return getSubstitutedItemUpc().map(SubstitutedItemUpc::getUom);
  }

  public Optional<String> getSubstitutedItemUpcDetail() {
    return getSubstitutedItemUpc().map(SubstitutedItemUpc::getUpc);
  }

  public List<SubstitutedItemUpc> getUpcs() {
    return Optional.ofNullable(this.upcs).orElse(Collections.emptyList());
  }

  public void addPostPricingPrices(
      BigDecimal adjustedPriceExVat,
      BigDecimal adjustedPrice,
      BigDecimal webAdjustedPrice,
      BigDecimal vatAmount) {
    substitutedItemPriceInfo.addPostPricingPrices(
        adjustedPriceExVat, adjustedPrice, webAdjustedPrice, vatAmount);
  }
}
