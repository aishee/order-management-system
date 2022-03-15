package com.walmart.oms.domain.error.exception

import com.walmart.oms.domain.error.ErrorType
import spock.lang.Specification

class OMSBadRequestExceptionSpec extends Specification {

    def "Bad Request Exception Error code and Error Type Validation"() {
        when:
        OMSBadRequestException omsBadRequestException = new OMSBadRequestException("Order Not Found Exception")

        then:
        omsBadRequestException.getMessage().equalsIgnoreCase("Order Not Found Exception")
        omsBadRequestException.getErrorType() == ErrorType.INVALID_REQUEST_EXCEPTION
        omsBadRequestException.getErrorResponse().getErrors().get(0).getCode() == 400
    }

    def "Bad Request Exception with throwable Error code and Error Type Validation"() {
        when:
        OMSBadRequestException omsBadRequestException = new OMSBadRequestException("Order Not Found Exception", new Exception())

        then:
        omsBadRequestException.getMessage().equalsIgnoreCase("Order Not Found Exception")
        omsBadRequestException.getErrorType() == ErrorType.INVALID_REQUEST_EXCEPTION
        omsBadRequestException.getErrorResponse().getErrors().get(0).getCode() == 400
    }
}
