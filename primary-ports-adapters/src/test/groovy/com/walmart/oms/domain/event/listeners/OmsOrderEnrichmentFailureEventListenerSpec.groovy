package com.walmart.oms.domain.event.listeners

import com.walmart.common.domain.event.processing.EventGeneratorService
import com.walmart.common.domain.type.Tenant
import com.walmart.common.domain.type.Vertical
import com.walmart.marketplace.order.domain.entity.type.Vendor
import com.walmart.oms.domain.event.messages.OmsOrderEnrichmentFailureEventMessage
import com.walmart.oms.eventprocessors.OmsCancelOrderCommandService
import com.walmart.oms.order.aggregateroot.OmsOrder
import com.walmart.oms.order.valueobject.MarketPlaceInfo
import spock.lang.Specification

class OmsOrderEnrichmentFailureEventListenerSpec extends Specification {
    OmsCancelOrderCommandService omsCancelOrderCommandService = Mock()
    EventGeneratorService eventGeneratorService = Mock()

    OmsOrderEnrichmentFailureEventListener omsOrderEnrichmentFailureEventListener

    def setup() {
        omsOrderEnrichmentFailureEventListener = new OmsOrderEnrichmentFailureEventListener(
                omsCancelOrderCommandService, eventGeneratorService)
    }

    def "test order enrichment failure event listener"() {
        given:
        OmsOrderEnrichmentFailureEventMessage message = mockMessage()
        OmsOrder omsOrder = getOmsOrder()

        when:
        omsOrderEnrichmentFailureEventListener.listen(message)

        then:
        1 * omsCancelOrderCommandService.cancelOrder(_) >> omsOrder
        1 * eventGeneratorService.publishApplicationEvent(_)

    }

    def "test order enrichment failure event listener when storeId is null"() {
        given:
        OmsOrderEnrichmentFailureEventMessage message = mockMessage()
        OmsOrder omsOrder = getOmsOrder()
        omsOrder.storeId = null

        when:
        omsOrderEnrichmentFailureEventListener.listen(message)

        then:
        1 * omsCancelOrderCommandService.cancelOrder(_) >> omsOrder
        0 * eventGeneratorService.publishApplicationEvent(_)
    }

    def "test order enrichment failure event listener when storeOrderId is null"() {
        given:
        OmsOrderEnrichmentFailureEventMessage message = mockMessage()
        OmsOrder omsOrder = getOmsOrder()
        omsOrder.storeOrderId = null

        when:
        omsOrderEnrichmentFailureEventListener.listen(message)

        then:
        1 * omsCancelOrderCommandService.cancelOrder(_) >> omsOrder
        0 * eventGeneratorService.publishApplicationEvent(_)
    }

    private static OmsOrderEnrichmentFailureEventMessage mockMessage() {
        return OmsOrderEnrichmentFailureEventMessage.builder().sourceOrderId("1111")
                .tenant(Tenant.ASDA).vertical(Vertical.ASDAGR).build()
    }

    private static OmsOrder getOmsOrder() {
        return OmsOrder.builder()
                .sourceOrderId("1111")
                .storeOrderId("123456789")
                .storeId("4401")
                .orderState("CANCELLED")
                .deliveryDate(new Date())
                .marketPlaceInfo(MarketPlaceInfo.builder().
                        vendor(Vendor.UBEREATS)
                        .vendorOrderId("ac")
                        .build())
                .build()
    }

}
