package com.walmart.common.mdc


import spock.lang.Specification

class MDCRecorderTest extends Specification {

    def "test init"() {
        given:
        String api = "test api"
        String domain = "test domain"

        MDCRecorder.initMDC(api, domain)
    }

    def "test clear"() {
        MDCRecorder.clear()
    }
}
