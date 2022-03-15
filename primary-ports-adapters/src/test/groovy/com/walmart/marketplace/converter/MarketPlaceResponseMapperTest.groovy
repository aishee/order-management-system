package com.walmart.marketplace.converter

import com.walmart.common.domain.type.Currency
import com.walmart.common.domain.type.SubstitutionOption
import com.walmart.marketplace.dto.response.MarketPlaceOrderResponse
import com.walmart.marketplace.order.aggregateroot.MarketPlaceOrder
import com.walmart.marketplace.order.domain.entity.MarketPlaceBundledItem
import com.walmart.marketplace.order.domain.entity.MarketPlaceItem
import com.walmart.marketplace.order.domain.entity.type.Vendor
import com.walmart.marketplace.order.domain.valueobject.ItemIdentifier
import com.walmart.marketplace.order.domain.valueobject.MarketPlaceItemPriceInfo
import com.walmart.marketplace.order.domain.valueobject.MarketPlaceOrderContactInfo
import com.walmart.marketplace.order.domain.valueobject.MarketPlaceOrderPaymentInfo
import com.walmart.marketplace.order.domain.valueobject.Money
import spock.lang.Specification

class MarketPlaceResponseMapperTest extends Specification {

    MarketPlaceResponseMapper marketPlaceResponseMapper

    def setup() {

        marketPlaceResponseMapper = new MarketPlaceResponseMapper()
    }

    def "Test MapToDto when all child entities are present"() {

        given:
        String externalOrderId = UUID.randomUUID().toString()
        String itemId = UUID.randomUUID().toString()
        Vendor vendor = Vendor.UBEREATS
        String createdOrderState = "CREATED"
        String storeId = "4401"
        String externalItemId = UUID.randomUUID().toString()
        String customerFirstName = "John"
        String customerLastName = "Doe"
        String instanceId = UUID.randomUUID().toString()

        ItemIdentifier itemIdentifier = ItemIdentifier.builder().itemId(externalItemId).itemType("CIN").build()
        MarketPlaceItemPriceInfo marketPlaceItemPriceInfo = MarketPlaceItemPriceInfo.builder().totalPrice(1.0).baseTotalPrice(1.0).unitPrice(1.0).baseUnitPrice(1.0).build()

        MarketPlaceOrderContactInfo marketPlaceOrderContactInfo = MarketPlaceOrderContactInfo.builder().firstName(customerFirstName).lastName(customerLastName).build()
        MarketPlaceOrderPaymentInfo marketPlaceOrderPaymentInfo = MarketPlaceOrderPaymentInfo.builder()
                .bagFee(new Money(new BigDecimal(1.0), Currency.GBP))
                .tax(new Money(new BigDecimal(2.0), Currency.GBP))
                .total(new Money(new BigDecimal(100.0), Currency.GBP))
                .totalFee(new Money(new BigDecimal(1.0), Currency.GBP))
                .totalFeeTax(new Money(new BigDecimal(0.5), Currency.GBP))
                .subTotal(new Money(new BigDecimal(95.5), Currency.GBP))
                .build()

        MarketPlaceOrder marketPlaceOrder = MarketPlaceOrder.builder()
                .vendorOrderId(externalOrderId)
                .orderDueTime(new Date())
                .orderState(createdOrderState)
                .marketPlaceItems([])
                .sourceModifiedDate(new Date())
                .storeId(storeId).vendorId(vendor)
                .paymentInfo(marketPlaceOrderPaymentInfo)
                .marketPlaceOrderContactInfo(marketPlaceOrderContactInfo).build()

        marketPlaceOrder.addMarketPlaceItem(itemId, externalItemId, "test item", 1, instanceId, itemIdentifier, marketPlaceItemPriceInfo, SubstitutionOption.SUBSTITUTE)

        when:
        MarketPlaceOrderResponse omsOrderResponse = marketPlaceResponseMapper.mapToDto(marketPlaceOrder)

        then:

        assert omsOrderResponse.getData().getExternalOrderId() == externalOrderId
        assert omsOrderResponse.getData().getMarketPlaceItems().size() == 1
        assert omsOrderResponse.getData().getFirstName() == "John"
        assert omsOrderResponse.getData().getOrderStatus() == "CREATED"
        assert omsOrderResponse.getData().getStoreId() == storeId
        assert omsOrderResponse.getData().getMarketPlaceItems().get(0).getSubstitutionOption() == SubstitutionOption.SUBSTITUTE
    }

    def "Test MapToDto when all child entities are not present"() {

        given:
        String externalOrderId = UUID.randomUUID().toString()
        String itemId = UUID.randomUUID().toString()
        Vendor vendor = Vendor.UBEREATS
        String createdOrderState = "CREATED"
        String storeId = "4401"
        String externalItemId = UUID.randomUUID().toString()
        String customerFirstName = "John"
        String customerLastName = "Doe"
        String instanceId = UUID.randomUUID().toString()

        ItemIdentifier itemIdentifier = ItemIdentifier.builder().itemId(externalItemId).itemType("CIN").build()
        MarketPlaceItemPriceInfo marketPlaceItemPriceInfo = MarketPlaceItemPriceInfo.builder().totalPrice(1.0).baseTotalPrice(1.0).unitPrice(1.0).baseUnitPrice(1.0).build()

        MarketPlaceOrderContactInfo marketPlaceOrderContactInfo = MarketPlaceOrderContactInfo.builder().firstName(customerFirstName).lastName(customerLastName).build()

        MarketPlaceOrder marketPlaceOrder = MarketPlaceOrder.builder()
                .vendorOrderId(externalOrderId)
                .orderDueTime(new Date())
                .orderState(createdOrderState)
                .marketPlaceItems([])
                .sourceModifiedDate(new Date())
                .storeId(storeId).vendorId(vendor)
                .marketPlaceOrderContactInfo(marketPlaceOrderContactInfo).build()

        marketPlaceOrder.addMarketPlaceItem(itemId, externalItemId, "test item", 1, instanceId, itemIdentifier, marketPlaceItemPriceInfo, SubstitutionOption.DO_NOT_SUBSTITUTE)

        when:
        MarketPlaceOrderResponse omsOrderResponse = marketPlaceResponseMapper.mapToDto(marketPlaceOrder)

        then:

        assert omsOrderResponse.getData().getExternalOrderId() == externalOrderId
        assert omsOrderResponse.getData().getMarketPlaceItems().size() == 1
        assert omsOrderResponse.getData().getFirstName() == "John"
        assert omsOrderResponse.getData().getMarketPlaceItems().get(0).getSubstitutionOption() == SubstitutionOption.DO_NOT_SUBSTITUTE
    }

    def "Test MapToDto when all child entities are present Bundles"() {

        given:
        String externalOrderId = UUID.randomUUID().toString()
        String itemId = UUID.randomUUID().toString()
        Vendor vendor = Vendor.UBEREATS
        String createdOrderState = "CREATED"
        String storeId = "4401"
        String externalItemId = UUID.randomUUID().toString()
        String customerFirstName = "John"
        String customerLastName = "Doe"
        String instanceId = UUID.randomUUID().toString()

        ItemIdentifier itemIdentifier = ItemIdentifier.builder().itemId(externalItemId).itemType("CIN").build()
        MarketPlaceItemPriceInfo marketPlaceItemPriceInfo = MarketPlaceItemPriceInfo.builder().totalPrice(1.0).baseTotalPrice(1.0).unitPrice(1.0).baseUnitPrice(1.0).build()

        MarketPlaceOrderContactInfo marketPlaceOrderContactInfo = MarketPlaceOrderContactInfo.builder().firstName(customerFirstName).lastName(customerLastName).build()
        MarketPlaceOrderPaymentInfo marketPlaceOrderPaymentInfo = MarketPlaceOrderPaymentInfo.builder()
                .bagFee(new Money(new BigDecimal(1.0), Currency.GBP))
                .tax(new Money(new BigDecimal(2.0), Currency.GBP))
                .total(new Money(new BigDecimal(100.0), Currency.GBP))
                .totalFee(new Money(new BigDecimal(1.0), Currency.GBP))
                .totalFeeTax(new Money(new BigDecimal(0.5), Currency.GBP))
                .subTotal(new Money(new BigDecimal(95.5), Currency.GBP))
                .build()

        MarketPlaceOrder marketPlaceOrder = MarketPlaceOrder.builder()
                .vendorOrderId(externalOrderId)
                .orderDueTime(new Date())
                .orderState(createdOrderState)
                .marketPlaceItems([])
                .sourceModifiedDate(new Date())
                .storeId(storeId).vendorId(vendor)
                .paymentInfo(marketPlaceOrderPaymentInfo)
                .marketPlaceOrderContactInfo(marketPlaceOrderContactInfo).build()

        MarketPlaceItem marketPlaceItem = new MarketPlaceItem(marketPlaceOrder, itemId, externalOrderId, "test", instanceId, itemIdentifier, 2, marketPlaceItemPriceInfo, getBundledItemList(), SubstitutionOption.CANCEL_ENTIRE_ORDER)
        List<MarketPlaceItem> list = new ArrayList<>()
        list.add(marketPlaceItem)
        marketPlaceOrder.addMarketPlaceItems(list)

        when:
        MarketPlaceOrderResponse omsOrderResponse = marketPlaceResponseMapper.mapToDto(marketPlaceOrder)

        then:
        assert omsOrderResponse.getData().getExternalOrderId() == externalOrderId
        assert omsOrderResponse.getData().getMarketPlaceItems().size() == 1
        assert omsOrderResponse.getData().getMarketPlaceItems().get(0).getBundledItems().size() == 1
        assert omsOrderResponse.getData().getFirstName() == "John"
        assert omsOrderResponse.getData().getOrderStatus() == "CREATED"
        assert omsOrderResponse.getData().getStoreId() == storeId
        assert omsOrderResponse.getData().getMarketPlaceItems().get(0).getSubstitutionOption() == SubstitutionOption.CANCEL_ENTIRE_ORDER
    }

    private static List<MarketPlaceBundledItem> getBundledItemList() {
        return Arrays.asList(MarketPlaceBundledItem.builder()
                .bundleInstanceId(UUID.randomUUID().toString())
                .bundleSkuId("123")
                .bundleQuantity(1)
                .bundleDescription("test")
                .build())
    }
}
