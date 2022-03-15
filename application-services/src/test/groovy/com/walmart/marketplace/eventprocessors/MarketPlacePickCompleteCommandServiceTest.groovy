package com.walmart.marketplace.eventprocessors

import com.walmart.common.domain.event.processing.EventGeneratorService
import com.walmart.common.domain.type.Currency
import com.walmart.common.domain.type.SubstitutionOption
import com.walmart.marketplace.commands.MarketPlacePickCompleteCommand
import com.walmart.marketplace.domain.event.messages.MarketPlacePickCompleteMessage
import com.walmart.marketplace.mappers.SubstituteItemCommandToMarketPlaceItemMapper
import com.walmart.marketplace.order.aggregateroot.MarketPlaceOrder
import com.walmart.marketplace.order.domain.MarketPlaceDomainService
import com.walmart.marketplace.order.domain.entity.MarketPlaceBundledItem
import com.walmart.marketplace.order.domain.entity.type.Vendor
import com.walmart.marketplace.order.domain.valueobject.*
import com.walmart.marketplace.order.factory.MarketPlaceOrderFactory
import com.walmart.oms.domain.error.exception.OMSBadRequestException
import spock.lang.Specification

class MarketPlacePickCompleteCommandServiceTest extends Specification {

    MarketPlacePickCompleteCommandService marketPlacePickCompleteCommandService

    MarketPlaceDomainService marketPlaceDomainService = Mock()
    EventGeneratorService eventGeneratorService = Mock()
    MarketPlaceOrderFactory marketPlaceOrderFactory = Mock()
    SubstituteItemCommandToMarketPlaceItemMapper marketPlaceCommandToEntityObjectMapper = Mock()


    String externalOrderId = UUID.randomUUID().toString()
    String sourceOrderId = UUID.randomUUID().toString()
    String externalItemId1 = UUID.randomUUID().toString()
    String externalItemId2 = UUID.randomUUID().toString()
    String vendorOrderId = UUID.randomUUID().toString()
    String storeId = UUID.randomUUID().toString()
    List<String> nilPicks = new ArrayList<>()
    Map<String, Integer> partialPicks = new HashMap<>()

    def setup() {
        marketPlacePickCompleteCommandService = new MarketPlacePickCompleteCommandService(
                 eventGeneratorService,
                 marketPlaceOrderFactory,
                 marketPlaceDomainService,
                 marketPlaceCommandToEntityObjectMapper
        )
    }

    def "Test PickCompleteOrder with pre existing order"() {

        given:
        MarketPlacePickCompleteCommand marketPlacePickCompleteCommand = getMarketPlacePickCompleteCommand()
        marketPlaceOrderFactory.getOrder(_ as String) >> mockMarketPlaceOrder()
        marketPlaceDomainService.pickCompleteMarketPlaceOrder(_ as MarketPlaceOrder) >> mockMarketPlaceOrder().get()

        when:
        marketPlacePickCompleteCommandService.pickCompleteOrder(marketPlacePickCompleteCommand)

        then:
        1 * eventGeneratorService.publishApplicationEvent(_ as MarketPlacePickCompleteMessage)
    }

    def "Test PickCompleteOrder with no nil partial picks"() {

        given:
        MarketPlacePickCompleteCommand marketPlacePickCompleteCommand = getMarketPlacePickCompleteCommandWithoutNilPartialPicks()
        marketPlaceDomainService.pickCompleteMarketPlaceOrder(_ as MarketPlaceOrder) >> mockMarketPlaceOrder().get()
        marketPlaceOrderFactory.getOrder(_ as String) >> mockMarketPlaceOrder()

        when:
        marketPlacePickCompleteCommandService.pickCompleteOrder(marketPlacePickCompleteCommand)

        then:
        1 * eventGeneratorService.publishApplicationEvent(_ as MarketPlacePickCompleteMessage)
    }

    def "Test PickCompleteOrder without pre existing order"() {

        given:
        MarketPlacePickCompleteCommand marketPlacePickCompleteCommand = getMarketPlacePickCompleteCommand()
        marketPlaceOrderFactory.getOrder(_ as String) >> mockMarketPlaceOrder() >> { throw new OMSBadRequestException(null) }

        when:
        marketPlacePickCompleteCommandService.pickCompleteOrder(marketPlacePickCompleteCommand)

        then:
        thrown(OMSBadRequestException.class)
    }

    private MarketPlacePickCompleteCommand getMarketPlacePickCompleteCommand() {
        nilPicks.add(externalItemId1)
        partialPicks.put(externalItemId2, 1)
        return MarketPlacePickCompleteCommand.builder()
                .data(MarketPlacePickCompleteCommand.MarketPlacePickCompleteCommandData.builder()
                        .sourceOrderId(sourceOrderId).vendorOrderId(vendorOrderId).storeId(storeId)
                        .vendorId(Vendor.UBEREATS)
                        .build()).build()
    }

    private MarketPlacePickCompleteCommand getMarketPlacePickCompleteCommandWithoutNilPartialPicks() {
        return MarketPlacePickCompleteCommand.builder()
                .data(MarketPlacePickCompleteCommand.MarketPlacePickCompleteCommandData.builder()
                        .sourceOrderId(sourceOrderId).vendorOrderId(vendorOrderId).storeId(storeId)
                        .vendorId(Vendor.UBEREATS)
                        .build()).build()
    }

    Optional<MarketPlaceOrder> mockMarketPlaceOrder() {

        String itemId1 = UUID.randomUUID().toString()
        String itemId2 = UUID.randomUUID().toString()
        Vendor vendor = Vendor.UBEREATS
        String createdOrderState = "PICK_COMPLETE"
        String storeId = "4401"
        String customerFirstName = "John"
        String customerLastName = "Doe"
        String instanceId1 = UUID.randomUUID().toString()
        String instanceId2 = UUID.randomUUID().toString()

        ItemIdentifier itemIdentifier1 = ItemIdentifier.builder().itemId(externalItemId1).itemType("CIN").build()
        ItemIdentifier itemIdentifier2 = ItemIdentifier.builder().itemId(externalItemId2).itemType("CIN").build()
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

        marketPlaceOrder.addMarketPlaceItem(itemId1, externalItemId1, "test item", 1, instanceId1, itemIdentifier1, marketPlaceItemPriceInfo, SubstitutionOption.DO_NOT_SUBSTITUTE)
        marketPlaceOrder.getMarketPlaceItems().get(0).setBundledItemList(new LinkedList<MarketPlaceBundledItem>())
        marketPlaceOrder.addMarketPlaceItem(itemId2, externalItemId2, "test item", 2, instanceId2, itemIdentifier2, marketPlaceItemPriceInfo, SubstitutionOption.DO_NOT_SUBSTITUTE)
        marketPlaceOrder.getMarketPlaceItems().get(1).setBundledItemList(new LinkedList<MarketPlaceBundledItem>())
        return Optional.of(marketPlaceOrder)
    }
}
