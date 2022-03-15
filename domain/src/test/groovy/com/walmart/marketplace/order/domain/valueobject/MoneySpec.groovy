package com.walmart.marketplace.order.domain.valueobject

import com.walmart.common.domain.type.Currency
import spock.lang.Specification

class MoneySpec extends Specification {

    Money money

    def setup() {
        money = new Money(100, Currency.GBP)
    }

    def "Compare Money Objects" () {
        given:
        Money money1 = new Money(200, Currency.GBP)

        when:
        boolean  s = money <=> money1

        then:
        s == true
    }

    def "Compare Money Objects IllegalArgumentException" () {
        given:
        Money money1 = new Money(100, Currency.DEFAULT)

        when:
        money <=> money1

        then:
        thrown(IllegalArgumentException)
    }

    def "Check Equality of Money Object" () {
        given:
        Money money1 = new Money(100, Currency.DEFAULT)

        when:
        boolean s = money.equals(money1)

        then:
        s == false
    }

    def "Check Equality of Money Object when object is null" () {
        given:
        Money money1 = null

        when:
        boolean s = money.equals(money1)

        then:
        s == false
    }

    def "Check Equality of Money Object when object is same" () {
        when:
        boolean s = money.equals(money)

        then:
        s == true
    }
}
