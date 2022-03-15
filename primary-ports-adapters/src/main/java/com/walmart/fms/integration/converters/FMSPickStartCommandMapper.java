package com.walmart.fms.integration.converters;

import com.walmart.fms.commands.FmsPickStartedOrderCommand;
import com.walmart.fms.integration.xml.beans.orderpickbegin.UpdateOrderPickingBeginStatusRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper
public abstract class FMSPickStartCommandMapper {
  public static final FMSPickStartCommandMapper INSTANCE =
      Mappers.getMapper(FMSPickStartCommandMapper.class);

  @Mapping(
      target = "storeOrderId",
      expression =
          "java(String.valueOf(updateOrderPickingBeginStatusRequest.getMessageBody().getCustomerOrder().get(0).getOrderHeader().getOrderNumber()))")
  public abstract FmsPickStartedOrderCommand convertToPickStartedCommand(
      UpdateOrderPickingBeginStatusRequest updateOrderPickingBeginStatusRequest);
}
