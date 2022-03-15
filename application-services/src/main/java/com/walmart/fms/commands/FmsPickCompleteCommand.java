package com.walmart.fms.commands;

import com.walmart.common.domain.type.CancellationReason;
import com.walmart.fms.commands.extensions.OrderInfo;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
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
public class FmsPickCompleteCommand {

  private static final String CANCELLED = "CANCELLED";
  private static final String CANCELLED_BY_STORE = "STORE";

  private FmsOrderData data;

  public String getStoreOrderId() {
    return this.data.getStoreOrderId();
  }

  public String getCancelReasonCode() {
    return this.data.getCancelReasonCode();
  }

  public String getCancelledReasonDescription() {
    return this.data.getCancelledReasonDescription();
  }

  public boolean isOrderCancelled() {
    return this.data.isOrderCancelled();
  }

  public boolean isAnyItemNotPickedCompletely() {
    return this.data.isAnyItemNotPickedCompletely();
  }

  public List<PickedItemInfo> getNotFullyPickedItems() {
    return this.data.getNotFullyPickedItems();
  }

  public List<String> getNotFullyPickedItemsCin() {
    return this.getNotFullyPickedItems().stream()
        .map(FmsPickCompleteCommand.PickedItemInfo::getCin)
        .collect(Collectors.toList());
  }

  @Getter
  @Setter
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class FmsOrderData {

    private OrderInfo orderInfo;

    private List<PickedItemInfo> pickedItems;

    public boolean isOrderCancelled() {
      return Optional.ofNullable(orderInfo)
          .map(OrderInfo::getOrderStatus)
          .filter(CANCELLED::equalsIgnoreCase)
          .isPresent();
    }

    public String getCancelReasonCode() {
      return Optional.ofNullable(orderInfo)
          .flatMap(OrderInfo::getOptionalCancelledReasonCode)
          .orElse(CancellationReason.DEFAULT.getCode());
    }

    public String getCancelledReasonDescription() {
      return Optional.ofNullable(orderInfo)
          .map(OrderInfo::getCancelledReasonDescription)
          .orElse(CancellationReason.DEFAULT.getDescription());
    }

    public String getStoreOrderId() {
      return this.orderInfo.getStoreOrderId();
    }

    public List<PickedItemInfo> getPickedItems() {
      return Optional.ofNullable(pickedItems).orElse(Collections.emptyList());
    }

    public boolean isAnyItemNotPickedCompletely() {
      return getPickedItems().stream().anyMatch(PickedItemInfo::isNotFullyPicked);
    }

    public List<PickedItemInfo> getNotFullyPickedItems() {
      return getPickedItems().stream()
          .filter(PickedItemInfo::isNotFullyPicked)
          .collect(Collectors.toList());
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

    private long orderedQuantity;

    private BigDecimal unitPrice;

    private List<SubstitutedItemInfo> substitutedItemInfoList;

    public List<PickedItemUpc> getPickedItemUpcs() {
      return Optional.ofNullable(pickedItemUpcs).orElse(Collections.emptyList());
    }

    public long getTotalPickedQuantity() {
      return getPickedItemUpcs().stream().mapToLong(PickedItemUpc::getPickedQuantity).sum();
    }

    public boolean isNotFullyPicked() {
      return getOrderedQuantity() != getTotalPickedQuantity();
    }

    public List<SubstitutedItemInfo> getSubstitutedItemInfoList() {
      return Optional.ofNullable(substitutedItemInfoList)
          .orElse(Collections.emptyList());
    }
  }

  @Getter
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class SubstitutedItemInfo {

    private String description;

    private String walmartItemNumber;

    private String consumerItemNumber;

    private String department;

    private Long quantity;

    private Double weight;

    private List<SubstitutedItemUpc> upcs;

    private BigDecimal unitPrice;

    public List<SubstitutedItemUpc> getUpcs() {
      return Optional.ofNullable(upcs)
          .orElse(Collections.emptyList());
    }
  }

  @Getter
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
  public static class PickedItemUpc {

    private long pickedQuantity;

    private String upc;

    private BigDecimal unitPrice;

    private String uom;

    private double weight;

    private String win;
  }
}
