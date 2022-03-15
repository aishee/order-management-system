package com.walmart.fms.eventprocessors

import com.walmart.common.domain.messaging.DomainEvent
import com.walmart.common.domain.messaging.DomainEventPublisher
import com.walmart.common.domain.type.Currency
import com.walmart.common.domain.type.DomainEventType
import com.walmart.common.domain.type.Domain
import com.walmart.common.domain.type.SubstitutionOption
import com.walmart.common.domain.type.Tenant
import com.walmart.common.domain.type.Vertical
import com.walmart.fms.commands.FmsDeliveredOrderCommand
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

class FmsDeliveredCommandServiceTest extends Specification {

    FmsDeliveredCommandService fmsDeliveredCommandService

    private FmsOrderFactory fmsOrderFactory = Mock()

    private DomainEventPublisher fmsDomainEventPublisher = Mock()

    private IFmsOrderRepository fmsOrderRepository = Mock()

    String storeOrderId = UUID.randomUUID().toString()
    String vendorOrderId = UUID.randomUUID().toString()

    def setup() {
        fmsDeliveredCommandService = new FmsDeliveredCommandService(
                fmsOrderRepository: fmsOrderRepository,
                fmsOrderFactory: fmsOrderFactory,
                fmsDomainEventPublisher: fmsDomainEventPublisher
        )
    }

    def " Test DeliverOrder with pre existing order"() {
        given:
        FmsOrder testFmsOrder = mockFmsOrder()

        FmsDeliveredOrderCommand deliveredOrderCommand = getFmsDeliveredOrderCommand()

        fmsOrderFactory.getFmsOrderByStoreOrder(_ as String) >> testFmsOrder

        when:
        fmsDeliveredCommandService.deliverOrder(deliveredOrderCommand)

        then:
        1 * fmsOrderRepository.save(_ as FmsOrder) >> { FmsOrder _fmsOrder ->
            assert _fmsOrder.getStoreOrderId() == storeOrderId
            assert _fmsOrder.getOrderState() == "DELIVERED"
            return testFmsOrder
        }

        1 * fmsDomainEventPublisher.publish(_ as DomainEvent, _ as String) >> { DomainEvent _domainEvent, String _destination ->
            assert _destination == "FMS_ORDER_UPDATES"
            assert _domainEvent.source == Domain.FMS
            assert _domainEvent.key == testFmsOrder.id
            assert _domainEvent.name == DomainEventType.FMS_ORDER_DELIVERED
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

    def " Test DeliverOrder without pre existing order"() {
        given:
        FmsDeliveredOrderCommand deliveredOrderCommand = getFmsDeliveredOrderCommand()

        fmsOrderFactory.getFmsOrderByStoreOrder(_ as String) >> new FmsOrder("INITIAL")

        when:
        fmsDeliveredCommandService.deliverOrder(deliveredOrderCommand)

        then:
        thrown(FMSBadRequestException.class)
    }

    def " Test DeliverOrder with pre existing Cancel order"() {
        given:
        FmsOrder testFmsOrder = mockFmsCancelOrder()

        FmsDeliveredOrderCommand deliveredOrderCommand = getFmsDeliveredOrderCommand()

        fmsOrderFactory.getFmsOrderByStoreOrder(_ as String) >> testFmsOrder

        when:
        fmsDeliveredCommandService.deliverOrder(deliveredOrderCommand)

        then:
        thrown(FMSBadRequestException.class)
    }

    def " Test DeliverOrder with pre existing Delivered order"() {
        given:
        FmsOrder testFmsOrder = mockFmsDeliveredOrder()

        FmsDeliveredOrderCommand deliveredOrderCommand = getFmsDeliveredOrderCommand()

        fmsOrderFactory.getFmsOrderByStoreOrder(_ as String) >> testFmsOrder

        when:
        fmsDeliveredCommandService.deliverOrder(deliveredOrderCommand)

        then:
        thrown(FMSBadRequestException.class)
    }

    FmsOrder mockFmsCancelOrder() {
        FmsOrder fmsOrder = mockFmsOrder();
        fmsOrder.orderState = FmsOrder.OrderStatus.CANCELLED.getName()
        return fmsOrder
    }

    FmsOrder mockFmsDeliveredOrder() {
        FmsOrder fmsOrder = mockFmsOrder();
        fmsOrder.orderState = FmsOrder.OrderStatus.DELIVERED.getName()
        return fmsOrder
    }

    FmsOrder mockFmsOrder() {

        String testCin = "4647474"
        FmsOrder fmsOrder = FmsOrder.builder()
                .tenant(Tenant.ASDA)
                .vertical(Vertical.MARKETPLACE)
                .orderState(FmsOrder.OrderStatus.PICK_COMPLETE.getName())
                .deliveryDate(new Date())
                .storeOrderId(storeOrderId)
                .sourceOrderId("2223333")
                .storeId("4401")
                .marketPlaceInfo(new MarketPlaceInfo(Vendor.UBEREATS, vendorOrderId))
                .build()

        fmsOrder.addItem(getFmsOrderItem(testCin, fmsOrder))
        return fmsOrder
    }

    private FmsOrderItem getFmsOrderItem(String testCin, FmsOrder fmsOrder) {
        return FmsOrderItem.builder().quantity(2)
                .consumerItemNumber(testCin)
                .fmsOrder(fmsOrder)
                .itemPriceInfo(new ItemPriceInfo(new Money(BigDecimal.valueOf(2.5), Currency.GBP)))
                .id(UUID.randomUUID().toString())
                .substitutionOption(SubstitutionOption.DO_NOT_SUBSTITUTE)
                .build()
    }

    private FmsDeliveredOrderCommand getFmsDeliveredOrderCommand() {
        return FmsDeliveredOrderCommand.builder()
                .data(FmsDeliveredOrderCommand.FmsDeliveredOrderCommandData.builder()
                        .storeOrderId(storeOrderId).build()).build()
    }
}
