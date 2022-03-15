package com.walmart.marketplace.infrastructure.gateway.util

import com.walmart.marketplace.order.domain.entity.type.Vendor
import com.walmart.marketplace.order.repository.IMarketPlaceGateWay
import spock.lang.Specification

class MarketPlaceGatewayFinderTest extends Specification {

    ServiceFinder serviceFinder = Mock()

    MarketPlaceGatewayFinder marketPlaceGatewayFinder

    IMarketPlaceGateWay expectedGateway = Mock()


    def setup() {
        marketPlaceGatewayFinder = new MarketPlaceGatewayFinder(
                serviceFinder: serviceFinder)
    }

    def "Test GetMarketPlaceGateway"() {
        given:
        serviceFinder.getService(_ as Class, _ as Vendor) >> expectedGateway

        when:
        IMarketPlaceGateWay actualGaetway = marketPlaceGatewayFinder.getMarketPlaceGateway(Vendor.UBEREATS)

        then:
        actualGaetway == expectedGateway

    }
}
