package com.walmart.oms.infrastructure.gateway.orderservice;

import com.walmart.oms.order.aggregateroot.OmsOrder;
import com.walmart.oms.order.domain.entity.OmsOrderItem;
import com.walmart.oms.order.domain.entity.PickedItem;
import com.walmart.oms.order.valueobject.ItemPriceInfo;
import com.walmart.oms.order.valueobject.Money;
import com.walmart.services.common.enums.ShipMethod;
import com.walmart.services.common.model.measurement.UnitOfMeasureEnum;
import com.walmart.services.oms.order.common.enums.OmsFulfillmentOption;
import com.walmart.services.oms.order.common.model.OmsAdditionalPriceInfo;
import com.walmart.services.oms.order.common.model.OmsCharge;
import com.walmart.services.oms.order.common.model.OmsLineQuantitySummary;
import com.walmart.services.oms.order.common.model.OmsOrderLine;
import com.walmart.services.oms.order.common.model.OmsOrderLineQuantityInfo;
import com.walmart.services.oms.order.common.model.OmsOrderProduct;
import com.walmart.services.oms.order.common.model.OmsUnitPrice;
import com.walmart.services.oms.order.common.model.PickedLine;
import com.walmart.services.oms.order.common.model.SubstitutedItem;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.mapstruct.BeforeMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;

@Slf4j
@Mapper
public abstract class OmsOrderLineToOrderServiceMapper {

  public static final OmsOrderLineToOrderServiceMapper INSTANCE =
      Mappers.getMapper(OmsOrderLineToOrderServiceMapper.class);
  public static final String GROCERY_ITEM = "GROCERY ITEM";
  public static final String ITEM_PRICE = "Item Price";
  public static final String ORDERED = "Ordered";
  public static final String E = "E";
  public static final String K = "K";
  public static final String SCHEDULED_PICKUP = "SCHEDULED_PICKUP";

  public void mapOrderLines(
      OmsOrder omsOrder, com.walmart.services.oms.order.common.model.OmsOrder order) {
    List<OmsOrderLine> orderLines = new ArrayList<>();
    int primeNo = 1;
    List<OmsOrderItem> orderItemList = omsOrder.getOrderItemList();
    for (OmsOrderItem omsOrderItem : orderItemList) {
      OmsOrderLine orderLine = mapOrderLine(omsOrderItem, omsOrder.getOrderState());
      orderLine.setPrimeLineNo(primeNo++);
      orderLine.setFulfillmentType(OmsFulfillmentOption.fromDescription(SCHEDULED_PICKUP));
      orderLine.setShippingMethod(ShipMethod.STORE_DELIVERY);
      orderLine.setIsPreOrder(Boolean.FALSE);
      orderLine.setOrderLineCustomAttributes(
          OsOrderLineCustomAttributeMapper.INSTANCE.createCustomAttributeMap(omsOrderItem));
      orderLines.add(orderLine);
    }
    order.setOrderLines(orderLines);
  }

  @Mapping(expression = "java(mapOrderProduct(omsOrderItem))", target = "orderProduct")
  @Mapping(
      expression =
          "java(new com.walmart.services.common.model.measurement.Measurement(getUOM(omsOrderItem.getUom()), java.math.BigDecimal.valueOf(omsOrderItem.getQuantity())))",
      target = "orderedQty")
  @Mapping(
      expression =
          "java(new com.walmart.services.common.model.money.MoneyType(omsOrderItem.getOrderedItemUnitPriceAmount(), com.walmart.services.common.model.money.CurrencyUnitEnum.GBP))",
      target = "unitPrice")
  @Mapping(
      expression = "java(java.util.Collections.singletonList(mapChargesData(omsOrderItem)))",
      target = "charges")
  @Mapping(
      expression =
          "java(java.util.Collections.singletonList(mapOrderLineQuantityInfo(omsOrderItem,orderStatus)))",
      target = "orderLineQuantityInfo")
  @Mapping(
      expression =
          "java(java.util.Collections.singletonList(mapLineQuantitySummaries(omsOrderItem)))",
      target = "lineQuantitySummaries")
  @Mapping(
      expression = "java(omsOrderItem.isSubstitutionAllowed())",
      target = "customerPreferences.isSubstitutable")
  @Mapping(expression = "java(mapPickedItem(omsOrderItem.getPickedItem()))", target = "pickedItem")
  @Mapping(
      expression = "java(mapAdditionalPriceInfo(omsOrderItem.getItemPriceInfo()))",
      target = "additionalPriceInfo")
  public abstract OmsOrderLine mapOrderLine(OmsOrderItem omsOrderItem, String orderStatus);

  @Mapping(
      expression = "java(mapOmsUnitPrice(itemPriceInfo.getVendorUnitPrice()))",
      target = "vendorUnitPrice")
  @Mapping(expression = "java(mapOmsUnitPrice(itemPriceInfo.getUnitPrice()))", target = "unitPrice")
  public abstract OmsAdditionalPriceInfo mapAdditionalPriceInfo(ItemPriceInfo itemPriceInfo);

  @Mapping(expression = "java(unitPrice.getAmount())", target = "currencyAmount")
  @Mapping(
      expression = "java(com.walmart.services.common.model.money.CurrencyUnitEnum.GBP)",
      target = "currencyUnit")
  @Mapping(
      expression = "java(com.walmart.services.common.model.measurement.UnitOfMeasureEnum.EACH)",
      target = "pricingUOM")
  public abstract OmsUnitPrice mapOmsUnitPrice(Money unitPrice);

  @Mapping(expression = "java(GROCERY_ITEM)", target = "productName")
  @Mapping(source = "omsOrderItem.cin", target = "offerLogistics.cin")
  @Mapping(
      source = "omsOrderItem.catalogItem.smallImageURL",
      target = "offerLogistics.itemImageURL")
  public abstract OmsOrderProduct mapOrderProduct(OmsOrderItem omsOrderItem);

  @Mapping(
      expression = "java(com.walmart.services.oms.order.common.enums.OmsChargeCategory.PRODUCT)",
      target = "chargeCategory")
  @Mapping(expression = "java(ITEM_PRICE)", target = "chargeName")
  @Mapping(
      expression =
          "java(new com.walmart.services.common.model.measurement.Measurement(getUOM(omsOrderItem.getUom()), java.math.BigDecimal.valueOf(omsOrderItem.getQuantity())))",
      target = "chargeQuantity")
  @Mapping(
      expression =
          "java(new com.walmart.services.common.model.money.MoneyType(omsOrderItem.getOrderedItemUnitPriceAmount(), com.walmart.services.common.model.money.CurrencyUnitEnum.GBP))",
      target = "chargePerUnit")
  public abstract OmsCharge mapChargesData(OmsOrderItem omsOrderItem);

  @Mapping(
      expression =
          "java(com.walmart.services.oms.order.common.enums.OmsOrderLineStatus.valueOf(OrderLineQuantityInfoStatus.getStatusDescription(orderStatus)))",
      target = "status")
  @Mapping(
      expression = "java(OrderLineQuantityInfoStatus.getStatusCode(orderStatus))",
      target = "statusCode")
  @Mapping(
      expression = "java(OrderLineQuantityInfoStatus.getStatusDescription(orderStatus))",
      target = "statusDescription")
  @Mapping(expression = "java(new java.util.Date())", target = "statusChangeDate")
  @Mapping(
      expression =
          "java(new com.walmart.services.common.model.measurement.Measurement(getUOM(omsOrderItem.getUom()), java.math.BigDecimal.valueOf(omsOrderItem.getQuantity())))",
      target = "statusQuantity")
  public abstract OmsOrderLineQuantityInfo mapOrderLineQuantityInfo(
      OmsOrderItem omsOrderItem, String orderStatus);

  @Mapping(expression = "java(ORDERED)", target = "type")
  @Mapping(
      expression =
          "java(new com.walmart.services.common.model.measurement.Measurement(getUOM(omsOrderItem.getUom()), java.math.BigDecimal.valueOf(omsOrderItem.getQuantity())))",
      target = "quantity")
  public abstract OmsLineQuantitySummary mapLineQuantitySummaries(OmsOrderItem omsOrderItem);

  @BeforeMapping
  public void beforePickedItemMapping(@MappingTarget PickedLine pickedLine, PickedItem pickedItem) {
    if (pickedItem.getPickedItemPriceInfo() != null) {
      if (pickedItem.getPickedItemPriceInfo().getUnitPrice() != null) {
        pickedLine.setUnitPrice(
            pickedItem.getPickedItemPriceInfo().getUnitPrice().getAmount().doubleValue());
      }
      // weigted unit price and unit price are same.
      if (pickedItem.getPickedItemPriceInfo().getAdjustedPriceExVat() != null) {
        pickedLine.setAdjPriceExVAT(
            pickedItem.getPickedItemPriceInfo().getAdjustedPriceExVat().getAmount().doubleValue());
      }
      if (pickedItem.getPickedItemPriceInfo().getAdjustedPrice() != null) {
        pickedLine.setAdjustedPrice(
            pickedItem.getPickedItemPriceInfo().getAdjustedPrice().getAmount().doubleValue());
      }
      if (pickedItem.getPickedItemPriceInfo().getWebAdjustedPrice() != null) {
        pickedLine.setWebAdjustedPrice(
            pickedItem.getPickedItemPriceInfo().getWebAdjustedPrice().getAmount().doubleValue());
      }
      if (pickedItem.getPickedItemPriceInfo().getVatAmount() != null) {
        pickedLine.setVatamount(
            pickedItem.getPickedItemPriceInfo().getVatAmount().getAmount().doubleValue());
      }
    }
  }

  @Mapping(source = "pickedItem.departmentID", target = "departmentID")
  @Mapping(source = "pickedItem.quantity", target = "quantity")
  @Mapping(source = "pickedItem.weight", target = "weight")
  @Mapping(source = "pickedItem.orderedCin", target = "consumerItemNumber")
  @Mapping(source = "pickedItem.pickedItemDescription", target = "description")
  @Mapping(source = "pickedItem.picker.pickerUserName", target = "pickedBy")
  // UKGRFF-393
  @Mapping(source = "pickedItem.pickedItemUpcList", target = "upcList")
  @Mapping(source = "pickedItem.omsOrderItem.skuId", target = "id")
  @Mapping(source = "pickedItem.pickedItemPriceInfo.unitPrice.amount", target = "unitPrice")
  @Mapping(
      expression = "java(pickedItem.getTotalSubstitutedItemsQuantity().orElse(0L))",
      target = "substitutedQuantity")
  @Mapping(
      expression =
          "java(java.util.Collections.singletonList(mapSubstitutedItemList(pickedItem.getSubstitutedItem().orElse(null), pickedItem.getOrderedCin(), pickedItem.getPickedByUser().orElse(null))))",
      target = "substitutedItemList")
  public abstract PickedLine mapPickedItem(PickedItem pickedItem);

  public UnitOfMeasureEnum getUOM(String uom) {
    UnitOfMeasureEnum result = null;
    try {
      if (E.equalsIgnoreCase(uom)) {
        result = UnitOfMeasureEnum.EACH;
      } else if (K.equalsIgnoreCase(uom)) {
        result = UnitOfMeasureEnum.KILOGRAM;
      }
    } catch (Exception e) {
      log.error("Error in getUOM :" + e);
    }
    return result;
  }

  @Mapping(source = "substitutedItem.id", target = "id")
  @Mapping(source = "substitutedItem.weight", target = "weight")
  @Mapping(source = "substitutedItem.quantity", target = "quantity")
  @Mapping(source = "substitutedItem.description", target = "description")
  @Mapping(source = "substitutedItem.consumerItemNumber", target = "substConsumerItemNumber")
  @Mapping(source = "pickerUserName", target = "pickedBy")
  @Mapping(source = "orderedCin", target = "consumerItemNumber")
  @Mapping(
      expression = "java(substitutedItem.getSubstitutedItemUpcDetail().orElse(null))",
      target = "upc")
  @Mapping(source = "substitutedItem.department", target = "department")
  @Mapping(source = "substitutedItem.walmartItemNumber", target = "wmItemNum")
  @Mapping(
      expression = "java(substitutedItem.getSubstitutedItemUnitPrice().doubleValue())",
      target = "unitPrice")
  @Mapping(
      expression = "java(substitutedItem.getSubstitutedItemTotalPrice().doubleValue())",
      target = "priceCharged")
  @Mapping(
      expression =
          "java(substitutedItem.getSubstitutedItemAdjustedPriceExVat().orElse(BigDecimal.ZERO).doubleValue())",
      target = "adjPriceExVAT")
  @Mapping(
      expression =
          "java(substitutedItem.getSubstitutedItemAdjustedPrice().orElse(BigDecimal.ZERO).doubleValue())",
      target = "adjustedPrice")
  @Mapping(
      expression =
          "java(substitutedItem.getSubstitutedItemWebAdjustedPrice().orElse(BigDecimal.ZERO).doubleValue())",
      target = "webAdjustedPrice")
  @Mapping(
      expression =
          "java(substitutedItem.getSubstitutedItemVatAmount().orElse(BigDecimal.ZERO).doubleValue())",
      target = "vatamount")
  @Mapping(
      expression =
          "java(substitutedItem.getSubstitutedItemVendorUnitPrice().orElse(BigDecimal.ZERO).doubleValue())",
      target = "storeUnitPrice")
  public abstract SubstitutedItem mapSubstitutedItemList(
      com.walmart.oms.order.domain.entity.SubstitutedItem substitutedItem,
      String orderedCin,
      String pickerUserName);
}
