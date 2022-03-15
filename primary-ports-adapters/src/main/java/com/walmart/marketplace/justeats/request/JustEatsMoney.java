package com.walmart.marketplace.justeats.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@Setter
@ToString
public class JustEatsMoney {

  @JsonProperty("inc_tax")
  public int incTax;

  @JsonProperty("tax")
  public int tax;

  public BigDecimal getAmountIncludingTax() {
    return PriceConversionUtil.convertPriceToAmount(getIncTax());
  }

  public BigDecimal getTax() {
    return PriceConversionUtil.convertPriceToAmount(tax);
  }
}
