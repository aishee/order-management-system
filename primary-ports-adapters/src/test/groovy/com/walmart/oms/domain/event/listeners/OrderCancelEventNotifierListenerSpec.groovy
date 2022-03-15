package com.walmart.oms.domain.event.listeners

import com.walmart.common.domain.type.CancellationSource
import com.walmart.oms.domain.event.messages.OrderCancelledDomainEventMessage
import com.walmart.oms.order.aggregateroot.OmsOrder
import com.walmart.oms.order.domain.DefaultCancelledOrderDomainEventPublisher
import com.walmart.oms.order.domain.OmsStoreCancelledOrderDomainEventPublisher
import com.walmart.oms.order.domain.OmsVendorCancelledOrderDomainEventPublisher
import com.walmart.oms.order.domain.factory.OmsOrderCancelDomainEventPublisherFactory
import com.walmart.oms.order.factory.OmsOrderFactory
import com.walmart.oms.order.valueobject.CancelDetails
import spock.lang.Specification

class OrderCancelEventNotifierListenerSpec extends Specification {
    OmsOrderFactory omsOrderFactory = Mock()
    OmsOrderCancelDomainEventPublisherFactory omsOrderCancelDomainEventPublisherFactory = Mock()
    OrderCancelEventNotifierListener orderCancelEventNotifierListener;

    def setup() {
        orderCancelEventNotifierListener = new OrderCancelEventNotifierListener(omsOrderCancelDomainEventPublisherFactory, omsOrderFactory)
    }

    def "Test Cancel order from store"() {
        given:

        CancelDetails cancelDetails = CancelDetails.builder().cancelledBy(CancellationSource.STORE).build()
        OmsOrder testOmsOrder = getOmsOrder(cancelDetails)

        when:
        orderCancelEventNotifierListener.listen(mockOrderCancelledDomainEventMessage())

        then:
        1 * omsOrderFactory.getOmsOrderBySourceOrder(_, _, _) >> testOmsOrder

        1 * omsOrderCancelDomainEventPublisherFactory.getOrderCancelDomainEventPublisher(_ as CancellationSource) >> {
            CancellationSource source ->
                source == CancellationSource.STORE
                return Mock(OmsStoreCancelledOrderDomainEventPublisher.class)
        }
    }

    def "Test Cancel order from vendor"() {
        given:
        CancelDetails cancelDetails = CancelDetails.builder()
                .cancelledBy(CancellationSource.VENDOR).build()
        OmsOrder testOmsOrder = getOmsOrder(cancelDetails)

        when:
        orderCancelEventNotifierListener.listen(mockOrderCancelledDomainEventMessage())

        then:
        1 * omsOrderFactory.getOmsOrderBySourceOrder(_, _, _) >> testOmsOrder
        1 * omsOrderCancelDomainEventPublisherFactory.getOrderCancelDomainEventPublisher(_ as CancellationSource) >> {
            CancellationSource source ->
                source == CancellationSource.VENDOR
                return Mock(OmsVendorCancelledOrderDomainEventPublisher.class)
        }
    }

    def "Test Cancel order from OMS"() {
        given:
        CancelDetails cancelDetails = CancelDetails.builder()
                .cancelledBy(CancellationSource.OMS).build()
        OmsOrder testOmsOrder = getOmsOrder(cancelDetails)

        when:
        orderCancelEventNotifierListener.listen(mockOrderCancelledDomainEventMessage())

        then:
        1 * omsOrderFactory.getOmsOrderBySourceOrder(_, _, _) >> testOmsOrder
        1 * omsOrderCancelDomainEventPublisherFactory.getOrderCancelDomainEventPublisher(_ as CancellationSource) >> {
            CancellationSource source ->
                source == CancellationSource.OMS
                return Mock(DefaultCancelledOrderDomainEventPublisher.class)
        }
    }

    def "Test Cancel order from store - isCancelOrder true"() {
        given:

        CancelDetails cancelDetails = CancelDetails.builder().cancelledBy(CancellationSource.STORE).build()
        OmsOrder testOmsOrder = getOmsOrder(cancelDetails)

        when:
        orderCancelEventNotifierListener.listen(mockOrderCancelledDomainEventMessage_cancel())

        then:
        1 * omsOrderFactory.getOmsOrderBySourceOrder(_, _, _) >> testOmsOrder

        2 * omsOrderCancelDomainEventPublisherFactory.getOrderCancelDomainEventPublisher(_ as CancellationSource) >> {
            CancellationSource source ->
                source == CancellationSource.STORE
                return Mock(OmsStoreCancelledOrderDomainEventPublisher.class)
        }
    }

    def "Test Cancel order from vendor - isCancelOrder true"() {
        given:
        CancelDetails cancelDetails = CancelDetails.builder()
                .cancelledBy(CancellationSource.VENDOR).build()
        OmsOrder testOmsOrder = getOmsOrder(cancelDetails)

        when:
        orderCancelEventNotifierListener.listen(mockOrderCancelledDomainEventMessage_cancel())

        then:
        1 * omsOrderFactory.getOmsOrderBySourceOrder(_, _, _) >> testOmsOrder
        2 * omsOrderCancelDomainEventPublisherFactory.getOrderCancelDomainEventPublisher(_ as CancellationSource) >> {
            CancellationSource source ->
                source == CancellationSource.VENDOR
                return Mock(OmsVendorCancelledOrderDomainEventPublisher.class)
        }
    }

    def "Test Cancel order from OMS - isCancelOrder true"() {
        given:
        CancelDetails cancelDetails = CancelDetails.builder()
                .cancelledBy(CancellationSource.OMS).build()
        OmsOrder testOmsOrder = getOmsOrder(cancelDetails)

        when:
        orderCancelEventNotifierListener.listen(mockOrderCancelledDomainEventMessage_cancel())

        then:
        1 * omsOrderFactory.getOmsOrderBySourceOrder(_, _, _) >> testOmsOrder
        2 * omsOrderCancelDomainEventPublisherFactory.getOrderCancelDomainEventPublisher(_ as CancellationSource) >> {
            CancellationSource source ->
                source == CancellationSource.OMS
                return Mock(DefaultCancelledOrderDomainEventPublisher.class)
        }
    }

    private static OrderCancelledDomainEventMessage mockOrderCancelledDomainEventMessage() {
        return OrderCancelledDomainEventMessage.builder()
        .storeId("123")
        .vendorOrderId("123")
        .sourceOrderId("123")
        .isCancelOrder(false)
        .storeOrderId("123")
        .build()
    }

    private static OrderCancelledDomainEventMessage mockOrderCancelledDomainEventMessage_cancel() {
        return OrderCancelledDomainEventMessage.builder()
                .storeId("123")
                .vendorOrderId("123")
                .sourceOrderId("123")
                .isCancelOrder(true)
                .storeOrderId("123")
                .build()
    }

    private static OmsOrder getOmsOrder(CancelDetails cancelDetails) {
        return OmsOrder.builder()
                .sourceOrderId("1111")
                .storeOrderId("123456789")
                .storeId("4401")
                .deliveryDate(new Date())
                .cancelDetails(cancelDetails)
                .orderState("CANCELLED")
                .build()
    }
}
