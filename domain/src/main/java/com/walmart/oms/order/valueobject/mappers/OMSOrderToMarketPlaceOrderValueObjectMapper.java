package com.walmart.oms.order.valueobject.mappers;

import com.walmart.marketplace.order.domain.valueobject.MarketPlaceOrderValueObject;
import com.walmart.marketplace.order.domain.valueobject.mappers.CancellationDetailsValueObjectMapper;
import com.walmart.oms.order.aggregateroot.OmsOrder;
import com.walmart.oms.order.domain.entity.OmsOrderItem;
import com.walmart.oms.order.domain.entity.PickedItem;
import com.walmart.oms.order.domain.entity.SubstitutedItem;
import com.walmart.oms.order.valueobject.CancelDetails;
import com.walmart.oms.order.valueobject.events.CancellationDetailsValueObject;
import java.util.List;
import java.util.stream.Collectors;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper
public abstract class OMSOrderToMarketPlaceOrderValueObjectMapper {

  public static final OMSOrderToMarketPlaceOrderValueObjectMapper INSTANCE =
      Mappers.getMapper(OMSOrderToMarketPlaceOrderValueObjectMapper.class);

  @Mapping(target = "vendorId", source = "marketPlaceInfo.vendor")
  @Mapping(target = "vendorOrderId", source = "marketPlaceInfo.vendorOrderId")
  @Mapping(target = "nilPicks", expression = "java(omsOrder.getNilPicks())")
  @Mapping(target = "partialPicks", expression = "java(omsOrder.getPartialPicks())")
  @Mapping(target = "items", expression = "java(mapItems(omsOrder))")
  @Mapping(target = "cancellationDetails", expression = "java(convertToCancellationDetailsValueObject(omsOrder.getCancelDetails()))")
  public abstract MarketPlaceOrderValueObject convertOmsOrderToMarketPlaceOrderValueObject(
      OmsOrder omsOrder);

  @Mapping(target = "quantity", source = "omsOrderItem.quantity")
  @Mapping(
      target = "itemIdentifier",
      expression = "java(convertOmsOrderItemToItemIdentifier(omsOrderItem))")
  @Mapping(
      target = "pickedItem",
      expression = "java(convertOmsOrderItemToPickedItem(omsOrderItem.getPickedItem()))")
  @Mapping(target = "itemPriceInfo", expression = "java(convertItemPriceInfo(omsOrderItem))")
  public abstract MarketPlaceOrderValueObject.Item convertOmsOrderItemToMarketplaceOrderValueObjectItem(
      OmsOrderItem omsOrderItem);

  @Mapping(
      target = "unitPrice",
      expression = "java(omsOrderItem.getOrderedOrPickedItemUnitPrice().doubleValue())")
  public abstract MarketPlaceOrderValueObject.ItemPriceInfo convertItemPriceInfo(
      OmsOrderItem omsOrderItem);

  @Mapping(target = "itemId", source = "omsOrderItem.cin")
  public abstract MarketPlaceOrderValueObject.ItemIdentifier convertOmsOrderItemToItemIdentifier(
      OmsOrderItem omsOrderItem);

  @Mapping(target = "itemId", source = "pickedItem.orderedCin")
  @Mapping(target = "pickedQuantity", source = "pickedItem.quantity")
  @Mapping(target = "substitutedItems", source = "pickedItem.substitutedItems")
  public abstract MarketPlaceOrderValueObject.PickedItem convertOmsOrderItemToPickedItem(
      PickedItem pickedItem);

  protected List<MarketPlaceOrderValueObject.Item> mapItems(OmsOrder omsOrder) {
    return omsOrder.getOrderItemList().stream()
        .map(this::convertOmsOrderItemToMarketplaceOrderValueObjectItem)
        .collect(Collectors.toList());
  }

  protected CancellationDetailsValueObject convertToCancellationDetailsValueObject(
      CancelDetails cancellationDetails) {
    return CancellationDetailsValueObjectMapper.INSTANCE.modelToValueObject(cancellationDetails);
  }


  @Mapping(target = "externalItemId", source = "substitutedItem.consumerItemNumber")
  @Mapping(target = "totalPrice", expression = "java(substitutedItem.getSubstitutedItemTotalPrice())")
  @Mapping(target = "unitPrice", expression = "java(substitutedItem.getSubstitutedItemUnitPrice())")
  public abstract MarketPlaceOrderValueObject.SubstitutedItem substitutedItemToSubstitutedItem(SubstitutedItem substitutedItem);
}