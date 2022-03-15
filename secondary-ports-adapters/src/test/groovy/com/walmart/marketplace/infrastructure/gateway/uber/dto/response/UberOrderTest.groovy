package com.walmart.marketplace.infrastructure.gateway.uber.dto.response

import com.walmart.common.domain.type.SubstitutionOption
import spock.lang.Specification

class UberOrderTest extends Specification {

    def "FulfillmentActionType REPLACE_FOR_ME conversion"() {
        when:
        UberOrder.FulfillmentActionType fulfillmentActionType = UberOrder.FulfillmentActionType.REPLACE_FOR_ME

        then:
        fulfillmentActionType.getSubstitutionOption() == SubstitutionOption.SUBSTITUTE
    }

    def "FulfillmentActionType SUBSTITUTE_ME conversion"() {
        when:
        UberOrder.FulfillmentActionType fulfillmentActionType = UberOrder.FulfillmentActionType.SUBSTITUTE_ME

        then:
        fulfillmentActionType.getSubstitutionOption() == SubstitutionOption.DO_NOT_SUBSTITUTE
    }

    def "FulfillmentActionType REMOVE_ITEM conversion"() {
        when:
        UberOrder.FulfillmentActionType fulfillmentActionType = UberOrder.FulfillmentActionType.REMOVE_ITEM

        then:
        fulfillmentActionType.getSubstitutionOption() == SubstitutionOption.DO_NOT_SUBSTITUTE
    }

    def "FulfillmentActionType CANCEL conversion"() {
        when:
        UberOrder.FulfillmentActionType fulfillmentActionType = UberOrder.FulfillmentActionType.CANCEL

        then:
        fulfillmentActionType.getSubstitutionOption() == SubstitutionOption.CANCEL_ENTIRE_ORDER
    }

}
