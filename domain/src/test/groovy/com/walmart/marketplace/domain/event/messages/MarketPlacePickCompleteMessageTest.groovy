package com.walmart.marketplace.domain.event.messages


import com.walmart.marketplace.order.domain.entity.type.Vendor
import spock.lang.Specification

class MarketPlacePickCompleteMessageTest extends Specification {

    String sourceOrderId = UUID.randomUUID().toString()
    String vendorOrderId = UUID.randomUUID().toString()
    String vendorStoreId = UUID.randomUUID().toString()
    String instanceId = UUID.randomUUID().toString()
    String externalItemId1 = UUID.randomUUID().toString()
    String expectedItemId1 = "4888484"
    String expectedItemId2 = "4888455"

    Vendor vendor = Vendor.UBEREATS
    String storeId = "4401"
    MarketPlacePickCompleteMessage marketPlacePickCompleteMessage

    def setup() {
        marketPlacePickCompleteMessage = new MarketPlacePickCompleteMessage()
    }

    def "When there are no nil picks and no partial picks1"() {
        given:
        List<MarketPlaceItemAttributes> marketPlaceItemAttributesList = new LinkedList<>();
        MarketPlaceItemAttributes marketPlaceItemAttributes = MarketPlaceItemAttributes.builder().
                orderedQuantity(1).pickedQuantity(1).build()
        marketPlaceItemAttributesList.add(marketPlaceItemAttributes)
        MarketPlacePickCompleteMessage marketPlacePickCompleteMessage =
                createMarketPlacePickCompleteMessage(marketPlaceItemAttributesList)
        when:
        boolean result = marketPlacePickCompleteMessage.containsNilOrPartialPick()
        then:
        assert result == false
    }

    def "When there are nil picks"() {
        given:
        List<MarketPlaceItemAttributes> marketPlaceItemAttributesList = new LinkedList<>();
        MarketPlaceItemAttributes marketPlaceItemAttributes = MarketPlaceItemAttributes.builder().
                orderedQuantity(5).pickedQuantity(0).build()
        marketPlaceItemAttributesList.add(marketPlaceItemAttributes)
        MarketPlacePickCompleteMessage marketPlacePickCompleteMessage =
                createMarketPlacePickCompleteMessage(marketPlaceItemAttributesList)
        when:
        boolean result = marketPlacePickCompleteMessage.containsNilOrPartialPick()
        then:
        assert result == true
    }

    def "When there are partial picks"() {
        given:
        List<MarketPlaceItemAttributes> marketPlaceItemAttributesList = new LinkedList<>();
        MarketPlaceItemAttributes marketPlaceItemAttributes = MarketPlaceItemAttributes.builder().
                orderedQuantity(5).pickedQuantity(2).build()
        marketPlaceItemAttributesList.add(marketPlaceItemAttributes)
        MarketPlacePickCompleteMessage marketPlacePickCompleteMessage =
                createMarketPlacePickCompleteMessage(marketPlaceItemAttributesList)
        when:
        boolean result = marketPlacePickCompleteMessage.containsNilOrPartialPick()
        then:
        assert result == true
    }

    private MarketPlacePickCompleteMessage createMarketPlacePickCompleteMessage(
            List<MarketPlaceItemAttributes> marketPlaceItemAttributesList) {
        return MarketPlacePickCompleteMessage.builder()
                .marketPlaceItemAttributes(marketPlaceItemAttributesList).sourceOrderId(sourceOrderId)
                .vendorOrderId(vendorOrderId).storeId(storeId).vendorId(vendor).build()
    }
}
