package com.walmart.marketplace.controller

import com.walmart.common.domain.type.SubstitutionOption
import com.walmart.common.metrics.MetricService
import com.walmart.common.domain.type.Currency
import com.walmart.marketplace.MarketPlaceApplicationService
import com.walmart.marketplace.commands.MarketPlaceCreateOrderCommand
import com.walmart.marketplace.converter.MarketPlaceResponseMapper
import com.walmart.marketplace.converter.RequestToCommandMapper
import com.walmart.marketplace.dto.request.CreateMarketPlaceOrderRequest
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

class MarketPlaceControllerTest extends Specification {

    MarketPlaceController marketPlaceController

    RequestToCommandMapper mapper = Spy()

    MarketPlaceResponseMapper responseMapper = Spy()

    MarketPlaceApplicationService marketPlaceApplicationService = Mock()

    MetricService metricService = Mock()

    String externalOrderId = UUID.randomUUID().toString()

    String externalItemId = UUID.randomUUID().toString()

    String itemId1 = "45727828"
    String itemId2 = "45727828"

    String storeId = "4401"

    String expectedOrderId = UUID.randomUUID().toString()

    String instanceId = UUID.randomUUID().toString()

    String bundleInstanceId = UUID.randomUUID().toString()


    def setup() {
        marketPlaceController = new MarketPlaceController(
                mapper: mapper,
                responseMapper: responseMapper,
                marketPlaceApplicationService: marketPlaceApplicationService,
                metricService: metricService)
    }

    def "Test CreateOrder"() {

        given:
        CreateMarketPlaceOrderRequest createMarketPlaceOrderRequest = CreateMarketPlaceOrderRequest.builder().data(
                CreateMarketPlaceOrderRequest.CreateMarketPlaceOrderRequestData.builder()
                        .storeId(storeId)
                        .firstName("John")
                        .lastName("Doe")
                        .vendor(Vendor.UBEREATS)
                        .externalOrderId(externalOrderId)
                        .payment(getPaymentInfo())
                        .sourceOrderCreationTime(new Date())
                        .marketPlaceItems(Arrays.asList(getMarketPlaceRequestItemData(itemId1)))
                        .build()).build()


        ItemIdentifier itemIdentifier = ItemIdentifier.builder().itemId(itemId1).itemType("CIN").build()
        MarketPlaceItemPriceInfo marketPlaceItemPriceInfo = MarketPlaceItemPriceInfo.builder().totalPrice(1.0).baseTotalPrice(1.0).unitPrice(1.0).baseUnitPrice(1.0).build()

        MarketPlaceOrder marketPlaceOrder = getMarketPlaceOrder()
        marketPlaceOrder.addMarketPlaceItem(UUID.randomUUID().toString(), externalItemId, "test item", 1, instanceId, itemIdentifier, marketPlaceItemPriceInfo, SubstitutionOption.DO_NOT_SUBSTITUTE)

        when:
        MarketPlaceOrderResponse marketPlaceOrderResponse = marketPlaceController.createOrder(createMarketPlaceOrderRequest)

        then:
        1 * marketPlaceApplicationService.createAndProcessMarketPlaceOrder(_ as MarketPlaceCreateOrderCommand) >> { MarketPlaceCreateOrderCommand _marketPlaceCreateOrderCommand ->
            _marketPlaceCreateOrderCommand.storeId == "4401"
            _marketPlaceCreateOrderCommand.payment.total.amount == 50.0
            _marketPlaceCreateOrderCommand.payment.totalFee.amount == 5.0
            _marketPlaceCreateOrderCommand.payment.bagFee.amount == 2.5
            _marketPlaceCreateOrderCommand.payment.totalFeeTax.amount == 1.25
            _marketPlaceCreateOrderCommand.payment.subTotal.amount == 45.0
            _marketPlaceCreateOrderCommand.firstName == "John"
            _marketPlaceCreateOrderCommand.lastName == "Doe"
            _marketPlaceCreateOrderCommand.marketPlaceItems.size() == 1
            _marketPlaceCreateOrderCommand.marketPlaceItems[0].itemId == itemId1
            _marketPlaceCreateOrderCommand.marketPlaceItems[0].itemDescription == "Test description"
            _marketPlaceCreateOrderCommand.marketPlaceItems[0].totalPrice == Double.valueOf(5.0)
            _marketPlaceCreateOrderCommand.marketPlaceItems[0].unitPrice == Double.valueOf(2.5)
            _marketPlaceCreateOrderCommand.marketPlaceItems[0].baseTotalPrice == Double.valueOf(5.0)
            _marketPlaceCreateOrderCommand.marketPlaceItems[0].baseUnitPrice == Double.valueOf(2.5)
            _marketPlaceCreateOrderCommand.marketPlaceItems[0].quantity == 2
            _marketPlaceCreateOrderCommand.vendor == Vendor.UBEREATS
            _marketPlaceCreateOrderCommand.externalOrderId == externalOrderId
            return marketPlaceOrder
        }
        assert marketPlaceOrderResponse.data.externalOrderId == externalOrderId
        assert marketPlaceOrderResponse.data.storeId == "4401"
        assert marketPlaceOrderResponse.data.marketPlaceItems.size() == 1
        assert marketPlaceOrderResponse.data.marketPlaceItems[0].itemId == itemId1
        assert marketPlaceOrderResponse.data.marketPlaceItems[0].externalItemId == externalItemId
        assert marketPlaceOrderResponse.data.id == expectedOrderId
    }

    def "Test CreateOrder multiple items"() {

        given:
        CreateMarketPlaceOrderRequest createMarketPlaceOrderRequest = CreateMarketPlaceOrderRequest.builder().data(
                CreateMarketPlaceOrderRequest.CreateMarketPlaceOrderRequestData.builder()
                        .storeId(storeId)
                        .firstName("John")
                        .lastName("Doe")
                        .vendor(Vendor.UBEREATS)
                        .externalOrderId(externalOrderId)
                        .payment(getPaymentInfo())
                        .sourceOrderCreationTime(new Date())
                        .marketPlaceItems(Arrays.asList(
                                getMarketPlaceRequestItemData(itemId1),
                                getMarketPlaceRequestItemData(itemId2)
                        ))
                        .build()).build()

        ItemIdentifier itemIdentifier = ItemIdentifier.builder().itemId(itemId1).itemType("CIN").build()
        MarketPlaceItemPriceInfo marketPlaceItemPriceInfo = MarketPlaceItemPriceInfo.builder().totalPrice(1.0).baseTotalPrice(1.0).unitPrice(1.0).baseUnitPrice(1.0).build()

        MarketPlaceOrder marketPlaceOrder = getMarketPlaceOrder()
        marketPlaceOrder.addMarketPlaceItem(UUID.randomUUID().toString(), externalItemId, "test item", 1, instanceId, itemIdentifier, marketPlaceItemPriceInfo, SubstitutionOption.DO_NOT_SUBSTITUTE)

        when:
        MarketPlaceOrderResponse marketPlaceOrderResponse = marketPlaceController.createOrder(createMarketPlaceOrderRequest)

        then:
        1 * marketPlaceApplicationService.createAndProcessMarketPlaceOrder(_ as MarketPlaceCreateOrderCommand) >> { MarketPlaceCreateOrderCommand _marketPlaceCreateOrderCommand ->
            _marketPlaceCreateOrderCommand.storeId == "4401"
            _marketPlaceCreateOrderCommand.payment.total.amount == 50.0
            _marketPlaceCreateOrderCommand.payment.totalFee.amount == 5.0
            _marketPlaceCreateOrderCommand.payment.bagFee.amount == 2.5
            _marketPlaceCreateOrderCommand.payment.totalFeeTax.amount == 1.25
            _marketPlaceCreateOrderCommand.payment.subTotal.amount == 45.0
            _marketPlaceCreateOrderCommand.firstName == "John"
            _marketPlaceCreateOrderCommand.lastName == "Doe"
            _marketPlaceCreateOrderCommand.marketPlaceItems.size() == 2
            _marketPlaceCreateOrderCommand.marketPlaceItems[0].itemId == itemId1
            _marketPlaceCreateOrderCommand.marketPlaceItems[0].itemDescription == "Test description"
            _marketPlaceCreateOrderCommand.marketPlaceItems[0].totalPrice == Double.valueOf(5.0)
            _marketPlaceCreateOrderCommand.marketPlaceItems[0].unitPrice == Double.valueOf(2.5)
            _marketPlaceCreateOrderCommand.marketPlaceItems[0].baseTotalPrice == Double.valueOf(5.0)
            _marketPlaceCreateOrderCommand.marketPlaceItems[0].baseUnitPrice == Double.valueOf(2.5)
            _marketPlaceCreateOrderCommand.marketPlaceItems[0].quantity == 2
            _marketPlaceCreateOrderCommand.vendor == Vendor.UBEREATS
            _marketPlaceCreateOrderCommand.externalOrderId == externalOrderId
            return marketPlaceOrder
        }
        assert marketPlaceOrderResponse.data.externalOrderId == externalOrderId
        assert marketPlaceOrderResponse.data.storeId == "4401"
        assert marketPlaceOrderResponse.data.marketPlaceItems.size() == 1
        assert marketPlaceOrderResponse.data.marketPlaceItems[0].itemId == itemId1
        assert marketPlaceOrderResponse.data.marketPlaceItems[0].externalItemId == externalItemId
        assert marketPlaceOrderResponse.data.id == expectedOrderId
    }

    def "Test CreateOrder Bundled Items"() {

        given:
        CreateMarketPlaceOrderRequest createMarketPlaceOrderRequest = CreateMarketPlaceOrderRequest.builder().data(
                CreateMarketPlaceOrderRequest.CreateMarketPlaceOrderRequestData.builder()
                        .storeId(storeId)
                        .firstName("John")
                        .lastName("Doe")
                        .vendor(Vendor.UBEREATS)
                        .externalOrderId(externalOrderId)
                        .payment(getPaymentInfo())
                        .sourceOrderCreationTime(new Date())
                        .marketPlaceItems(Arrays.asList(CreateMarketPlaceOrderRequest.MarketPlaceRequestItemData.builder()
                                .itemId(itemId1)
                                .itemType("CIN")
                                .externalItemId(externalItemId)
                                .totalPrice(5.0)
                                .unitPrice(2.5)
                                .quantity(2)
                                .itemDescription("Test description")
                                .baseUnitPrice(2.5)
                                .baseTotalPrice(5.0)
                                .marketPlaceBundledItems(Arrays.asList(CreateMarketPlaceOrderRequest.MarketPlaceRequestBundledItemData.builder()
                                        .bundleDescription("Test description")
                                        .bundleQuantity(1).bundleInstanceId(bundleInstanceId)
                                        .bundleSkuId("123").build())).build()))
                        .build()).build()


        ItemIdentifier itemIdentifier = ItemIdentifier.builder().itemId(itemId1).itemType("CIN").build()
        MarketPlaceItemPriceInfo marketPlaceItemPriceInfo = MarketPlaceItemPriceInfo.builder().totalPrice(1.0).baseTotalPrice(1.0).unitPrice(1.0).baseUnitPrice(1.0).build()

        MarketPlaceOrder marketPlaceOrder = getMarketPlaceOrder()

        List<MarketPlaceBundledItem> marketPlaceBundledItems = Arrays.asList(MarketPlaceBundledItem.builder()
                .bundleSkuId("123")
                .bundleInstanceId(bundleInstanceId)
                .bundleQuantity(1)
                .bundleDescription("Test description")
                .build())

        MarketPlaceItem marketPlaceItem = new MarketPlaceItem(marketPlaceOrder,
                UUID.randomUUID().toString(), externalItemId, "test item", instanceId, itemIdentifier, 2, marketPlaceItemPriceInfo, marketPlaceBundledItems, SubstitutionOption.DO_NOT_SUBSTITUTE)
        marketPlaceOrder.addMarketPlaceItems(Collections.singletonList(marketPlaceItem))

        when:
        MarketPlaceOrderResponse marketPlaceOrderResponse = marketPlaceController.createOrder(createMarketPlaceOrderRequest)

        then:
        1 * marketPlaceApplicationService.createAndProcessMarketPlaceOrder(_ as MarketPlaceCreateOrderCommand) >> { MarketPlaceCreateOrderCommand _marketPlaceCreateOrderCommand ->
            _marketPlaceCreateOrderCommand.storeId == "4401"
            _marketPlaceCreateOrderCommand.payment.total.amount == 50.0
            _marketPlaceCreateOrderCommand.payment.totalFee.amount == 5.0
            _marketPlaceCreateOrderCommand.payment.bagFee.amount == 2.5
            _marketPlaceCreateOrderCommand.payment.totalFeeTax.amount == 1.25
            _marketPlaceCreateOrderCommand.payment.subTotal.amount == 45.0
            _marketPlaceCreateOrderCommand.firstName == "John"
            _marketPlaceCreateOrderCommand.lastName == "Doe"
            _marketPlaceCreateOrderCommand.marketPlaceItems.size() == 1
            _marketPlaceCreateOrderCommand.marketPlaceItems[0].itemId == itemId1
            _marketPlaceCreateOrderCommand.marketPlaceItems[0].itemDescription == "Test description"
            _marketPlaceCreateOrderCommand.marketPlaceItems[0].totalPrice == Double.valueOf(5.0)
            _marketPlaceCreateOrderCommand.marketPlaceItems[0].unitPrice == Double.valueOf(2.5)
            _marketPlaceCreateOrderCommand.marketPlaceItems[0].baseTotalPrice == Double.valueOf(5.0)
            _marketPlaceCreateOrderCommand.marketPlaceItems[0].baseUnitPrice == Double.valueOf(2.5)
            _marketPlaceCreateOrderCommand.marketPlaceItems[0].quantity == 2
            _marketPlaceCreateOrderCommand.vendor == Vendor.UBEREATS
            _marketPlaceCreateOrderCommand.externalOrderId == externalOrderId
            _marketPlaceCreateOrderCommand.marketPlaceItems[0].bundledItems.size() == 1
            _marketPlaceCreateOrderCommand.marketPlaceItems[0].bundledItems[0].bundleQuantity == 1
            return marketPlaceOrder
        }
        assert marketPlaceOrderResponse.data.externalOrderId == externalOrderId
        assert marketPlaceOrderResponse.data.storeId == "4401"
        assert marketPlaceOrderResponse.data.marketPlaceItems.size() == 1
        assert marketPlaceOrderResponse.data.marketPlaceItems[0].itemId == itemId1
        assert marketPlaceOrderResponse.data.marketPlaceItems[0].externalItemId == externalItemId
        assert marketPlaceOrderResponse.data.id == expectedOrderId
        assert marketPlaceOrderResponse.data.marketPlaceItems[0].bundledItems[0].bundleQuantity == 1
    }

    def "Test GetOrder"() {

        given:
        String vendorOrderId = UUID.randomUUID().toString()

        ItemIdentifier itemIdentifier = ItemIdentifier.builder().itemId(itemId1).itemType("CIN").build()
        MarketPlaceItemPriceInfo marketPlaceItemPriceInfo = MarketPlaceItemPriceInfo.builder().totalPrice(1.0).baseTotalPrice(1.0).unitPrice(1.0).baseUnitPrice(1.0).build()
        MarketPlaceOrder marketPlaceOrder = getMarketPlaceOrder()

        marketPlaceOrder.addMarketPlaceItem(UUID.randomUUID().toString(), externalItemId, "test item", 1, instanceId, itemIdentifier, marketPlaceItemPriceInfo, SubstitutionOption.DO_NOT_SUBSTITUTE)

        when:
        MarketPlaceOrderResponse marketPlaceOrderResponse = marketPlaceController.getOrder(vendorOrderId)

        then:
        1 * marketPlaceApplicationService.getOrder(_ as String) >> { String _vendorOrderId ->
            assert _vendorOrderId == vendorOrderId
            return marketPlaceOrder
        }

        marketPlaceOrderResponse.data.externalOrderId == externalOrderId
        marketPlaceOrderResponse.data.storeId == "4401"
        marketPlaceOrderResponse.data.id == expectedOrderId
        marketPlaceOrderResponse.data.marketPlaceItems.size() == 1
        marketPlaceOrderResponse.data.marketPlaceItems[0].externalItemId == externalItemId
        marketPlaceOrderResponse.data.marketPlaceItems[0].quantity == 1
    }

    def "Test GetOrder Bundled Items"() {

        given:
        String vendorOrderId = UUID.randomUUID().toString()

        ItemIdentifier itemIdentifier = ItemIdentifier.builder().itemId(itemId1).itemType("CIN").build()
        MarketPlaceItemPriceInfo marketPlaceItemPriceInfo = MarketPlaceItemPriceInfo.builder().totalPrice(1.0).baseTotalPrice(1.0).unitPrice(1.0).baseUnitPrice(1.0).build()

        MarketPlaceOrder marketPlaceOrder = getMarketPlaceOrder()
        List<MarketPlaceBundledItem> marketPlaceBundledItems = Arrays.asList(MarketPlaceBundledItem.builder()
                .id("13")
                .bundleSkuId("123")
                .bundleInstanceId(bundleInstanceId)
                .bundleQuantity(2)
                .bundleDescription("Test description").build())

        MarketPlaceItem marketPlaceItem = new MarketPlaceItem(marketPlaceOrder,
                UUID.randomUUID().toString(), externalItemId, "test item", instanceId, itemIdentifier, 2, marketPlaceItemPriceInfo, marketPlaceBundledItems,SubstitutionOption.DO_NOT_SUBSTITUTE)
        marketPlaceOrder.addMarketPlaceItems(Collections.singletonList(marketPlaceItem))

        when:
        MarketPlaceOrderResponse marketPlaceOrderResponse = marketPlaceController.getOrder(vendorOrderId)

        then:
        1 * marketPlaceApplicationService.getOrder(_ as String) >> { String _vendorOrderId ->
            assert _vendorOrderId == vendorOrderId
            return marketPlaceOrder
        }

        marketPlaceOrderResponse.data.externalOrderId == externalOrderId
        marketPlaceOrderResponse.data.storeId == "4401"
        marketPlaceOrderResponse.data.id == expectedOrderId
        marketPlaceOrderResponse.data.marketPlaceItems.size() == 1
        marketPlaceOrderResponse.data.marketPlaceItems[0].externalItemId == externalItemId
        marketPlaceOrderResponse.data.marketPlaceItems[0].quantity == 2
    }

    private CreateMarketPlaceOrderRequest.PaymentInfo getPaymentInfo() {
        return CreateMarketPlaceOrderRequest.PaymentInfo.builder()
                .total(50.0)
                .totalFee(5.0)
                .tax(1.25)
                .bagFee(2.5)
                .totalFeeTax(1.25)
                .subTotal(45.0)
                .build()
    }

    private MarketPlaceOrderContactInfo getMarketPlaceOrderContactInfo() {
        return MarketPlaceOrderContactInfo.builder().firstName("John").lastName("Doe").build()
    }

    private MarketPlaceOrderPaymentInfo getMarketPlaceOrderPaymentInfo() {
        return MarketPlaceOrderPaymentInfo.builder()
                .bagFee(new Money(new BigDecimal(1.0), Currency.GBP))
                .tax(new Money(new BigDecimal(2.0), Currency.GBP))
                .total(new Money(new BigDecimal(100.0), Currency.GBP))
                .totalFee(new Money(new BigDecimal(1.0), Currency.GBP))
                .totalFeeTax(new Money(new BigDecimal(0.5), Currency.GBP))
                .subTotal(new Money(new BigDecimal(95.5), Currency.GBP))
                .build()
    }

    private MarketPlaceOrder getMarketPlaceOrder() {
        return MarketPlaceOrder.builder()
                .id(expectedOrderId)
                .vendorOrderId(externalOrderId)
                .orderDueTime(new Date())
                .orderState("CREATED")
                .marketPlaceItems([])
                .sourceModifiedDate(new Date())
                .storeId(storeId).vendorId(Vendor.UBEREATS)
                .paymentInfo(getMarketPlaceOrderPaymentInfo())
                .marketPlaceOrderContactInfo(getMarketPlaceOrderContactInfo()).build()
    }

    private CreateMarketPlaceOrderRequest.MarketPlaceRequestItemData getMarketPlaceRequestItemData(String itemId) {
        return CreateMarketPlaceOrderRequest.MarketPlaceRequestItemData.builder()
                .itemId(itemId)
                .itemType("CIN")
                .externalItemId(externalItemId)
                .totalPrice(5.0)
                .unitPrice(2.5)
                .quantity(2)
                .itemDescription("Test description")
                .baseUnitPrice(2.5)
                .baseTotalPrice(5.0).build()
    }

}
