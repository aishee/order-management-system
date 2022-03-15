package com.walmart.fms


import com.walmart.common.domain.type.Currency
import com.walmart.common.domain.type.SubstitutionOption
import com.walmart.common.domain.type.Tenant
import com.walmart.common.domain.type.Vertical
import com.walmart.fms.order.aggregateroot.FmsOrder
import com.walmart.fms.order.domain.entity.FmsAddressInfo
import com.walmart.fms.order.domain.entity.FmsCustomerContactInfo
import com.walmart.fms.order.domain.entity.FmsOrderItem
import com.walmart.fms.order.domain.entity.FmsSchedulingInfo
import com.walmart.fms.order.valueobject.*
import com.walmart.marketplace.order.domain.entity.type.Vendor

class FmsMockOrderFactory {
    static String vendorOrderId = UUID.randomUUID()
    static String sourceOrderId = UUID.randomUUID()
    static Vendor vendor = Vendor.UBEREATS


    static FmsOrder transientFmsOrder() {
        return new FmsOrder("INITIAL")
    }


    static FmsOrder give_me_a_default_order_of_status(String orderState) {
        FmsOrder testFmsOrder = FmsOrder.builder()
                .vertical(Vertical.ASDAGR)
                .tenant(Tenant.ASDA)
                .storeId("4401")
                .sourceOrderId("12345566778")
                .storeOrderId(UUID.randomUUID().toString())
                .pickupLocationId("4401")
                .deliveryDate(new Date())
                .orderState(orderState)
                .priceInfo(OrderPriceInfo.builder()
                        .webOrderTotal(40.0).build()).build()

        return testFmsOrder
    }

    static FmsOrder give_me_a_marketplace_order_without_items() {
        FmsOrder testFmsOrder = FmsOrder.builder()
                .vertical(Vertical.ASDAGR)
                .tenant(Tenant.ASDA)
                .storeId("4401")
                .sourceOrderId(UUID.randomUUID().toString())
                .storeOrderId(UUID.randomUUID().toString())
                .pickupLocationId("4401")
                .deliveryDate(new Date())
                .orderState(FmsOrder.OrderStatus.READY_FOR_STORE.name)
                .marketPlaceInfo(MarketPlaceInfo.builder().vendor(vendor).vendorOrderId(vendorOrderId).build())
                .priceInfo(OrderPriceInfo.builder().webOrderTotal(40.0).build()).build()

        return testFmsOrder;
    }

    static FmsOrder give_me_a_valid_market_place_order() {
        FmsOrder testFmsOrder = plainFmsOrder()
        testFmsOrder.vertical = Vertical.MARKETPLACE
        testFmsOrder.addMarketPlaceInfo(MarketPlaceInfo.builder().vendor(vendor).vendorOrderId(vendorOrderId).build())
        return testFmsOrder
    }

    static FmsOrder plainFmsOrder() {
        FmsOrder testFmsOrder = FmsOrder.builder()
                .id(UUID.randomUUID().toString())
                .vertical(Vertical.ASDAGR)
                .tenant(Tenant.ASDA)
                .storeId("4401")
                .sourceOrderId(sourceOrderId)
                .storeOrderId("123456789")
                .pickupLocationId("4401")
                .deliveryDate(new Date())
                .orderState(FmsOrder.OrderStatus.READY_FOR_STORE.name)
                .priceInfo(OrderPriceInfo.builder()
                        .webOrderTotal(40.0).build()).build()


        testFmsOrder.addAddressInfo(FmsAddressInfo.builder()
                .order(testFmsOrder)
                .addressOne("test addressOne")
                .addressTwo("test addressTwo")
                .addressThree("Test adddressThree")
                .city("test city")
                .latitude("57.2252552")
                .longitude("-6.24343").build())


        testFmsOrder.addSchedulingInfo(FmsSchedulingInfo.builder().order(testFmsOrder)
                .tripId("trip_id_2222").build())

        testFmsOrder.addContactInfo(FmsCustomerContactInfo.builder()
                .order(testFmsOrder)
                .fullName(new FullName(null, "John", null, "Doe"))
                .phoneNumberOne(new TelePhone("0123456789")).build())


        testFmsOrder.addItem(mockFmsItem(testFmsOrder))

        return testFmsOrder
    }

    private static FmsOrderItem mockFmsItem(FmsOrder fmsOrder) {
        FmsOrderItem fmsItem = FmsOrderItem.builder()
                .fmsOrder(fmsOrder)
                .consumerItemNumber("464646")
                .itemPriceInfo(new ItemPriceInfo(new Money(BigDecimal.valueOf(2.5), Currency.GBP)))
                .quantity(2)
                .substitutionOption(SubstitutionOption.DO_NOT_SUBSTITUTE)
                .build()
        fmsItem.addCatalogInfo(mockItemCatalogInfo())
        fmsItem.addUpcInfo(mockFmsItemUpcInfo())
        return fmsItem

    }

    private static ItemCatalogInfo mockItemCatalogInfo() {
        return new ItemCatalogInfo("EACH", "E", "Asda Fresh ",
                "https://i.groceries.asda.com/image.jpg", 3,
                4, "Ambient", true)


    }

    private static ItemUpcInfo mockFmsItemUpcInfo() {
        return ItemUpcInfo.builder()
                .upcNumbers(["22233", "44455"])
                .build()
    }

    private static FmsOrder fmsOrder_without_items(FmsOrder validFmsOrder) {
        validFmsOrder.fmsOrderItems = null
        return validFmsOrder
    }

    private static fmsOrder_which_is_not_ready_for_store() {
        FmsOrder validFmsOrder = plainFmsOrder()
        validFmsOrder.orderState = "NOT_READY_FOR_STORE"
        return validFmsOrder
    }

    static FmsOrder valid_fms_marketplace_order() {
        FmsOrder validFmsOrder = plainFmsOrder()
        validFmsOrder.marketPlaceInfo = MarketPlaceInfo.builder().vendor(vendor).vendorOrderId(vendorOrderId).build()
        validFmsOrder.vertical = Vertical.MARKETPLACE
        return validFmsOrder
    }

    static FmsOrder valid_fms_ghs_order() {
        FmsOrder validFmsOrder = plainFmsOrder()
        validFmsOrder.vertical = Vertical.ASDAGR
        return validFmsOrder
    }

    static FmsOrder ghs_order_not_ready_for_store() {
        FmsOrder order = fmsOrder_which_is_not_ready_for_store()
        order.vertical = Vertical.ASDAGR
        return order
    }

    static FmsOrder give_me_a_marketplace_order_not_ready_for_store() {
        FmsOrder order = give_me_a_valid_market_place_order()
        order.orderState = FmsOrder.OrderStatus.CANCELLED.name
        return order
    }

    static FmsOrder marketplace_order_with_source_order_id_null() {
        FmsOrder order = valid_fms_marketplace_order()
        order.sourceOrderId = null
        return order
    }

    static FmsOrder marketplace_order_without_items() {
        return fmsOrder_without_items(valid_fms_marketplace_order())
    }

    static FmsOrder ghs_order_without_items() {
        return fmsOrder_without_items(valid_fms_ghs_order())
    }



}
