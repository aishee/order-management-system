package com.walmart.marketplace.commands.mapper;

import com.walmart.marketplace.commands.MarketPlacePickCompleteCommand;
import com.walmart.marketplace.domain.event.messages.MarketPlaceItemAttributes;
import com.walmart.marketplace.domain.event.messages.MarketPlacePickCompleteMessage;
import com.walmart.marketplace.domain.event.messages.MarketplaceBundledItemAttributes;
import com.walmart.marketplace.order.aggregateroot.MarketPlaceOrder;
import com.walmart.marketplace.order.domain.entity.MarketPlaceBundledItem;
import com.walmart.marketplace.order.domain.entity.MarketPlaceItem;
import java.util.List;
import java.util.stream.Collectors;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class MarketPlacePickCompleteCommandToDomainMessageMapper {

  public static MarketPlacePickCompleteMessage mapToMarketPlacePickCompleteMessage(
      MarketPlacePickCompleteCommand marketPlacePickCompleteCommand, MarketPlaceOrder order) {

    return MarketPlacePickCompleteMessage.builder()
        .marketPlaceItemAttributes(
            getListOfMarketPlaceItemAttributes(
                order.getMarketPlaceItems(), marketPlacePickCompleteCommand))
        .sourceOrderId(marketPlacePickCompleteCommand.getSourceOrderId())
        .vendorOrderId(marketPlacePickCompleteCommand.getVendorOrderId())
        .vendorId(marketPlacePickCompleteCommand.getVendorId())
        .storeId(marketPlacePickCompleteCommand.getStoreId())
        .vendorStoreId(order.getVendorStoreId())
        .build();
  }

  private static List<MarketPlaceItemAttributes> getListOfMarketPlaceItemAttributes(
      List<MarketPlaceItem> marketPlaceItemList,
      MarketPlacePickCompleteCommand marketPlacePickCompleteCommand) {
    return marketPlaceItemList.stream()
        .map(
            marketPlaceItem ->
                getMarketPlaceItemAttributes(marketPlaceItem, marketPlacePickCompleteCommand))
        .collect(Collectors.toList());
  }

  private static MarketPlaceItemAttributes getMarketPlaceItemAttributes(
      MarketPlaceItem marketPlaceItem,
      MarketPlacePickCompleteCommand marketPlacePickCompleteCommand) {
    String itemId = marketPlaceItem.getItemId();
    return MarketPlaceItemAttributes.builder()
        .itemId(itemId)
        .vendorInstanceId(marketPlaceItem.getVendorInstanceId())
        .externalItemId(marketPlaceItem.getExternalItemId())
        .pickedQuantity(marketPlacePickCompleteCommand.getPickedQuantity(itemId))
        .orderedQuantity(marketPlacePickCompleteCommand.getOrderedQuantity(itemId))
        .marketplaceBundledItemAttributesList(
            getListOfMarketPlaceBundleItemAttributes(marketPlaceItem.getBundledItemList()))
        .build();
  }

  private static List<MarketplaceBundledItemAttributes> getListOfMarketPlaceBundleItemAttributes(
      List<MarketPlaceBundledItem> marketPlaceBundledItems) {
    return marketPlaceBundledItems.stream()
        .map(
            MarketPlacePickCompleteCommandToDomainMessageMapper::getMarketPlaceBundledItemAttributes)
        .collect(Collectors.toList());
  }

  private static MarketplaceBundledItemAttributes getMarketPlaceBundledItemAttributes(
      MarketPlaceBundledItem marketPlaceBundledItem) {
    return MarketplaceBundledItemAttributes.builder()
        .bundledQuantity((int) marketPlaceBundledItem.getTotalBundleItemQuantity())
        .bundleInstanceId(marketPlaceBundledItem.getBundleInstanceId())
        .build();
  }
}
