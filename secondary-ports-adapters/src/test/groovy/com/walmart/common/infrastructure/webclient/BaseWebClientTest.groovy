package com.walmart.common.infrastructure.webclient

import org.springframework.http.HttpStatus
import spock.lang.Specification

class BaseWebClientTest extends Specification {

    def "Server Error Failure Check"() {
        when:
        boolean notServerError = BaseWebClient.isServerError(HttpStatus.ACCEPTED)

        then:
        !notServerError
    }

    def "Server Error Success Check"() {
        when:
        boolean notServerError = BaseWebClient.isServerError(HttpStatus.INTERNAL_SERVER_ERROR)

        then:
        notServerError
    }

}
