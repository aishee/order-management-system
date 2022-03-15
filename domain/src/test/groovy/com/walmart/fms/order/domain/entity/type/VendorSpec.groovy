package com.walmart.fms.order.domain.entity.type

import spock.lang.Specification

class VendorSpec extends Specification {

    def setup() {
    }

    def "Test whether unique storeWebOrderID is not null and in long range "() {
        when:
        Long id = Vendor.SequenceGenerator.INSTANCE.nextId();

        then:
        assert id != null
        assert id >= 0
        assert id <= Long.MAX_VALUE
    }

}
