package com.walmart.fms.commands.converters;

import com.walmart.fms.commands.CreateFmsOrderCommand;
import com.walmart.oms.order.valueobject.EmailAddress;
import com.walmart.oms.order.valueobject.MobilePhone;
import com.walmart.oms.order.valueobject.events.FmsOrderItemUpcValueObject;
import com.walmart.oms.order.valueobject.events.FmsOrderItemvalueObject;
import com.walmart.oms.order.valueobject.events.FmsOrderValueObject;
import com.walmart.oms.order.valueobject.events.FmsSchedulingInfoValueObject;
import java.util.Optional;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper
public abstract class FMSValueObjectToCreateCmdConverter {

  public static final FMSValueObjectToCreateCmdConverter INSTANCE =
      Mappers.getMapper(FMSValueObjectToCreateCmdConverter.class);

  @Mapping(source = "sourceOrderId", target = "data.orderInfo.sourceOrderId")
  @Mapping(source = "vertical", target = "data.orderInfo.vertical")
  @Mapping(source = "pickupLocationId", target = "data.orderInfo.pickupLocationId")
  @Mapping(source = "storeId", target = "data.orderInfo.storeId")
  @Mapping(source = "deliveryDate", target = "data.orderInfo.deliveryDate")
  @Mapping(source = "tenant", target = "data.orderInfo.tenant")
  @Mapping(source = "marketPlaceInfo", target = "data.marketPlaceInfo")
  @Mapping(source = "schedulingInfo", target = "data.schedulingInfo")
  @Mapping(source = "contactInfo", target = "data.contactinfo")
  @Mapping(source = "fmsOrderItemvalueObjectList", target = "data.items")
  @Mapping(source = "priceInfo", target = "data.priceInfo")
  @Mapping(source = "addressInfo", target = "data.addressInfo")
  @Mapping(source = "storeOrderId", target = "data.orderInfo.storeOrderId")
  public abstract CreateFmsOrderCommand convertVoToCommand(FmsOrderValueObject fmsOrderValueObject);

  @Mapping(source = "orderTotal", target = "webOrderTotal")
  @Mapping(source = "carrierBagCharge", target = "carrierBagCharge")
  public abstract CreateFmsOrderCommand.PriceInfo convertPriceInfo(
      FmsOrderValueObject.OrderPriceInfo orderPriceInfo);

  @Mapping(source = "plannedDueTime", target = "slotStartTime")
  @Mapping(source = "plannedDueTime", target = "slotEndTime")
  @Mapping(source = "plannedDueTime", target = "orderDueTime")
  public abstract CreateFmsOrderCommand.SchedulingInfo convertSchedulingInfo(
      FmsSchedulingInfoValueObject fmsSchedulingInfoValueObject);

  @Mapping(source = "cin", target = "consumerItemNumber")
  @Mapping(source = "uom", target = "unitOfMeasurement")
  @Mapping(source = "imageUrl", target = "imageURL")
  @Mapping(source = "itemDescription", target = "pickerItemDescription")
  @Mapping(source = "upcs", target = "upcs")
  public abstract CreateFmsOrderCommand.FmsItemInfo convertItemsToCommand(
      FmsOrderItemvalueObject fmsOrderItemvalueObject);

  public String convertItemsUpcToCommandUpc(FmsOrderItemUpcValueObject fmsOrderItemUpcValueObject) {
    return Optional.ofNullable(fmsOrderItemUpcValueObject)
        .map(FmsOrderItemUpcValueObject::getUpc)
        .orElse(null);
  }

  protected String convertEmail(EmailAddress email) {
    return Optional.ofNullable(email).map(EmailAddress::getAddress).orElse(null);
  }

  protected String convertMobileNumber(MobilePhone mobilePhone) {
    return Optional.ofNullable(mobilePhone).map(MobilePhone::getNumber).orElse(null);
  }
}
