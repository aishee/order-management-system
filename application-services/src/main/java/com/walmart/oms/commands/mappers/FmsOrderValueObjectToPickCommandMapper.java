package com.walmart.oms.commands.mappers;

import com.walmart.oms.commands.PickCompleteCommand;
import com.walmart.oms.order.valueobject.events.FmsOrderItemvalueObject;
import com.walmart.oms.order.valueobject.events.FmsOrderValueObject;
import com.walmart.oms.order.valueobject.events.FmsPickedItemUpcVo;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper
public abstract class FmsOrderValueObjectToPickCommandMapper {

  public static final FmsOrderValueObjectToPickCommandMapper INSTANCE =
      Mappers.getMapper(FmsOrderValueObjectToPickCommandMapper.class);

  @Mapping(source = "valueObject.tenant", target = "data.orderInfo.tenant")
  @Mapping(source = "valueObject.vertical", target = "data.orderInfo.vertical")
  @Mapping(source = "valueObject.sourceOrderId", target = "data.orderInfo.sourceOrderId")
  @Mapping(source = "valueObject.storeId", target = "data.orderInfo.storeId")
  @Mapping(source = "valueObject.pickupLocationId", target = "data.orderInfo.pickupLocationId")
  @Mapping(source = "valueObject.deliveryDate", target = "data.orderInfo.deliveryDate")
  @Mapping(source = "valueObject.authStatus", target = "data.orderInfo.authStatus")
  @Mapping(source = "valueObject.fmsOrderItemvalueObjectList", target = "data.pickedItems")
  public abstract PickCompleteCommand convertToCommand(FmsOrderValueObject valueObject);

  @Mapping(source = "pickedItem.orderedCin", target = "cin")
  @Mapping(source = "pickedItem.departmentID", target = "departmentId")
  @Mapping(source = "pickedItem.pickerUserName", target = "pickedBy")
  @Mapping(source = "pickedItem.pickedItemDescription", target = "pickedItemDescription")
  @Mapping(source = "pickedItem.pickedItemUpcList", target = "pickedItemUpcs")
  @Mapping(source = "pickedItem.substitutedItems", target = "substitutedItems")
  public abstract PickCompleteCommand.PickedItemInfo convertToPickedItemInfo(
      FmsOrderItemvalueObject itemValueObject);

  @Mapping(source = "quantity", target = "pickedQuantity")
  @Mapping(source = "storeUnitPrice", target = "unitPrice")
  public abstract PickCompleteCommand.PickedItemUpc convertToPickedItemUpc(
      FmsPickedItemUpcVo fmsPickedItemUpcVo);

}
