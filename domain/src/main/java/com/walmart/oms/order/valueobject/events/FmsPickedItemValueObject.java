package com.walmart.oms.order.valueobject.events;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.math.BigDecimal;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class FmsPickedItemValueObject {

  private String departmentID;

  private String orderedCin;

  private long quantity;

  private double weight;

  private String pickedItemDescription;

  private String pickerUserName;

  private BigDecimal unitPrice;

  private List<FmsPickedItemUpcVo> pickedItemUpcList;

  private List<FmsSubstitutedItemValueObject> substitutedItems;

  @Getter
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  @ToString
  public static class FmsSubstitutedItemValueObject {
    private String walmartItemNumber;
    private String department;
    private String description;
    private Long quantity;
    private String consumerItemNumber;
    private Double weight;
    private List<SubstitutedItemUpcValueObject> upcs;
    private SubstitutedItemPriceInfoValueObject substitutedItemPriceInfo;
  }

  @Getter
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  @ToString
  public static class SubstitutedItemUpcValueObject {
    private String upc;
    private String uom;
  }

  @Getter
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  @ToString
  public static class SubstitutedItemPriceInfoValueObject {
    private BigDecimal totalPrice;
    private BigDecimal unitPrice;
  }
}
