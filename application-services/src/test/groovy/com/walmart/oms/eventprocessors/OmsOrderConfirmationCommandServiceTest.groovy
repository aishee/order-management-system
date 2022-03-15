package com.walmart.oms.eventprocessors

import com.walmart.common.domain.type.DomainEventType
import com.walmart.common.domain.type.Domain
import com.walmart.common.metrics.MetricService
import com.walmart.common.domain.messaging.DomainEvent
import com.walmart.common.domain.messaging.DomainEventPublisher
import com.walmart.common.domain.type.Currency
import com.walmart.common.domain.type.FulfillmentType
import com.walmart.common.domain.type.Tenant
import com.walmart.common.domain.type.Vertical
import com.walmart.marketplace.order.domain.entity.type.Vendor
import com.walmart.marketplace.order.domain.valueobject.MarketPlaceOrderValueObject
import com.walmart.oms.commands.OrderConfirmationCommand
import com.walmart.oms.order.aggregateroot.OmsOrder
import com.walmart.oms.order.domain.entity.OmsOrderItem
import com.walmart.oms.domain.error.exception.OMSBadRequestException
import com.walmart.oms.order.factory.OmsOrderFactory
import com.walmart.oms.order.repository.IOmsOrderRepository
import com.walmart.oms.order.valueobject.ItemPriceInfo
import com.walmart.oms.order.valueobject.MarketPlaceInfo
import com.walmart.oms.order.valueobject.Money
import spock.lang.Specification

class OmsOrderConfirmationCommandServiceTest extends Specification {

    OmsOrderConfirmationCommandService omsOrderConfirmationCommandService

    OmsOrderFactory omsOrderFactory = Mock()

    IOmsOrderRepository omsOrderRepository = Mock()

    DomainEventPublisher omsDomainEventPublisher = Mock()

    String sourceOrderId = UUID.randomUUID().toString()

    String vendorOrderId = UUID.randomUUID().toString()

    MetricService metricService = Mock()

    def setup() {

        omsOrderConfirmationCommandService = new OmsOrderConfirmationCommandService(
                omsOrderFactory: omsOrderFactory,
                omsOrderRepository: omsOrderRepository,
                omsDomainEventPublisher: omsDomainEventPublisher,
                metricService: metricService)


    }

    def "Test OrderConfirmedAtStore with pre existing order"() {
        given:
        OmsOrder testOmsOrder = mockOmsOrder()
        OrderConfirmationCommand orderConfirmationCommand = mockOrderConfirmationCommand(sourceOrderId)
        omsOrderFactory.getOmsOrderBySourceOrder(_ as String, _ as Tenant, _ as Vertical) >> testOmsOrder

        when:
        omsOrderConfirmationCommandService.orderConfirmedAtStore(orderConfirmationCommand)

        then:
        1 * omsOrderRepository.save(_ as OmsOrder) >> { OmsOrder _omsOrder ->
            assert _omsOrder.getSourceOrderId() == sourceOrderId
            assert _omsOrder.getOrderState() == "RECD_AT_STORE"
            return testOmsOrder
        }

        1 * omsDomainEventPublisher.publish(_ as DomainEvent, _ as String) >> { DomainEvent _domainEvent, String _destination ->
            assert _destination == "OMS_ORDER_UPDATES"
            assert _domainEvent.source == Domain.OMS
            assert _domainEvent.key == testOmsOrder.id
            assert _domainEvent.name == DomainEventType.OMS_ORDER_CONFIRM
            assert _domainEvent.message != null
            Optional<MarketPlaceOrderValueObject> valueObject = _domainEvent.createObjectFromJson(MarketPlaceOrderValueObject.class)
            assert testOmsOrder.sourceOrderId == valueObject.get().sourceOrderId
            assert testOmsOrder.orderState == valueObject.get().orderState
            assert testOmsOrder.storeId == valueObject.get().storeId
            assert testOmsOrder.marketPlaceInfo.vendorOrderId == valueObject.get().vendorOrderId
            assert testOmsOrder.marketPlaceInfo.vendor == valueObject.get().vendorId
        }
    }

    OmsOrder mockOmsOrder() {

        String testCin = "4647474"
        OmsOrder omsOrder = OmsOrder.builder()
                .tenant(Tenant.ASDA)
                .vertical(Vertical.MARKETPLACE)
                .orderState("READY_FOR_STORE")
                .deliveryDate(new Date())
                .sourceOrderId(sourceOrderId)
                .storeId("4401")
                .storeOrderId("7373737")
                .fulfillmentType(FulfillmentType.INSTORE_PICKUP)
                .marketPlaceInfo(new MarketPlaceInfo(Vendor.UBEREATS, vendorOrderId))
                .spokeStoreId("4401")
                .build()

        omsOrder.addItem(OmsOrderItem.builder().quantity(2)
                .itemDescription("test description")
                .cin(testCin)
                .omsOrder(omsOrder)
                .itemPriceInfo(new ItemPriceInfo(new Money(BigDecimal.valueOf(2.5), Currency.GBP), new Money(BigDecimal.valueOf(5.0), Currency.GBP)))
                .id(UUID.randomUUID().toString())
                .build())

        return omsOrder
    }

    OrderConfirmationCommand mockOrderConfirmationCommand(String sourceOrderId) {
        return OrderConfirmationCommand.builder()
                .sourceOrderId(sourceOrderId)
                .tenant(Tenant.ASDA)
                .vertical(Vertical.MARKETPLACE)
                .build()
    }

    def " Test Confirm order without pre existing order"() {
        given:
        OmsOrder omsOrder = mockOmsOrder()
        OrderConfirmationCommand orderConfirmationCommand = mockOrderConfirmationCommand(sourceOrderId)
        omsOrderFactory.getOmsOrderBySourceOrder(_ as String, _ as Tenant, _ as Vertical) >> null

        when:
        omsOrderConfirmationCommandService.orderConfirmedAtStore(orderConfirmationCommand)

        then:
        thrown(OMSBadRequestException.class)
    }

    def " Test Confirm already CANCELLED order"() {
        given:
        OmsOrder testOmsOrder = mockCancelledOmsOrder()
        OrderConfirmationCommand orderConfirmationCommand = mockOrderConfirmationCommand(sourceOrderId)
        omsOrderFactory.getOmsOrderBySourceOrder(_ as String, _ as Tenant, _ as Vertical) >> testOmsOrder

        when:
        omsOrderConfirmationCommandService.orderConfirmedAtStore(orderConfirmationCommand)

        then:
        0 * omsOrderRepository.save(_ as OmsOrder) >> { OmsOrder _omsOrder ->
            assert _omsOrder.getSourceOrderId() == sourceOrderId
            assert _omsOrder.getOrderState() == "CANCELLED"
            return testOmsOrder
        }
        thrown(OMSBadRequestException)

    }

    def " Test Confirm already EPOSComplete order"() {
        given:
        OmsOrder testOmsOrder = mockPickCompleteOmsOrder()
        OrderConfirmationCommand orderConfirmationCommand = mockOrderConfirmationCommand(sourceOrderId)
        omsOrderFactory.getOmsOrderBySourceOrder(_ as String, _ as Tenant, _ as Vertical) >> testOmsOrder

        when:
        omsOrderConfirmationCommandService.orderConfirmedAtStore(orderConfirmationCommand)

        then:
        omsOrderRepository.save(_ as OmsOrder) >> { OmsOrder _omsOrder ->
            assert _omsOrder.getSourceOrderId() == sourceOrderId
            assert _omsOrder.getOrderState() == OmsOrder.OrderStatus.EPOS_COMPLETE.getName()
            return testOmsOrder
        }
        thrown(OMSBadRequestException)

    }

    def " Test Confirm already Delivered order"() {
        given:
        OmsOrder testOmsOrder = mockDeliveredOmsOrder()
        OrderConfirmationCommand orderConfirmationCommand = mockOrderConfirmationCommand(sourceOrderId)
        omsOrderFactory.getOmsOrderBySourceOrder(_ as String, _ as Tenant, _ as Vertical) >> testOmsOrder

        when:
        omsOrderConfirmationCommandService.orderConfirmedAtStore(orderConfirmationCommand)

        then:
        0 * omsOrderRepository.save(_ as OmsOrder) >> { OmsOrder _omsOrder ->
            assert _omsOrder.getSourceOrderId() == sourceOrderId
            assert _omsOrder.getOrderState() == OmsOrder.OrderStatus.DELIVERED.getName()
            return testOmsOrder
        }
        thrown(OMSBadRequestException)

    }

    OmsOrder mockPickCompleteOmsOrder() {
        OmsOrder mockOrder = mockOmsOrder()
        mockOrder.orderState = OmsOrder.OrderStatus.EPOS_COMPLETE.getName()
        return mockOrder
    }

    OmsOrder mockDeliveredOmsOrder() {
        OmsOrder mockOrder = mockOmsOrder()
        mockOrder.orderState = OmsOrder.OrderStatus.DELIVERED.getName()
        return mockOrder
    }

    OmsOrder mockCancelledOmsOrder() {
        OmsOrder mockOrder = mockOmsOrder()
        mockOrder.orderState = "CANCELLED"
        return mockOrder
    }
}
