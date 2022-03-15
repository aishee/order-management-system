package com.walmart.fms.eventprocessors

import com.walmart.common.domain.messaging.DomainEvent
import com.walmart.common.domain.messaging.DomainEventPublisher
import com.walmart.common.domain.type.Currency
import com.walmart.common.domain.type.DomainEventType
import com.walmart.common.domain.type.Domain
import com.walmart.common.domain.type.SubstitutionOption
import com.walmart.common.domain.type.Tenant
import com.walmart.common.domain.type.Vertical
import com.walmart.fms.commands.FmsPickStartedOrderCommand
import com.walmart.fms.domain.error.exception.FMSBadRequestException
import com.walmart.fms.order.aggregateroot.FmsOrder
import com.walmart.fms.order.domain.entity.FmsOrderItem
import com.walmart.fms.order.factory.FmsOrderFactory
import com.walmart.fms.order.repository.IFmsOrderRepository
import com.walmart.fms.order.valueobject.ItemPriceInfo
import com.walmart.fms.order.valueobject.MarketPlaceInfo
import com.walmart.fms.order.valueobject.Money
import com.walmart.fms.order.valueobject.events.FmsOrderValueObject
import com.walmart.marketplace.order.domain.entity.type.Vendor
import spock.lang.Specification

class FmsPickStartedCommandServiceTest extends Specification {
    FmsPickStartedCommandService fmsPickStartedCommandService
    private FmsOrderFactory fmsOrderFactory = Mock()

    private IFmsOrderRepository fmsOrderRepository = Mock()

    DomainEventPublisher fmsDomainEventPublisher = Mock()

    String storeOrderId = UUID.randomUUID().toString()
    String vendorOrderId = UUID.randomUUID().toString()

    def setup() {
        fmsPickStartedCommandService = new FmsPickStartedCommandService(
                fmsOrderRepository: fmsOrderRepository,
                fmsDomainEventPublisher: fmsDomainEventPublisher,
                fmsOrderFactory: fmsOrderFactory)
    }

    def " Test Pick Started Order with pre existing order"() {
        given:
        FmsOrder testFmsOrder = mockFmsOrder()

        FmsPickStartedOrderCommand fmsPickStartedOrderCommand = mockPickStartedCommand(storeOrderId)

        fmsOrderFactory.getFmsOrderByStoreOrder(_ as String) >> testFmsOrder

        when:
        fmsPickStartedCommandService.orderPickStartedStore(fmsPickStartedOrderCommand)

        then:
        1 * fmsOrderRepository.save(_ as FmsOrder) >> { FmsOrder _fmsOrder ->
            assert _fmsOrder.getStoreOrderId() == storeOrderId
            assert _fmsOrder.getOrderState() == FmsOrder.OrderStatus.PICKING_STARTED.getName()
            assert _fmsOrder.orderTimestamps.pickingStartTime != null
            return testFmsOrder
        }

        1 * fmsDomainEventPublisher.publish(_ as DomainEvent, _ as String) >> { DomainEvent _domainEvent, String _destination ->
            assert _destination == "FMS_ORDER_UPDATES"
            assert _domainEvent.source == Domain.FMS
            assert _domainEvent.key == testFmsOrder.id
            assert _domainEvent.name == DomainEventType.FMS_ORDER_PICK_STARTED
            assert _domainEvent.message != null
            Optional<FmsOrderValueObject> valueObject = _domainEvent.createObjectFromJson(FmsOrderValueObject.class)
            assert testFmsOrder.sourceOrderId == valueObject.get().sourceOrderId
            assert testFmsOrder.orderState == valueObject.get().orderState
            assert testFmsOrder.storeId == valueObject.get().storeId
            assert testFmsOrder.marketPlaceInfo.vendorOrderId == valueObject.get().marketPlaceInfo.vendorOrderId
            assert testFmsOrder.marketPlaceInfo.vendor == valueObject.get().marketPlaceInfo.vendor
            assert testFmsOrder.deliveryDate == valueObject.get().deliveryDate
            assert testFmsOrder.authStatus == valueObject.get().authStatus
            assert testFmsOrder.tenant == valueObject.get().tenant
            assert testFmsOrder.pickupLocationId == valueObject.get().pickupLocationId
            assert testFmsOrder.vertical == valueObject.get().vertical
            assert testFmsOrder.fmsOrderItems[0].quantity == valueObject.get().fmsOrderItemvalueObjectList[0].quantity
            assert testFmsOrder.fmsOrderItems[0].consumerItemNumber == valueObject.get().fmsOrderItemvalueObjectList[0].cin

        }

    }

    def " Test Pick Started Order with Cancel order"() {
        given:
        FmsOrder testFmsOrder = mockCancelledFmsOrder()

        FmsPickStartedOrderCommand fmsPickStartedOrderCommand = mockPickStartedCommand(storeOrderId)

        fmsOrderFactory.getFmsOrderByStoreOrder(_ as String) >> testFmsOrder

        when:
        fmsPickStartedCommandService.orderPickStartedStore(fmsPickStartedOrderCommand)

        then:
        fmsOrderRepository.save(_ as FmsOrder) >> { FmsOrder _fmsOrder ->
            assert _fmsOrder.getStoreOrderId() == storeOrderId
            assert _fmsOrder.getOrderState() == FmsOrder.OrderStatus.CANCELLED.getName()
            return testFmsOrder
        }
        fmsDomainEventPublisher.publish(_ as DomainEvent, _ as String) >> { DomainEvent _domainEvent, String _destination ->
            assert _destination == null
        }
        thrown(FMSBadRequestException)
    }

    def " Test Pick Started Order with Delivered order"() {
        given:
        FmsOrder testFmsOrder = mockDeliveredFmsOrder()

        FmsPickStartedOrderCommand fmsPickStartedOrderCommand = mockPickStartedCommand(storeOrderId)

        fmsOrderFactory.getFmsOrderByStoreOrder(_ as String) >> testFmsOrder

        when:
        fmsPickStartedCommandService.orderPickStartedStore(fmsPickStartedOrderCommand)

        then:
        fmsOrderRepository.save(_ as FmsOrder) >> { FmsOrder _fmsOrder ->
            assert _fmsOrder.getStoreOrderId() == storeOrderId
            assert _fmsOrder.getOrderState() == FmsOrder.OrderStatus.DELIVERED.getName()
            return testFmsOrder
        }
        fmsDomainEventPublisher.publish(_ as DomainEvent, _ as String) >> { DomainEvent _domainEvent, String _destination ->
            assert _destination == null
        }
        thrown(FMSBadRequestException)
    }

    def " Test Pick Started Order with Pick Complete order"() {
        given:
        FmsOrder testFmsOrder = mockPickCompleteFmsOrder()

        FmsPickStartedOrderCommand fmsPickStartedOrderCommand = mockPickStartedCommand(storeOrderId)

        fmsOrderFactory.getFmsOrderByStoreOrder(_ as String) >> testFmsOrder

        when:
        fmsPickStartedCommandService.orderPickStartedStore(fmsPickStartedOrderCommand)

        then:
        fmsOrderRepository.save(_ as FmsOrder) >> { FmsOrder _fmsOrder ->
            assert _fmsOrder.getStoreOrderId() == storeOrderId
            assert _fmsOrder.getOrderState() == FmsOrder.OrderStatus.PICK_COMPLETE.getName()
            return testFmsOrder
        }
        fmsDomainEventPublisher.publish(_ as DomainEvent, _ as String) >> { DomainEvent _domainEvent, String _destination ->
            assert _destination == null
        }
        thrown(FMSBadRequestException)
    }

    def " Test Pick Started Order with pre existing Order Confirmation order"() {
        given:
        FmsOrder testFmsOrder = mockOrderConfirmationFmsOrder()

        FmsPickStartedOrderCommand fmsPickStartedOrderCommand = mockPickStartedCommand(storeOrderId)

        fmsOrderFactory.getFmsOrderByStoreOrder(_ as String) >> testFmsOrder

        when:
        fmsPickStartedCommandService.orderPickStartedStore(fmsPickStartedOrderCommand)

        then:
        1 * fmsOrderRepository.save(_ as FmsOrder) >> { FmsOrder _fmsOrder ->
            assert _fmsOrder.getStoreOrderId() == storeOrderId
            assert _fmsOrder.getOrderState() == FmsOrder.OrderStatus.PICKING_STARTED.getName()
            return testFmsOrder
        }

        1 * fmsDomainEventPublisher.publish(_ as DomainEvent, _ as String) >> { DomainEvent _domainEvent, String _destination ->
            assert _destination == "FMS_ORDER_UPDATES"
            assert _domainEvent.source == Domain.FMS
            assert _domainEvent.key == testFmsOrder.id
            assert _domainEvent.name == DomainEventType.FMS_ORDER_PICK_STARTED
            assert _domainEvent.message != null
            Optional<FmsOrderValueObject> valueObject = _domainEvent.createObjectFromJson(FmsOrderValueObject.class)
            assert testFmsOrder.sourceOrderId == valueObject.get().sourceOrderId
            assert testFmsOrder.orderState == valueObject.get().orderState
            assert testFmsOrder.storeId == valueObject.get().storeId
            assert testFmsOrder.marketPlaceInfo.vendorOrderId == valueObject.get().marketPlaceInfo.vendorOrderId
            assert testFmsOrder.marketPlaceInfo.vendor == valueObject.get().marketPlaceInfo.vendor
            assert testFmsOrder.deliveryDate == valueObject.get().deliveryDate
            assert testFmsOrder.authStatus == valueObject.get().authStatus
            assert testFmsOrder.tenant == valueObject.get().tenant
            assert testFmsOrder.pickupLocationId == valueObject.get().pickupLocationId
            assert testFmsOrder.vertical == valueObject.get().vertical
            assert testFmsOrder.fmsOrderItems[0].quantity == valueObject.get().fmsOrderItemvalueObjectList[0].quantity
            assert testFmsOrder.fmsOrderItems[0].consumerItemNumber == valueObject.get().fmsOrderItemvalueObjectList[0].cin

        }

    }

    FmsOrder mockFmsOrder() {

        String testCin = "4647475"
        FmsOrder fmsOrder = getFmsOrder()

        fmsOrder.addItem(FmsOrderItem.builder().quantity(2)
                .consumerItemNumber(testCin)
                .fmsOrder(fmsOrder)
                .itemPriceInfo(new ItemPriceInfo(new Money(BigDecimal.valueOf(2.5), Currency.GBP)))
                .id(UUID.randomUUID().toString())
                .substitutionOption(SubstitutionOption.DO_NOT_SUBSTITUTE)
                .build())

        return fmsOrder
    }

    private FmsOrder getFmsOrder() {
        return FmsOrder.builder()
                .tenant(Tenant.ASDA)
                .vertical(Vertical.MARKETPLACE)
                .orderState("READY_FOR_STORE")
                .deliveryDate(new Date())
                .sourceOrderId("44443333")
                .storeOrderId(storeOrderId)
                .storeId("4401")
                .marketPlaceInfo(new MarketPlaceInfo(Vendor.UBEREATS, vendorOrderId))
                .build()
    }

    FmsPickStartedOrderCommand mockPickStartedCommand(String storeOrderId) {
        return FmsPickStartedOrderCommand.builder()
                .storeOrderId(storeOrderId)
                .build()
    }

    FmsOrder mockCancelledFmsOrder() {
        FmsOrder mockOrder = mockFmsOrder()
        mockOrder.orderState = "CANCELLED"
        return mockOrder
    }

    FmsOrder mockOrderConfirmationFmsOrder() {
        FmsOrder mockOrder = mockFmsOrder()
        mockOrder.orderState = FmsOrder.OrderStatus.RECEIVED_AT_STORE
        return mockOrder
    }

    FmsOrder mockPickCompleteFmsOrder() {
        FmsOrder mockOrder = mockFmsOrder()
        mockOrder.orderState = FmsOrder.OrderStatus.PICK_COMPLETE
        return mockOrder
    }

    FmsOrder mockDeliveredFmsOrder() {
        FmsOrder mockOrder = mockFmsOrder()
        mockOrder.orderState = FmsOrder.OrderStatus.DELIVERED
        return mockOrder
    }

}
