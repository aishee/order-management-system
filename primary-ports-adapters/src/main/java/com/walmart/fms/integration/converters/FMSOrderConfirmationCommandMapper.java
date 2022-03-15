package com.walmart.fms.integration.converters;

import com.walmart.fms.commands.FmsOrderConfirmationCommand;
import com.walmart.fms.integration.xml.beans.orderconfirm.UpdateOrderFulfillmentBeginStatusRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper
public abstract class FMSOrderConfirmationCommandMapper {
  public static final FMSOrderConfirmationCommandMapper INSTANCE =
      Mappers.getMapper(FMSOrderConfirmationCommandMapper.class);

  @Mapping(
      target = "storeOrderId",
      expression =
          "java(String.valueOf(updateOrderFulfillmentBeginStatusRequest.getMessageBody().getCustomerOrder().get(0).getFulfillmentOrders().get(0).getOrderHeader().getOrderNumber()))")
  public abstract FmsOrderConfirmationCommand convertToOrderConfirmation(
      UpdateOrderFulfillmentBeginStatusRequest updateOrderFulfillmentBeginStatusRequest);
}
