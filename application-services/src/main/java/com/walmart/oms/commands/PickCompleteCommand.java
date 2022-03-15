package com.walmart.oms.commands;

import com.walmart.common.domain.type.Tenant;
import com.walmart.common.domain.type.Vertical;
import com.walmart.oms.commands.extensions.OrderInfo;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PickCompleteCommand {

  private PickCompleteCommand.OmsOrderData data;

  public String getSourceOrderId() {
    return this.getData().getSourceOrderId();
  }

  public Tenant getTenant() {
    return this.getData().getTenant();
  }

  public Vertical getVertical() {
    return this.getData().getVertical();
  }

  public List<PickedItemInfo> getPickedItems() {
    return this.getData().getPickedItems();
  }

  @Getter
  @Setter
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class OmsOrderData {

    private OrderInfo orderInfo;

    private List<PickedItemInfo> pickedItems;

    public String getSourceOrderId() {
      return this.getOrderInfo().getSourceOrderId();
    }

    public Tenant getTenant() {
      return this.getOrderInfo().getTenant();
    }

    public Vertical getVertical() {
      return this.getOrderInfo().getVertical();
    }
  }

  @Getter
  @Setter
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class PickedItemInfo {

    private String cin;

    private String departmentId;

    private String pickedItemDescription;

    private String pickedBy;

    private List<PickedItemUpc> pickedItemUpcs;

    private List<SubstitutedItemInfo> substitutedItems;

    public List<SubstitutedItemInfo> getSubstitutedItems() {
      return Optional.ofNullable(substitutedItems)
          .orElse(Collections.emptyList());
    }
  }

  @Getter
  @Setter
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class SubstitutedItemInfo {
    private String walmartItemNumber;
    private String department;
    private String description;
    private Long quantity;
    private String consumerItemNumber;
    private Double weight;
    private List<SubstitutedItemUpc> upcs;
    private SubstitutedItemPriceInfo substitutedItemPriceInfo;

    public BigDecimal getUnitPrice() {
      return substitutedItemPriceInfo.getUnitPrice();
    }

    public BigDecimal getTotalPrice() {
      return substitutedItemPriceInfo.getTotalPrice();
    }

    public List<SubstitutedItemUpc> getUpcs() {
      return Optional.ofNullable(upcs)
          .orElse(Collections.emptyList());
    }
  }

  @Getter
  @Setter
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class SubstitutedItemUpc {
    private String upc;
    private String uom;
  }

  @Getter
  @Setter
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class SubstitutedItemPriceInfo {
    private BigDecimal totalPrice;
    private BigDecimal unitPrice;
  }

  @Getter
  @Setter
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class PickedItemUpc {

    private long pickedQuantity;

    private String upc;

    private BigDecimal unitPrice;

    private String uom;

    private double weight;

    private String win;
  }
}
