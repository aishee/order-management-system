package com.walmart.oms.domain.error

import spock.lang.Specification

class ErrorDetailTest extends Specification {

    def "test error detail"() {
        given:
        ErrorDetail errorDetail = new ErrorDetail("fieldName", "issue")

        assert errorDetail.getField() == "fieldName"
        assert errorDetail.getIssue() == "issue"
        assert errorDetail.toString() != null
    }
}
