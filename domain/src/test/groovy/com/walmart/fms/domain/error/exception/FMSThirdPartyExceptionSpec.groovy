package com.walmart.fms.domain.error.exception

import com.walmart.fms.domain.error.ErrorType
import spock.lang.Specification

class FMSThirdPartyExceptionSpec extends Specification {

    def "Third Party Exception Error code and Error Type Validation"() {
        when:
        FMSThirdPartyException fmsThirdPartyException = new FMSThirdPartyException("Runtime Exception")

        then:
        fmsThirdPartyException.getMessage().equalsIgnoreCase("Runtime Exception")
        fmsThirdPartyException.getErrorType() == ErrorType.INTERNAL_SERVICE_EXCEPTION
        fmsThirdPartyException.getErrorResponse().getErrors().get(0).getCode() == 500
    }

}
