package com.walmart.common.domain.type

import spock.lang.Specification

class CurrencyTest extends Specification {

    def "Test get method for Currency"() {
        when:
        Currency currency = Currency.get("GBP")
        then:
        currency == Currency.GBP
        currency == Currency.valueOf("GBP")
    }
}
