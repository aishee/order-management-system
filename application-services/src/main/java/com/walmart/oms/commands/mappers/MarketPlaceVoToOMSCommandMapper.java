package com.walmart.oms.commands.mappers;

import com.walmart.common.domain.type.Tenant;
import com.walmart.common.domain.type.Vertical;
import com.walmart.marketplace.order.domain.valueobject.MarketPlaceOrderValueObject;
import com.walmart.oms.commands.CreateOmsOrderCommand;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper
public abstract class MarketPlaceVoToOMSCommandMapper {

  public static final MarketPlaceVoToOMSCommandMapper INSTANCE =
      Mappers.getMapper(MarketPlaceVoToOMSCommandMapper.class);

  @Mapping(source = "valueObject.contactInfo", target = "data.contactinfo")
  @Mapping(source = "valueObject.storeId", target = "data.orderInfo.storeId")
  @Mapping(source = "valueObject.orderDueTime", target = "data.orderInfo.deliveryDate")
  @Mapping(source = "valueObject.vendorId", target = "data.marketPlaceInfo.vendor")
  @Mapping(source = "valueObject.sourceOrderId", target = "data.orderInfo.sourceOrderId")
  @Mapping(source = "valueObject.vendorOrderId", target = "data.marketPlaceInfo.vendorOrderId")
  @Mapping(source = "valueObject.orderDueTime", target = "data.schedulingInfo.plannedDueTime")
  @Mapping(source = "osn", target = "data.schedulingInfo.scheduleNumber")
  @Mapping(source = "valueObject.marketPlaceOrderPaymentInfo", target = "data.priceInfo")
  @Mapping(source = "valueObject.items", target = "data.items")
  @Mapping(source = "tenant", target = "data.orderInfo.tenant")
  @Mapping(source = "vertical", target = "data.orderInfo.vertical")
  @Mapping(source = "valueObject.storeId", target = "data.orderInfo.pickupLocationId")
  public abstract CreateOmsOrderCommand convertToCommand(
      MarketPlaceOrderValueObject valueObject, Tenant tenant, Vertical vertical, String osn);

  @Mapping(source = "itemIdentifier.itemId", target = "cin")
  @Mapping(source = "itemPriceInfo.totalPrice", target = "vendorTotalPrice")
  @Mapping(source = "itemPriceInfo.unitPrice", target = "vendorUnitPrice")
  public abstract CreateOmsOrderCommand.OrderItemInfo convertToCommand(
      MarketPlaceOrderValueObject.Item item);

  @Mapping(source = "total.amount", target = "orderTotal")
  @Mapping(source = "bagFee.amount", target = "carrierBagCharge")
  public abstract CreateOmsOrderCommand.PriceInfo convertPriceInfo(
      MarketPlaceOrderValueObject.PaymentInfo paymentInfo);
}
