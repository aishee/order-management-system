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
public class Adjustments {

  @JsonProperty("name")
  public String name;

  @JsonProperty("price")
  public JustEatsMoney price;

  public BigDecimal getAdjustmentPriceIncTax() {
    return getPrice().getAmountIncludingTax();
  }

  public boolean isBagFee() {
    return "bagFee".equalsIgnoreCase(getName());
  }
}
