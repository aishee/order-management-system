package com.walmart.oms.eventprocessors

import com.walmart.common.domain.messaging.DomainEvent
import com.walmart.common.domain.messaging.DomainEventPublisher
import com.walmart.common.domain.type.Currency
import com.walmart.common.domain.type.DomainEventType
import com.walmart.common.domain.type.Domain
import com.walmart.common.domain.type.FulfillmentType
import com.walmart.common.domain.type.Tenant
import com.walmart.common.domain.type.Vertical
import com.walmart.marketplace.order.domain.entity.type.Vendor
import com.walmart.marketplace.order.domain.valueobject.MarketPlaceOrderValueObject
import com.walmart.oms.commands.DeliveredOrderCommand
import com.walmart.oms.order.aggregateroot.OmsOrder
import com.walmart.oms.order.domain.entity.OmsOrderItem
import com.walmart.oms.domain.error.exception.OMSBadRequestException
import com.walmart.oms.order.factory.OmsOrderFactory
import com.walmart.oms.order.repository.IOmsOrderRepository
import com.walmart.oms.order.valueobject.ItemPriceInfo
import com.walmart.oms.order.valueobject.MarketPlaceInfo
import com.walmart.oms.order.valueobject.Money
import spock.lang.Specification

class OmsDeliveredCommandServiceTest extends Specification {
    String testCin = "4647474"

    OmsDeliveredCommandService omsDeliveredCommandService

    private OmsOrderFactory omsOrderFactory = Mock()

    private IOmsOrderRepository omsOrderRepository = Mock()

    DomainEventPublisher omsDomainEventPublisher = Mock()

    String sourceOrderId = UUID.randomUUID().toString()
    String vendorOrderId = UUID.randomUUID().toString()

    def setup() {
        omsDeliveredCommandService = new OmsDeliveredCommandService(
                omsOrderRepository: omsOrderRepository,
                omsOrderFactory: omsOrderFactory,
                omsDomainEventPublisher: omsDomainEventPublisher)
    }

    def " Test DeliverOrder with pre existing order"() {
        given:
        OmsOrder testOmsOrder = mockOmsOrder()
        DeliveredOrderCommand deliveredOrderCommand = getDeliveredOrderCommand()
        omsOrderFactory.getOmsOrderBySourceOrder(_ as String, _ as Tenant, _ as Vertical) >> testOmsOrder

        when:
        omsDeliveredCommandService.deliverOrder(deliveredOrderCommand)

        then:
        1 * omsOrderRepository.save(_ as OmsOrder) >> { OmsOrder _omsOrder ->
            assert _omsOrder.getSourceOrderId() == sourceOrderId
            assert _omsOrder.getOrderState() == "NO_PENDING_ACTION"
            return testOmsOrder
        }
    }

    def " Test DeliverOrder with pre existing non marketplace order "() {
        given:
        OmsOrder testOmsOrder = mockNonMarketPlaceOmsOrder()

        DeliveredOrderCommand deliveredOrderCommand = DeliveredOrderCommand.builder()
                .data(DeliveredOrderCommand.DeliveredOrderCommandData.builder()
                        .sourceOrderId(sourceOrderId).storeId("4401")
                        .tenant(Tenant.ASDA).vertical(Vertical.ASDAGR).build()).build()

        omsOrderFactory.getOmsOrderBySourceOrder(_ as String, _ as Tenant, _ as Vertical) >> testOmsOrder

        when:
        omsDeliveredCommandService.deliverOrder(deliveredOrderCommand)

        then:
        1 * omsOrderRepository.save(_ as OmsOrder) >> { OmsOrder _omsOrder ->
            assert _omsOrder.getSourceOrderId() == sourceOrderId
            assert _omsOrder.getOrderState() == "DELIVERED"
            return testOmsOrder
        }

        1 * omsDomainEventPublisher.publish(_ as DomainEvent, _ as String) >> { DomainEvent _domainEvent, String _destination ->
            assert _destination == "OMS_ORDER_UPDATES"
            assert _domainEvent.source == Domain.OMS
            assert _domainEvent.key == testOmsOrder.id
            assert _domainEvent.name == DomainEventType.OMS_ORDER_DELIVERED
            assert _domainEvent.message != null
            Optional<MarketPlaceOrderValueObject> valueObject = _domainEvent.createObjectFromJson(MarketPlaceOrderValueObject.class)
            assert testOmsOrder.sourceOrderId == valueObject.get().sourceOrderId
            assert testOmsOrder.orderState == valueObject.get().orderState
            assert testOmsOrder.storeId == valueObject.get().storeId
            assert testOmsOrder.marketPlaceInfo.vendorOrderId == valueObject.get().vendorOrderId
            assert testOmsOrder.marketPlaceInfo.vendor == valueObject.get().vendorId

        }
    }

    def " Test DeliverOrder without pre existing order"() {
        given:
        OmsOrder testOmsOrder = mockOmsOrder()
        DeliveredOrderCommand deliveredOrderCommand = getDeliveredOrderCommand()
        omsOrderFactory.getOmsOrderBySourceOrder(_ as String, _ as Tenant, _ as Vertical) >> null

        when:
        omsDeliveredCommandService.deliverOrder(deliveredOrderCommand)

        then:
        0 * omsOrderRepository.save(_ as OmsOrder) >> { OmsOrder _omsOrder ->
            assert _omsOrder.getSourceOrderId() == sourceOrderId
            assert _omsOrder.getOrderState() == "DELIVERED"
            return testOmsOrder
        }
    }

    def " Test DeliverOrder with Cancel existing order"() {
        given:
        OmsOrder testOmsOrder = mockCancelOmsOrder()

        DeliveredOrderCommand deliveredOrderCommand = getDeliveredOrderCommand()

        omsOrderFactory.getOmsOrderBySourceOrder(_ as String, _ as Tenant, _ as Vertical) >> testOmsOrder

        when:
        omsDeliveredCommandService.deliverOrder(deliveredOrderCommand)
        then:
        thrown(OMSBadRequestException)
    }

    def " Test DeliverdeliverOrderOrder with  existing Delivered order"() {
        given:
        OmsOrder testOmsOrder = mockDeliveredOmsOrder()
        DeliveredOrderCommand deliveredOrderCommand = getDeliveredOrderCommand()
        omsOrderFactory.getOmsOrderBySourceOrder(_ as String, _ as Tenant, _ as Vertical) >> testOmsOrder

        when:
        omsDeliveredCommandService.deliverOrder(deliveredOrderCommand)
        then:
        thrown(OMSBadRequestException)
    }

    OmsOrder mockCancelOmsOrder() {
        OmsOrder omsOrder = mockOmsOrder()
        omsOrder.orderState = OmsOrder.OrderStatus.CANCELLED.getName();
        return omsOrder
    }

    OmsOrder mockDeliveredOmsOrder() {
        OmsOrder omsOrder = mockOmsOrder()
        omsOrder.orderState = OmsOrder.OrderStatus.DELIVERED.getName();
        return omsOrder
    }

    private DeliveredOrderCommand getDeliveredOrderCommand() {
        return DeliveredOrderCommand.builder()
                .data(DeliveredOrderCommand.DeliveredOrderCommandData.builder()
                        .sourceOrderId(sourceOrderId).storeId("4401")
                        .tenant(Tenant.ASDA).vertical(Vertical.MARKETPLACE).build()).build()
    }

    OmsOrder mockOmsOrder() {
        OmsOrder omsOrder = OmsOrder.builder()
                .tenant(Tenant.ASDA)
                .vertical(Vertical.MARKETPLACE)
                .orderState("EPOS_COMPLETE")
                .deliveryDate(new Date())
                .sourceOrderId(sourceOrderId)
                .storeOrderId("12345678")
                .storeId("4401")
                .fulfillmentType(FulfillmentType.INSTORE_PICKUP)
                .marketPlaceInfo(new MarketPlaceInfo(Vendor.UBEREATS, vendorOrderId))
                .spokeStoreId("4401")
                .build()

        omsOrder.addItem(getOmsOrderItem(omsOrder))
        return omsOrder

    }

    OmsOrder mockNonMarketPlaceOmsOrder() {
        OmsOrder omsOrder = OmsOrder.builder()
                .tenant(Tenant.ASDA)
                .vertical(Vertical.ASDAGR)
                .orderState("EPOS_COMPLETE")
                .deliveryDate(new Date())
                .sourceOrderId(sourceOrderId)
                .storeOrderId("123456789")
                .storeId("4401")
                .fulfillmentType(FulfillmentType.INSTORE_PICKUP)
                .marketPlaceInfo(new MarketPlaceInfo(Vendor.UBEREATS, vendorOrderId))
                .spokeStoreId("4401")
                .build()

        omsOrder.addItem(getOmsOrderItem(omsOrder))
        return omsOrder

    }

    private OmsOrderItem getOmsOrderItem(OmsOrder omsOrder) {
        return OmsOrderItem.builder().quantity(2)
                .itemDescription("test description")
                .cin(testCin)
                .omsOrder(omsOrder)
                .itemPriceInfo(new ItemPriceInfo(new Money(BigDecimal.valueOf(2.5), Currency.GBP), new Money(BigDecimal.valueOf(5.0), Currency.GBP)))
                .id(UUID.randomUUID().toString())
                .build()
    }
}
