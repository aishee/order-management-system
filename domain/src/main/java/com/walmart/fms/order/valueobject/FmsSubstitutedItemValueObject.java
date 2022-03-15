package com.walmart.fms.order.valueobject;

import java.math.BigDecimal;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FmsSubstitutedItemValueObject {
  private String description;

  private String walmartItemNumber;

  private String consumerItemNumber;

  private String department;

  private Long quantity;

  private Double weight;

  private SubstitutedItemPriceInfoValueObject substitutedItemPriceInfo;

  private List<FmsSubstitutedItemUpcValueObject> upcs;


  @Getter
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class FmsSubstitutedItemUpcValueObject {
    private String uom;
    private String upc;
  }

  @Getter
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class SubstitutedItemPriceInfoValueObject {
    private BigDecimal unitPrice;
    private BigDecimal totalPrice;
  }
}
