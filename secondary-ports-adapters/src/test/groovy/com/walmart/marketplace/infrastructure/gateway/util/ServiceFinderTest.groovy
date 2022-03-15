package com.walmart.marketplace.infrastructure.gateway.util

import com.walmart.marketplace.infrastructure.gateway.uber.UberOrderGateway
import com.walmart.marketplace.order.domain.entity.type.Vendor
import com.walmart.marketplace.order.repository.IMarketPlaceGateWay
import spock.lang.Specification
import spock.lang.Unroll

class ServiceFinderTest extends Specification {

     ServiceFinder serviceFinder = Spy(ServiceFinder)

    def "get Service Implementation default"() {


        when:
        Object bean = serviceFinder.getService(IMarketPlaceGateWay.class, vendor)

        then:
        bean == marketPlaceGateway
        1 * serviceFinder.qualifiedBeanOfType(*_) >> marketPlaceGateway

        where:
        marketPlaceGateway | vendor
        new UberOrderGateway() | Vendor.UBEREATS
    }


    @Unroll
    def "get VendorType scenario"() {

        when:
        Vendor vendor = ServiceFinder.defaultVendorCache.get(vendorType)

        then:
        vendor == vendorId

        where:
        vendorId           | vendorType
        Vendor.UBEREATS    | Vendor.VendorType.UBEREATS
    }

    def "Test Vendor type where Gateway implementation is not present"() {
        when:
        serviceFinder.getService(IMarketPlaceGateWay.class,Vendor.TESTVENDOR)

        then:
        thrown Exception
    }
}
