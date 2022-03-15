package com.walmart.fms.integration.converters;

import com.walmart.fms.commands.FmsDeliveredOrderCommand;
import com.walmart.fms.integration.xml.beans.uods.UpdateOrderDispensedStatusRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(
    componentModel = "spring",
    imports = {String.class})
public abstract class FMSOrderedDeliverdCommandMapper {
  public static final FMSOrderedDeliverdCommandMapper INSTANCE =
      Mappers.getMapper(FMSOrderedDeliverdCommandMapper.class);

  @Mapping(
      target = "data.storeOrderId",
      expression =
          "java(String.valueOf(updateOrderDispensedStatusRequest.getMessageBody().getCustomerOrder().get(0).getOrderHeader().getOrderNumber()))")
  public abstract FmsDeliveredOrderCommand convertToOrderDeliveryCommand(
      UpdateOrderDispensedStatusRequest updateOrderDispensedStatusRequest);
}
