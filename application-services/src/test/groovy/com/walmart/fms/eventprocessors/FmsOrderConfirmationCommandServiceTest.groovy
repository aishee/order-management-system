package com.walmart.fms.eventprocessors

import com.walmart.common.domain.messaging.DomainEvent
import com.walmart.common.domain.messaging.DomainEventPublisher
import com.walmart.common.domain.type.*
import com.walmart.fms.commands.FmsOrderConfirmationCommand
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

class FmsOrderConfirmationCommandServiceTest extends Specification {

    FmsOrderConfirmationCommandService fmsOrderConfirmationService

    private FmsOrderFactory fmsOrderFactory = Mock()

    private IFmsOrderRepository fmsOrderRepository = Mock()

    DomainEventPublisher fmsDomainEventPublisher = Mock()

    String storeOrderId = UUID.randomUUID().toString()
    String vendorOrderId = UUID.randomUUID().toString()

    def setup() {
        fmsOrderConfirmationService = new FmsOrderConfirmationCommandService(
                fmsOrderRepository: fmsOrderRepository,
                fmsDomainEventPublisher: fmsDomainEventPublisher,
                fmsOrderFactory: fmsOrderFactory)
    }

    def " Test Confirm Order with pre existing order"() {
        given:
        FmsOrder testFmsOrder = mockFmsOrder()

        FmsOrderConfirmationCommand orderConfirmationCommand = mockOrderConfirmationCommand(storeOrderId)

        fmsOrderFactory.getFmsOrderByStoreOrder(_ as String) >> testFmsOrder

        when:
        fmsOrderConfirmationService.orderConfirmedAtStore(orderConfirmationCommand)

        then:
        1 * fmsOrderRepository.save(_ as FmsOrder) >> { FmsOrder _fmsOrder ->
            assert _fmsOrder.getStoreOrderId() == storeOrderId
            assert _fmsOrder.getOrderState() == FmsOrder.OrderStatus.RECEIVED_AT_STORE.getName()
            return testFmsOrder
        }

        1 * fmsDomainEventPublisher.publish(_ as DomainEvent, _ as String) >> { DomainEvent _domainEvent, String _destination ->
            assert _destination == "FMS_ORDER_UPDATES"
            assert _domainEvent.source == Domain.FMS
            assert _domainEvent.key == testFmsOrder.id
            assert _domainEvent.name == DomainEventType.FMS_ORDER_CONFIRM
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

    def " Test Confirm order without pre existing order"() {
        given:
        FmsOrder testFmsOrder = mockFmsOrder()

        FmsOrderConfirmationCommand orderConfirmationCommand = mockOrderConfirmationCommand(storeOrderId)

        fmsOrderFactory.getFmsOrderByStoreOrder(_ as String) >> null

        when:
        fmsOrderConfirmationService.orderConfirmedAtStore(orderConfirmationCommand)

        then:
        thrown(FMSBadRequestException.class)
    }

    def " Test Confirm already CANCELLED order"() {
        given:
        FmsOrder testFmsOrder = mockCancelledFmsOrder()

        FmsOrderConfirmationCommand orderConfirmationCommand = mockOrderConfirmationCommand(storeOrderId)

        fmsOrderFactory.getFmsOrderByStoreOrder(_ as String) >> testFmsOrder

        when:
        fmsOrderConfirmationService.orderConfirmedAtStore(orderConfirmationCommand)

        then:
        0 * fmsOrderRepository.save(_ as FmsOrder) >> { FmsOrder _fmsOrder ->
            assert _fmsOrder.getStoreOrderId() == storeOrderId
            assert _fmsOrder.getOrderState() == "CANCELLED"
            return testFmsOrder
        }
        thrown(FMSBadRequestException)

    }

    def " Test Pick Started Order with Delivered order"() {
        given:
        FmsOrder testFmsOrder = mockDeliveredFmsOrder()

        FmsOrderConfirmationCommand orderConfirmationCommand = mockOrderConfirmationCommand(storeOrderId)

        fmsOrderFactory.getFmsOrderByStoreOrder(_ as String) >> testFmsOrder

        when:
        fmsOrderConfirmationService.orderConfirmedAtStore(orderConfirmationCommand)

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

    def " Test Pick Started Order with Order Confirmation order"() {
        given:
        FmsOrder testFmsOrder = mockOrderConfirmFmsOrder()

        FmsOrderConfirmationCommand orderConfirmationCommand = mockOrderConfirmationCommand(storeOrderId)

        fmsOrderFactory.getFmsOrderByStoreOrder(_ as String) >> testFmsOrder

        when:
        fmsOrderConfirmationService.orderConfirmedAtStore(orderConfirmationCommand)

        then:
        fmsOrderRepository.save(_ as FmsOrder) >> { FmsOrder _fmsOrder ->
            assert _fmsOrder.getStoreOrderId() == storeOrderId
            assert _fmsOrder.getOrderState() == FmsOrder.OrderStatus.READY_FOR_STORE.getName()
            return testFmsOrder
        }
        fmsDomainEventPublisher.publish(_ as DomainEvent, _ as String) >> { DomainEvent _domainEvent, String _destination ->
            assert _destination == null
        }
        thrown(FMSBadRequestException)
    }

    FmsOrder mockCancelledFmsOrder() {
        FmsOrder mockOrder = mockFmsOrder()
        mockOrder.orderState = FmsOrder.OrderStatus.CANCELLED
        return mockOrder
    }

    FmsOrder mockDeliveredFmsOrder() {
        FmsOrder mockOrder = mockFmsOrder()
        mockOrder.orderState = FmsOrder.OrderStatus.DELIVERED.getName()
        return mockOrder
    }

    FmsOrder mockOrderConfirmFmsOrder() {
        FmsOrder mockOrder = mockFmsOrder()
        mockOrder.orderState = FmsOrder.OrderStatus.RECEIVED_AT_STORE.getName()
        return mockOrder
    }

    FmsOrder mockFmsOrder() {

        String testCin = "4647474"
        FmsOrder fmsOrder = FmsOrder.builder()
                .tenant(Tenant.ASDA)
                .vertical(Vertical.MARKETPLACE)
                .orderState(FmsOrder.OrderStatus.READY_FOR_STORE.getName())
                .deliveryDate(new Date())
                .sourceOrderId("44443333")
                .storeOrderId(storeOrderId)
                .storeId("4401")
                .marketPlaceInfo(new MarketPlaceInfo(Vendor.UBEREATS, vendorOrderId))
                .build()

        fmsOrder.addItem(FmsOrderItem.builder().quantity(2)
                .consumerItemNumber(testCin)
                .fmsOrder(fmsOrder)
                .itemPriceInfo(new ItemPriceInfo(new Money(BigDecimal.valueOf(2.5), Currency.GBP)))
                .id(UUID.randomUUID().toString())
                .substitutionOption(SubstitutionOption.DO_NOT_SUBSTITUTE)
                .build())

        return fmsOrder

    }

    FmsOrderConfirmationCommand mockOrderConfirmationCommand(String storeOrderId) {
        return FmsOrderConfirmationCommand.builder()
                .storeOrderId(storeOrderId)
                .build()
    }
}
