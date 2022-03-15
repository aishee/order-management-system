package com.walmart.marketplace.repository

import com.walmart.common.domain.type.SubstitutionOption
import com.walmart.marketplace.order.aggregateroot.MarketPlaceOrder
import com.walmart.marketplace.order.configuration.MarketPlaceOrderConfig
import com.walmart.marketplace.order.domain.entity.type.Vendor
import com.walmart.marketplace.order.domain.valueobject.ItemIdentifier
import com.walmart.marketplace.order.domain.valueobject.MarketPlaceItemPriceInfo
import com.walmart.marketplace.order.domain.valueobject.MarketPlaceOrderContactInfo
import com.walmart.marketplace.repository.infrastructure.mssql.IMarketPlaceSqlServerRepository
import spock.lang.Specification

class MarketPlaceRepositoryTest extends Specification {

    IMarketPlaceSqlServerRepository marketPlaceSqlServerRepository = Mock()

    MarketPlaceRepository marketPlaceRepository

    MarketPlaceOrderConfig marketPlaceOrderConfig = Mock()

    def setup() {
        marketPlaceRepository = new MarketPlaceRepository(
                marketPlaceSqlServerRepository: marketPlaceSqlServerRepository)
    }

    def " Test Get"() {
        given:
        String externalOrderId = UUID.randomUUID().toString()
        String itemId = UUID.randomUUID().toString()
        Vendor vendor = Vendor.UBEREATS;
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
        marketPlaceRepository.get(externalOrderId)

        then:
        1 * marketPlaceSqlServerRepository.findByVendorOrderId(_ as String) >> marketPlaceOrder
    }

    def "Test Save"() {
        given:
        String externalOrderId = UUID.randomUUID().toString()
        String itemId = UUID.randomUUID().toString()
        Vendor vendor = Vendor.UBEREATS;
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
        marketPlaceRepository.save(marketPlaceOrder)

        then:
        1 * marketPlaceSqlServerRepository.save(_ as MarketPlaceOrder) >> marketPlaceOrder
    }

    def " Test GetInProgressOrderCount"() {
        given:
        String storeId = "4401"
        List<String> orderStates = Arrays.asList("CREATED", "ACCEPTED", "RECD_AT_STORE")
        when:
        marketPlaceRepository.getInProgressOrderCount(orderStates, storeId)

        then:
        1 * marketPlaceSqlServerRepository.countMarketPlaceOrdersByOrderStateInAndStoreIdAndOrderDueTimeGreaterThanEqual(_ as List, _ as String, _ as Date) >> 10
    }

    def " Test GetNextIdentify"() {

        when:
        String id = marketPlaceRepository.getNextIdentity()

        then:
        UUID uuid = UUID.fromString(id)
        assert uuid != null
    }
}
