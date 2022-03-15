package com.walmart.fms.domain.error.exception

import com.walmart.fms.domain.error.ErrorType
import spock.lang.Specification

class FMSBadRequestExceptionSpec extends Specification {

    def "Bad Request Exception Error code and Error Type Validation"() {
        when:
        FMSBadRequestException fmsBadRequestException = new FMSBadRequestException("Order Not Found Exception")

        then:
        fmsBadRequestException.getMessage().equalsIgnoreCase("Order Not Found Exception")
        fmsBadRequestException.getErrorType() == ErrorType.INVALID_REQUEST_EXCEPTION
        fmsBadRequestException.getErrorResponse().getErrors().get(0).getCode() == 400
    }
}
