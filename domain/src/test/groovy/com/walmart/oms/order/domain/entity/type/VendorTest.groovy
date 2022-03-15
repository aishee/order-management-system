package com.walmart.oms.order.domain.entity.type

import spock.lang.Specification

class VendorTest extends Specification {

    def "test vendor instance"() {
        when:
        Vendor vendor = Vendor.UBEREATS

        then:
        assert vendor.name() == "UBEREATS"
        assert vendor.ordinal() == 0
    }

    def "test vendor sequence generator next id"() {
        given:
        Vendor.SequenceGenerator generator = Vendor.SequenceGenerator.INSTANCE

        when:
        long nextId = generator.nextId()

        then:
        assert nextId > 0


    }

}
