package com.walmart.oms.order.valueobject;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Objects;
import java.util.Optional;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Embeddable
@NoArgsConstructor
public class SubstitutedItemPriceInfo implements Serializable {

  @Column(name = "UNIT_PRICE")
  private BigDecimal unitPrice;

  @Column(name = "TOTAL_PRICE")
  private BigDecimal totalPrice;

  @Column(name = "ADJUSTED_PRICE_EX_VAT")
  private BigDecimal adjustedPriceExVat;

  @Column(name = "ADJUSTED_PRICE")
  private BigDecimal adjustedPrice;

  @Column(name = "WEB_ADJUSTED_PRICE")
  private BigDecimal webAdjustedPrice;

  @Column(name = "VAT_AMOUNT")
  private BigDecimal vatAmount;

  @Column(name = "VENDOR_UNIT_PRICE")
  private BigDecimal vendorUnitPrice;

  @Column(name = "INFLATION_RATE")
  private BigDecimal inflationRate;

  @Builder
  public SubstitutedItemPriceInfo(BigDecimal unitPrice, BigDecimal totalPrice) {
    this.unitPrice = Objects.requireNonNull(unitPrice);
    this.totalPrice = Objects.requireNonNull(totalPrice);
  }

  public void addPostPricingPrices(
      BigDecimal adjustedPriceExVat,
      BigDecimal adjustedPrice,
      BigDecimal webAdjustedPrice,
      BigDecimal vatAmount) {
    this.adjustedPriceExVat = Objects.requireNonNull(adjustedPriceExVat);
    this.adjustedPrice = Objects.requireNonNull(adjustedPrice);
    this.webAdjustedPrice = Objects.requireNonNull(webAdjustedPrice);
    this.vatAmount = Objects.requireNonNull(vatAmount);
  }

  public Optional<BigDecimal> getVendorUnitPrice() {
    return Optional.ofNullable(vendorUnitPrice);
  }

  public Optional<BigDecimal> getAdjustedPriceExVat() {
    return Optional.ofNullable(adjustedPriceExVat);
  }

  public Optional<BigDecimal> getAdjustedPrice() {
    return Optional.ofNullable(adjustedPrice);
  }

  public Optional<BigDecimal> getWebAdjustedPrice() {
    return Optional.ofNullable(webAdjustedPrice);
  }

  public Optional<BigDecimal> getVatAmount() {
    return Optional.ofNullable(vatAmount);
  }
}
