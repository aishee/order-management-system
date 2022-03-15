package com.walmart.marketplace.order.domain.entity

import spock.lang.Specification

class MarketPlaceBundledItemTest extends Specification {

    def "test successful build of marketplace bundle item"() {
        when:
        MarketPlaceBundledItem marketPlaceBundledItem = MarketPlaceBundledItem.builder()
                .bundleInstanceId("abc")
                .bundleDescription("combo 1")
                .bundleQuantity(2)
                .itemQuantity(1)
                .bundleSkuId("2222").build()
        then:
        assert marketPlaceBundledItem.getBundleInstanceId() == "abc"
        assert marketPlaceBundledItem.getBundleQuantity() == 2
        assert marketPlaceBundledItem.getBundleSkuId() == "2222"
        assert marketPlaceBundledItem.getBundleDescription() == "combo 1"
        assert marketPlaceBundledItem.getItemQuantity() == 1
    }
}
