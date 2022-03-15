package com.walmart.fms.converter

import com.walmart.common.domain.type.CancellationReason
import com.walmart.common.domain.type.CancellationSource
import com.walmart.common.domain.type.Tenant
import com.walmart.common.domain.type.Vertical
import com.walmart.common.domain.valueobject.CancellationDetails
import com.walmart.oms.commands.OmsCancelOrderCommand
import com.walmart.oms.converter.OmsCancelOrderCommandMapper
import com.walmart.oms.domain.event.messages.OmsOrderEnrichmentFailureEventMessage
import spock.lang.Specification

class OmsCancelOrderCommandMapperSpec extends Specification {
    def "test mapToCommand method"() {

        given:

        OmsOrderEnrichmentFailureEventMessage omsOrderEnrichmentFailureEventMessage = OmsOrderEnrichmentFailureEventMessage.builder()
                .vertical(Vertical.MARKETPLACE)
                .tenant(Tenant.ASDA)
                .sourceOrderId("abcd")
                .build()

        CancellationDetails cancelDetails = CancellationDetails.builder()
                .cancelledBy(CancellationSource.OMS)
                .cancelledReasonCode(CancellationReason.IRO_FAILURE.getCode())
                .cancelledReasonDescription(CancellationReason.IRO_FAILURE.getDescription())
                .build()
        when:
        OmsCancelOrderCommand command = OmsCancelOrderCommandMapper.INSTANCE.mapToOmsCancelOrderCommand(omsOrderEnrichmentFailureEventMessage)
        then:
        command.getCancellationDetails() == cancelDetails
        omsOrderEnrichmentFailureEventMessage.getSourceOrderId() == command.getSourceOrderId()
        omsOrderEnrichmentFailureEventMessage.getTenant() == command.getTenant()
        omsOrderEnrichmentFailureEventMessage.getVertical() == command.getVertical()
    }
}
