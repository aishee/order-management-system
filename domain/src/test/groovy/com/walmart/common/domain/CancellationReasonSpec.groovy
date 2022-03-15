package com.walmart.common.domain

import com.walmart.common.domain.type.CancellationReason
import spock.lang.Specification

class CancellationReasonSpec extends Specification{

    CancellationReason cancellationReason

    def "Test Getter for All fields"() {
        when :
        cancellationReason =  CancellationReason.get("1")
        then :
        cancellationReason.getCode() == "1"
        cancellationReason.getDescription() == "Changed Mind"
        cancellationReason == CancellationReason.CHANGED_MIND
    }
}
