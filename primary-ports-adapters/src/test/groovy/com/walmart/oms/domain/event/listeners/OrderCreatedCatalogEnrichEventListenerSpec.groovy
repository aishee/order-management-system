package com.walmart.oms.domain.event.listeners

import com.walmart.common.domain.type.Tenant
import com.walmart.common.domain.type.Vertical
import com.walmart.oms.domain.event.messages.OrderCreatedDomainEventMessage
import com.walmart.oms.order.aggregateroot.OmsOrder
import com.walmart.oms.order.domain.OmsOrderCreatedDomainService
import com.walmart.oms.order.factory.OmsOrderFactory
import spock.lang.Specification

class OrderCreatedCatalogEnrichEventListenerSpec extends Specification {

    OmsOrderFactory omsOrderFactory = Mock()
    OmsOrderCreatedDomainService omsOrderCreatedDomainService = Mock()

    OrderCreatedCatalogEnrichEventListener orderCreatedDomainEventListener

    def setup() {
        orderCreatedDomainEventListener = new OrderCreatedCatalogEnrichEventListener(
                omsOrderFactory, omsOrderCreatedDomainService
        )
    }

   def "test order created event listener"(){
       given:
       OrderCreatedDomainEventMessage orderCreatedDomainEventMessage = mockOrderCompleteDomainEventMessage()
       OmsOrder omsOrder = getOmsOrder()
       omsOrderFactory.getOmsOrderBySourceOrder("1111", Tenant.ASDA, Vertical.ASDAGR) >> omsOrder

       when:
       orderCreatedDomainEventListener.listen(orderCreatedDomainEventMessage)

       then:
       1 * omsOrderFactory.getOmsOrderBySourceOrder(_ as String, _ as Tenant, _ as Vertical)
   }

   private static OrderCreatedDomainEventMessage mockOrderCompleteDomainEventMessage(){
       return OrderCreatedDomainEventMessage.builder().sourceOrderId("1111")
               .tenant(Tenant.ASDA).vertical(Vertical.ASDAGR).build()
   }
   private static OmsOrder getOmsOrder() {
        return OmsOrder.builder()
                .sourceOrderId("1111")
                .storeOrderId("123456789")
                .storeId("4401")
                .deliveryDate(new Date())
                .build()
   }
}
