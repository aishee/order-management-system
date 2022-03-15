package com.walmart.oms.converter

import com.walmart.common.domain.type.Currency
import com.walmart.common.domain.type.FulfillmentType
import com.walmart.common.domain.type.Tenant
import com.walmart.common.domain.type.Vertical
import com.walmart.marketplace.order.domain.entity.type.Vendor
import com.walmart.oms.order.aggregateroot.OmsOrder
import com.walmart.oms.order.domain.entity.*
import com.walmart.oms.order.valueobject.*

class OmsMockOrder {
    static String vendorOrderId = UUID.randomUUID().toString()
    static String externalOrderId = UUID.randomUUID().toString()

    static OmsOrder give_Oms_Order_all_child_entities_present() {
        OmsOrder omsOrder = mockOmsOrder()

        omsOrder.addAddress(mockAddressInfo(omsOrder))
        omsOrder.addSchedulingInfo(mockSchedulingInfo(omsOrder))
        omsOrder.addContactInfo(mockCustomerContactInfo(omsOrder))

        OmsOrderItem omsOrderItem = mockOmsOrderItem(omsOrder)
        omsOrderItem.enrichPickedInfoWithPickedItem(mockPickedItemWithUpcs(omsOrderItem))
        omsOrder.addItem(omsOrderItem)

        Map catalogData = new HashMap<>();
        catalogData.put("464646", CatalogItem.builder().upcNumbers(Arrays.asList("1424242")).build())

        omsOrder.enrichItemsWithCatalogData(catalogData)
        return omsOrder
    }

    static OmsOrder give_me_Oms_Order_all_child_entities_not_present() {
        OmsOrder omsOrder = OmsOrder.builder()
                .vertical(Vertical.ASDAGR)
                .tenant(Tenant.ASDA)
                .storeId("4401")
                .sourceOrderId(externalOrderId)
                .storeOrderId("123456789")
                .pickupLocationId("4401")
                .spokeStoreId("4401")
                .deliveryDate(new Date())
                .fulfillmentType(FulfillmentType.INSTORE_PICKUP)
                .marketPlaceInfo(MarketPlaceInfo.builder().
                        vendor(Vendor.UBEREATS).vendorOrderId(vendorOrderId).build())
                .orderState("RECD_AT_STORE")
                .priceInfo(OrderPriceInfo.builder()
                        .orderSubTotal(40.0).build()).build()

        OmsOrderItem omsOrderItem = mockOmsOrderItem(omsOrder)
        omsOrderItem.enrichPickedInfoWithPickedItem(mockPickedItemWithUpcs(omsOrderItem))
        omsOrder.addItem(omsOrderItem)
        return omsOrder
    }

    static OmsOrder give_me_Oms_Order_no_picked_Item() {
        OmsOrder omsOrder = mockOmsOrder()

        omsOrder.addAddress(mockAddressInfo(omsOrder))
        omsOrder.addSchedulingInfo(mockSchedulingInfo(omsOrder))
        omsOrder.addContactInfo(mockCustomerContactInfo(omsOrder))

        OmsOrderItem omsOrderItem = mockOmsOrderItem(omsOrder)
        omsOrder.addItem(omsOrderItem)

        Map catalogData = new HashMap<>();
        catalogData.put("464646", CatalogItem.builder().upcNumbers(Arrays.asList("1424242")).build())
        omsOrder.enrichItemsWithCatalogData(catalogData)
        return omsOrder
    }

    static OmsOrder give_me_Oms_Order_picked_item_no_upcs() {
        OmsOrder omsOrder = mockOmsOrder()

        omsOrder.addAddress(mockAddressInfo(omsOrder))
        omsOrder.addSchedulingInfo(mockSchedulingInfo(omsOrder))
        omsOrder.addContactInfo(mockCustomerContactInfo(omsOrder))

        OmsOrderItem omsOrderItem = mockOmsOrderItem(omsOrder)
        omsOrderItem.enrichPickedInfoWithPickedItem(mockPickedItemWithoutUpcs(omsOrderItem))
        omsOrder.addItem(omsOrderItem)

        Map catalogData = new HashMap<>();
        catalogData.put("464646", CatalogItem.builder().upcNumbers(Arrays.asList("1424242")).build())
        omsOrder.enrichItemsWithCatalogData(catalogData)
        return omsOrder
    }

    private static CustomerContactInfo mockCustomerContactInfo(OmsOrder omsOrder) {
        return CustomerContactInfo.builder()
                .order(omsOrder)
                .fullName(new FullName(null, "John", null, "Doe"))
                .phoneNumberOne(new TelePhone("0123456789")).build()
    }

    private static SchedulingInfo mockSchedulingInfo(OmsOrder omsOrder) {
        return SchedulingInfo.builder()
                .order(omsOrder)
                .plannedDueTime(new Date()).build()
    }

    private static AddressInfo mockAddressInfo(OmsOrder omsOrder) {
        return AddressInfo.builder()
                .omsOrder(omsOrder)
                .addressOne("test addressOne")
                .addressTwo("test addressTwo")
                .addressThree("Test adddressThree")
                .city("test city")
                .latitude("57.2252552")
                .longitude("-6.24343").build()
    }

    private static OmsOrder mockOmsOrder() {
        return OmsOrder.builder()
                .vertical(Vertical.ASDAGR)
                .tenant(Tenant.ASDA)
                .storeId("4401")
                .sourceOrderId(externalOrderId)
                .storeOrderId("123456789")
                .pickupLocationId("4401")
                .spokeStoreId("4401")
                .deliveryDate(new Date())
                .fulfillmentType(FulfillmentType.INSTORE_PICKUP)
                .marketPlaceInfo(MarketPlaceInfo.builder().vendor(Vendor.UBEREATS)
                        .vendorOrderId(vendorOrderId).build())
                .orderState("READY_FOR_STORE")
                .priceInfo(OrderPriceInfo.builder().orderSubTotal(40.0).build())
                .build()
    }

    private static PickedItem mockPickedItemWithUpcs(OmsOrderItem orderItem) {
        PickedItem pickedItem = mockPickedItemWithoutUpcs(orderItem)
        pickedItem.pickedItemUpcList = [
                mockOmsPickedItemUpc()
        ]
        return pickedItem
    }

    private static OmsOrderItem mockOmsOrderItem(OmsOrder omsOrder) {
        return OmsOrderItem.builder()
                .omsOrder(omsOrder)
                .cin("464646")
                .itemPriceInfo(new ItemPriceInfo(new Money(BigDecimal.valueOf(2.5), Currency.GBP),
                        new Money(BigDecimal.valueOf(5.0), Currency.GBP)))
                .quantity(2)
                .salesUnit("EACH")
                .uom("E")
                .build()
    }

    private static PickedItem mockPickedItemWithoutUpcs(OmsOrderItem orderItem) {
        return new PickedItem(
                omsOrderItem: orderItem,
                departmentID: null,
                orderedCin: null,
                quantity: 3,
                weight: 2.2,
                pickedItemDescription: "Asda Biscuits picked",
                picker: new Picker(
                        pickerUserName: "TestPicker"
                ),
                pickedItemPriceInfo: new PickedItemPriceInfo(
                        unitPrice: new Money(BigDecimal.valueOf(3.33), Currency.GBP),
                        adjustedPriceExVat: null,
                        adjustedPrice: new Money(BigDecimal.valueOf(4.44), Currency.GBP),
                        webAdjustedPrice: null,
                        displayPrice: null,
                        vatAmount: null
                )
        )
    }

    private static PickedItemUpc mockOmsPickedItemUpc() {
        return new PickedItemUpc(
                upc: "55555555",
                uom: "K",
                win: "88888888",
                quantity: 3,
                weight: 0,
                storeUnitPrice: new com.walmart.oms.order.valueobject.Money(BigDecimal.valueOf(5.00), Currency.GBP)
        )
    }
}
