package com.walmart.fms.infrastructure.integration.mapper;

import com.walmart.fms.infrastructure.integration.gateway.store.dto.orderdownload.Customer;
import com.walmart.fms.infrastructure.integration.gateway.store.dto.orderdownload.PlaceFulfillmentOrderRequest;
import com.walmart.fms.order.aggregateroot.FmsOrder;
import org.apache.commons.lang3.StringUtils;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public abstract class OrderDownloadMapper {

  public static final OrderDownloadMapper INSTANCE = Mappers.getMapper(OrderDownloadMapper.class);

  @Mapping(target = "messageHeader.tranId", source = "fmsOrder.storeOrderId")
  @Mapping(
      target = "messageBody.customerOrderInfo.customerInfo.customerDetails.customer",
      source = "fmsOrder")
  public abstract PlaceFulfillmentOrderRequest toPlaceFulfillmentOrderRequest(FmsOrder fmsOrder);

  @Mapping(target = "id", expression = "java(fmsOrder.getVendorId())")
  @Mapping(target = "contact.email", source = "fmsOrder.contactInfo.email.address")
  @Mapping(target = "firstName", source = "fmsOrder.contactInfo.fullName.firstName")
  @Mapping(
      target = "lastName",
      expression =
          "java(getLastNameWithOrderId(fmsOrder.getContactInfo().getFullName().getLastName() ,fmsOrder.getMarketPlaceInfo().getVendorOrderId()))")
  public abstract Customer toPfoCustomer(FmsOrder fmsOrder);

  public String getLastNameWithOrderId(String lastName, String vendorOrderId) {

    int orderIdLength = StringUtils.length(vendorOrderId);

    if (orderIdLength > 5) {
      return lastName + StringUtils.substring(vendorOrderId, vendorOrderId.length() - 5);
    } else if (orderIdLength > 0) {
      return lastName + vendorOrderId;
    }
    return lastName;
  }
}
