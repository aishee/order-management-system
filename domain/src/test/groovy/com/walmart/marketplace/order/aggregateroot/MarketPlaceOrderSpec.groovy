package com.walmart.marketplace.order.aggregateroot

import com.walmart.common.domain.type.Currency
import com.walmart.common.domain.type.SubstitutionOption
import com.walmart.marketplace.order.domain.entity.MarketPlaceBundledItem
import com.walmart.marketplace.order.domain.entity.MarketPlaceItem
import com.walmart.marketplace.order.domain.entity.type.Vendor
import com.walmart.marketplace.order.domain.valueobject.*
import com.walmart.oms.domain.error.exception.OMSBadRequestException
import spock.lang.Specification

class MarketPlaceOrderSpec extends Specification {
    MarketPlaceOrder marketPlaceOrder
    String externalOrderId = UUID.randomUUID().toString()
    String sourceOrderId = UUID.randomUUID().toString()
    String externalItemId1 = UUID.randomUUID().toString()
    String externalItemId2 = UUID.randomUUID().toString()
    String itemId1 = UUID.randomUUID().toString()
    String itemId2 = UUID.randomUUID().toString()
    String instanceId1 = UUID.randomUUID().toString()
    String instanceId2 = UUID.randomUUID().toString()

    def setup() {
        marketPlaceOrder = new MarketPlaceOrder()
    }

    def "Successfully add MarketPlaceItem to Marketplace order"() {
        given:
        ItemIdentifier itemIdentifier1 = ItemIdentifier.builder().itemId(externalItemId1).itemType("CIN").build()
        ItemIdentifier itemIdentifier2 = ItemIdentifier.builder().itemId(externalItemId2).itemType("CIN").build()
        MarketPlaceItemPriceInfo marketPlaceItemPriceInfo = MarketPlaceItemPriceInfo.builder().totalPrice(1.0).baseTotalPrice(1.0).unitPrice(1.0).baseUnitPrice(1.0).build()
        marketPlaceOrder = mockMarketPlaceOrder()

        when:
        marketPlaceOrder.addMarketPlaceItem(itemId1, externalItemId1, "test item", 1, instanceId1, itemIdentifier1, marketPlaceItemPriceInfo, SubstitutionOption.DO_NOT_SUBSTITUTE)
        marketPlaceOrder.addMarketPlaceItem(itemId2, externalItemId2, "test item", 2, instanceId2, itemIdentifier2, marketPlaceItemPriceInfo, SubstitutionOption.DO_NOT_SUBSTITUTE)

        then:
        marketPlaceOrder.getMarketPlaceItems().size() == 2
    }

    def "add MarketPlaceItem with same itemIdentifier to Marketplace order"() {
        given:
        ItemIdentifier itemIdentifier1 = ItemIdentifier.builder().itemId(externalItemId1).itemType("CIN").build()
        MarketPlaceItemPriceInfo marketPlaceItemPriceInfo = MarketPlaceItemPriceInfo.builder().totalPrice(1.0).baseTotalPrice(1.0).unitPrice(1.0).baseUnitPrice(1.0).build()
        marketPlaceOrder = mockMarketPlaceOrder()

        when:
        marketPlaceOrder.addMarketPlaceItem(itemId1, externalItemId1, "test item", 1, instanceId1, itemIdentifier1, marketPlaceItemPriceInfo, SubstitutionOption.DO_NOT_SUBSTITUTE)
        marketPlaceOrder.addMarketPlaceItem(itemId2, externalItemId2, "test item", 2, instanceId2, itemIdentifier1, marketPlaceItemPriceInfo, SubstitutionOption.DO_NOT_SUBSTITUTE)

        then:
        thrown(OMSBadRequestException)
    }

    def "set MarketPlaceItem list to Marketplace order"() {
        given:
        ItemIdentifier itemIdentifier1 = ItemIdentifier.builder().itemId(externalItemId1).itemType("CIN").build()
        ItemIdentifier itemIdentifier2 = ItemIdentifier.builder().itemId(externalItemId2).itemType("CIN").build()
        MarketPlaceItemPriceInfo marketPlaceItemPriceInfo = MarketPlaceItemPriceInfo.builder().totalPrice(1.0).baseTotalPrice(1.0).unitPrice(1.0).baseUnitPrice(1.0).build()
        marketPlaceOrder = mockMarketPlaceOrder()
        List<MarketPlaceBundledItem> bundledItemList = new ArrayList<>()
        MarketPlaceBundledItem bundledItem1 = new MarketPlaceBundledItem(new MarketPlaceItem(), "abc", "c2", 2, 1, "abc-def-ghi", "abda")
        MarketPlaceBundledItem bundledItem2 = new MarketPlaceBundledItem(new MarketPlaceItem(), "abc", "c2", 2, 1, "abc-def-ghi", "agfaef")
        bundledItemList.add(bundledItem1)
        bundledItemList.add(bundledItem2)
        MarketPlaceItem marketPlaceItem1 = new MarketPlaceItem(marketPlaceOrder, itemId1, externalItemId1, "test item", instanceId1, itemIdentifier1, 1, marketPlaceItemPriceInfo, bundledItemList, SubstitutionOption.DO_NOT_SUBSTITUTE)
        MarketPlaceItem marketPlaceItem2 = new MarketPlaceItem(marketPlaceOrder, itemId2, externalItemId2, "test item", instanceId2, itemIdentifier2, 2, marketPlaceItemPriceInfo, bundledItemList, SubstitutionOption.DO_NOT_SUBSTITUTE)
        List<MarketPlaceItem> marketPlaceItemList = new ArrayList<>()
        marketPlaceItemList.add(marketPlaceItem1)
        marketPlaceItemList.add(marketPlaceItem2)

        when:
        marketPlaceOrder.addMarketPlaceItems(marketPlaceItemList)

        then:
        marketPlaceOrder.getMarketPlaceItems().size() == 2
    }

    def "Successfully cancel Marketplace order"() {
        given:
        ItemIdentifier itemIdentifier1 = ItemIdentifier.builder().itemId(externalItemId1).itemType("CIN").build()
        ItemIdentifier itemIdentifier2 = ItemIdentifier.builder().itemId(externalItemId2).itemType("CIN").build()
        MarketPlaceItemPriceInfo marketPlaceItemPriceInfo = MarketPlaceItemPriceInfo.builder().totalPrice(1.0).baseTotalPrice(1.0).unitPrice(1.0).baseUnitPrice(1.0).build()
        marketPlaceOrder = mockMarketPlaceOrder()
        marketPlaceOrder.addMarketPlaceItem(itemId1, externalItemId1, "test item", 1, instanceId1, itemIdentifier1, marketPlaceItemPriceInfo, SubstitutionOption.DO_NOT_SUBSTITUTE)
        marketPlaceOrder.addMarketPlaceItem(itemId2, externalItemId2, "test item", 2, instanceId2, itemIdentifier2, marketPlaceItemPriceInfo, SubstitutionOption.DO_NOT_SUBSTITUTE)

        when:
        marketPlaceOrder.cancel()

        then:
        marketPlaceOrder.orderState == "CANCELLED"
    }

    def "Unsuccessful cancel Marketplace order"() {
        given:
        ItemIdentifier itemIdentifier1 = ItemIdentifier.builder().itemId(externalItemId1).itemType("CIN").build()
        ItemIdentifier itemIdentifier2 = ItemIdentifier.builder().itemId(externalItemId2).itemType("CIN").build()
        MarketPlaceItemPriceInfo marketPlaceItemPriceInfo = MarketPlaceItemPriceInfo.builder().totalPrice(1.0).baseTotalPrice(1.0).unitPrice(1.0).baseUnitPrice(1.0).build()
        marketPlaceOrder = mockMarketPlaceOrder()
        marketPlaceOrder.orderState = "DELIVERED"
        marketPlaceOrder.addMarketPlaceItem(itemId1, externalItemId1, "test item", 1, instanceId1, itemIdentifier1, marketPlaceItemPriceInfo, SubstitutionOption.DO_NOT_SUBSTITUTE)
        marketPlaceOrder.addMarketPlaceItem(itemId2, externalItemId2, "test item", 2, instanceId2, itemIdentifier2, marketPlaceItemPriceInfo, SubstitutionOption.DO_NOT_SUBSTITUTE)

        when:
        marketPlaceOrder.cancel()

        then:
        thrown(OMSBadRequestException)
    }

    def "Unsuccessful cancel Marketplace order Order state as null"() {
        given:
        ItemIdentifier itemIdentifier1 = ItemIdentifier.builder().itemId(externalItemId1).itemType("CIN").build()
        ItemIdentifier itemIdentifier2 = ItemIdentifier.builder().itemId(externalItemId2).itemType("CIN").build()
        MarketPlaceItemPriceInfo marketPlaceItemPriceInfo = MarketPlaceItemPriceInfo.builder().totalPrice(1.0).baseTotalPrice(1.0).unitPrice(1.0).baseUnitPrice(1.0).build()
        marketPlaceOrder = mockMarketPlaceOrder()
        marketPlaceOrder.orderState = null
        marketPlaceOrder.addMarketPlaceItem(itemId1, externalItemId1, "test item", 1, instanceId1, itemIdentifier1, marketPlaceItemPriceInfo, SubstitutionOption.DO_NOT_SUBSTITUTE)
        marketPlaceOrder.addMarketPlaceItem(itemId2, externalItemId2, "test item", 2, instanceId2, itemIdentifier2, marketPlaceItemPriceInfo, SubstitutionOption.DO_NOT_SUBSTITUTE)

        when:
        marketPlaceOrder.cancel()

        then:
        thrown(OMSBadRequestException)
    }

    def "Successfully pick complete Marketplace order"() {
        given:
        ItemIdentifier itemIdentifier1 = ItemIdentifier.builder().itemId(externalItemId1).itemType("CIN").build()
        ItemIdentifier itemIdentifier2 = ItemIdentifier.builder().itemId(externalItemId2).itemType("CIN").build()
        MarketPlaceItemPriceInfo marketPlaceItemPriceInfo = MarketPlaceItemPriceInfo.builder().totalPrice(1.0).baseTotalPrice(1.0).unitPrice(1.0).baseUnitPrice(1.0).build()
        marketPlaceOrder = mockMarketPlaceOrder()
        marketPlaceOrder.addMarketPlaceItem(itemId1, externalItemId1, "test item", 1, instanceId1, itemIdentifier1, marketPlaceItemPriceInfo, SubstitutionOption.DO_NOT_SUBSTITUTE)
        marketPlaceOrder.addMarketPlaceItem(itemId2, externalItemId2, "test item", 2, instanceId2, itemIdentifier2, marketPlaceItemPriceInfo, SubstitutionOption.DO_NOT_SUBSTITUTE)

        when:
        marketPlaceOrder.pickCompleteOrder()

        then:
        marketPlaceOrder.orderState == "PICK_COMPLETE"
    }

    def "Successfully delivered Marketplace order"() {
        given:
        ItemIdentifier itemIdentifier1 = ItemIdentifier.builder().itemId(externalItemId1).itemType("CIN").build()
        ItemIdentifier itemIdentifier2 = ItemIdentifier.builder().itemId(externalItemId2).itemType("CIN").build()
        MarketPlaceItemPriceInfo marketPlaceItemPriceInfo = MarketPlaceItemPriceInfo.builder().totalPrice(1.0).baseTotalPrice(1.0).unitPrice(1.0).baseUnitPrice(1.0).build()
        marketPlaceOrder = mockMarketPlaceOrder()
        marketPlaceOrder.addMarketPlaceItem(itemId1, externalItemId1, "test item", 1, instanceId1, itemIdentifier1, marketPlaceItemPriceInfo, SubstitutionOption.DO_NOT_SUBSTITUTE)
        marketPlaceOrder.addMarketPlaceItem(itemId2, externalItemId2, "test item", 2, instanceId2, itemIdentifier2, marketPlaceItemPriceInfo, SubstitutionOption.DO_NOT_SUBSTITUTE)

        when:
        marketPlaceOrder.markOrderAsDelivered()

        then:
        marketPlaceOrder.orderState == "DELIVERED"
    }

    def "Successfully confirmed at store Marketplace order"() {
        given:
        ItemIdentifier itemIdentifier1 = ItemIdentifier.builder().itemId(externalItemId1).itemType("CIN").build()
        ItemIdentifier itemIdentifier2 = ItemIdentifier.builder().itemId(externalItemId2).itemType("CIN").build()
        MarketPlaceItemPriceInfo marketPlaceItemPriceInfo = MarketPlaceItemPriceInfo.builder().totalPrice(1.0).baseTotalPrice(1.0).unitPrice(1.0).baseUnitPrice(1.0).build()
        marketPlaceOrder = mockMarketPlaceOrder()
        marketPlaceOrder.addMarketPlaceItem(itemId1, externalItemId1, "test item", 1, instanceId1, itemIdentifier1, marketPlaceItemPriceInfo, SubstitutionOption.DO_NOT_SUBSTITUTE)
        marketPlaceOrder.addMarketPlaceItem(itemId2, externalItemId2, "test item", 2, instanceId2, itemIdentifier2, marketPlaceItemPriceInfo, SubstitutionOption.DO_NOT_SUBSTITUTE)

        when:
        marketPlaceOrder.orderConfirmedAtStore()

        then:
        marketPlaceOrder.orderState == "RECD_AT_STORE"
    }

    def "Unsuccessful pick complete Marketplace order"() {
        given:
        ItemIdentifier itemIdentifier1 = ItemIdentifier.builder().itemId(externalItemId1).itemType("CIN").build()
        ItemIdentifier itemIdentifier2 = ItemIdentifier.builder().itemId(externalItemId2).itemType("CIN").build()
        MarketPlaceItemPriceInfo marketPlaceItemPriceInfo = MarketPlaceItemPriceInfo.builder().totalPrice(1.0).baseTotalPrice(1.0).unitPrice(1.0).baseUnitPrice(1.0).build()
        marketPlaceOrder = mockMarketPlaceOrder()
        marketPlaceOrder.orderState = null
        marketPlaceOrder.addMarketPlaceItem(itemId1, externalItemId1, "test item", 1, instanceId1, itemIdentifier1, marketPlaceItemPriceInfo, SubstitutionOption.DO_NOT_SUBSTITUTE)
        marketPlaceOrder.addMarketPlaceItem(itemId2, externalItemId2, "test item", 2, instanceId2, itemIdentifier2, marketPlaceItemPriceInfo, SubstitutionOption.DO_NOT_SUBSTITUTE)

        when:
        marketPlaceOrder.pickCompleteOrder()

        then:
        thrown(OMSBadRequestException)
    }

    def "Unsuccessful delivered Marketplace order"() {
        given:
        ItemIdentifier itemIdentifier1 = ItemIdentifier.builder().itemId(externalItemId1).itemType("CIN").build()
        ItemIdentifier itemIdentifier2 = ItemIdentifier.builder().itemId(externalItemId2).itemType("CIN").build()
        MarketPlaceItemPriceInfo marketPlaceItemPriceInfo = MarketPlaceItemPriceInfo.builder().totalPrice(1.0).baseTotalPrice(1.0).unitPrice(1.0).baseUnitPrice(1.0).build()
        marketPlaceOrder = mockMarketPlaceOrder()
        marketPlaceOrder.orderState = "CANCELLED"
        marketPlaceOrder.addMarketPlaceItem(itemId1, externalItemId1, "test item", 1, instanceId1, itemIdentifier1, marketPlaceItemPriceInfo, SubstitutionOption.DO_NOT_SUBSTITUTE)
        marketPlaceOrder.addMarketPlaceItem(itemId2, externalItemId2, "test item", 2, instanceId2, itemIdentifier2, marketPlaceItemPriceInfo, SubstitutionOption.DO_NOT_SUBSTITUTE)

        when:
        marketPlaceOrder.markOrderAsDelivered()

        then:
        thrown(OMSBadRequestException)
    }

    def "Unsuccessful order confirmed at store Marketplace order"() {
        given:
        ItemIdentifier itemIdentifier1 = ItemIdentifier.builder().itemId(externalItemId1).itemType("CIN").build()
        ItemIdentifier itemIdentifier2 = ItemIdentifier.builder().itemId(externalItemId2).itemType("CIN").build()
        MarketPlaceItemPriceInfo marketPlaceItemPriceInfo = MarketPlaceItemPriceInfo.builder().totalPrice(1.0).baseTotalPrice(1.0).unitPrice(1.0).baseUnitPrice(1.0).build()
        marketPlaceOrder = mockMarketPlaceOrder()
        marketPlaceOrder.orderState = "CANCELLED"
        marketPlaceOrder.addMarketPlaceItem(itemId1, externalItemId1, "test item", 1, instanceId1, itemIdentifier1, marketPlaceItemPriceInfo, SubstitutionOption.DO_NOT_SUBSTITUTE)
        marketPlaceOrder.addMarketPlaceItem(itemId2, externalItemId2, "test item", 2, instanceId2, itemIdentifier2, marketPlaceItemPriceInfo, SubstitutionOption.DO_NOT_SUBSTITUTE)

        when:
        marketPlaceOrder.orderConfirmedAtStore()

        then:
        thrown(OMSBadRequestException)
    }

    MarketPlaceOrder mockMarketPlaceOrder() {

        Vendor vendor = Vendor.UBEREATS
        String createdOrderState = "CREATED"
        String storeId = "4401"

        MarketPlaceOrderContactInfo marketPlaceOrderContactInfo = getMarketPlaceOrderContactInfo()
        MarketPlaceOrderPaymentInfo marketPlaceOrderPaymentInfo = getMarketPlaceOrderPaymentInfo()

        MarketPlaceOrder marketPlaceOrder = MarketPlaceOrder.builder()
                .vendorOrderId(externalOrderId)
                .orderDueTime(new Date())
                .id(sourceOrderId)
                .orderState(createdOrderState)
                .marketPlaceItems([])
                .sourceModifiedDate(new Date())
                .storeId(storeId).vendorId(vendor)
                .paymentInfo(marketPlaceOrderPaymentInfo)
                .marketPlaceOrderContactInfo(marketPlaceOrderContactInfo).build()
        return marketPlaceOrder
    }

    private static MarketPlaceOrderContactInfo getMarketPlaceOrderContactInfo() {
        String customerFirstName = "John"
        String customerLastName = "Doe"
        return MarketPlaceOrderContactInfo.builder()
                .firstName(customerFirstName)
                .lastName(customerLastName)
                .build()
    }

    private static MarketPlaceOrderPaymentInfo getMarketPlaceOrderPaymentInfo() {
        return MarketPlaceOrderPaymentInfo.builder()
                .bagFee(new Money(new BigDecimal(1.0), Currency.GBP))
                .tax(new Money(new BigDecimal(2.0), Currency.GBP))
                .total(new Money(new BigDecimal(100.0), Currency.GBP))
                .totalFee(new Money(new BigDecimal(1.0), Currency.GBP))
                .totalFeeTax(new Money(new BigDecimal(0.5), Currency.GBP))
                .subTotal(new Money(new BigDecimal(95.5), Currency.GBP))
                .build()
    }

}
