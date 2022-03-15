package com.walmart.oms.order.valueobject;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class PricingResponse implements Serializable {

  private Double posOrderTotalPrice;

  private Map<String, ItemPriceService> itemPriceServiceMap;

  public boolean hasItemPricingDetails(String consumerItemNumber) {
    return itemPriceServiceMap.containsKey(consumerItemNumber);
  }

  public ItemPriceService getItemPricingDetails(String consumerItemNumber) {
    return itemPriceServiceMap.get(consumerItemNumber);
  }

  @Data
  @NoArgsConstructor
  public static class ItemPriceService implements Serializable {
    private Double adjustedPrice;

    private Double adjustedPriceExVat;

    private Double webAdjustedPrice;

    private Double displayPrice;

    private Double vatAmount;

    private Map<String, SubstitutedItemPriceResponse> substitutedItemPriceResponseMap;

    public Map<String, SubstitutedItemPriceResponse> getSubstitutedItemPriceResponseMap() {
      return Optional.ofNullable(substitutedItemPriceResponseMap).orElse(Collections.emptyMap());
    }

    public boolean hasSubstitutedItemPriceDetails(String walmartItemNumber) {
      return getSubstitutedItemPriceResponseMap().containsKey(walmartItemNumber);
    }

    public SubstitutedItemPriceResponse getSubstitutedItemPriceDetails(String walmartItemNumber) {
      return getSubstitutedItemPriceResponseMap().get(walmartItemNumber);
    }
  }

  @Data
  @NoArgsConstructor
  public static class SubstitutedItemPriceResponse implements Serializable {
    private Double adjustedPrice;

    private Double adjustedPriceExVat;

    private Double webAdjustedPrice;

    private Double vatAmount;

    public BigDecimal getAdjustedPrice() {
      return BigDecimal.valueOf(adjustedPrice);
    }

    public BigDecimal getAdjustedPriceExVat() {
      return BigDecimal.valueOf(adjustedPriceExVat);
    }

    public BigDecimal getWebAdjustedPrice() {
      return BigDecimal.valueOf(webAdjustedPrice);
    }

    public BigDecimal getVatAmount() {
      return BigDecimal.valueOf(vatAmount);
    }
  }
}
