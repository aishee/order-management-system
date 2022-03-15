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
public class Item {

  @JsonProperty("name")
  public String name;

  @JsonProperty("description")
  public String description;

  @JsonProperty("plu")
  public String plu;

  @JsonProperty("price")
  public int price;

  @JsonProperty("notes")
  public String notes;

  public BigDecimal getItemPrice() {
    return PriceConversionUtil.convertPriceToAmount(getPrice());
  }
}
