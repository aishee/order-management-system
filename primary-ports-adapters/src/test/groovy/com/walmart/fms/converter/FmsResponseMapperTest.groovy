package com.walmart.fms.converter

import com.walmart.common.domain.type.Currency
import com.walmart.common.domain.type.SubstitutionOption
import com.walmart.common.domain.type.Tenant
import com.walmart.common.domain.type.Vertical
import com.walmart.fms.dto.FmsOrderResponse
import com.walmart.fms.order.aggregateroot.FmsOrder
import com.walmart.fms.order.domain.entity.FmsAddressInfo
import com.walmart.fms.order.domain.entity.FmsCustomerContactInfo
import com.walmart.fms.order.domain.entity.FmsOrderItem
import com.walmart.fms.order.domain.entity.FmsOrderTimestamps
import com.walmart.fms.order.domain.entity.FmsPickedItem
import com.walmart.fms.order.domain.entity.FmsPickedItemUpc
import com.walmart.fms.order.domain.entity.FmsSchedulingInfo
import com.walmart.fms.order.valueobject.*
import com.walmart.marketplace.order.domain.entity.type.Vendor
import spock.lang.Specification

class FmsResponseMapperTest extends Specification {

    FmsResponseMapper fmsResponseMapper

    def setup() {

        fmsResponseMapper = new FmsResponseMapper()

    }

    def "Test ConvertToOrderResponse when all child entities are present"() {

        given:

        String vendorOrderId = UUID.randomUUID().toString()
        String externalOrderId = UUID.randomUUID().toString()

        FmsOrder fmsOrder = mockFmsOrder(externalOrderId, vendorOrderId)
        fmsOrder.addAddressInfo(mockFmsAddressInfo(fmsOrder))
        fmsOrder.addSchedulingInfo(mockFmsSchedulingInfo(fmsOrder))
        fmsOrder.addContactInfo(mockFmsCustomerContactInfo(fmsOrder))
        fmsOrder.addItem(mockFmsItemWithCompleteInfo(fmsOrder))
        addMockTimestampsToOrder(fmsOrder)

        when:
        FmsOrderResponse fmsOrderResponse = fmsResponseMapper.convertToOrderResponse(fmsOrder)

        then:

        assert fmsOrderResponse.getData().getOrder().getExternalOrderId() == externalOrderId
        assert fmsOrderResponse.getData().getOrder().getFulfillmentItems().size() == 1
        assert fmsOrderResponse.getData().getOrder().getAddressInfo().addressOne == "test addressOne"
        assert fmsOrderResponse.getData().getOrder().getContactInfo().getFirstName() == "John"
        assert fmsOrderResponse.getData().getOrder().getContactInfo().getFirstName() == fmsOrder.contactInfo.getFirstlNameVal()
        assert fmsOrderResponse.getData().getOrder().getContactInfo().getLastName() == fmsOrder.contactInfo.getLastNameVal()
        assert fmsOrderResponse.getData().getOrder().getContactInfo().getMiddleName() == fmsOrder.contactInfo.getMiddleNameVal()
        assert fmsOrderResponse.getData().getOrder().getContactInfo().getEmail() == fmsOrder.contactInfo.getEmailAddr()
        assert fmsOrderResponse.getData().getOrder().getContactInfo().getMobileNumber() == fmsOrder.contactInfo.getMobileNo()
        assert fmsOrderResponse.getData().getOrder().getContactInfo().getPhoneNumberOne() == fmsOrder.contactInfo.getPhoneNoOne()
        assert fmsOrderResponse.getData().getOrder().getContactInfo().getPhoneNumberTwo() == fmsOrder.contactInfo.getPhoneNumberTwo()
        assert fmsOrderResponse.getData().getOrder().getContactInfo().getTitle() == fmsOrder.contactInfo.getCustomerTitle()

        assert fmsOrderResponse.data.order.fulfillmentItems[0].consumerItemNumber == fmsOrder.fmsOrderItems[0].consumerItemNumber
        assert fmsOrderResponse.data.order.fulfillmentItems[0].pickedItem.pickedItemDescription == fmsOrder.fmsOrderItems[0].pickedItem.pickedItemDescription
        assert fmsOrderResponse.data.order.fulfillmentItems[0].pickedItem.consumerItemNumber == fmsOrder.fmsOrderItems[0].pickedItem.cin
        assert fmsOrderResponse.data.order.fulfillmentItems[0].pickedItem.walmartItemNumber == fmsOrder.fmsOrderItems[0].pickedItem.walmartItemNumber
        assert fmsOrderResponse.data.order.fulfillmentItems[0].pickedItem.departmentID == fmsOrder.fmsOrderItems[0].pickedItem.departmentID
        assert fmsOrderResponse.data.order.fulfillmentItems[0].pickedItem.weight == fmsOrder.fmsOrderItems[0].pickedItem.weight
        assert fmsOrderResponse.data.order.fulfillmentItems[0].pickedItem.quantity == fmsOrder.fmsOrderItems[0].pickedItem.quantity
        assert fmsOrderResponse.data.order.fulfillmentItems[0].pickedItem.pickedBy == fmsOrder.fmsOrderItems[0].pickedItem.picker.pickerUserName
        assert fmsOrderResponse.data.order.fulfillmentItems[0].pickedItem.sellByDate == fmsOrder.fmsOrderItems[0].pickedItem.sellByDate
        assert fmsOrderResponse.data.order.fulfillmentItems[0].pickedItem.unitPrice == (fmsOrder.fmsOrderItems[0].pickedItem.pickedItemPriceInfo.unitPrice != null?fmsOrder.fmsOrderItems[0].pickedItem.pickedItemPriceInfo.unitPrice.amount : 0)
        assert fmsOrderResponse.data.order.fulfillmentItems[0].pickedItem.pickedItemUpcs[0].walmartItemNumber == fmsOrder.fmsOrderItems[0].pickedItem.pickedItemUpcList[0].win
        assert fmsOrderResponse.data.order.fulfillmentItems[0].pickedItem.pickedItemUpcs[0].quantity == fmsOrder.fmsOrderItems[0].pickedItem.pickedItemUpcList[0].quantity
        assert fmsOrderResponse.data.order.fulfillmentItems[0].pickedItem.pickedItemUpcs[0].weight == fmsOrder.fmsOrderItems[0].pickedItem.pickedItemUpcList[0].weight
        assert fmsOrderResponse.data.order.fulfillmentItems[0].pickedItem.pickedItemUpcs[0].unitOfMeasurement == fmsOrder.fmsOrderItems[0].pickedItem.pickedItemUpcList[0].uom
        assert fmsOrderResponse.data.order.fulfillmentItems[0].pickedItem.pickedItemUpcs[0].upc == fmsOrder.fmsOrderItems[0].pickedItem.pickedItemUpcList[0].upc
        assert fmsOrderResponse.data.order.fulfillmentItems[0].pickedItem.pickedItemUpcs[0].storeUnitPrice == (fmsOrder.fmsOrderItems[0].pickedItem.pickedItemUpcList[0].storeUnitPrice.amount != null ? fmsOrder.fmsOrderItems[0].pickedItem.pickedItemUpcList[0].storeUnitPrice.amount : 0)
        assert fmsOrderResponse.data.order.fulfillmentItems[0].nilPickQty == 0

        assert fmsOrderResponse.data.order.orderTimestamps.pickCompleteTime == fmsOrder.orderTimestamps.pickCompleteTime
        assert fmsOrderResponse.data.order.orderTimestamps.pickingStartTime == fmsOrder.orderTimestamps.pickingStartTime
        assert fmsOrderResponse.data.order.orderTimestamps.pickupReadyTime == fmsOrder.orderTimestamps.pickupReadyTime
        assert fmsOrderResponse.data.order.orderTimestamps.pickupTime == fmsOrder.orderTimestamps.pickupTime
        assert fmsOrderResponse.data.order.orderTimestamps.orderDeliveredTime == fmsOrder.orderTimestamps.orderDeliveredTime
        assert fmsOrderResponse.data.order.orderTimestamps.cancelledTime == fmsOrder.orderTimestamps.cancelledTime
        assert fmsOrderResponse.data.order.orderTimestamps.shipConfirmTime == fmsOrder.orderTimestamps.shipConfirmTime


    }

  def "Test ConvertToOrderResponse when Item has not pickedItem "() {

        given:

        String vendorOrderId = UUID.randomUUID().toString()
        String externalOrderId = UUID.randomUUID().toString()

        FmsOrder fmsOrder = mockFmsOrder(externalOrderId, vendorOrderId)
        fmsOrder.addItem(mockFmsItemWithoutPickedItem(fmsOrder))

        when:
        FmsOrderResponse fmsOrderResponse = fmsResponseMapper.convertToOrderResponse(fmsOrder)

        then:

        assert fmsOrderResponse.getData().getOrder().getExternalOrderId() == externalOrderId
        assert fmsOrderResponse.getData().getOrder().getFulfillmentItems().size() == 1

        assert fmsOrderResponse.data.order.fulfillmentItems[0].consumerItemNumber == fmsOrder.fmsOrderItems[0].consumerItemNumber
        assert fmsOrderResponse.data.order.fulfillmentItems[0].pickedItem == null

    }

    def "Test ConvertToOrderResponse when PickedItem has no Upcs "() {

        given:

        String vendorOrderId = UUID.randomUUID().toString()
        String externalOrderId = UUID.randomUUID().toString()

        FmsOrder fmsOrder = mockFmsOrder(externalOrderId, vendorOrderId)
        FmsOrderItem orderItem = mockFmsOrderItem(fmsOrder)
        orderItem.enrichPickedInfoWithPickedItem(mockPickedItemWithoutUpcs())
        fmsOrder.addItem(orderItem)

        when:
        FmsOrderResponse fmsOrderResponse = fmsResponseMapper.convertToOrderResponse(fmsOrder)

        then:

        assert fmsOrderResponse.getData().getOrder().getExternalOrderId() == externalOrderId
        assert fmsOrderResponse.getData().getOrder().getFulfillmentItems().size() == 1

        assert fmsOrderResponse.data.order.fulfillmentItems[0].consumerItemNumber == fmsOrder.fmsOrderItems[0].consumerItemNumber
        assert fmsOrderResponse.data.order.fulfillmentItems[0].pickedItem != null
        assert fmsOrderResponse.data.order.fulfillmentItems[0].pickedItem.pickedItemUpcs.size() == 0

    }
    private FmsCustomerContactInfo mockFmsCustomerContactInfo(FmsOrder fmsOrder) {
        FmsCustomerContactInfo.builder()
                .order(fmsOrder)
                .fullName(new FullName("Test", "John", "Wick", "Doe"))
                .phoneNumberOne(new TelePhone("0123456789")).build()
    }

    private FmsSchedulingInfo mockFmsSchedulingInfo(FmsOrder fmsOrder) {
        FmsSchedulingInfo.builder()
                .order(fmsOrder)
                .tripId("tripID-2333344").build()
    }

    private FmsAddressInfo mockFmsAddressInfo(FmsOrder fmsOrder) {
        FmsAddressInfo.builder()
                .order(fmsOrder)
                .addressOne("test addressOne")
                .addressTwo("test addressTwo")
                .addressThree("Test adddressThree")
                .city("test city")
                .latitude("57.2252552")
                .longitude("-6.24343").build()
    }

    private FmsOrder mockFmsOrder(String externalOrderId, String vendorOrderId) {
        FmsOrder fmsOrder = FmsOrder.builder()
                .vertical(Vertical.ASDAGR)
                .tenant(Tenant.ASDA)
                .storeId("4401")
                .sourceOrderId(externalOrderId)
                .pickupLocationId("4401")
                .deliveryDate(new Date())
                .marketPlaceInfo(MarketPlaceInfo.builder().vendor(Vendor.UBEREATS).vendorOrderId(vendorOrderId).build())
                .orderState(FmsOrder.OrderStatus.RECEIVED_AT_STORE.getName())
                .priceInfo(OrderPriceInfo.builder()
                        .webOrderTotal(40.0).build())
                .build()

    }
    private addMockTimestampsToOrder(FmsOrder fmsOrder){
        fmsOrder.orderTimestamps = mockFmsOrderTimestamps(fmsOrder)
        return  fmsOrder
    }
    private FmsOrderTimestamps mockFmsOrderTimestamps(FmsOrder fmsOrder) {

        return new FmsOrderTimestamps(
                order: fmsOrder,
                pickCompleteTime: new Date(),
                pickingStartTime: new Date(),
                pickupReadyTime: new Date(),
                orderDeliveredTime: new Date(),
                cancelledTime: null,
                shipConfirmTime: new Date()
        )

    }


    def "Test ConvertToOrderResponse when all child entities are not present"() {
        given:

        String vendorOrderId = UUID.randomUUID().toString()
        String externalOrderId = UUID.randomUUID().toString()
        FmsOrder fmsOrder = FmsOrder.builder()
                .vertical(Vertical.ASDAGR)
                .tenant(Tenant.ASDA)
                .storeId("4401")
                .sourceOrderId(externalOrderId)
                .pickupLocationId("4401")
                .deliveryDate(new Date())
                .marketPlaceInfo(MarketPlaceInfo.builder().vendor(Vendor.UBEREATS).vendorOrderId(vendorOrderId).build())
                .orderState(FmsOrder.OrderStatus.RECEIVED_AT_STORE.getName())
                .priceInfo(OrderPriceInfo.builder()
                        .webOrderTotal(40.0).build()).build()

        fmsOrder.addItem(mockFmsItemWithCompleteInfo(fmsOrder))

        when:
        FmsOrderResponse fmsOrderResponse = fmsResponseMapper.convertToOrderResponse(fmsOrder)

        then:

        assert fmsOrderResponse.getData().getOrder().getExternalOrderId() == externalOrderId
        assert fmsOrderResponse.getData().getOrder().getFulfillmentItems().size() == 1
        assert fmsOrderResponse.getData().getOrder().getFulfillmentItems().size() == 1
        assert fmsOrderResponse.getData().getOrder().getFulfillmentItems()[0].maxIdealDayValue == fmsOrder.getFmsOrderItems()[0].catalogInfo.maxIdealDays
        assert fmsOrderResponse.getData().getOrder().getFulfillmentItems()[0].minIdealDayValue == fmsOrder.getFmsOrderItems()[0].catalogInfo.minIdealDays
        assert fmsOrderResponse.getData().getOrder().getFulfillmentItems()[0].salesUnit == fmsOrder.getFmsOrderItems()[0].catalogInfo.salesUnit
        assert fmsOrderResponse.getData().getOrder().getFulfillmentItems()[0].unitPrice == fmsOrder.getFmsOrderItems()[0].itemPriceInfo.unitPrice.amount
        assert fmsOrder.getFmsOrderItems()[0].upcInfo.upcNumbers.contains(fmsOrderResponse.getData().getOrder().getFulfillmentItems()[0].orderItemUpcs[0].upc)

        assert fmsOrderResponse.getData().getOrder().getFulfillmentItems()[0].minIdealDayValue == fmsOrder.getFmsOrderItems()[0].getMinIdealDayValue()
        assert fmsOrderResponse.getData().getOrder().getFulfillmentItems()[0].maxIdealDayValue == fmsOrder.getFmsOrderItems()[0].getMaxIdealDayValue()
        assert fmsOrderResponse.getData().getOrder().getFulfillmentItems()[0].unitPrice == fmsOrder.getFmsOrderItems()[0].getUnitPrice()
        assert fmsOrderResponse.getData().getOrder().getFulfillmentItems()[0].unitOfMeasurement == fmsOrder.getFmsOrderItems()[0].getUnitOfMeasurement()
        assert fmsOrderResponse.getData().getOrder().getFulfillmentItems()[0].imageURL == fmsOrder.getFmsOrderItems()[0].getImageUrl()
        assert fmsOrderResponse.getData().getOrder().getFulfillmentItems()[0].getQuantity() == fmsOrder.getFmsOrderItems()[0].getQuantity()
        assert fmsOrderResponse.getData().getOrder().getFulfillmentItems()[0].getNilPickQty() == fmsOrder.getFmsOrderItems()[0].getNilPickQuantity()

        assert fmsOrderResponse.getData().getOrder().getFulfillmentItems()[0].isSellbyDateRequired() == fmsOrder.getFmsOrderItems()[0].isSellbyDateRequired()
        assert fmsOrderResponse.getData().getOrder().getFulfillmentItems()[0].getTemperatureZone() == fmsOrder.getFmsOrderItems()[0].getTemparatureZone()
        assert fmsOrderResponse.getData().getOrder().getFulfillmentItems()[0].getSalesUnit() == fmsOrder.getFmsOrderItems()[0].getSalesUnit()
        assert fmsOrderResponse.getData().getOrder().getFulfillmentItems()[0].getPickerItemDescription() == fmsOrder.getFmsOrderItems()[0].getItemDescription()
        assert fmsOrderResponse.getData().getOrder().getFulfillmentItems()[0].getPickedItem().quantity == fmsOrder.getFmsOrderItems()[0].getPickedItemQuantity()

    }

    private ItemCatalogInfo mockItemCatalogInfo() {
        return new ItemCatalogInfo("EACH", "E", "Asda Fresh ",
                "https://i.groceries.asda.com/image.jpg", 3,
                4, "Ambient", true);


    }

    private FmsOrderItem mockFmsItemWithCompleteInfo(FmsOrder fmsOrder) {
        FmsOrderItem fmsItem = mockFmsOrderItem(fmsOrder)
        fmsItem.addCatalogInfo(mockItemCatalogInfo());
        fmsItem.addUpcInfo(mockFmsItemUpcInfo())
        fmsItem.enrichPickedInfoWithPickedItem(mockPickedItemWithUpcs(fmsItem))
        return fmsItem

    }

    private FmsOrderItem mockFmsItemWithoutPickedItem(FmsOrder fmsOrder) {
        FmsOrderItem fmsItem = mockFmsOrderItem(fmsOrder)
        fmsItem.addCatalogInfo(mockItemCatalogInfo());
        fmsItem.addUpcInfo(mockFmsItemUpcInfo())
        return fmsItem

    }

    private FmsOrderItem mockFmsOrderItem(FmsOrder fmsOrder) {
        FmsOrderItem.builder()
                .fmsOrder(fmsOrder)
                .consumerItemNumber("464646")
                .itemPriceInfo(new ItemPriceInfo(new Money(BigDecimal.valueOf(2.5), Currency.GBP)))
                .quantity(3)
                .substitutionOption(SubstitutionOption.DO_NOT_SUBSTITUTE)
                .build()
    }

    private ItemUpcInfo mockFmsItemUpcInfo() {
        return ItemUpcInfo.builder()
                .upcNumbers(["22233", "44455"])
                .build()
    }

    private mockPickedItemWithUpcs(FmsOrderItem orderItem){
        FmsPickedItem pickedItem = mockPickedItemWithoutUpcs(orderItem)
        pickedItem.pickedItemUpcList = [
                mockFmsPickedItemUpc()
        ]
        return pickedItem
    }

    private FmsPickedItem mockPickedItemWithoutUpcs(FmsOrderItem orderItem) {
       return new FmsPickedItem(
                fmsOrderItem: orderItem,
                departmentID: null,
                cin: null,
                walmartItemNumber: null,
                quantity: 3,
                weight: 2.2,
                pickedItemDescription: "Asda Biscuits picked",
                sellByDate: new Date(),
                picker: new Picker(
                        pickerUserName: "TestPicker"
                ),
                pickedItemPriceInfo: new PickedItemPriceInfo(
                        unitPrice: new Money(BigDecimal.valueOf(3.33), Currency.GBP),
                )

        )
    }

    private FmsPickedItemUpc mockFmsPickedItemUpc() {
        new FmsPickedItemUpc(
                upc: "55555555",
                uom: "K",
                win: "88888888",
                quantity: 3,
                weight: 0,
                storeUnitPrice: new Money(BigDecimal.valueOf(5.00), Currency.GBP),
        )
    }
}