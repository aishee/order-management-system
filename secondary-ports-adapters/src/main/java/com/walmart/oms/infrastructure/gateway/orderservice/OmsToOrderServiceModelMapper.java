package com.walmart.oms.infrastructure.gateway.orderservice;

import com.walmart.oms.order.aggregateroot.OmsOrder;
import com.walmart.oms.order.domain.entity.CustomerContactInfo;
import com.walmart.oms.order.domain.entity.SchedulingInfo;
import com.walmart.oms.order.gateway.orderservice.OrdersEvent;
import com.walmart.oms.order.valueobject.MarketPlaceInfo;
import com.walmart.services.oms.order.common.enums.OmsPaymentStatus;
import com.walmart.services.oms.order.common.model.OmsBuyerInfo;
import com.walmart.services.oms.order.common.model.OmsDeliveryReservationDetail;
import com.walmart.services.oms.order.common.model.OmsOrderInfo;
import com.walmart.services.oms.order.common.model.OmsThirdPartyDetails;
import java.sql.Timestamp;
import java.util.Date;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper
public abstract class OmsToOrderServiceModelMapper {

  public static final OmsToOrderServiceModelMapper INSTANCE =
      Mappers.getMapper(OmsToOrderServiceModelMapper.class);
  public static final String EVENT_NAME = "order"; // OMS Order Ingestion
  public static final String EVENT_SOURCE = "UK_OMS"; // UK_GR_OMS
  public static final String VERTICAL_ID = "2";
  public static final String TENANT_ID = "5";
  public static final String GBP = "GBP";
  public static final String EUROPE_LONDON = "Europe/London";
  public static final String DELIVERY = "DELIVERY";
  public static final String THIRD_PT_DELIVERY = "3P_DELIVERY";
  private static final String STATUS_BUNDLED_ORDER = "BUNDLE_ORDER";
  private static final String STATUS_NON_BUNDLED_ORDER = "NON_BUNDLE_ORDER";

  public OrdersEvent<com.walmart.services.oms.order.common.model.OmsOrder> generateOrderEvent(
      OmsOrder omsOrder) {
    OrdersEvent<com.walmart.services.oms.order.common.model.OmsOrder> event = new OrdersEvent<>();
    event.setEventPayload(mapEventPayload(omsOrder));
    event.setEventId(omsOrder.getStoreOrderId());
    event.setEventName(EVENT_NAME);
    event.setEventSource(EVENT_SOURCE);
    event.setEventTime(new Timestamp(System.currentTimeMillis()));
    event.setSrcCreatedDt(getSrcCreatedDt(omsOrder.getCreatedDate()));
    event.setSrcModifiedDt(getSrcCreatedDt(omsOrder.getModifiedDate()));
    event.setVerticalId(VERTICAL_ID);
    event.setTenantId(TENANT_ID);
    return event;
  }

  private Timestamp getSrcCreatedDt(Date date) {
    return date != null ? new Timestamp(date.getTime()) : new Timestamp(System.currentTimeMillis());
  }

  public com.walmart.services.oms.order.common.model.OmsOrder mapEventPayload(OmsOrder omsOrder) {
    com.walmart.services.oms.order.common.model.OmsOrder order = mapOrderAttributes(omsOrder);
    order.setOrderCustomAttributes(
        OsOrderCustomAttributeMapper.INSTANCE.createCustomAttributeMap(omsOrder));
    order.setCreatets(getSrcCreatedDt(omsOrder.getCreatedDate()));
    order.setSubmittedDate(getSrcCreatedDt(omsOrder.getCreatedDate()));
    OmsOrderLineToOrderServiceMapper.INSTANCE.mapOrderLines(omsOrder, order);
    order.setOrderBundleItem(OmsOrderBundleItemMapper.INSTANCE.mapToBundleItemInfo(omsOrder));
    order.setBundleStatus(getBundleStatus(omsOrder));
    return order;
  }

  private String getBundleStatus(OmsOrder omsOrder) {
    return omsOrder.hasBundles() ? STATUS_BUNDLED_ORDER : STATUS_NON_BUNDLED_ORDER;
  }

  @Mapping(source = "storeOrderId", target = "orderNo")
  @Mapping(source = "deliveryDate", target = "orderDate")
  @Mapping(source = "modifiedDate", target = "lastModified")
  @Mapping(source = "marketPlaceInfo.vendor.vendorId", target = "affiliateId")
  @Mapping(source = "orderState", target = "status")
  @Mapping(expression = "java(TENANT_ID)", target = "tenantId")
  @Mapping(expression = "java(VERTICAL_ID)", target = "verticalId")
  @Mapping(expression = "java(EVENT_SOURCE)", target = "orderSource")
  @Mapping(
      expression = "java(com.walmart.services.oms.order.common.enums.OmsOrderType.DOMESTIC)",
      target = "orderType")
  @Mapping(expression = "java(mapPaymentStatus(omsOrder))", target = "paymentStatus")
  @Mapping(
      expression =
          "java(new com.walmart.services.common.model.money.MoneyType(java.math.BigDecimal.valueOf(omsOrder.getOrderTotal()), com.walmart.services.common.model.money.CurrencyUnitEnum.GBP))",
      target = "orderSummary.totalAmount")
  @Mapping(
      expression =
          "java(new com.walmart.services.common.model.money.MoneyType(java.math.BigDecimal.valueOf(omsOrder.getPosTotal()), com.walmart.services.common.model.money.CurrencyUnitEnum.GBP))",
      target = "orderSummary.posTotal")
  @Mapping(
      expression =
          "java(new com.walmart.services.common.model.money.MoneyType(java.math.BigDecimal.valueOf(omsOrder.getCarrierBagCharge()), com.walmart.services.common.model.money.CurrencyUnitEnum.GBP))",
      target = "orderSummary.carrierBagCharge")
  @Mapping(
      expression =
          "java(new com.walmart.services.common.model.money.MoneyType(java.math.BigDecimal.valueOf(omsOrder.getDeliveryCharge()), com.walmart.services.common.model.money.CurrencyUnitEnum.GBP))",
      target = "orderSummary.deliveryCharge")
  @Mapping(
      expression =
          "java(new com.walmart.services.common.model.money.MoneyType(java.math.BigDecimal.ZERO, com.walmart.services.common.model.money.CurrencyUnitEnum.GBP))",
      target = "orderTotals.promotionTotal")
  @Mapping(
      expression =
          "java(new com.walmart.services.common.model.money.MoneyType(java.math.BigDecimal.valueOf(omsOrder.getOrderSubTotal()), com.walmart.services.common.model.money.CurrencyUnitEnum.GBP))",
      target = "orderTotals.orderSubTotal")
  @Mapping(
      expression =
          "java(java.util.Collections.singletonList(mapDeliveryReservationDetail(omsOrder.getSchedulingInfo(),omsOrder.getStoreId(),omsOrder.getMarketPlaceInfo())))",
      target = "deliveryReservationDetails")
  @Mapping(expression = "java(mapBuyerInfo(omsOrder.getContactInfo()))", target = "buyerInfo")
  @Mapping(expression = "java(mapOmsOrderInfo(omsOrder))", target = "orderInfo")
  @Mapping(expression = "java(omsOrder.getCancelledBySourceName().orElse(null))", target = "cancelledBy")
  @Mapping(expression = "java(omsOrder.getCancelledReasonCode().orElse(null))", target = "cancelledReasonCode")
  public abstract com.walmart.services.oms.order.common.model.OmsOrder mapOrderAttributes(
      OmsOrder omsOrder);

  public OmsPaymentStatus mapPaymentStatus(OmsOrder omsOrder) {
    if (omsOrder.getMarketPlaceInfo() != null
        && omsOrder.getMarketPlaceInfo().getVendorOrderId() != null) {
      return OmsPaymentStatus.AUTHORIZED;
    }
    return null;
  }

  @Mapping(expression = "java(EUROPE_LONDON)", target = "timezone")
  @Mapping(
      expression =
          "java(schedulingInfo!=null?String.valueOf(schedulingInfo.getDoorStepTime()):null)",
      target = "doorStepTime")
  @Mapping(source = "schedulingInfo.createdDate", target = "bookDate")
  @Mapping(source = "schedulingInfo.plannedDueTime", target = "plannedDeliveryTime")
  @Mapping(source = "schedulingInfo.scheduleNumber", target = "scheduleNumber")
  @Mapping(source = "storeId", target = "fulfillmentLocationId")
  @Mapping(source = "schedulingInfo.plannedDueTime", target = "fulfillmentDate")
  @Mapping(source = "schedulingInfo.vanId", target = "vanId")
  @Mapping(expression = "java(deriveDispenseType(mktInfo))", target = "dispenseType")
  @Mapping(source = "schedulingInfo.loadNumber", target = "loadNumber")
  public abstract OmsDeliveryReservationDetail mapDeliveryReservationDetail(
      SchedulingInfo schedulingInfo, String storeId, MarketPlaceInfo mktInfo);

  public String deriveDispenseType(MarketPlaceInfo mktInfo) {
    if (mktInfo == null) {
      return DELIVERY;
    } else {
      return THIRD_PT_DELIVERY;
    }
  }

  @Mapping(target = "id", ignore = true)
  @Mapping(expression = "java(Boolean.FALSE)", target = "isGuest")
  @Mapping(
      source = "customerContactInfo.fullName.firstName",
      target = "primaryContact.name.firstName")
  @Mapping(
      source = "customerContactInfo.fullName.lastName",
      target = "primaryContact.name.lastName")
  @Mapping(
      source = "customerContactInfo.fullName.title",
      target = "primaryContact.name.titleOfRespect")
  @Mapping(
      source = "customerContactInfo.email.address",
      target = "primaryContact.email.emailAddress")
  public abstract OmsBuyerInfo mapBuyerInfo(CustomerContactInfo customerContactInfo);

  @Mapping(expression = "java(true)", target = "third_party_delivery")
  @Mapping(expression = "java(mapOmsThirdPartyDetails(omsOrder))", target = "thirdPartyDetails")
  public abstract OmsOrderInfo mapOmsOrderInfo(OmsOrder omsOrder);

  @Mapping(expression = "java(omsOrder.getVendorId())", target = "third_party_delivery_provider")
  public abstract OmsThirdPartyDetails mapOmsThirdPartyDetails(OmsOrder omsOrder);
}
