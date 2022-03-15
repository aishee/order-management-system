package com.walmart.oms.order.domain

import com.walmart.common.domain.event.processing.EventGeneratorService
import com.walmart.common.domain.type.*
import com.walmart.marketplace.order.domain.entity.type.Vendor
import com.walmart.oms.domain.event.messages.OrderCreatedDomainEventMessage
import com.walmart.oms.order.aggregateroot.OmsOrder
import com.walmart.oms.order.domain.entity.AddressInfo
import com.walmart.oms.order.domain.entity.CustomerContactInfo
import com.walmart.oms.order.domain.entity.OmsOrderItem
import com.walmart.oms.order.domain.entity.SchedulingInfo
import com.walmart.oms.order.domain.factory.OmsOrderCancelDomainEventPublisherFactory
import com.walmart.oms.order.repository.IOmsOrderRepository
import com.walmart.oms.order.valueobject.CancelDetails
import com.walmart.oms.order.valueobject.CatalogItem
import com.walmart.oms.order.valueobject.FullName
import com.walmart.oms.order.valueobject.ItemPriceInfo
import com.walmart.oms.order.valueobject.MarketPlaceInfo
import com.walmart.oms.order.valueobject.Money
import com.walmart.oms.order.valueobject.OrderPriceInfo
import com.walmart.oms.order.valueobject.TelePhone
import spock.lang.Specification

class OmsOrderDomainServiceSpec extends Specification {

    public static final String OSN = "U" + 1000
    OmsOrderDomainService omsOrderDomainService

    IOmsOrderRepository omsOrderRepository = Mock()

    EventGeneratorService eventGeneratorService = Mock()

    String sourceOrderId = UUID.randomUUID().toString()
    String vendorOrderId = UUID.randomUUID().toString()

    def setup() {
        omsOrderDomainService = new OmsOrderDomainService(omsOrderRepository, eventGeneratorService)
    }

    def "Test ProcessOmsOrder"() {
        given:

        OmsOrder testOmsOrder = mockOrder()

        HashMap catalogData = new HashMap()

        CatalogItem catalogItem = CatalogItem.builder()
                .cin("464646")
                .price("£2.50")
                .upcNumbers(Arrays.asList("535353535"))
                .build()
        catalogData.put("464646", catalogItem)

        when:
        omsOrderDomainService.processOmsOrder(testOmsOrder)

        then:
        1 * omsOrderRepository.save(_ as OmsOrder) >> { OmsOrder _omsOrder ->
            assert _omsOrder.getSourceOrderId() == sourceOrderId
            assert _omsOrder.getStoreId() == "4401"
            return testOmsOrder
        }
    }

    def "Test ProcessOmsOrder for one sale items"() {
        given:
        OmsOrder testOmsOrder = mockOrder()
        HashMap catalogData = new HashMap()
        CatalogItem catalogItem = CatalogItem.builder()
                .cin("464646")
                .price("£2.50")
                .onSale(true)
                .salePrice("£1.00")
                .upcNumbers(Arrays.asList("535353535"))
                .build()
        catalogData.put("464646", catalogItem)

        when:
        omsOrderDomainService.processOmsOrder(testOmsOrder)

        then:
        1 * omsOrderRepository.save(_ as OmsOrder)
    }

    def "Test processOmsOrder for invalid omsOrder"() {
        given:
        OmsOrder testOmsOrder = new OmsOrder()

        when:
        omsOrderDomainService.processOmsOrder(testOmsOrder)

        then:
        0 * omsOrderRepository.save(_ as OmsOrder)
    }

    def "Test publishOrderCreatedDomainEvent"() {
        given:
        OmsOrder testOmsOrder = mockOrder()

        when:
        omsOrderDomainService.publishOrderCreatedDomainEvent(testOmsOrder)

        then:
        1 * eventGeneratorService.publishApplicationEvent(_ as OrderCreatedDomainEventMessage)
    }

    OmsOrder mockOrder() {
        OmsOrder testOmsOrder = OmsOrder.builder()
                .vertical(Vertical.ASDAGR)
                .tenant(Tenant.ASDA)
                .storeId("4401")
                .sourceOrderId(sourceOrderId)
                .storeOrderId("123456789")
                .pickupLocationId("4401")
                .spokeStoreId("4401")
                .deliveryDate(new Date())
                .fulfillmentType(FulfillmentType.INSTORE_PICKUP)
                .marketPlaceInfo(MarketPlaceInfo.builder().vendor(Vendor.UBEREATS).vendorOrderId(vendorOrderId).build())
                .orderState("READY_FOR_STORE")
                .priceInfo(OrderPriceInfo.builder()
                        .orderSubTotal(40.0).build()).build()


        testOmsOrder.addAddress(AddressInfo.builder()
                .omsOrder(testOmsOrder)
                .addressOne("test addressOne")
                .addressTwo("test addressTwo")
                .addressThree("Test adddressThree")
                .city("test city")
                .latitude("57.2252552")
                .longitude("-6.24343").build())


        testOmsOrder.addSchedulingInfo(SchedulingInfo.builder()
                .order(testOmsOrder)
                .scheduleNumber(OSN)
                .plannedDueTime(new Date()).build())

        testOmsOrder.addContactInfo(CustomerContactInfo.builder()
                .order(testOmsOrder)
                .fullName(new FullName(null, "John", null, "Doe"))
                .phoneNumberOne(new TelePhone("0123456789")).build())

        OmsOrderItem testOmsOrderItem = OmsOrderItem.builder()
                .omsOrder(testOmsOrder)
                .cin("464646")
                .itemPriceInfo(new ItemPriceInfo(new Money(BigDecimal.valueOf(2.5), Currency.GBP), new Money(BigDecimal.valueOf(5.0), Currency.GBP)))
                .quantity(2)
                .salesUnit("EACH")
                .uom("E")
                .build()

        testOmsOrder.addItem(testOmsOrderItem)

        return testOmsOrder
    }

    OmsOrder mockDeliveredOrder() {
        OmsOrder testOmsOrder = OmsOrder.builder()
                .vertical(Vertical.ASDAGR)
                .tenant(Tenant.ASDA)
                .storeId("4401")
                .sourceOrderId(sourceOrderId)
                .storeOrderId("123456789")
                .pickupLocationId("4401")
                .spokeStoreId("4401")
                .deliveryDate(new Date())
                .fulfillmentType(FulfillmentType.INSTORE_PICKUP)
                .marketPlaceInfo(MarketPlaceInfo.builder().vendor(Vendor.UBEREATS).vendorOrderId(vendorOrderId).build())
                .orderState("DELIVERED")
                .priceInfo(OrderPriceInfo.builder()
                        .orderSubTotal(40.0).build()).build()


        testOmsOrder.addAddress(AddressInfo.builder()
                .omsOrder(testOmsOrder)
                .addressOne("test addressOne")
                .addressTwo("test addressTwo")
                .addressThree("Test adddressThree")
                .city("test city")
                .latitude("57.2252552")
                .longitude("-6.24343").build())


        testOmsOrder.addSchedulingInfo(SchedulingInfo.builder()
                .order(testOmsOrder)
                .scheduleNumber(OSN)
                .plannedDueTime(new Date()).build())

        testOmsOrder.addContactInfo(CustomerContactInfo.builder()
                .order(testOmsOrder)
                .fullName(new FullName(null, "John", null, "Doe"))
                .phoneNumberOne(new TelePhone("0123456789")).build())

        OmsOrderItem testOmsOrderItem = OmsOrderItem.builder()
                .omsOrder(testOmsOrder)
                .cin("464646")
                .itemPriceInfo(new ItemPriceInfo(new Money(BigDecimal.valueOf(2.5), Currency.GBP), new Money(BigDecimal.valueOf(5.0), Currency.GBP)))
                .quantity(2)
                .salesUnit("EACH")
                .uom("E")
                .build()

        testOmsOrder.addItem(testOmsOrderItem)

        return testOmsOrder
    }
}