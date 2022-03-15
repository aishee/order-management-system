package com.walmart.oms

import com.walmart.common.domain.type.CancellationSource
import com.walmart.common.domain.valueobject.CancellationDetails
import com.walmart.oms.commands.mappers.CancellationDetailsMapper
import com.walmart.oms.order.valueobject.CancelDetails
import spock.lang.Specification

class CancellationDetailsMapperSpec extends Specification {

    def "test convertToValueObject"() {
        given:
        CancellationDetails cancellationDetails = CancellationDetails.builder()
                .cancelledBy(CancellationSource.OMS)
                .cancelledReasonCode("3")
                .cancelledReasonDescription("lost order")
                .build()
        when:
        CancelDetails cancelDetails = CancellationDetailsMapper.INSTANCE.convertToValueObject(cancellationDetails)
        then:
        cancelDetails.cancelledReasonDescription == "lost order"
        cancelDetails.cancelledReasonCode == "3"
        cancelDetails.cancelledBy == CancellationSource.OMS

    }

    def "test convertToValueObject for null"() {
        when:
        CancellationDetails cancellationDetails = CancellationDetailsMapper.INSTANCE.convertToValueObject(null)
        then:
        cancellationDetails == null
    }

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
