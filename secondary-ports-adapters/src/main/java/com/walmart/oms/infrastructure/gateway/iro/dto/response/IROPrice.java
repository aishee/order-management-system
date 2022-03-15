package com.walmart.oms.infrastructure.gateway.iro.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class IROPrice implements Serializable {

  @JsonProperty("price_info")
  private IROPriceInfoDTO priceInfo;

  @JsonProperty("is_on_sale")
  private boolean isOnSale;

  @JsonIgnoreProperties(ignoreUnknown = true)
  @Data
  public static class IROPriceInfoDTO implements Serializable {

    @JsonProperty("price_per_uom")
    private String pricePerUom;

    @JsonProperty("price")
    private String price;

    @JsonProperty("sale_price")
    private String salePrice;
  }
}
