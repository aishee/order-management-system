package com.walmart.marketplace.eventprocessors

import com.walmart.common.domain.type.Currency
import com.walmart.common.domain.type.SubstitutionOption
import com.walmart.marketplace.commands.MarketPlaceDeliveredOrderCommand
import com.walmart.marketplace.order.aggregateroot.MarketPlaceOrder
import com.walmart.marketplace.order.domain.entity.type.Vendor
import com.walmart.marketplace.order.domain.valueobject.*
import com.walmart.marketplace.order.factory.MarketPlaceOrderFactory
import com.walmart.marketplace.order.repository.IMarketPlaceRepository
import com.walmart.oms.domain.error.exception.OMSBadRequestException
import spock.lang.Specification

class MarketPlaceDeliveredCommandServiceTest extends Specification {

    MarketPlaceDeliveredCommandService marketPlaceDeliveredCommandService

    MarketPlaceOrderFactory marketPlaceOrderFactory = Mock()

    IMarketPlaceRepository marketPlaceRepository = Mock()


    String sourceOrderId = UUID.randomUUID().toString()

    def setup() {

        marketPlaceDeliveredCommandService = new MarketPlaceDeliveredCommandService(
                marketPlaceRepository: marketPlaceRepository,
                marketPlaceOrderFactory: marketPlaceOrderFactory)

    }

    def " Test DeliverOrder with pre existing order"() {

        given:
        Optional<MarketPlaceOrder> testMarketPlaceOrder = mockMarketPlaceOrder()

        MarketPlaceDeliveredOrderCommand marketPlaceDeliveredOrderCommand = getMarketPlaceDeliveredOrderCommand()

        marketPlaceOrderFactory.getOrder(_ as String) >> testMarketPlaceOrder

        when:
        marketPlaceDeliveredCommandService.deliverOrder(marketPlaceDeliveredOrderCommand)

        then:
        1 * marketPlaceRepository.save(_ as MarketPlaceOrder) >> { MarketPlaceOrder _marketPlaceOrder ->
            assert _marketPlaceOrder.getId() == sourceOrderId
            assert _marketPlaceOrder.getOrderState() == "DELIVERED"
            return _marketPlaceOrder
        }
    }

    def " Test DeliverOrder without pre existing order"() {

        given:

        MarketPlaceDeliveredOrderCommand marketPlaceDeliveredOrderCommand = getMarketPlaceDeliveredOrderCommand()

        marketPlaceOrderFactory.getOrder(_ as String) >> Optional.empty()

        when:
        marketPlaceDeliveredCommandService.deliverOrder(marketPlaceDeliveredOrderCommand)

        then:
        thrown(OMSBadRequestException.class)
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
                .storeId(storeId).vendorId(vendor)
                .paymentInfo(marketPlaceOrderPaymentInfo)
                .marketPlaceOrderContactInfo(marketPlaceOrderContactInfo).build()

        marketPlaceOrder.addMarketPlaceItem(itemId, externalItemId, "test item", 1, instanceId, itemIdentifier, marketPlaceItemPriceInfo, SubstitutionOption.DO_NOT_SUBSTITUTE)

        return Optional.of(marketPlaceOrder)
    }

    private MarketPlaceDeliveredOrderCommand getMarketPlaceDeliveredOrderCommand() {
        return MarketPlaceDeliveredOrderCommand.builder()
                .data(MarketPlaceDeliveredOrderCommand.MarketPlaceDeliveredOrderCommandData.builder()
                        .sourceOrderId(sourceOrderId).build()).build()
    }

}
