package com.walmart.oms.infrastructure.gateway.orderservice

import com.walmart.oms.order.aggregateroot.OmsOrder
import com.walmart.oms.order.domain.entity.OmsOrderItem
import com.walmart.oms.order.gateway.orderservice.OrdersEvent
import com.walmart.services.common.enums.ShipMethod
import com.walmart.services.oms.order.common.enums.OmsFulfillmentOption
import com.walmart.services.oms.order.common.enums.OmsOrderType
import com.walmart.services.oms.order.common.enums.OmsPaymentStatus
import com.walmart.services.oms.order.common.model.OmsBuyerInfo
import com.walmart.services.oms.order.common.model.OmsDeliveryReservationDetail
import com.walmart.services.oms.order.common.model.OmsOrderLine
import com.walmart.services.oms.order.common.model.OmsOrderSummary
import com.walmart.services.oms.order.common.model.OmsOrderTotals
import io.strati.libs.google.gson.JsonArray;
import io.strati.libs.google.gson.JsonObject;

class OrderServiceAssertions {
    public static final String ISO_DATE_TIME_REGEX = "([0-9]{4})-([0-9]{2})-([0-9]{2})T([0-9]{2}):([0-9]{2}):[0-9]{2}\\.[0-9]{3}[+|-][0-9]{4}"
    public static final String EMAIL_REGEX = "\\w+([-+.']\\w+)*@\\w+([-.]\\w+)*\\.\\w+([-.]\\w+)*";
    public static final String SCHEDULED__PICKUP = "SCHEDULED_PICKUP"

    static void assertOrderCustomAttributesJson(JsonObject customAttributes) {
    }

    static void assertOrderSummaryJson(JsonObject orderSummary) {
        assert orderSummary.get("totalAmount").getAsJsonObject() != null
        assert orderSummary.get("totalAmount").getAsJsonObject().get("currencyAmount").asNumber != null
        assert orderSummary.get("totalAmount").getAsJsonObject().get("currencyUnit").asString
    }

    static void assertDeliveryReservationDetailsJson(JsonArray deliveryReservationDetail) {
        assert deliveryReservationDetail.get(0).getAsJsonObject() != null
        assert deliveryReservationDetail.get(0).getAsJsonObject().get("plannedDeliveryTime").asString.matches(ISO_DATE_TIME_REGEX)
        assert deliveryReservationDetail.get(0).getAsJsonObject().get("fulfillmentDate").asString.matches(ISO_DATE_TIME_REGEX)
    }

    static void assertOrderTotalsJson(JsonObject orderTotals) {
        assert orderTotals.get("promotionTotal").getAsJsonObject() != null
        assert orderTotals.get("promotionTotal").getAsJsonObject().get("currencyAmount").asNumber != null
        assert orderTotals.get("promotionTotal").getAsJsonObject().get("currencyUnit").asString
    }

    static void assertBuyerInfoJson(JsonObject buyerInfo) {
        assert buyerInfo.get("primaryContact").getAsJsonObject() != null
        assert buyerInfo.get("primaryContact").getAsJsonObject().get("email").getAsJsonObject() != null
        assert buyerInfo.get("primaryContact").getAsJsonObject().get("email").getAsJsonObject().get("emailAddress").asString.matches(EMAIL_REGEX)
    }

    static void assertOsOrderJson(JsonObject order) {
        assert order.get("orderDate").asString.matches(ISO_DATE_TIME_REGEX)
        assert order.get("orderCustomAttributes") != null
    }


    static void assertOrderEventJson(JsonObject eventJsonObject) {
        assert eventJsonObject != null
        assert eventJsonObject.get("event_payload").asJsonObject != null
        if (eventJsonObject.get("src_created_dt") != null && eventJsonObject.get("src_modified_dt") != null) {
            assert eventJsonObject.get("src_created_dt").asString.matches(ISO_DATE_TIME_REGEX)
            assert eventJsonObject.get("src_modified_dt").asString.matches(ISO_DATE_TIME_REGEX)
        }
        assert eventJsonObject.get("event_time").asString.matches(ISO_DATE_TIME_REGEX)
    }

    static void assertOrderEventObject(OrdersEvent event, OmsOrder omsOrder) {
        assert event.eventId == omsOrder.getStoreOrderId()
        assert event.verticalId == OmsToOrderServiceModelMapper.VERTICAL_ID
        assert event.tenantId == OmsToOrderServiceModelMapper.TENANT_ID
        assert event.eventSource == OmsToOrderServiceModelMapper.EVENT_SOURCE
        assert event.eventName == OmsToOrderServiceModelMapper.EVENT_NAME
        assert event.srcCreatedDt != null
        assert event.srcModifiedDt != null
    }

    static void assertOsOrderObject(com.walmart.services.oms.order.common.model.OmsOrder osOrder, OmsOrder omsOrder) {
        assert osOrder.orderSource == OmsToOrderServiceModelMapper.EVENT_SOURCE
        assert osOrder.verticalId == OmsToOrderServiceModelMapper.VERTICAL_ID
        assert osOrder.tenantId == OmsToOrderServiceModelMapper.TENANT_ID
        assert osOrder.orderType == OmsOrderType.DOMESTIC
        assert osOrder.orderNo == omsOrder.storeOrderId
        assert osOrder.orderDate == omsOrder.deliveryDate
        assert osOrder.createts == omsOrder.createdDate
        if (isMarketPlaceOrder(omsOrder)) {
            assert osOrder.paymentStatus == OmsPaymentStatus.AUTHORIZED
            assert osOrder.affiliateId == omsOrder.marketPlaceInfo.vendor.vendorId
        } else {
            //TODO add the single profile id check here for GHS orders.
        }
    }

    private static boolean isMarketPlaceOrder(OmsOrder omsOrder) {
        omsOrder.marketPlaceInfo.vendor != null && omsOrder.marketPlaceInfo.vendorOrderId != null
    }

    static void assertOrderCustomAttributeMap(Map<String, String> map, OmsOrder omsOrder) {
        assert map."mappingVersion" == OsOrderCustomAttributeMapper.MAPPING_VERSION
        assert map."status" == omsOrder.orderState
        assert map."storeId" == omsOrder.storeId
        assert map."vendorId" == omsOrder.marketPlaceInfo.vendor.vendorId
        assert map."vendorOrderId" == omsOrder.marketPlaceInfo.vendorOrderId
    }

    static void assertOrderSummary(OmsOrderSummary orderSummary, OmsOrder omsOrder) {
        assert orderSummary.totalAmount != null
        assert orderSummary.totalAmount.currencyAmount == omsOrder.priceInfo.orderTotal
        assert orderSummary.totalAmount.currencyUnit.toString() == "GBP"
    }

    static void assertDeliveryReservationDetails(List<OmsDeliveryReservationDetail> DeliveryReservationDetails, OmsOrder omsOrder) {
        assert !DeliveryReservationDetails.isEmpty()
        assert DeliveryReservationDetails.get(0).timezone == "Europe/London"
        assert DeliveryReservationDetails.get(0).doorStepTime == omsOrder.schedulingInfo.doorStepTime.toString()
        assert DeliveryReservationDetails.get(0).bookDate == omsOrder.schedulingInfo.createdDate
        assert DeliveryReservationDetails.get(0).plannedDeliveryTime == omsOrder.schedulingInfo.plannedDueTime
        assert DeliveryReservationDetails.get(0).scheduleNumber == omsOrder.schedulingInfo.scheduleNumber
        assert DeliveryReservationDetails.get(0).fulfillmentLocationId == omsOrder.storeId
        assert DeliveryReservationDetails.get(0).fulfillmentDate == omsOrder.schedulingInfo.plannedDueTime
        assert DeliveryReservationDetails.get(0).vanId == omsOrder.schedulingInfo.vanId
        if (omsOrder.marketPlaceInfo != null) {
            assert DeliveryReservationDetails.get(0).dispenseType == "3P_DELIVERY"
        } else {
            assert DeliveryReservationDetails.get(0).dispenseType == "DELIVERY"
        }
        assert DeliveryReservationDetails.get(0).loadNumber == omsOrder.schedulingInfo.loadNumber
    }

    static void assertOrderTotals(OmsOrderTotals orderTotals, OmsOrder omsOrder) {
        assert orderTotals.promotionTotal != null
        assert orderTotals.promotionTotal.currencyAmount != null
        assert orderTotals.promotionTotal.currencyAmount == BigDecimal.ZERO
        assert orderTotals.promotionTotal.currencyUnit.toString() == "GBP"
    }

    static void assertBuyerInfo(OmsBuyerInfo buyerInfo, OmsOrder omsOrder) {
        assert !buyerInfo.isGuest
        assert buyerInfo.primaryContact != null
        assert buyerInfo.primaryContact.name != null
        assert buyerInfo.primaryContact.name.firstName != null
        assert buyerInfo.primaryContact.name.firstName == omsOrder.contactInfo.fullName.firstName
        assert buyerInfo.primaryContact.name.lastName != null
        assert buyerInfo.primaryContact.name.lastName == omsOrder.contactInfo.fullName.lastName
        assert buyerInfo.primaryContact.email != null
        assert buyerInfo.primaryContact.email.emailAddress != null
        assert buyerInfo.primaryContact.email.emailAddress == omsOrder.contactInfo.email.address
    }

    static void assertOrderLines(List<OmsOrderLine> orderLines, OmsOrder omsOrder) {
        assert orderLines != null && !orderLines.isEmpty()
        assert omsOrder.orderItemList != null && !omsOrder.orderItemList.isEmpty()
        for (OmsOrderLine orderLine in orderLines) {
            for (OmsOrderItem omsOrderItem in omsOrder.orderItemList) {
                if (omsOrderItem.getCin() == orderLine.orderProduct.offerLogistics.cin) {
                    assertOrderProduct(orderLine, omsOrderItem)
                    assertOrderedQty(orderLine, omsOrderItem)
                    assertUnitPrice(orderLine, omsOrderItem)
                    assertCharges(orderLine, omsOrderItem)
                    assertLineQuantitySummaries(orderLine, omsOrderItem)
                    assertCustomerPreferences(orderLine, omsOrderItem)
                    assertPickedItem(orderLine, omsOrderItem, omsOrder.orderState)
                    assertOrderLineQuantityInfo(orderLine, omsOrderItem)
                    assert orderLine.fulfillmentType.toString() == OmsFulfillmentOption.fromDescription(SCHEDULED__PICKUP).toString()
                    assert orderLine.shippingMethod.toString() == ShipMethod.STORE_DELIVERY.toString()
                    assert !orderLine.isPreOrder
                }
            }
        }
    }

    static void assertOrderProduct(OmsOrderLine orderLine, OmsOrderItem omsOrderItem) {
        assert orderLine.orderProduct != null
        assert orderLine.orderProduct.offerLogistics.cin == omsOrderItem.cin
        assert orderLine.orderProduct.offerLogistics.itemImageURL == omsOrderItem.catalogItem.smallImageURL
    }

    static void assertOrderedQty(OmsOrderLine orderLine, OmsOrderItem omsOrderItem) {
        assert orderLine.orderedQty != null
        assert orderLine.orderedQty.unitOfMeasure.toString() == getUOM(omsOrderItem.uom)
        assert orderLine.orderedQty.measurementValue.toLong() == omsOrderItem.quantity
    }

    static void assertUnitPrice(OmsOrderLine orderLine, OmsOrderItem omsOrderItem) {
        assert orderLine.unitPrice != null
        assert orderLine.unitPrice.currencyAmount == omsOrderItem.itemPriceInfo.unitPrice.amount
        assert orderLine.unitPrice.currencyUnit.toString() == "GBP"
    }

    static void assertCharges(OmsOrderLine orderLine, OmsOrderItem omsOrderItem) {
        assert orderLine.charges != null
        assert orderLine.charges.get(0).chargeCategory.description() == "Product"
        assert orderLine.charges.get(0).chargeName == "Item Price"
        assert orderLine.charges.get(0).chargeQuantity != null
        assert orderLine.charges.get(0).chargeQuantity.unitOfMeasure.toString() == getUOM(omsOrderItem.uom)
        assert orderLine.charges.get(0).chargeQuantity.measurementValue.toLong() == omsOrderItem.quantity
        assert orderLine.charges.get(0).chargePerUnit.currencyAmount == omsOrderItem.itemPriceInfo.unitPrice.amount
        assert orderLine.charges.get(0).chargePerUnit.currencyUnit.toString() == "GBP"
    }

    static void assertLineQuantitySummaries(OmsOrderLine orderLine, OmsOrderItem omsOrderItem) {
        assert orderLine.lineQuantitySummaries != null
        assert orderLine.lineQuantitySummaries.get(0).type == "Ordered"
        assert orderLine.lineQuantitySummaries.get(0).quantity != null
        assert orderLine.lineQuantitySummaries.get(0).quantity.unitOfMeasure.toString() == getUOM(omsOrderItem.uom)
        assert orderLine.lineQuantitySummaries.get(0).quantity.measurementValue.toLong() == omsOrderItem.quantity
    }

    static void assertCustomerPreferences(OmsOrderLine orderLine, OmsOrderItem omsOrderItem) {
        assert orderLine.customerPreferences != null
        assert orderLine.customerPreferences.isSubstitutable == Boolean.FALSE
    }

    static void assertPickedItem(OmsOrderLine orderLine, OmsOrderItem omsOrderItem, String orderState) {
        if (("EPOS_COMPLETE").equalsIgnoreCase(orderState)) {
            assert orderLine.pickedItem != null
            assert orderLine.pickedItem.quantity == omsOrderItem.pickedItem.quantity
            assert orderLine.pickedItem.weight == omsOrderItem.pickedItem.weight
            assert orderLine.pickedItem.consumerItemNumber == omsOrderItem.pickedItem.orderedCin
            assert orderLine.pickedItem.description == omsOrderItem.pickedItem.pickedItemDescription
            assert orderLine.pickedItem.pickedBy == omsOrderItem.pickedItem.picker.pickerUserName
            assert orderLine.pickedItem.unitPrice != null
            assert orderLine.pickedItem.unitPrice == omsOrderItem.pickedItem.pickedItemPriceInfo.unitPrice.amount.toDouble()
            assert orderLine.pickedItem.adjustedPrice != null
            assert orderLine.pickedItem.adjustedPrice == omsOrderItem.pickedItem.pickedItemPriceInfo.adjustedPrice.amount.toDouble()
            assert orderLine.pickedItem.adjPriceExVAT != null
            assert orderLine.pickedItem.adjPriceExVAT == omsOrderItem.pickedItem.pickedItemPriceInfo.adjustedPriceExVat.amount.toDouble()
            assert orderLine.pickedItem.vatAmount != null
            assert orderLine.pickedItem.vatAmount == omsOrderItem.pickedItem.pickedItemPriceInfo.vatAmount.amount.toDouble()
            assert orderLine.pickedItem.webAdjustedPrice != null
            assert orderLine.pickedItem.webAdjustedPrice == omsOrderItem.pickedItem.pickedItemPriceInfo.webAdjustedPrice.amount.toDouble()
            //UKGRFF-393 START
            assert orderLine.pickedItem.upcList.get(0).upc == omsOrderItem.pickedItem.pickedItemUpcList.get(0).upc
            assert orderLine.pickedItem.upcList.get(0).win == omsOrderItem.pickedItem.pickedItemUpcList.get(0).win
            assert orderLine.pickedItem.upcList.get(0).quantity == omsOrderItem.pickedItem.pickedItemUpcList.get(0).quantity
            assert orderLine.pickedItem.upcList.get(0).weight == omsOrderItem.pickedItem.pickedItemUpcList.get(0).weight
            assert orderLine.pickedItem.upcList.get(0).uom.toString() == getUOM(omsOrderItem.pickedItem.pickedItemUpcList.get(0).uom)

            assert orderLine.pickedItem.skuId == omsOrderItem.skuId
            assert orderLine.pickedItem.weightedUnitPrice == omsOrderItem.pickedItem.pickedItemPriceInfo.unitPrice.amount.toDouble()
           // UKGRFF-393 END
        }
    }

    static void assertOrderLineQuantityInfo(OmsOrderLine orderLine, OmsOrderItem omsOrderItem) {
        assert orderLine.orderLineQuantityInfo != null
        assert !orderLine.orderLineQuantityInfo.get(0).status.toString().isEmpty()
        assert !orderLine.orderLineQuantityInfo.get(0).statusCode.toString().isEmpty()
        assert !orderLine.orderLineQuantityInfo.get(0).statusDescription.isEmpty()
        assert !orderLine.orderLineQuantityInfo.get(0).statusChangeDate.toString().isEmpty()
        assert orderLine.orderLineQuantityInfo.get(0).statusQuantity != null
        assert orderLine.orderLineQuantityInfo.get(0).statusQuantity.unitOfMeasure.toString() == getUOM(omsOrderItem.uom)
        assert orderLine.orderLineQuantityInfo.get(0).statusQuantity.measurementValue.toLong() == omsOrderItem.quantity
    }

    static String getUOM(String uom) {
        if ("E".equalsIgnoreCase(uom)) {
            return "EACH"
        } else if ("K".equalsIgnoreCase(uom)) {
            return "KILOGRAM"
        }
    }
}
