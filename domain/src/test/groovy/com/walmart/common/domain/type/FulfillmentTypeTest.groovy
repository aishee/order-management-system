package com.walmart.common.domain.type

import spock.lang.Specification

class FulfillmentTypeTest extends Specification {

    def "Test get method for FulfillmentType"() {
        when:
        FulfillmentType fulfillmentType = FulfillmentType.get("HOME_DELIVERY")
        then:
        fulfillmentType == FulfillmentType.HOME_DELIVERY
        fulfillmentType == FulfillmentType.valueOf("HOME_DELIVERY")
    }
}
