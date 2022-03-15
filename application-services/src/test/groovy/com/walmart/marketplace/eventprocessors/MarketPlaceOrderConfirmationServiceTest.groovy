package com.walmart.marketplace.eventprocessors

import com.walmart.common.domain.type.SubstitutionOption
import com.walmart.common.metrics.MetricService
import com.walmart.common.domain.type.Currency
import com.walmart.marketplace.commands.MarketPlaceOrderConfirmationCommand
import com.walmart.marketplace.order.aggregateroot.MarketPlaceOrder
import com.walmart.marketplace.order.domain.entity.type.Vendor
import com.walmart.marketplace.order.domain.valueobject.ItemIdentifier
import com.walmart.marketplace.order.domain.valueobject.MarketPlaceItemPriceInfo
import com.walmart.marketplace.order.domain.valueobject.MarketPlaceOrderContactInfo
import com.walmart.marketplace.order.domain.valueobject.MarketPlaceOrderPaymentInfo
import com.walmart.marketplace.order.domain.valueobject.Money
import com.walmart.marketplace.order.factory.MarketPlaceOrderFactory
import com.walmart.marketplace.order.repository.IMarketPlaceRepository
import com.walmart.oms.domain.error.exception.OMSBadRequestException
import spock.lang.Specification

class MarketPlaceOrderConfirmationServiceTest extends Specification {

    MarketPlaceOrderConfirmationService marketPlaceOrderConfirmationService

    MarketPlaceOrderFactory marketPlaceOrderFactory = Mock()

    IMarketPlaceRepository marketPlaceRepository = Mock()

    String sourceOrderId = UUID.randomUUID().toString()

    MetricService metricService = Mock()

    def setup() {
        marketPlaceOrderConfirmationService = new MarketPlaceOrderConfirmationService(
                marketPlaceOrderFactory: marketPlaceOrderFactory,
                marketPlaceRepository: marketPlaceRepository,
                metricService: metricService
        )
    }

    def "Test OrderConfirmedAtStore with pre existing order"() {

        given:
        Optional<MarketPlaceOrder> testMarketPlaceOrder = mockMarketPlaceOrder()

        MarketPlaceOrderConfirmationCommand marketPlaceOrderConfirmationCommand = getMarketPlaceOrderConfirmationCommand()

        marketPlaceOrderFactory.getOrder(_ as String) >> testMarketPlaceOrder

        when:
        marketPlaceOrderConfirmationService.orderConfirmedAtStore(marketPlaceOrderConfirmationCommand)

        then:
        1 * marketPlaceRepository.save(_ as MarketPlaceOrder) >> { MarketPlaceOrder _marketPlaceOrder ->
            assert _marketPlaceOrder.getId() == sourceOrderId
            assert _marketPlaceOrder.getOrderState() == "RECD_AT_STORE"
            return _marketPlaceOrder
        }

    }

    def " Test OrderConfirmation without pre existing order"() {

        given:

        MarketPlaceOrderConfirmationCommand marketPlaceOrderConfirmationCommand = getMarketPlaceOrderConfirmationCommand()

        marketPlaceOrderFactory.getOrder(_ as String) >> Optional.empty()

        when:
        marketPlaceOrderConfirmationService.orderConfirmedAtStore(marketPlaceOrderConfirmationCommand)

        then:
        thrown(OMSBadRequestException.class)
    }

    private MarketPlaceOrderConfirmationCommand getMarketPlaceOrderConfirmationCommand() {
        return MarketPlaceOrderConfirmationCommand.builder()
                .data(MarketPlaceOrderConfirmationCommand.MarketPlaceOrderConfirmationCommandData.builder()
                        .sourceOrderId(sourceOrderId).build()).build()
    }

    Optional<MarketPlaceOrder> mockMarketPlaceOrder(){

        String externalOrderId = UUID.randomUUID().toString()
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
                .id(sourceOrderId)
                .orderState(createdOrderState)
                .marketPlaceItems([])
                .sourceModifiedDate(new Date())
                .storeId(storeId)
                .vendorStoreId(vendorStoreId)
                .vendorId(vendor)
                .paymentInfo(marketPlaceOrderPaymentInfo)
                .marketPlaceOrderContactInfo(marketPlaceOrderContactInfo).build()

        marketPlaceOrder.addMarketPlaceItem(itemId, externalItemId, "test item", 1, instanceId, itemIdentifier, marketPlaceItemPriceInfo, SubstitutionOption.DO_NOT_SUBSTITUTE)

        return Optional.of(marketPlaceOrder)
    }

}
