package com.walmart.oms.domain.error

import spock.lang.Specification

class ErrorTest extends Specification {

    def "test oms error"() {
        given:
        Error error = new Error(500, ErrorType.INTERNAL_SERVICE_EXCEPTION, "Any Error")

        assert error.getCode() == 500
        assert error.getType() == ErrorType.INTERNAL_SERVICE_EXCEPTION
        assert error.getMessage() == "Any Error"
        assert error.toString() != null
    }
}
