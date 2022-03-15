package com.walmart.fms.order.valueobject.mappers;

import com.walmart.fms.order.aggregateroot.FmsOrder;
import com.walmart.fms.order.domain.entity.FmsOrderItem;
import com.walmart.fms.order.domain.entity.FmsPickedItem;
import com.walmart.fms.order.domain.entity.FmsPickedItemUpc;
import com.walmart.fms.order.valueobject.events.FmsOrderItemvalueObject;
import com.walmart.fms.order.valueobject.events.FmsOrderValueObject;
import com.walmart.fms.order.valueobject.events.FmsPickedItemUpcVo;
import com.walmart.fms.order.valueobject.events.FmsPickedItemValueObject;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper
public abstract class FMSOrderToFmsOrderValueObjectMapper {

  public static final FMSOrderToFmsOrderValueObjectMapper INSTANCE =
      Mappers.getMapper(FMSOrderToFmsOrderValueObjectMapper.class);

  @Mapping(source = "contactInfo.phoneNumberOne.number", target = "contactInfo.phoneNumberOne")
  @Mapping(source = "contactInfo.phoneNumberTwo.number", target = "contactInfo.phoneNumberTwo")
  @Mapping(source = "contactInfo.fullName.firstName", target = "contactInfo.firstName")
  @Mapping(source = "contactInfo.fullName.lastName", target = "contactInfo.lastName")
  @Mapping(source = "fmsOrderItems", target = "fmsOrderItemvalueObjectList")
  @Mapping(source = "cancelDetails", target = "cancellationDetails")
  public abstract FmsOrderValueObject convertFmsOrderToFmsOrderValueObject(FmsOrder fmsOrder);

  @Mapping(source = "itemId", target = "skuId")
  @Mapping(source = "consumerItemNumber", target = "cin")
  @Mapping(source = "itemPriceInfo.unitPrice.amount", target = "unitPrice")
  public abstract FmsOrderItemvalueObject convertFmsItemToValueObject(FmsOrderItem fmsOrderItem);

  @Mapping(source = "storeUnitPrice.amount", target = "storeUnitPrice")
  public abstract FmsPickedItemUpcVo convertFmsPickedItemUPCToFMSVO(FmsPickedItemUpc pickedItemUpc);

  @Mapping(source = "picker.pickerUserName", target = "pickerUserName")
  @Mapping(target = "orderedCin", source = "cin")
  public abstract FmsPickedItemValueObject convertToPickedItemvalueObject(
      FmsPickedItem fmsPickedItem);
}
