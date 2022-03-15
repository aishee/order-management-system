package com.walmart.oms.converter

import com.walmart.oms.dto.OmsOrderDto
import com.walmart.oms.dto.OmsOrderResponse
import com.walmart.oms.order.aggregateroot.OmsOrder

class OmsResponseMapperAssertions {

    static void assertPickItems(OmsOrderResponse omsOrderResponse, OmsOrder omsOrder) {
        def omsOrderResponsePickedItem = omsOrderResponse.getData().getOrder().getOrderItems().get(0).pickedItem
        def omsOrderPickedItem = omsOrder.getOrderItemList().get(0).pickedItem
        assert omsOrderResponsePickedItem.pickedItemDescription == omsOrderPickedItem.pickedItemDescription
        assert omsOrderResponsePickedItem.consumerItemNum == omsOrderPickedItem.orderedCin
        assert omsOrderResponsePickedItem.departmentId == omsOrderPickedItem.departmentID
        assert omsOrderResponsePickedItem.weight == omsOrderPickedItem.weight
        assert omsOrderResponsePickedItem.quantity == omsOrderPickedItem.quantity
        assert omsOrderResponsePickedItem.pickedBy == omsOrderPickedItem.picker.pickerUserName
        assert omsOrderResponsePickedItem.unitPrice == omsOrderPickedItem.pickedItemPriceInfo.unitPrice.amount
        assert omsOrderResponsePickedItem.adjustedPrice == omsOrderPickedItem.pickedItemPriceInfo.adjustedPrice.amount
        assert omsOrderResponsePickedItem.adjustedPriceExVat == 0
        assert omsOrderResponsePickedItem.webAdjustedPrice == 0
    }

    static void assertPickedItemsUpc(OmsOrderResponse omsOrderResponse, OmsOrder omsOrder) {
        def omsOrderResponsePickedItemUpcs = omsOrderResponse.getData().getOrder().getOrderItems().get(0).pickedItem.pickedItemUpcs.get(0)
        def omsOrderPickedItemUpcs = omsOrder.getOrderItemList().get(0).pickedItem.pickedItemUpcList.get(0)
        assert omsOrderResponsePickedItemUpcs.walmartItemNumber == omsOrderPickedItemUpcs.win
        assert omsOrderResponsePickedItemUpcs.weight == omsOrderPickedItemUpcs.weight
        assert omsOrderResponsePickedItemUpcs.quantity == omsOrderPickedItemUpcs.quantity
        assert omsOrderResponsePickedItemUpcs.unitOfMeasurement == omsOrderPickedItemUpcs.uom
        assert omsOrderResponsePickedItemUpcs.upc == omsOrderPickedItemUpcs.upc
        assert omsOrderResponsePickedItemUpcs.storeUnitPrice == omsOrderPickedItemUpcs.storeUnitPrice.amount
    }

    static void assertOmsOrderAllEntities(OmsOrderResponse omsOrderResponse, OmsOrder omsOrder) {
        def OmsOrderDto omsOrderDto = omsOrderResponse.getData().getOrder();
        assert omsOrderDto.getExternalOrderId() == omsOrder.getSourceOrderId()
        assert omsOrderDto.getOrderItems().size() == 1
        assert omsOrderDto.getAddressInfos().get(0).addressOne == "test addressOne"
        assert omsOrderDto.getContactInfo().getFirstName() == "John"

        assert omsOrderDto.getContactInfo().getFirstName() == omsOrder.contactInfo.getFirstName()
        assert omsOrderDto.getContactInfo().getMiddleName() == omsOrder.contactInfo.getMiddleName()
        assert omsOrderDto.getContactInfo().getLastName() == omsOrder.contactInfo.getLastName()
        assert omsOrderDto.getContactInfo().getPhoneNumberOne() == omsOrder.contactInfo.getRefPhoneNumberOne()
        assert omsOrderDto.getContactInfo().getPhoneNumberTwo() == omsOrder.contactInfo.getRefPhoneNumberTwo()
        assert omsOrderDto.getContactInfo().getMobileNumber() == omsOrder.contactInfo.getMobileNumber()

        assert omsOrderDto.getOrderItems().get(0).consumerItemNumber == omsOrder.getOrderItemList().get(0).cin
    }

    static void assertOmsOrder(OmsOrderResponse omsOrderResponse, OmsOrder omsOrder) {
        def OmsOrderDto omsOrderDto = omsOrderResponse.getData().getOrder();
        assert omsOrderDto.getExternalOrderId() == omsOrder.getSourceOrderId()
        assert omsOrderDto.getStoreOrderId() == omsOrder.getStoreOrderId()
        assert omsOrderDto.getOrderItems().size() == 1
        assert omsOrderDto.getOrderItems().get(0).consumerItemNumber == omsOrder.getOrderItemList().get(0).cin
        assert omsOrderDto.getOrderItems().get(0).getSubstitutionOption() == omsOrder.getOrderItemList().get(0).getSubstitutionOption()
    }

    static void assertNoPickedItem(OmsOrderResponse omsOrderResponse, OmsOrder omsOrder) {
        assert omsOrderResponse.getData().getOrder().getOrderItems().get(0).pickedItem == null
    }

    static void assertNoPickedItemUpc(OmsOrderResponse omsOrderResponse, OmsOrder omsOrder) {
        assert omsOrderResponse.getData().getOrder().getOrderItems().get(0).pickedItem.pickedItemUpcs.size() == 0
    }
}