package com.walmart.fms.order.gateway

import spock.lang.Specification

class StoreEventsSpec extends Specification {

    def "Valid Store events"() {
        expect:
        StoreEvents.values().size() == 2
        StoreEvents.values().contains(StoreEvents.PFO)
        StoreEvents.values().contains(StoreEvents.UFO)
    }

}
