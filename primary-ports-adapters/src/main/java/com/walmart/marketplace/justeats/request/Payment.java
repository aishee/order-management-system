package com.walmart.marketplace.justeats.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@Setter
@ToString
public class Payment {

  @JsonProperty("items_in_cart")
  public JustEatsMoney itemsInCart;

  @JsonProperty("adjustments")
  public List<Adjustments> adjustments;

  @JsonProperty("final")
  public JustEatsMoney finalAmount;

  public BigDecimal getFinalAmountIncludingTax() {
    return this.getFinalAmount().getAmountIncludingTax();
  }

  public BigDecimal getTaxAmount() {
    return this.getFinalAmount().getTax();
  }

  public BigDecimal getSubTotal() {
    return getFinalAmountIncludingTax().subtract(getTaxAmount());
  }

  public List<Adjustments> getAdjustments() {
    return Optional.ofNullable(adjustments).orElse(Collections.emptyList());
  }

  /** @return */
  public BigDecimal getBagFee() {

    return getAdjustments().stream()
        .filter(Adjustments::isBagFee)
        .findFirst()
        .map(Adjustments::getAdjustmentPriceIncTax)
        .orElse(BigDecimal.ZERO);
  }
}
