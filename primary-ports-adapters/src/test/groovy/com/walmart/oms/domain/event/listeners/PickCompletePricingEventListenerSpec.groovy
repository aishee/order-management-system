package com.walmart.oms.domain.event.listeners

import com.walmart.common.domain.type.Tenant
import com.walmart.common.domain.type.Vertical
import com.walmart.oms.domain.event.messages.PickCompleteDomainEventMessage
import com.walmart.oms.order.aggregateroot.OmsOrder
import com.walmart.oms.order.domain.OmsOrderPickCompleteDomainService
import com.walmart.oms.order.factory.OmsOrderFactory
import spock.lang.Specification

class PickCompletePricingEventListenerSpec extends Specification {

    OmsOrderPickCompleteDomainService omsOrderPickCompleteDomainService = Mock()
    OmsOrderFactory omsOrderFactory = Mock()

    PickCompletePricingEventListener pricingEventListener

    def setup() {
        pricingEventListener = new PickCompletePricingEventListener(
                omsOrderPickCompleteDomainService,
                omsOrderFactory
        )
    }

    def "test Pick complete pricing event listener"() {
        given:
        PickCompleteDomainEventMessage message = mockPickCompleteDomainEventMessage()
        OmsOrder omsOrder = getOmsOrder()
        omsOrderFactory.getOmsOrderBySourceOrder("1234", Tenant.ASDA, Vertical.ASDAGR) >> omsOrder

        when:
        pricingEventListener.listen(message)

        then:
        1 * omsOrderFactory.getOmsOrderBySourceOrder(_ as String, _ as Tenant, _ as Vertical)
    }

    private static PickCompleteDomainEventMessage mockPickCompleteDomainEventMessage() {
        return PickCompleteDomainEventMessage.builder().sourceOrderId("1234").tenant(Tenant.ASDA).vertical(Vertical.ASDAGR).build()
    }

    private static OmsOrder getOmsOrder() {
        return OmsOrder.builder()
                .sourceOrderId("1234")
                .storeOrderId("123456789")
                .storeId("4401")
                .deliveryDate(new Date())
                .build()
    }

}
