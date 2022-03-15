package com.walmart.fms.order.domain

import com.walmart.common.domain.messaging.DomainEvent
import com.walmart.common.domain.messaging.DomainEventPublisher
import com.walmart.common.domain.type.CancellationSource
import com.walmart.common.domain.type.Currency
import com.walmart.common.domain.type.DomainEventType
import com.walmart.common.domain.type.Domain
import com.walmart.common.domain.type.SubstitutionOption
import com.walmart.common.domain.type.Tenant
import com.walmart.common.domain.type.Vertical
import com.walmart.fms.domain.error.exception.FMSBadRequestException
import com.walmart.fms.order.aggregateroot.FmsOrder
import com.walmart.fms.order.domain.entity.FmsAddressInfo
import com.walmart.fms.order.domain.entity.FmsCustomerContactInfo
import com.walmart.fms.order.domain.entity.FmsOrderItem
import com.walmart.fms.order.domain.entity.FmsSchedulingInfo
import com.walmart.fms.order.gateway.IStoreGateway
import com.walmart.fms.order.repository.IFmsOrderRepository
import com.walmart.fms.order.valueobject.*
import com.walmart.fms.order.valueobject.events.FmsOrderValueObject
import com.walmart.marketplace.order.domain.entity.type.Vendor
import spock.lang.Specification

class FmsOrderDomainServiceSpec extends Specification {

    FmsOrderDomainService fmsOrderDomainService

    IFmsOrderRepository fmsOrderRepository = Mock()

    DomainEventPublisher fmsDomainEventPublisher = Mock()

    IStoreGateway storeGateway = Mock()

    String sourceOrderId = UUID.randomUUID().toString()
    String vendorOrderId = UUID.randomUUID().toString()
    String storeWebOrderID = com.walmart.fms.order.domain.entity.type.Vendor.SequenceGenerator.INSTANCE.nextId();

    def setup() {
        fmsOrderDomainService = new FmsOrderDomainService(
                fmsOrderRepository:fmsOrderRepository,
                fmsDomainEventPublisher:fmsDomainEventPublisher,
                storeGateway:storeGateway
        )
    }

    def "Test Null FmsOrder throws exception"() {
        given:
        FmsOrder nullFmsOrder = null

        when:
        fmsOrderDomainService.processFmsOrder(nullFmsOrder)

        then:
        thrown(IllegalArgumentException)

    }

    def "Test invalid fms order " () {
        given:
        FmsOrder fmsOrder = new FmsOrder()

        when:
        fmsOrderDomainService.processFmsOrder(fmsOrder)

        then:
        0 * fmsOrderRepository.save(_ as FmsOrder)
        0 * storeGateway.sendMarketPlaceOrderDownloadAsync(_)
    }

    def "Test ProcessFmsOrder"() {
        given:

        FmsOrder testFmsOrder = FmsOrder.builder()
                .vertical(Vertical.ASDAGR)
                .tenant(Tenant.ASDA)
                .storeId("4401")
                .sourceOrderId(sourceOrderId)
                .storeOrderId(storeWebOrderID)
                .pickupLocationId("4401")
                .deliveryDate(new Date())
                .marketPlaceInfo(MarketPlaceInfo.builder().vendor(Vendor.UBEREATS).vendorOrderId(vendorOrderId).build())
                .orderState(FmsOrder.OrderStatus.RECEIVED_AT_STORE.getName())
                .priceInfo(OrderPriceInfo.builder()
                        .webOrderTotal(40.0).build()).build()


        testFmsOrder.addAddressInfo(FmsAddressInfo.builder()
                .order(testFmsOrder)
                .addressOne("test addressOne")
                .addressTwo("test addressTwo")
                .addressThree("Test adddressThree")
                .city("test city")
                .latitude("57.2252552")
                .longitude("-6.24343").build())


        testFmsOrder.addSchedulingInfo(FmsSchedulingInfo.builder().order(testFmsOrder)
                .tripId("trip_id_2222").build())

        testFmsOrder.addContactInfo(FmsCustomerContactInfo.builder()
                .order(testFmsOrder)
                .fullName(new FullName(null,"John",null, "Doe"))
                .phoneNumberOne(new TelePhone("0123456789")).build())


        testFmsOrder.addItem(mockFmsItem(testFmsOrder))

        when:
        fmsOrderDomainService.processFmsOrder(testFmsOrder)

        then:
        1 * fmsOrderRepository.save(_ as FmsOrder) >> { FmsOrder _fmsOrder ->
            assert _fmsOrder.getSourceOrderId() == sourceOrderId
            assert _fmsOrder.getStoreId() == "4401"

            assert _fmsOrder.getOrderState() == FmsOrder.OrderStatus.READY_FOR_STORE.getName()

            assert _fmsOrder.getStoreOrderId() != null
            assert Long.parseLong(_fmsOrder.getStoreOrderId()) >=Long.MIN_VALUE
            assert Long.parseLong(_fmsOrder.getStoreOrderId()) <=Long.MAX_VALUE

            return testFmsOrder
        }
        1* storeGateway.sendMarketPlaceOrderDownloadAsync(_)


    }

    def "Test CancelFmsOrder with Store cancellation"() {
        given:
        FmsOrder testFmsOrder = mockFmsOrder()
        when:
        fmsOrderDomainService.cancelFmsOrder(testFmsOrder, "STORE", CancellationSource.STORE, "cancelled by store")

        then:

        1 * fmsOrderRepository.save(_ as FmsOrder) >> { FmsOrder _fmsOrder ->
            assert _fmsOrder.getStoreOrderId() == storeWebOrderID
            assert _fmsOrder.getOrderState() == "CANCELLED"
            return testFmsOrder
        }
        1 * fmsDomainEventPublisher.publish(_ as DomainEvent, _ as String) >> { DomainEvent _domainEvent, String _destination ->
            assert _destination == "FMS_ORDER_UPDATES"
            assert _domainEvent.source == Domain.FMS
            assert _domainEvent.key == testFmsOrder.id
            assert _domainEvent.name == DomainEventType.FMS_ORDER_CANCELLED
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

    def "Test Vendor initiated cancellation" () {
        given:
        FmsOrder testFmsOrder = mockFmsOrder()
        when:
        fmsOrderDomainService.cancelFmsOrder(testFmsOrder, "VENDOR", CancellationSource.VENDOR, "VENDOR")

        then:
        1 * fmsOrderRepository.save(_ as FmsOrder) >> { FmsOrder _fmsOrder ->
            assert _fmsOrder.getStoreOrderId() == storeWebOrderID
            assert _fmsOrder.getOrderState() == "CANCELLED"
            return testFmsOrder
        }
    }

    def "Test Cancel non cancellable order" () {
        given:
        FmsOrder testFmsOrder = mockFmsOrder()
        testFmsOrder.orderState = FmsOrder.OrderStatus.CANCELLED.getName()
        when:
        fmsOrderDomainService.cancelFmsOrder(testFmsOrder, "VENDOR", CancellationSource.VENDOR, "VENDOR")

        then:
        thrown(FMSBadRequestException)
    }

    private static ItemCatalogInfo mockItemCatalogInfo() {
        return new ItemCatalogInfo("EACH", "E", "Asda Fresh ",
                "https://i.groceries.asda.com/image.jpg", 3,
                4, "Ambient", true);


    }

    private FmsOrderItem mockFmsItem(FmsOrder fmsOrder) {
        FmsOrderItem fmsItem = FmsOrderItem.builder()
                .fmsOrder(fmsOrder)
                .consumerItemNumber("464646")
                .itemPriceInfo(new ItemPriceInfo(new Money(BigDecimal.valueOf(2.5), Currency.GBP)))
                .quantity(2)
                .substitutionOption(SubstitutionOption.DO_NOT_SUBSTITUTE)
                .build()
        fmsItem.addCatalogInfo(mockItemCatalogInfo())
        fmsItem.addUpcInfo(mockFmsItemUpcInfo())
        return fmsItem

    }

    private static ItemUpcInfo mockFmsItemUpcInfo() {
        return ItemUpcInfo.builder()
                .upcNumbers(["22233", "44455"])
                .build()
    }

    FmsOrder mockFmsOrder() {
        FmsOrder testFmsOrder = FmsOrder.builder()
                .vertical(Vertical.ASDAGR)
                .tenant(Tenant.ASDA)
                .storeId("4401")
                .sourceOrderId(sourceOrderId)
                .storeOrderId(storeWebOrderID)
                .pickupLocationId("4401")
                .deliveryDate(new Date())
                .marketPlaceInfo(MarketPlaceInfo.builder().vendor(Vendor.UBEREATS).vendorOrderId(vendorOrderId).build())
                .orderState("RECD_AT_STORE")
                .priceInfo(OrderPriceInfo.builder()
                        .webOrderTotal(40.0).build()).build()


        testFmsOrder.addAddressInfo(FmsAddressInfo.builder()
                .order(testFmsOrder)
                .addressOne("test addressOne")
                .addressTwo("test addressTwo")
                .addressThree("Test adddressThree")
                .city("test city")
                .latitude("57.2252552")
                .longitude("-6.24343").build())


        testFmsOrder.addSchedulingInfo(FmsSchedulingInfo.builder().order(testFmsOrder)
                .tripId("trip_id_2222").build())

        testFmsOrder.addContactInfo(FmsCustomerContactInfo.builder()
                .order(testFmsOrder)
                .fullName(new FullName(null,"John",null, "Doe"))
                .phoneNumberOne(new TelePhone("0123456789")).build())


        testFmsOrder.addItem(mockFmsItem(testFmsOrder))

        return testFmsOrder
    }

}