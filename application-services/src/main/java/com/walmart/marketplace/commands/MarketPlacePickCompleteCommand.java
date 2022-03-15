package com.walmart.marketplace.commands;

import com.walmart.marketplace.order.domain.entity.type.Vendor;
import com.walmart.marketplace.order.domain.valueobject.MarketPlaceOrderValueObject;
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
@AllArgsConstructor
@Builder
public class MarketPlacePickCompleteCommand {

  private MarketPlacePickCompleteCommand.MarketPlacePickCompleteCommandData data;

  /**
   * Build MarketPlacePickCompleteCommand object
   *
   * @param marketPlaceOrderValueObject
   * @return
   */
  public static MarketPlacePickCompleteCommand buildMarketPlacePickCompleteCommand(
      MarketPlaceOrderValueObject marketPlaceOrderValueObject) {
    return MarketPlacePickCompleteCommand.builder()
        .data(
            MarketPlacePickCompleteCommandData.builder()
                .sourceOrderId(marketPlaceOrderValueObject.getSourceOrderId())
                .vendorId(marketPlaceOrderValueObject.getVendorId())
                .vendorOrderId(marketPlaceOrderValueObject.getVendorOrderId())
                .storeId(marketPlaceOrderValueObject.getStoreId())
                .marketplacePickCompleteItemCommands(
                    getMarketPlacePickCompleteItemCommandList(marketPlaceOrderValueObject))
                .build())
        .build();
  }

  private static List<MarketplacePickCompleteItemCommand> getMarketPlacePickCompleteItemCommandList(
      MarketPlaceOrderValueObject marketPlaceOrderValueObject) {
    return marketPlaceOrderValueObject.getItems().stream()
        .map(MarketPlacePickCompleteCommand::buildMarketplacePickCompleteItem)
        .collect(Collectors.toList());
  }

  private static MarketplacePickCompleteItemCommand buildMarketplacePickCompleteItem(
      MarketPlaceOrderValueObject.Item item) {
    return MarketplacePickCompleteItemCommand.builder()
        .itemId(item.getItemIdentifier().getItemId())
        .orderedQuantity(item.getQuantity())
        .instanceId(item.getExternalItemId())
        .pickedItemCommand(buildPickedItemCommand(item))
        .build();
  }

  private static PickedItemCommand buildPickedItemCommand(MarketPlaceOrderValueObject.Item item) {
    return PickedItemCommand.builder()
        .orderedCin(item.getPickedItemId())
        .pickedQuantity((int) item.getPickedItemQuantity())
        .substitutedItems(item.getSubstitutedItems()
            .stream()
            .map(MarketPlacePickCompleteCommand::buildSubstitutedItem)
            .collect(Collectors.toList()))
        .build();
  }

  private static SubstitutedItem buildSubstitutedItem(MarketPlaceOrderValueObject.SubstitutedItem item) {
    return SubstitutedItem.builder()
        .externalItemId(item.getExternalItemId())
        .description(item.getDescription())
        .quantity(item.getQuantity())
        .unitPrice(item.getUnitPrice())
        .totalPrice(item.getTotalPrice())
        .build();
  }

  public String getSourceOrderId() {
    return this.getData().getSourceOrderId();
  }

  public String getVendorOrderId() {
    return getData().getVendorOrderId();
  }

  public Vendor getVendorId() {
    return getData().getVendorId();
  }

  public String getStoreId() {
    return getData().getStoreId();
  }

  public int getPickedQuantity(String itemId) {
    return getData().getPickedQuantity(itemId);
  }

  public int getOrderedQuantity(String itemId) {
    return getData().getOrderedQuantity(itemId);
  }

  @Builder
  @Getter
  @AllArgsConstructor
  public static class MarketPlacePickCompleteCommandData {
    private String sourceOrderId;
    private String vendorOrderId;
    private Vendor vendorId;
    private String storeId;
    private List<MarketplacePickCompleteItemCommand> marketplacePickCompleteItemCommands;

    public List<MarketplacePickCompleteItemCommand> getMarketplacePickCompleteItemCommands() {
      return Optional.ofNullable(marketplacePickCompleteItemCommands)
          .orElse(Collections.emptyList());
    }

    public int getPickedQuantity(String itemId) {
      return getMarketplacePickCompleteItemCommands().stream()
          .filter(
              marketplacePickCompleteItemCommand ->
                  marketplacePickCompleteItemCommand.getItemId().equalsIgnoreCase(itemId))
          .findFirst()
          .map(MarketplacePickCompleteItemCommand::getPickedQuantity)
          .orElse(0);
    }

    public int getOrderedQuantity(String itemId) {
      return getMarketplacePickCompleteItemCommands().stream()
          .filter(
              marketplacePickCompleteItemCommand ->
                  marketplacePickCompleteItemCommand.getItemId().equalsIgnoreCase(itemId))
          .findFirst()
          .map(MarketplacePickCompleteItemCommand::getOrderedQuantity)
          .orElse(0);
    }
  }

  @Getter
  @Builder
  @AllArgsConstructor
  public static class MarketplacePickCompleteItemCommand {
    private String itemId;
    private long orderedQuantity;
    private String instanceId;
    private PickedItemCommand pickedItemCommand;

    private int getPickedQuantity() {
      return pickedItemCommand.getPickedQuantity();
    }

    public String getOrderedCin() {
      return pickedItemCommand.getOrderedCin();
    }

    public List<SubstitutedItem> getSubstitutedItems() {
      return Optional.ofNullable(pickedItemCommand.getSubstitutedItems())
          .orElse(Collections.emptyList());
    }

    public boolean isSubstituted() {
      return !getSubstitutedItems().isEmpty();
    }

    private int getOrderedQuantity() {
      return (int) orderedQuantity;
    }
  }

  @Getter
  @Builder
  @AllArgsConstructor
  public static class PickedItemCommand {
    private String orderedCin;
    private int pickedQuantity;
    private List<SubstitutedItem> substitutedItems;
  }

  @Getter
  @Setter
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class SubstitutedItem {
    private Long quantity;
    private String description;
    private String externalItemId;
    private BigDecimal totalPrice;
    private BigDecimal unitPrice;
  }
}
