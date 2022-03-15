package com.walmart.marketplace.order.domain.entity

import com.walmart.marketplace.order.domain.valueobject.MarketPlaceSubstitutedItemPriceInfo
import spock.lang.Specification

class MarketPlaceSubstitutedItemSpec extends Specification {
    def "test successful build of marketplace substitute item"() {
        when:
        MarketPlaceSubstitutedItemPriceInfo priceInfo = MarketPlaceSubstitutedItemPriceInfo.builder()
                .unitPrice(BigDecimal.valueOf(30.3))
                .totalPrice(BigDecimal.valueOf(60.6))
                .build()
        MarketPlaceSubstitutedItem marketPlaceSubstitutedItem = MarketPlaceSubstitutedItem.builder()
                .id("abc")
                .description("combo 1")
                .quantity(2L)
                .substitutedItemPriceInfo(priceInfo)
                .externalItemId("2222").build()
        then:
        assert marketPlaceSubstitutedItem.getId() == "abc"
        assert marketPlaceSubstitutedItem.getQuantity() == 2L
        assert marketPlaceSubstitutedItem.getExternalItemId() == "2222"
        assert marketPlaceSubstitutedItem.getDescription() == "combo 1"
        assert marketPlaceSubstitutedItem.getSubstitutedItemPriceInfo().getUnitPrice() == priceInfo.getUnitPrice()
        assert marketPlaceSubstitutedItem.getSubstitutedItemPriceInfo().getTotalPrice() == priceInfo.getTotalPrice()
    }
}
