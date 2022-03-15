package com.walmart.marketplace.mappers;

import com.walmart.marketplace.commands.MarketPlacePickCompleteCommand;
import com.walmart.marketplace.order.domain.entity.MarketPlaceItem;
import com.walmart.marketplace.order.domain.entity.MarketPlaceSubstitutedItem;
import com.walmart.marketplace.order.domain.valueobject.MarketPlaceSubstitutedItemPriceInfo;
import com.walmart.marketplace.order.repository.IMarketPlaceRepository;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.beans.factory.annotation.Autowired;

@Mapper(componentModel = "spring")
public abstract class SubstituteItemCommandToMarketPlaceItemMapper {

  @Autowired
  protected IMarketPlaceRepository marketPlaceRepository;

  @Mapping(
      target = "substitutedItemPriceInfo",
      expression = "java(mapSubstitutedItemPrice(substitutedItem))")
  @Mapping(target = "id",
      expression = "java(marketPlaceRepository.getNextIdentity())")
  @Mapping(target = "marketPlaceItem",
      source = "marketPlaceItem")
  @Mapping(target = "quantity",
      source = "substitutedItem.quantity")
  @Mapping(target = "description",
      source = "substitutedItem.description")
  @Mapping(target = "externalItemId",
      source = "substitutedItem.externalItemId")
  public abstract MarketPlaceSubstitutedItem mapToSubstitutedItemEntity(
      MarketPlacePickCompleteCommand.SubstitutedItem substitutedItem,
      MarketPlaceItem marketPlaceItem);

  public void mapToSubstitutedItemEntityList(List<MarketPlaceItem> marketPlaceItems,
                                             List<MarketPlacePickCompleteCommand
                                                 .MarketplacePickCompleteItemCommand>
                                                 pickedItems) {
    Map<String, MarketPlaceItem> marketPlaceItemMap = marketPlaceItems.stream()
        .collect(Collectors.toMap(MarketPlaceItem::getExternalItemId,
            Function.identity()));

    pickedItems.stream().filter(MarketPlacePickCompleteCommand
        .MarketplacePickCompleteItemCommand::isSubstituted)
        .forEach(pickedItemCommand ->
            mapToSubstitutedItemEntityList(
                pickedItemCommand, marketPlaceItemMap.get(pickedItemCommand.getOrderedCin())));
  }

  public void mapToSubstitutedItemEntityList(
      MarketPlacePickCompleteCommand.MarketplacePickCompleteItemCommand pickCompleteItemCommand,
      MarketPlaceItem marketPlaceItem) {
    List<MarketPlaceSubstitutedItem> substitutedItems = pickCompleteItemCommand
        .getSubstitutedItems()
        .stream()
        .map(substitutedItem -> mapToSubstitutedItemEntity(substitutedItem, marketPlaceItem))
        .collect(Collectors.toList());
    marketPlaceItem.setSubstitutedItemList(substitutedItems);
  }

  public abstract MarketPlaceSubstitutedItemPriceInfo mapSubstitutedItemPrice(
      MarketPlacePickCompleteCommand.SubstitutedItem substitutedItem);

}
