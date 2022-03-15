package com.walmart.fms.order.valueobject.events;

import java.math.BigDecimal;
import java.util.List;
import lombok.Data;

@Data
public class FmsSubstitutedItemValueObject {
  private String description;
  private String walmartItemNumber;
  private String consumerItemNumber;
  private String department;
  private Long quantity;
  private Double weight;
  private FmsSubstitutedItemPriceInfoVo substitutedItemPriceInfo;
  private List<FmsSubstitutedItemUpcValueObject> upcs;

  @Data
  public static class FmsSubstitutedItemPriceInfoVo {
    private BigDecimal unitPrice;
    private BigDecimal totalPrice;
  }
}
