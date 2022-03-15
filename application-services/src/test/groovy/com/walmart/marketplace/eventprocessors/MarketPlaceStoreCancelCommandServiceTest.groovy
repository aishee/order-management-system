package com.walmart.marketplace.eventprocessors

import com.walmart.common.domain.type.CancellationSource
import com.walmart.common.domain.type.Currency
import com.walmart.common.domain.type.SubstitutionOption
import com.walmart.common.metrics.MetricService
import com.walmart.marketplace.MarketPlaceApplicationService
import com.walmart.marketplace.commands.CancelMarketPlaceOrderCommand
import com.walmart.marketplace.order.aggregateroot.MarketPlaceOrder
import com.walmart.marketplace.order.domain.entity.type.Vendor
import com.walmart.marketplace.order.domain.valueobject.ItemIdentifier
import com.walmart.marketplace.order.domain.valueobject.MarketPlaceItemPriceInfo
import com.walmart.marketplace.order.domain.valueobject.MarketPlaceOrderContactInfo
import com.walmart.marketplace.order.domain.valueobject.MarketPlaceOrderPaymentInfo
import com.walmart.marketplace.order.domain.valueobject.Money
import com.walmart.oms.domain.error.exception.OMSBadRequestException
import com.walmart.common.domain.valueobject.CancellationDetails
import spock.lang.Specification

class MarketPlaceStoreCancelCommandServiceTest extends Specification {

    MarketPlaceStoreCancelCommandService marketPlaceStoreCancelCommandService

    MarketPlaceApplicationService marketPlaceApplicationService = Mock()
    MetricService metricService = Mock()

    String sourceOrderId = UUID.randomUUID().toString()

    def setup() {

        marketPlaceStoreCancelCommandService = new MarketPlaceStoreCancelCommandService(
                marketPlaceApplicationService: marketPlaceApplicationService,
                metricService: metricService)

    }

    def "Test CancelOrder"() {

        given:

        Optional<MarketPlaceOrder> testMarketPlaceOrder = mockMarketPlaceOrder()

        CancelMarketPlaceOrderCommand cancelMarketPlaceOrderCommand = CancelMarketPlaceOrderCommand.builder()
                .sourceOrderId(sourceOrderId)
                .cancellationDetails(CancellationDetails.builder()
                        .cancelledReasonCode("store")
                        .cancelledBy(CancellationSource.STORE)
                        .cancelledReasonDescription("cancelled at store")
                        .build())
                .build()
        marketPlaceApplicationService.cancelOrder(cancelMarketPlaceOrderCommand) >> testMarketPlaceOrder

        when:
        marketPlaceStoreCancelCommandService.cancelOrder(cancelMarketPlaceOrderCommand)

        then:
        1 * marketPlaceApplicationService.cancelOrder(_ as CancelMarketPlaceOrderCommand) >> { CancelMarketPlaceOrderCommand _cancelMarketPlaceOrderCommand ->
            _cancelMarketPlaceOrderCommand.sourceOrderId == sourceOrderId
            return mockMarketPlaceOrder().get()
        }
    }

    def "Test CancelOrder OMSBadRequestException Exception"() {

        given:

        CancelMarketPlaceOrderCommand cancelMarketPlaceOrderCommand = CancelMarketPlaceOrderCommand.builder()
                .sourceOrderId(sourceOrderId)
                .cancellationDetails(CancellationDetails.builder()
                        .cancelledReasonCode("store")
                        .cancelledBy(CancellationSource.STORE)
                        .cancelledReasonDescription("cancelled at store")
                        .build())
                .build()
        marketPlaceApplicationService.cancelOrder(_ as CancelMarketPlaceOrderCommand) >> { throw new OMSBadRequestException(null) }

        when:
        marketPlaceStoreCancelCommandService.cancelOrder(cancelMarketPlaceOrderCommand)

        then:
        thrown(OMSBadRequestException.class)
    }

    def "Test CancelOrder Exception"() {

        given:

        CancelMarketPlaceOrderCommand cancelMarketPlaceOrderCommand = CancelMarketPlaceOrderCommand.builder()
                .sourceOrderId(sourceOrderId)
                .cancellationDetails(CancellationDetails.builder()
                        .cancelledReasonCode("store")
                        .cancelledBy(CancellationSource.STORE)
                        .cancelledReasonDescription("cancelled at store")
                        .build())
                .build()
        marketPlaceApplicationService.cancelOrder(_ as CancelMarketPlaceOrderCommand) >> { throw new Exception() }

        when:
        marketPlaceStoreCancelCommandService.cancelOrder(cancelMarketPlaceOrderCommand)

        then:
        thrown(Exception)
    }

    Optional<MarketPlaceOrder> mockMarketPlaceOrder() {

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
                .id(sourceOrderId)
                .orderState("CREATED")
                .marketPlaceItems([])
                .sourceModifiedDate(new Date())
                .storeId(storeId).vendorId(vendor)
                .paymentInfo(marketPlaceOrderPaymentInfo)
                .marketPlaceOrderContactInfo(marketPlaceOrderContactInfo).build()

        marketPlaceOrder.addMarketPlaceItem(itemId, externalItemId, "test item", 1, instanceId, itemIdentifier, marketPlaceItemPriceInfo, SubstitutionOption.DO_NOT_SUBSTITUTE)

        return Optional.of(marketPlaceOrder)
    }
}