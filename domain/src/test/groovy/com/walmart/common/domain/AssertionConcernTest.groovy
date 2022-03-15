package com.walmart.common.domain

import spock.lang.Specification

class AssertionConcernTest extends Specification {
    AssertionConcern assertionConcern
    String message = "message"
    int maxLength = 6
    int minLength = 4

    def setup() {
        assertionConcern = new AssertionConcern()
    }

    def "Test assertArgumentLength with length of value less than minLength"() {
        given:
        String value = "val"
        when:
        assertionConcern.assertArgumentLength(value, minLength, maxLength, message)
        then:
        thrown(IllegalArgumentException)
    }

    def "Test assertArgumentLength with length of value more than maxLength"() {
        given:
        String value = "values1"
        when:
        assertionConcern.assertArgumentLength(value, minLength, maxLength, message)
        then:
        thrown(IllegalArgumentException)
    }

    def "Test assertArgumentLength with no Exception"() {
        given:
        String value = "values"
        when:
        assertionConcern.assertArgumentLength(value, minLength, maxLength, message)
        then:
        noExceptionThrown()
    }

    def "Test assertArgumentNotEmpty with null Value"() {
        when:
        assertionConcern.assertArgumentNotEmpty(null, message)
        then:
        thrown(IllegalArgumentException)
    }

    def "Test assertArgumentNotEmpty with zero length value"() {
        String value = ""
        when:
        assertionConcern.assertArgumentNotEmpty(value, message)
        then:
        thrown(IllegalArgumentException)
    }

    def "Test assertArgumentNotEmpty with no Exception"() {
        String value = "value"
        when:
        assertionConcern.assertArgumentNotEmpty(value, message)
        then:
        noExceptionThrown()
    }
}
