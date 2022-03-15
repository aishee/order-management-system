package com.walmart.marketplace.commands.mapper

import com.walmart.common.domain.type.Currency
import com.walmart.common.domain.type.SubstitutionOption
import com.walmart.marketplace.commands.MarketPlacePickCompleteCommand
import com.walmart.marketplace.domain.event.messages.MarketPlacePickCompleteMessage
import com.walmart.marketplace.order.aggregateroot.MarketPlaceOrder
import com.walmart.marketplace.order.domain.entity.MarketPlaceBundledItem
import com.walmart.marketplace.order.domain.entity.type.Vendor
import com.walmart.marketplace.order.domain.valueobject.*
import spock.lang.Specification

import java.lang.reflect.Constructor

class MarketPlacePickCompleteCommandToDomainMessageMapperTest extends Specification {
    String externalOrderId = UUID.randomUUID().toString()
    String sourceOrderId = UUID.randomUUID().toString()
    String vendorOrderId = UUID.randomUUID().toString()
    String storeId = UUID.randomUUID().toString()
    String externalItemId1 = UUID.randomUUID().toString()
    String externalItemId2 = UUID.randomUUID().toString()
    List<String> nilPicks = new ArrayList<>()
    Map<String, Integer> partialPicks = new HashMap<>()
    String instanceId = UUID.randomUUID().toString()

    def "Successful Mapping"() {
        given:
        MarketPlaceOrder testMarketPlaceOrder = mockMarketPlaceOrder()
        MarketPlacePickCompleteCommand marketPlacePickCompleteCommand = getMarketPlacePickCompleteCommand()

        when:
        MarketPlacePickCompleteMessage marketPlacePickCompleteMessage = MarketPlacePickCompleteCommandToDomainMessageMapper.mapToMarketPlacePickCompleteMessage(marketPlacePickCompleteCommand, testMarketPlaceOrder)

        then:
        marketPlacePickCompleteMessage.vendorId == testMarketPlaceOrder.vendorId
    }

    def "test instance"() {
        given:
        Constructor<?>[] constructor = MarketPlacePickCompleteCommandToDomainMessageMapper.class.getDeclaredConstructors();
        Constructor<?> cons = constructor[0];
        cons.setAccessible(true);

        when:
        MarketPlacePickCompleteCommandToDomainMessageMapper domainMessageMapper = cons.newInstance() as MarketPlacePickCompleteCommandToDomainMessageMapper;

        then:
        domainMessageMapper instanceof MarketPlacePickCompleteCommandToDomainMessageMapper
    }

    MarketPlaceOrder mockMarketPlaceOrder() {

        String itemId1 = UUID.randomUUID().toString()
        String itemId2 = UUID.randomUUID().toString()
        Vendor vendor = Vendor.UBEREATS
        String createdOrderState = "CREATED"
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
                .marketPlaceOrderContactInfo(marketPlaceOrderContactInfo)
                .build()

        marketPlaceOrder.addMarketPlaceItem(itemId1, externalItemId1, "test item", 1, instanceId1, itemIdentifier1, marketPlaceItemPriceInfo, SubstitutionOption.DO_NOT_SUBSTITUTE)
        marketPlaceOrder.getMarketPlaceItems().get(0).setBundledItemList(new LinkedList<MarketPlaceBundledItem>())
        marketPlaceOrder.addMarketPlaceItem(itemId2, externalItemId2, "test item", 2, instanceId2, itemIdentifier2, marketPlaceItemPriceInfo, SubstitutionOption.DO_NOT_SUBSTITUTE)
        marketPlaceOrder.getMarketPlaceItems().get(1).setBundledItemList(new LinkedList<MarketPlaceBundledItem>())
        return marketPlaceOrder
    }

    private MarketPlacePickCompleteCommand getMarketPlacePickCompleteCommand() {
        nilPicks.add(externalItemId1)
        partialPicks.put(externalItemId2, 1)
        MarketPlacePickCompleteCommand.PickedItemCommand pickedItemCommand =
                MarketPlacePickCompleteCommand.PickedItemCommand.builder().
                        orderedCin("testOrderedCin").pickedQuantity(2).build()
        List<MarketPlacePickCompleteCommand.MarketplacePickCompleteItemCommand> marketplacePickCompleteItemCommands = new LinkedList<>()
        MarketPlacePickCompleteCommand.MarketplacePickCompleteItemCommand marketplacePickCompleteItemCommand =
                MarketPlacePickCompleteCommand.MarketplacePickCompleteItemCommand.builder()
                        .itemId("testItemId").orderedQuantity(2)
                        .instanceId(instanceId).pickedItemCommand(pickedItemCommand).build()
        marketplacePickCompleteItemCommands.add(marketplacePickCompleteItemCommand)
        return MarketPlacePickCompleteCommand.builder()
                .data(MarketPlacePickCompleteCommand.MarketPlacePickCompleteCommandData.builder()
                        .sourceOrderId(sourceOrderId).vendorOrderId(vendorOrderId).storeId(storeId)
                        .vendorId(Vendor.UBEREATS)
                        .marketplacePickCompleteItemCommands(marketplacePickCompleteItemCommands)
                        .build()).build()
    }
}
