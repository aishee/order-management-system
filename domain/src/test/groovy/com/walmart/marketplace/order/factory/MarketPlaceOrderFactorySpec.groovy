package com.walmart.marketplace.order.factory

import com.walmart.common.domain.type.Currency
import com.walmart.common.domain.type.SubstitutionOption
import com.walmart.marketplace.order.aggregateroot.MarketPlaceOrder
import com.walmart.marketplace.order.domain.entity.type.Vendor
import com.walmart.marketplace.order.domain.valueobject.*
import com.walmart.marketplace.order.gateway.IMarketPlaceGatewayFinder
import com.walmart.marketplace.order.repository.IMarketPlaceGateWay
import com.walmart.marketplace.order.repository.IMarketPlaceRepository
import spock.lang.Specification

class MarketPlaceOrderFactorySpec extends Specification {

    MarketPlaceOrderFactory marketPlaceOrderFactory

    IMarketPlaceRepository marketPlaceRepository = Mock()

    IMarketPlaceGatewayFinder marketPlaceGatewayFinder = Mock()

    IMarketPlaceGateWay marketPlaceGateWay = Mock()


    def setup() {
        marketPlaceOrderFactory = new MarketPlaceOrderFactory(
                marketPlaceRepository: marketPlaceRepository,
                marketPlaceGatewayFinder: marketPlaceGatewayFinder)
    }

    def "GetMarketPlaceOrderFromGateway"() {
        given:
        String externalOrderId = UUID.randomUUID().toString()
        String externalNativeOrderId = UUID.randomUUID().toString()
        String itemId = UUID.randomUUID().toString()
        Vendor vendor = Vendor.UBEREATS
        String createdOrderState = "CREATED"
        String storeId = "4401"
        String externalItemId = UUID.randomUUID().toString()
        String customerFirstName = "John"
        String customerLastName = "Doe"
        String resourceUrl = "http://localhost:8080"
        String instanceId = UUID.randomUUID().toString()
        ItemIdentifier itemIdentifier = ItemIdentifier.builder().itemId(externalItemId).itemType("CIN").build()
        MarketPlaceItemPriceInfo marketPlaceItemPriceInfo = MarketPlaceItemPriceInfo.builder().totalPrice(1.0).baseTotalPrice(1.0).unitPrice(1.0).baseUnitPrice(1.0).build()

        MarketPlaceOrderContactInfo marketPlaceOrderContactInfo = MarketPlaceOrderContactInfo.builder().firstName(customerFirstName).lastName(customerLastName).build()

        MarketPlaceOrder marketPlaceOrder = MarketPlaceOrder.builder()
                .vendorOrderId(externalOrderId)
                .vendorNativeOrderId(externalNativeOrderId)
                .orderDueTime(new Date())
                .orderState(createdOrderState)
                .marketPlaceItems([])
                .sourceModifiedDate(new Date())
                .storeId(storeId).vendorId(vendor)
                .marketPlaceOrderContactInfo(marketPlaceOrderContactInfo).build()

        marketPlaceOrder.addMarketPlaceItem(itemId, externalItemId, "test item", 1, instanceId, itemIdentifier, marketPlaceItemPriceInfo, SubstitutionOption.DO_NOT_SUBSTITUTE)

        when:
        MarketPlaceOrder actualOrder = marketPlaceOrderFactory.getMarketPlaceOrderFromGateway(externalOrderId, resourceUrl, Vendor.UBEREATS)

        then:
        1 * marketPlaceRepository.get(_ as String) >> null
        1 * marketPlaceGatewayFinder.getMarketPlaceGateway(_ as Vendor) >> { Vendor _vendor ->
            assert _vendor == vendor
            return marketPlaceGateWay

        }
        1 * marketPlaceGateWay.getOrder(_ as String, _ as String) >> { String _vendorId, String _resourceUrl ->
            return marketPlaceOrder
        }

        assert actualOrder.vendorOrderId == marketPlaceOrder.vendorOrderId

    }

    def "GetMarketPlaceOrderFromGateway when order is returned from db"() {
        given:
        String externalOrderId = UUID.randomUUID().toString()
        String externalNativeOrderId = UUID.randomUUID().toString()
        String itemId = UUID.randomUUID().toString()
        Vendor vendor = Vendor.UBEREATS;
        String createdOrderState = "CREATED"
        String storeId = "4401"
        String externalItemId = UUID.randomUUID().toString()
        String customerFirstName = "John"
        String customerLastName = "Doe"
        String resourceUrl = "http://localhost:8080"
        String instanceId = UUID.randomUUID().toString()
        ItemIdentifier itemIdentifier = ItemIdentifier.builder().itemId(externalItemId).itemType("CIN").build()
        MarketPlaceItemPriceInfo marketPlaceItemPriceInfo = MarketPlaceItemPriceInfo.builder().totalPrice(1.0).baseTotalPrice(1.0).unitPrice(1.0).baseUnitPrice(1.0).build()

        MarketPlaceOrderContactInfo marketPlaceOrderContactInfo = MarketPlaceOrderContactInfo.builder().firstName(customerFirstName).lastName(customerLastName).build()

        MarketPlaceOrder persistedOrder = MarketPlaceOrder.builder()
                .vendorOrderId(externalOrderId)
                .vendorNativeOrderId(externalNativeOrderId)
                .orderDueTime(new Date())
                .orderState(createdOrderState)
                .marketPlaceItems([])
                .sourceModifiedDate(new Date())
                .storeId(storeId).vendorId(vendor)
                .marketPlaceOrderContactInfo(marketPlaceOrderContactInfo).build()

        persistedOrder.addMarketPlaceItem(itemId, externalItemId, "test item", 1, instanceId, itemIdentifier, marketPlaceItemPriceInfo, SubstitutionOption.DO_NOT_SUBSTITUTE)

        when:
        MarketPlaceOrder actualOrder = marketPlaceOrderFactory.getMarketPlaceOrderFromGateway(externalOrderId, resourceUrl, Vendor.UBEREATS)

        then:
        1 * marketPlaceRepository.get(_ as String) >> persistedOrder
        0 * marketPlaceGatewayFinder.getMarketPlaceGateway(_ as Vendor) >> { Vendor _vendor ->
            assert _vendor == vendor
            return marketPlaceGateWay

        }
        assert actualOrder.vendorOrderId == persistedOrder.vendorOrderId

    }

    def "Test GetMarketPlaceOrderFromCommand"() {

        given:
        String externalOrderId = UUID.randomUUID().toString()
        String externalNativeOrderId = UUID.randomUUID().toString()
        String itemId = UUID.randomUUID().toString()
        Vendor vendor = Vendor.UBEREATS;
        String createdOrderState = "CREATED"
        String storeId = "4401"
        String externalItemId = UUID.randomUUID().toString()
        String customerFirstName = "John"
        String customerLastName = "Doe"
        String vendorStoreId = "a48c2238-5f8d-48ad-917b-b91e2d577cdc"
        String instanceId = UUID.randomUUID().toString()
        ItemIdentifier itemIdentifier = ItemIdentifier.builder().itemId(externalItemId).itemType("CIN").build()
        MarketPlaceItemPriceInfo marketPlaceItemPriceInfo = MarketPlaceItemPriceInfo.builder().totalPrice(1.0).baseTotalPrice(1.0).unitPrice(1.0).baseUnitPrice(1.0).build()

        MarketPlaceOrderContactInfo marketPlaceOrderContactInfo = MarketPlaceOrderContactInfo.builder().firstName(customerFirstName).lastName(customerLastName).build()

        MarketPlaceOrderPaymentInfo marketPlaceOrderPaymentInfo = MarketPlaceOrderPaymentInfo.builder()
                .total(new Money(50.0, Currency.GBP))
                .subTotal(new Money(45.0, Currency.GBP))
                .tax(new Money(5.0, Currency.GBP))
                .build()

        MarketPlaceOrder marketPlaceOrder = MarketPlaceOrder.builder()
                .vendorOrderId(externalOrderId)
                .vendorNativeOrderId(externalNativeOrderId)
                .orderDueTime(new Date())
                .orderState(createdOrderState)
                .marketPlaceItems([])
                .sourceModifiedDate(new Date())
                .storeId(storeId)
                .vendorStoreId(vendorStoreId)
                .vendorId(vendor)
                .marketPlaceOrderContactInfo(marketPlaceOrderContactInfo).build()

        marketPlaceOrder.addMarketPlaceItem(itemId, externalItemId, "test item", 1, instanceId, itemIdentifier, marketPlaceItemPriceInfo, SubstitutionOption.DO_NOT_SUBSTITUTE)

        when:
        MarketPlaceOrder actualOrder = marketPlaceOrderFactory.getMarketPlaceOrderFromCommand(externalOrderId, externalOrderId, customerFirstName, customerLastName, storeId, vendorStoreId, new Date(), vendor,
                new Date(), marketPlaceOrderPaymentInfo)

        then:
        1 * marketPlaceRepository.get(_ as String) >> null
        assert actualOrder.vendorOrderId == marketPlaceOrder.vendorOrderId
        assert actualOrder.storeId == storeId

    }

    def "Test GetMarketPlaceOrderFromCommand with existing order"() {

        given:
        String externalOrderId = UUID.randomUUID().toString()
        String externalNativeOrderId = UUID.randomUUID().toString()
        String itemId = UUID.randomUUID().toString()
        Vendor vendor = Vendor.UBEREATS
        String createdOrderState = "CREATED"
        String storeId = "4401"
        String externalItemId = UUID.randomUUID().toString()
        String customerFirstName = "John"
        String customerLastName = "Doe"
        String instanceId = UUID.randomUUID().toString()
        String vendorStoreId = "a48c2238-5f8d-48ad-917b-b91e2d577cdc"
        ItemIdentifier itemIdentifier = ItemIdentifier.builder().itemId(externalItemId).itemType("CIN").build()
        MarketPlaceItemPriceInfo marketPlaceItemPriceInfo = MarketPlaceItemPriceInfo.builder().totalPrice(1.0).baseTotalPrice(1.0).unitPrice(1.0).baseUnitPrice(1.0).build()

        MarketPlaceOrderContactInfo marketPlaceOrderContactInfo = MarketPlaceOrderContactInfo.builder().firstName(customerFirstName).lastName(customerLastName).build()

        MarketPlaceOrderPaymentInfo marketPlaceOrderPaymentInfo = MarketPlaceOrderPaymentInfo.builder()
                .total(new Money(50.0, Currency.GBP))
                .subTotal(new Money(45.0, Currency.GBP))
                .tax(new Money(5.0, Currency.GBP))
                .build()

        MarketPlaceOrder persistedOrder = MarketPlaceOrder.builder()
                .vendorOrderId(externalOrderId)
                .vendorNativeOrderId(externalNativeOrderId)
                .orderDueTime(new Date())
                .orderState(createdOrderState)
                .marketPlaceItems([])
                .sourceModifiedDate(new Date())
                .storeId(storeId).vendorId(vendor)
                .paymentInfo(marketPlaceOrderPaymentInfo)
                .marketPlaceOrderContactInfo(marketPlaceOrderContactInfo).build()

        persistedOrder.addMarketPlaceItem(itemId, externalItemId, "test item", 1, instanceId, itemIdentifier, marketPlaceItemPriceInfo, SubstitutionOption.DO_NOT_SUBSTITUTE)

        when:
        marketPlaceOrderFactory.getMarketPlaceOrderFromCommand(externalOrderId, externalNativeOrderId, customerFirstName, customerLastName, storeId, vendorStoreId, new Date(), vendor,
                new Date(), marketPlaceOrderPaymentInfo)

        then:
        1 * marketPlaceRepository.get(_ as String) >> persistedOrder
        thrown(RuntimeException)

    }

    def "Test Payment Info"() {

        given:
        Money total = new Money(35.0, Currency.GBP)
        Money tax = new Money(10.0, Currency.GBP)
        Money totalFee = new Money(3.0, Currency.GBP)
        Money totalFeeTax = new Money(5.0, Currency.GBP)
        Money bagFee = new Money(2.0, Currency.GBP)
        Money subTotal = new Money(30.0, Currency.GBP)

        when:
        MarketPlaceOrderPaymentInfo marketPlaceOrderPaymentInfo = marketPlaceOrderFactory.getPaymentInfo(total, subTotal, tax, totalFee, totalFeeTax, bagFee)

        then:
        assert marketPlaceOrderPaymentInfo.subTotal == subTotal
        assert marketPlaceOrderPaymentInfo.bagFee == bagFee
        assert marketPlaceOrderPaymentInfo.tax == tax
        assert marketPlaceOrderPaymentInfo.totalFeeTax == totalFeeTax
        assert marketPlaceOrderPaymentInfo.total == total


    }


}
