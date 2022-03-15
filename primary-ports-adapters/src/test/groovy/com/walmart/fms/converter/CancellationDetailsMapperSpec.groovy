package com.walmart.fms.converter

import com.walmart.common.domain.type.CancellationSource
import com.walmart.common.domain.valueobject.CancellationDetails
import com.walmart.fms.order.valueobject.CancelDetails
import spock.lang.Specification

class CancellationDetailsMapperSpec extends Specification {

    def "test convertToDomainObject"() {
        given:
        CancelDetails cancelDetails = CancelDetails.builder()
                .cancelledBy(CancellationSource.OMS)
                .cancelledReasonCode("3")
                .cancelledReasonDescription("lost order")
                .build()
        when:
        CancellationDetails cancellationDetails = CancellationDetailsMapper.INSTANCE.convertToDomainObject(cancelDetails)
        then:
        cancellationDetails.cancelledReasonDescription == "lost order"
        cancellationDetails.cancelledReasonCode == "3"
        cancellationDetails.cancelledBy == CancellationSource.OMS
    }

    def "test convertToDomainObject for null"() {
        when:
        CancellationDetails cancellationDetails = CancellationDetailsMapper.INSTANCE.convertToDomainObject(null)
        then:
        cancellationDetails == null
    }
}
