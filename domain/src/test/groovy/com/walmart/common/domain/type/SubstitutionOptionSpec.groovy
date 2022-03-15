package com.walmart.common.domain.type

import spock.lang.Specification

class SubstitutionOptionSpec extends Specification {
    def "Test for FulfillmentActionType"() {
        when:
        SubstitutionOption actionType = SubstitutionOption.DO_NOT_SUBSTITUTE;
        then:
        SubstitutionOption.values().size() == 3
        actionType.ordinal() == 0
    }
}
