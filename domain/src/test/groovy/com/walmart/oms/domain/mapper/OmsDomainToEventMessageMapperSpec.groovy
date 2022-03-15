package com.walmart.oms.domain.mapper

import com.walmart.common.domain.type.Tenant
import com.walmart.common.domain.type.Vertical
import com.walmart.oms.domain.event.messages.DwhOrderEventMessage
import com.walmart.oms.domain.event.messages.OrderCancelledDomainEventMessage
import com.walmart.oms.domain.event.messages.OrderCreatedDomainEventMessage
import com.walmart.oms.domain.event.messages.PickCompleteDomainEventMessage
import com.walmart.oms.order.aggregateroot.OmsOrder
import com.walmart.oms.order.gateway.orderservice.OrdersEvent
import spock.lang.Specification

import java.lang.reflect.Constructor
import java.lang.reflect.InvocationTargetException

class OmsDomainToEventMessageMapperSpec extends Specification {

    def "Pick Complete message event from OmsOrder"() {

        given:
        OmsOrder omsOrder = mockOmsOrder()

        when:
        PickCompleteDomainEventMessage pickCompleteDomainEventMessage = OmsDomainToEventMessageMapper.mapToPickCompleteDomainEventMessage(omsOrder)

        then:
        pickCompleteDomainEventMessage.getSourceOrderId().equalsIgnoreCase("1234")
        pickCompleteDomainEventMessage.getVertical() == Vertical.ASDAGR
        pickCompleteDomainEventMessage.getTenant() == Tenant.ASDA
        pickCompleteDomainEventMessage.toString() != null

    }

    def "get Order Created message event from OmsOrder"() {

        given:
        OmsOrder omsOrder = mockOmsOrder()

        when:
        OrderCreatedDomainEventMessage orderCreatedDomainEventMessage = OmsDomainToEventMessageMapper.mapToOrderCreatedDomainEventMessage(omsOrder)

        then:
        orderCreatedDomainEventMessage.getSourceOrderId().equalsIgnoreCase("1234")
        orderCreatedDomainEventMessage.getVertical() == Vertical.ASDAGR
        orderCreatedDomainEventMessage.getTenant() == Tenant.ASDA
    }

    def "get Order Cancelled message event from OmsOrder"() {
        given:
        OmsOrder omsOrder = mockOmsOrder()

        when:
        OrderCancelledDomainEventMessage orderCancelledDomainEventMessage =
                OmsDomainToEventMessageMapper.mapToOrderCancelledDomainEventMessage(omsOrder)

        then:
        orderCancelledDomainEventMessage.getSourceOrderId().equalsIgnoreCase("1234")
        orderCancelledDomainEventMessage.getVertical() == Vertical.ASDAGR
        orderCancelledDomainEventMessage.getTenant() == Tenant.ASDA
    }

    def "test for exception"() {
        given:
        Constructor<OmsDomainToEventMessageMapper> constructor = OmsDomainToEventMessageMapper.class.getDeclaredConstructor()
        constructor.setAccessible(true)

        when:
        constructor.newInstance()

        then:
        thrown InvocationTargetException

    }

    def "test PickCompleteDomainEventMessage constructor"() {
        when:
        PickCompleteDomainEventMessage completeDomainEventMessage = new PickCompleteDomainEventMessage()

        then:
        completeDomainEventMessage != null
    }

    def "test PickCompleteDomainEventMessage builder"() {
        when:
        PickCompleteDomainEventMessage completeDomainEventMessage = PickCompleteDomainEventMessage.builder()
                .sourceOrderId("source id").tenant(Tenant.ASDA).vertical(Vertical.ASDAGR)
                .build()

        then:
        completeDomainEventMessage != null
        completeDomainEventMessage.tenant == Tenant.ASDA
        completeDomainEventMessage.toString() != null
    }

    def "test OrderCreatedDomainEventMessage constructor"() {
        when:
        OrderCreatedDomainEventMessage createdDomainEventMessage = new OrderCreatedDomainEventMessage()

        then:
        createdDomainEventMessage != null
    }

    def "test OrderCancelledDomainEventMessage constructor"() {
        when:
        OrderCancelledDomainEventMessage cancelledDomainEventMessage = new OrderCancelledDomainEventMessage()

        then:
        cancelledDomainEventMessage != null
    }

    def "test OrderCancelledDomainEventMessage Builder"() {
        when:
        OrderCancelledDomainEventMessage cancelledDomainEventMessage = OrderCancelledDomainEventMessage.builder()
                .storeId("storeId").storeOrderId("storeOrderId").vendorOrderId("vendorOrderId")
                .isCancelOrder(false)
                .build()

        then:
        cancelledDomainEventMessage != null
        cancelledDomainEventMessage.toString() != null

    }

    def "Get dwh Order Event Message"() {
        given:
        OrdersEvent<com.walmart.services.oms.order.common.model.OmsOrder> orderOrdersEvent = mockOrdersEvent()

        when:
        DwhOrderEventMessage dwhOrderEventMessage =
                OmsDomainToEventMessageMapper.mapToDwhOrderEventMessage(orderOrdersEvent, "1234")

        then:
        dwhOrderEventMessage.getStoreOrderId().equalsIgnoreCase("1234")
        dwhOrderEventMessage.getOmsOrderOrdersEvent().getEventPayload() != null
    }

    OrdersEvent<com.walmart.services.oms.order.common.model.OmsOrder> mockOrdersEvent() {
        OrdersEvent<com.walmart.services.oms.order.common.model.OmsOrder> orderOrdersEvent = new OrdersEvent<>()
        orderOrdersEvent.eventPayload = mockOmsOrder()
        return orderOrdersEvent
    }

    OmsOrder mockOmsOrder() {
        OmsOrder omsOrder = Mock()
        omsOrder.getSourceOrderId() >> "1234"
        omsOrder.getVertical() >> Vertical.ASDAGR
        omsOrder.getTenant() >> Tenant.ASDA
        return omsOrder
    }

}
