package com.walmart.oms.order.domain.entity

import com.walmart.oms.order.aggregateroot.OmsOrder
import spock.lang.Specification

class AddressInfoTest extends Specification {

    def "test address info"() {
        given:
        OmsOrder omsOrder = Mock()
        AddressInfo addressInfo = AddressInfo.builder()
                .omsOrder(omsOrder).id("test").addressType("type")
                .country("IN").state("Bangalore").postalCode("112233")
                .county("county")
                .build()

        assert addressInfo.getOrder() != null
        assert addressInfo.id == "test"
        assert addressInfo.getAddressType() == "type"
        assert addressInfo.getCountry() == "IN"
        assert addressInfo.getCounty() == "county"
        assert addressInfo.getPostalCode() == "112233"
        assert addressInfo.getState() == "Bangalore"
        assert addressInfo.toString() != null
    }
}
