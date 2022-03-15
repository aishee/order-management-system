package com.walmart.fms.integration.converters;

import com.walmart.common.domain.type.CancellationReason;
import com.walmart.fms.commands.FmsCancelOrderCommand;
import com.walmart.fms.integration.xml.beans.cfo.CancelFulfillmentOrderRequest;
import com.walmart.fms.integration.xml.beans.cfo.Reason;
import com.walmart.fms.integration.xml.beans.cfo.Status;
import java.util.Optional;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper
public abstract class FMSStoreCancelOrderCommandMapper {
  public static final FMSStoreCancelOrderCommandMapper INSTANCE =
      Mappers.getMapper(FMSStoreCancelOrderCommandMapper.class);

  @Mapping(
      target = "data.storeOrderId",
      expression =
          "java(String.valueOf(cancelFulfillmentOrderRequest.getMessageBody().getCustomerOrder().get(0).getOrderHeader().getOrderNumber()))")
  @Mapping(
      target = "data.cancelledReasonCode",
      expression =
          "java(getCancelledReasonCode(cancelFulfillmentOrderRequest.getMessageBody().getCustomerOrder().get(0).getFulfillmentOrders().get(0).getStatus()))")
  @Mapping(
      target = "data.cancelledReasonDescription",
      expression =
          "java(getCancelledReasonDescription(cancelFulfillmentOrderRequest.getMessageBody().getCustomerOrder().get(0).getFulfillmentOrders().get(0).getStatus()))")
  @Mapping(target = "data.cancellationSource", constant = "STORE")
  public abstract FmsCancelOrderCommand convertToStoreCancelOrderCommand(
      CancelFulfillmentOrderRequest cancelFulfillmentOrderRequest);

  String getCancelledReasonCode(Status status) {
    return Optional.ofNullable(status.getStatusChangeReason()).map(Reason::getCode)
        .orElse(CancellationReason.DEFAULT.getCode());
  }

  String getCancelledReasonDescription(Status status) {
    return Optional.ofNullable(status.getStatusChangeReason()).map(Reason::getDescription)
        .orElse(CancellationReason.DEFAULT.getDescription());
  }
}
