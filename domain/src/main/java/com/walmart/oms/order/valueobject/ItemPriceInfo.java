package com.walmart.oms.order.valueobject;

import com.walmart.common.domain.AssertionConcern;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Objects;
import java.util.Optional;
import javax.persistence.AttributeOverride;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@Getter
@NoArgsConstructor
public class ItemPriceInfo extends AssertionConcern implements Serializable {

  public ItemPriceInfo(Money vendorUnitPrice, Money vendorTotalPrice) {

    this.vendorTotalPrice =
        Objects.requireNonNull(vendorTotalPrice, "Vendor total price cannot be null");
    this.vendorUnitPrice =
        Objects.requireNonNull(vendorUnitPrice, "Vendor Unit price cannot be null");
    this.assertArgumentTrue(
        vendorUnitPrice.getAmount().compareTo(BigDecimal.ZERO) > 0,
        "Vendor unit price amount should be greater than 0 ");
    this.vendorUnitPrice = vendorUnitPrice;
    this.vendorTotalPrice = vendorTotalPrice;
  }

  public ItemPriceInfo(Money vendorUnitPrice, Money vendorTotalPrice, Money unitPrice) {

    this.vendorTotalPrice =
        Objects.requireNonNull(vendorTotalPrice, "Vendor total price cannot be null");
    this.vendorUnitPrice =
        Objects.requireNonNull(vendorUnitPrice, "Vendor Unit price cannot be null");
    this.assertArgumentTrue(
        vendorUnitPrice.getAmount().compareTo(BigDecimal.ZERO) > 0,
        "Vendor unit price amount should be greater than 0 ");
    this.vendorUnitPrice = vendorUnitPrice;
    this.vendorTotalPrice = vendorTotalPrice;
    this.unitPrice = unitPrice;
  }

  @AttributeOverride(name = "amount", column = @Column(name = "UNIT_PRICE"))
  private Money unitPrice;

  @AttributeOverride(name = "amount", column = @Column(name = "VENDOR_UNIT_PRICE"))
  private Money vendorUnitPrice;

  @AttributeOverride(name = "amount", column = @Column(name = "VENDOR_TOTAL_PRICE"))
  private Money vendorTotalPrice;

  public void withUnitPriceFromCatalog(Money unitPriceFromCatalog) {
    this.unitPrice =
        Objects.requireNonNull(unitPriceFromCatalog, "Unit price from catalog cannot be null");
  }

  public BigDecimal getUnitPriceAmount() {
    return Optional.ofNullable(this.unitPrice).map(Money::getAmount).orElse(BigDecimal.ZERO);
  }

  public BigDecimal getVendorUnitPriceAmount() {
    return Optional.ofNullable(this.vendorUnitPrice).map(Money::getAmount).orElse(BigDecimal.ZERO);
  }

  public BigDecimal getVendorTotalPriceAmount() {
    return Optional.ofNullable(this.vendorTotalPrice).map(Money::getAmount).orElse(BigDecimal.ZERO);
  }
}
