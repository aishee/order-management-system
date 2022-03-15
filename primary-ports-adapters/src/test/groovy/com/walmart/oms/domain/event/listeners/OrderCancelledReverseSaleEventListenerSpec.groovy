package com.walmart.oms.domain.event.listeners

import com.walmart.common.domain.type.Tenant
import com.walmart.common.domain.type.Vertical
import com.walmart.fms.order.aggregateroot.FmsOrder
import com.walmart.fms.order.valueobject.MarketPlaceInfo
import com.walmart.marketplace.order.domain.entity.type.Vendor
import com.walmart.oms.domain.error.exception.OMSBadRequestException
import com.walmart.oms.domain.event.messages.OrderCancelledDomainEventMessage
import com.walmart.oms.order.aggregateroot.OmsOrder
import com.walmart.oms.order.factory.OmsOrderFactory
import com.walmart.oms.order.gateway.IPricingGateway
import spock.lang.Specification

class OrderCancelledReverseSaleEventListenerSpec extends Specification {

    IPricingGateway pricingGateway = Mock()

    OmsOrderFactory omsOrderFactory = Mock()

    OrderCancelledReverseSaleEventListener orderCancelledReverseSaleEventListener

    String storeOrderId = UUID.randomUUID().toString()

    def setup() {
        orderCancelledReverseSaleEventListener = new OrderCancelledReverseSaleEventListener(
                omsOrderFactory,
                pricingGateway
        )
    }

    def "test reverse sale when order state is equal or before pick_complete"(){
        given:
        OrderCancelledDomainEventMessage orderCancelledDomainEventMessage = mockOrderCancelledDomainEventMessage();
        OmsOrder testOrder = MockPickCompleteOmsOrder()
        omsOrderFactory.getOmsOrderBySourceOrder(_ as String, _ as Tenant, _ as Vertical) >> testOrder

        when:
        orderCancelledReverseSaleEventListener.listen(orderCancelledDomainEventMessage)

        then:
        0 * pricingGateway.reverseSale(_ as OrderCancelledDomainEventMessage)
        thrown(OMSBadRequestException)

    }

    def "test reverse sale when order state is equal or later than epos_complete"(){
        given:
        OrderCancelledDomainEventMessage orderCancelledDomainEventMessage = mockOrderCancelledDomainEventMessage();
        OmsOrder testOrder = MockEposCompleteOmsOrder()
        omsOrderFactory.getOmsOrderBySourceOrder(_ as String, _ as Tenant, _ as Vertical) >> testOrder

        when:
        orderCancelledReverseSaleEventListener.listen(orderCancelledDomainEventMessage)

        then:
        1 * pricingGateway.reverseSale(_ as OrderCancelledDomainEventMessage)
    }

    private static OrderCancelledDomainEventMessage mockOrderCancelledDomainEventMessage(){
        return OrderCancelledDomainEventMessage.builder().sourceOrderId("1111")
                .tenant(Tenant.ASDA).vertical(Vertical.ASDAGR)
                .storeOrderId("12345")
                .storeOrderId("5755")
                .vendorOrderId("abcd-efgh").build()
    }

    OmsOrder MockPickCompleteOmsOrder() {
        OmsOrder omsOrder = getOmsOrder()
        omsOrder.orderState = OmsOrder.OrderStatus.PICK_COMPLETE.getName()
        return omsOrder
    }

    OmsOrder MockEposCompleteOmsOrder() {
        OmsOrder omsOrder = getOmsOrder()
        omsOrder.orderState = OmsOrder.OrderStatus.EPOS_COMPLETE.getName()
        return omsOrder
    }

    private OmsOrder getOmsOrder() {
        return OmsOrder.builder()
                .tenant(Tenant.ASDA)
                .vertical(Vertical.MARKETPLACE)
                .orderState(FmsOrder.OrderStatus.RECEIVED_AT_STORE.getName())
                .deliveryDate(new Date())
                .sourceOrderId("333333")
                .storeOrderId(storeOrderId)
                .storeId("4401")
                .build()
    }
}
