package com.walmart.oms.domain

import com.walmart.common.domain.messaging.DomainEvent
import com.walmart.common.domain.messaging.DomainEventPublisher
import com.walmart.common.domain.type.Currency
import com.walmart.common.domain.type.FulfillmentType
import com.walmart.common.domain.type.Tenant
import com.walmart.common.domain.type.Vertical
import com.walmart.marketplace.order.domain.entity.type.Vendor
import com.walmart.oms.order.aggregateroot.OmsOrder
import com.walmart.oms.order.domain.DefaultCancelledOrderDomainEventPublisher
import com.walmart.oms.order.domain.OmsOrderCancelDomainEventPublisher
import com.walmart.oms.order.domain.entity.OmsOrderItem
import com.walmart.oms.order.valueobject.ItemPriceInfo
import com.walmart.oms.order.valueobject.MarketPlaceInfo
import com.walmart.oms.order.valueobject.Money
import spock.lang.Specification

class DefaultCancelledOrderDomainServiceSpec extends Specification {
    DefaultCancelledOrderDomainEventPublisher defaultCancelledOrderDomainService

    DomainEventPublisher omsDomainEventPublisher = Mock()

    def setup() {
        defaultCancelledOrderDomainService = new DefaultCancelledOrderDomainEventPublisher(omsDomainEventPublisher)
    }

    def "Test for oms cancelled order"() {
        when:
        defaultCancelledOrderDomainService.sendCancelOrderEvent(mockOmsOrder())
        then:
        1 * omsDomainEventPublisher.publish(_ as DomainEvent, _ as String) >> { DomainEvent domainEvent, String destination ->
            assert destination == "OMS_ORDER_UPDATES"
        }
    }

    OmsOrder mockOmsOrder() {
        OmsOrder omsOrder = OmsOrder.builder()
                .tenant(Tenant.ASDA)
                .vertical(Vertical.MARKETPLACE)
                .orderState("CREATED")
                .deliveryDate(new Date())
                .sourceOrderId("q2hsd")
                .storeOrderId("12345678")
                .storeId("4401")
                .fulfillmentType(FulfillmentType.INSTORE_PICKUP)
                .marketPlaceInfo(new MarketPlaceInfo(Vendor.UBEREATS, "1bqbwq1ou2"))
                .spokeStoreId("4401")
                .build()

        omsOrder.addItem(OmsOrderItem.builder().quantity(2)
                .itemDescription("test description")
                .cin("12121")
                .omsOrder(omsOrder)
                .itemPriceInfo(new ItemPriceInfo(
                        new Money(BigDecimal.valueOf(2.5),
                                Currency.GBP),
                        new Money(BigDecimal.valueOf(5.0),
                                Currency.GBP)))
                .id(UUID.randomUUID().toString())
                .build())
        return omsOrder

    }

}
