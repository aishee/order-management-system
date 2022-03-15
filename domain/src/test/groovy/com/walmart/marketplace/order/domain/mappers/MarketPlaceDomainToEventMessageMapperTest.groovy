package com.walmart.marketplace.order.domain.mappers

import com.walmart.common.domain.type.CancellationSource
import com.walmart.common.domain.type.SubstitutionOption
import com.walmart.marketplace.domain.event.messages.MarketPlaceOrderCancelMessage
import com.walmart.marketplace.order.aggregateroot.MarketPlaceOrder
import com.walmart.marketplace.order.domain.entity.type.Vendor
import com.walmart.marketplace.order.domain.valueobject.ItemIdentifier
import com.walmart.marketplace.order.domain.valueobject.MarketPlaceItemPriceInfo
import com.walmart.marketplace.order.domain.valueobject.MarketPlaceOrderContactInfo
import com.walmart.common.domain.valueobject.CancellationDetails
import spock.lang.Specification

import java.lang.reflect.Constructor

class MarketPlaceDomainToEventMessageMapperTest extends Specification {

    def "test map to MarketPlaceOrderCancelMessage"() {
        given:
        String vendorOrderId = UUID.randomUUID().toString()
        CancellationDetails cancellationDetails = CancellationDetails.builder()
                .cancelledReasonCode("12")
                .cancelledBy(CancellationSource.STORE).build();

        when:
        MarketPlaceOrderCancelMessage cancelMessage = MarketPlaceDomainToEventMessageMapper.mapToMarketPlaceOrderCancelMessage(mockMarketPlaceOrder(vendorOrderId),
                cancellationDetails)

        then:
        cancelMessage != null
        cancelMessage.getVendorOrderId() == vendorOrderId
        cancelMessage.getCancellationSource() == CancellationSource.STORE
        cancelMessage.getCancelledReasonCode() == "12"
        cancelMessage.isCancelledByStore()
        !cancelMessage.isTestOrder()
        !cancelMessage.isCancelledDueToNilPick()
        cancelMessage.getStoreId() == "4401"
        cancelMessage.getVendor() == Vendor.UBEREATS
        cancelMessage.getVendorStoreId() == "1212"
        cancelMessage.getExternalItemIds().size() == 1
    }

    def "test marketplace mapper instance"() {
        given:
        Constructor<?>[] constructor = MarketPlaceDomainToEventMessageMapper.class.getDeclaredConstructors();
        Constructor<?> cons = constructor[0];
        cons.setAccessible(true);

        when:
        MarketPlaceDomainToEventMessageMapper domainToEventMessageMapper = cons.newInstance();

        then:
        domainToEventMessageMapper instanceof MarketPlaceDomainToEventMessageMapper
    }


    private MarketPlaceOrder mockMarketPlaceOrder(String vendorOrderId) {
        String externalOrderId = vendorOrderId
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
                .vendorStoreId("1212")
                .marketPlaceOrderContactInfo(marketPlaceOrderContactInfo).build()

        marketPlaceOrder.addMarketPlaceItem(itemId, externalItemId, "test item", 1, instanceId, itemIdentifier, marketPlaceItemPriceInfo, SubstitutionOption.DO_NOT_SUBSTITUTE)
        return marketPlaceOrder
    }
}