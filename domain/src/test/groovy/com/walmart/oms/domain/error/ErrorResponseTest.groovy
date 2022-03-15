package com.walmart.oms.domain.error

import spock.lang.Specification

class ErrorResponseTest extends Specification {

    def "test error response"() {
        given:
        Error error = new Error(500, ErrorType.INTERNAL_SERVICE_EXCEPTION, "Any Error")
        List<Error> errorList = Arrays.asList(error)
        ErrorResponse errorResponse = new ErrorResponse(errorList)

        assert errorResponse.toString() != null
        assert ErrorType.values() != null
        assert ErrorType.valueOf("INTERNAL_SERVICE_EXCEPTION") == ErrorType.INTERNAL_SERVICE_EXCEPTION

    }
}
