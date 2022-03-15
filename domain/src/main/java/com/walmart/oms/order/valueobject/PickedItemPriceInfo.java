package com.walmart.oms.order.valueobject;

import com.walmart.common.utils.NumberUtils;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Objects;
import java.util.Optional;
import javax.persistence.AttributeOverride;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Embedded;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Embeddable
@NoArgsConstructor
public class PickedItemPriceInfo implements Serializable {

  @Embedded
  @AttributeOverride(name = "amount", column = @Column(name = "UNIT_PRICE"))
  private Money unitPrice;

  @Embedded
  @AttributeOverride(name = "amount", column = @Column(name = "ADJUSTED_PRICE_EX_VAT"))
  private Money adjustedPriceExVat;

  @Embedded
  @AttributeOverride(name = "amount", column = @Column(name = "ADJUSTED_PRICE"))
  private Money adjustedPrice;

  @Embedded
  @AttributeOverride(name = "amount", column = @Column(name = "WEB_ADJUSTED_PRICE"))
  private Money webAdjustedPrice;

  @Embedded
  @AttributeOverride(name = "amount", column = @Column(name = "DISPLAY_PRICE"))
  private Money displayPrice;

  @Embedded
  @AttributeOverride(name = "amount", column = @Column(name = "VAT_AMOUNT"))
  private Money vatAmount;

  @Builder
  public PickedItemPriceInfo(Money unitPrice) {
    this.unitPrice = Objects.requireNonNull(unitPrice);
  }

  public void addPostPricingPrices(
      Money adjustedPriceExVat,
      Money adjustedPrice,
      Money webAdjustedPrice,
      Money displayPrice,
      Money vatAmount) {

    this.adjustedPriceExVat = Objects.requireNonNull(adjustedPriceExVat);
    this.adjustedPrice = Objects.requireNonNull(adjustedPrice);
    this.displayPrice = Objects.requireNonNull(displayPrice);
    this.vatAmount = Objects.requireNonNull(vatAmount);
    this.webAdjustedPrice = Objects.requireNonNull(webAdjustedPrice);
  }

  public BigDecimal getUnitPriceAmount() {
    return Optional.ofNullable(this.unitPrice)
        .map(Money::getAmount)
        .map(NumberUtils::getRoundedBigDecimal)
        .orElse(BigDecimal.ZERO);
  }
}
