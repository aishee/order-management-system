package com.walmart.oms.order.domain

import com.walmart.common.domain.event.processing.EventGeneratorService
import com.walmart.common.domain.event.processing.Message
import com.walmart.common.domain.messaging.DomainEvent
import com.walmart.common.domain.messaging.DomainEventPublisher
import com.walmart.common.domain.type.Currency
import com.walmart.common.domain.type.DomainEventType
import com.walmart.common.domain.type.Domain
import com.walmart.common.domain.type.FulfillmentType
import com.walmart.common.domain.type.Tenant
import com.walmart.common.domain.type.Vertical
import com.walmart.marketplace.order.domain.entity.type.Vendor
import com.walmart.oms.domain.error.exception.OMSBadRequestException
import com.walmart.oms.order.aggregateroot.OmsOrder
import com.walmart.oms.order.domain.entity.AddressInfo
import com.walmart.oms.order.domain.entity.CustomerContactInfo
import com.walmart.oms.order.domain.entity.OmsOrderItem
import com.walmart.oms.order.domain.entity.SchedulingInfo
import com.walmart.oms.order.gateway.ICatalogGateway
import com.walmart.oms.order.repository.IOmsOrderRepository
import com.walmart.oms.order.valueobject.CatalogItem
import com.walmart.oms.order.valueobject.CatalogItemInfoQuery
import com.walmart.oms.order.valueobject.FullName
import com.walmart.oms.order.valueobject.ItemPriceInfo
import com.walmart.oms.order.valueobject.Money
import com.walmart.oms.order.valueobject.TelePhone
import com.walmart.oms.order.valueobject.events.FmsOrderValueObject
import spock.lang.Specification
import com.walmart.oms.order.valueobject.MarketPlaceInfo
import com.walmart.oms.order.valueobject.OrderPriceInfo

class OmsOrderCreatedDomainServiceSpec extends Specification {

    public static final String OSN = "U" + 1000
    String sourceOrderId = UUID.randomUUID().toString()
    String vendorOrderId = UUID.randomUUID().toString()

    OmsOrderCreatedDomainService omsOrderCreatedDomainService
    IOmsOrderRepository omsOrderRepository = Mock()
    DomainEventPublisher domainEventPublisher = Mock()
    ICatalogGateway catalogGateway = Mock()
    OmsOrderDomainService omsOrderDomainService = Mock()
    EventGeneratorService eventGeneratorService = Mock()

    def setup() {
        omsOrderCreatedDomainService = new OmsOrderCreatedDomainService(
                omsOrderRepository,
                domainEventPublisher,
                catalogGateway,
                omsOrderDomainService,
                eventGeneratorService
        )
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


        catalogGateway.fetchCatalogData(_ as CatalogItemInfoQuery) >> catalogData

        when:
        omsOrderCreatedDomainService.enrichSaveAndPublishCreatedOmsOrderToFms(testOmsOrder)

        then:
        1 * omsOrderRepository.save(_ as OmsOrder) >> { OmsOrder _omsOrder ->
            assert _omsOrder.getSourceOrderId() == sourceOrderId
            assert _omsOrder.getStoreId() == "4401"
            return testOmsOrder
        }
        1 * domainEventPublisher.publish(_ as DomainEvent, _ as String) >> { DomainEvent _domainEvent, String _destination ->
            assert _destination == "OMS_ORDER_CREATED"
            assert _domainEvent.source == Domain.OMS
            assert _domainEvent.key == testOmsOrder.id
            assert _domainEvent.name == DomainEventType.OMS_ORDER_CREATED
            assert _domainEvent.message != null
            Optional<FmsOrderValueObject> valueObject = _domainEvent.createObjectFromJson(FmsOrderValueObject.class)
            assert testOmsOrder.sourceOrderId == valueObject.get().sourceOrderId
            assert testOmsOrder.storeOrderId == valueObject.get().storeOrderId
            assert testOmsOrder.orderState == valueObject.get().orderState
            assert testOmsOrder.storeId == valueObject.get().storeId
            assert testOmsOrder.contactInfo.fullName.firstName == valueObject.get().contactInfo.firstName
            assert testOmsOrder.contactInfo.fullName.lastName == valueObject.get().contactInfo.lastName
            assert testOmsOrder.contactInfo.email == valueObject.get().contactInfo.email
            assert testOmsOrder.contactInfo.mobileNumber == valueObject.get().contactInfo.mobileNumber
            assert testOmsOrder.contactInfo.phoneNumberOne.number == valueObject.get().contactInfo.phoneNumberOne
            assert testOmsOrder.marketPlaceInfo.vendorOrderId == valueObject.get().marketPlaceInfo.vendorOrderId
            assert testOmsOrder.marketPlaceInfo.vendor == valueObject.get().marketPlaceInfo.vendor
            assert testOmsOrder.deliveryDate == valueObject.get().deliveryDate
            assert testOmsOrder.schedulingInfo.plannedDueTime == valueObject.get().schedulingInfo.plannedDueTime
            assert testOmsOrder.schedulingInfo.doorStepTime == valueObject.get().schedulingInfo.doorStepTime
            assert testOmsOrder.schedulingInfo.loadNumber == valueObject.get().schedulingInfo.loadNumber
            assert valueObject.get().schedulingInfo.scheduleNumber == OSN
            assert testOmsOrder.schedulingInfo.scheduleNumber == valueObject.get().schedulingInfo.scheduleNumber
            assert testOmsOrder.schedulingInfo.tripId == valueObject.get().schedulingInfo.tripId
            assert testOmsOrder.schedulingInfo.vanId == valueObject.get().schedulingInfo.vanId
            assert testOmsOrder.authStatus == valueObject.get().authStatus
            assert testOmsOrder.spokeStoreId == valueObject.get().spokeStoreId
            assert testOmsOrder.fulfillmentType == valueObject.get().fulfillmentType
            assert testOmsOrder.tenant == valueObject.get().tenant
            assert testOmsOrder.pickupLocationId == valueObject.get().pickupLocationId
            assert testOmsOrder.vertical == valueObject.get().vertical
            assert testOmsOrder.priceInfo.orderTotal == valueObject.get().priceInfo.orderTotal
            assert testOmsOrder.priceInfo.carrierBagCharge == valueObject.get().priceInfo.carrierBagCharge
            assert testOmsOrder.priceInfo.deliveryCharge == valueObject.get().priceInfo.deliveryCharge
            assert testOmsOrder.priceInfo.orderSubTotal == valueObject.get().priceInfo.orderSubTotal
            assert testOmsOrder.orderItemList[0].quantity == valueObject.get().fmsOrderItemvalueObjectList[0].quantity
            assert testOmsOrder.orderItemList[0].itemDescription == valueObject.get().fmsOrderItemvalueObjectList[0].itemDescription
            assert testOmsOrder.orderItemList[0].cin == valueObject.get().fmsOrderItemvalueObjectList[0].cin
            assert testOmsOrder.orderItemList[0].itemPriceInfo.unitPrice.amount == valueObject.get().fmsOrderItemvalueObjectList[0].unitPrice
            assert testOmsOrder.orderItemList[0].catalogItem.upcNumbers[0] == valueObject.get().fmsOrderItemvalueObjectList[0].upcs[0].upc
            assert testOmsOrder.contactInfo.getFirstName() == valueObject.get().contactInfo.firstName
            assert testOmsOrder.contactInfo.getLastName() == valueObject.get().contactInfo.lastName
            assert testOmsOrder.contactInfo.getRefPhoneNumberOne() == valueObject.get().contactInfo.phoneNumberOne
            assert testOmsOrder.contactInfo.getRefPhoneNumberTwo() == valueObject.get().contactInfo.phoneNumberTwo
            assert testOmsOrder.contactInfo.getRefMobileNumber() == valueObject.get().contactInfo.mobileNumber
        }
    }

    def "When IROCatalogGateway returns with invalid items "() {
        OmsOrder omsOrder = new OmsOrder()
        omsOrder.builder().sourceOrderId("1234").storeOrderId("abce")
        catalogGateway.fetchCatalogData(_ as CatalogItemInfoQuery) >> { throw new OMSBadRequestException("Invalid Catalog item fetch request") }

        when:
        omsOrderCreatedDomainService.enrichSaveAndPublishCreatedOmsOrderToFms(omsOrder)

        then:
        thrown(OMSBadRequestException.class)
        1 * eventGeneratorService.publishApplicationEvent(_ as Message)
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

    def "test schedulinginfo instance"() {
        when:
        SchedulingInfo schedulingInfo = new SchedulingInfo()

        then:
        assert schedulingInfo != null
    }

    def "test scheduling info all arg constructor"() {
        given:
        String tripId = "tripId"
        OmsOrder order = Mock()
        int doorStepTime = 1
        String vanId = "vanId"
        String scheduleNumber = "scheduleNumber"
        Date plannedDueTime = Mock()
        String loadNumber = "loadNumber"

        when:
        SchedulingInfo schedulingInfo = new SchedulingInfo(order, tripId, doorStepTime, vanId, scheduleNumber, plannedDueTime, loadNumber)

        then:
        assert schedulingInfo.getTripId() == "tripId"
    }
}