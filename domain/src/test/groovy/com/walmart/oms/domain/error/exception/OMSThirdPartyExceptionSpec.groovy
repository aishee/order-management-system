package com.walmart.oms.domain.error.exception

import com.walmart.oms.domain.error.ErrorType
import spock.lang.Specification

class OMSThirdPartyExceptionSpec extends Specification {

    def "Third Party Exception Error code and Error Type Validation"() {
        when:
        OMSThirdPartyException omsThirdPartyException = new OMSThirdPartyException("Connection Timeout")

        then:
        omsThirdPartyException.getMessage().equalsIgnoreCase("Connection Timeout")
        omsThirdPartyException.getErrorType() == ErrorType.INTERNAL_SERVICE_EXCEPTION
        omsThirdPartyException.getErrorResponse().getErrors().get(0).getCode() == 500
    }

    def "Third Party Exception Throwable Error code and Error Type Validation"() {
        when:
        OMSThirdPartyException omsThirdPartyException = new OMSThirdPartyException("Connection Timeout", new Exception())

        then:
        omsThirdPartyException.getMessage().equalsIgnoreCase("Connection Timeout")
        omsThirdPartyException.getErrorType() == ErrorType.INTERNAL_SERVICE_EXCEPTION
        omsThirdPartyException.getErrorResponse().getErrors().get(0).getCode() == 500
    }
}
