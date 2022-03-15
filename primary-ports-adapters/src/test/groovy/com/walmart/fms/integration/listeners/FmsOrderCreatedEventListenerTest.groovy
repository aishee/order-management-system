package com.walmart.fms.integration.listeners

import com.walmart.common.domain.messaging.DomainEvent
import com.walmart.common.domain.type.Currency
import com.walmart.common.domain.type.Domain
import com.walmart.common.domain.type.FulfillmentType
import com.walmart.common.domain.type.Tenant
import com.walmart.common.domain.type.Vertical
import com.walmart.common.domain.type.DomainEventType
import com.walmart.fms.FmsOrderApplicationService
import com.walmart.fms.commands.CreateFmsOrderCommand
import com.walmart.marketplace.order.domain.entity.type.Vendor
import com.walmart.oms.order.aggregateroot.OmsOrder
import com.walmart.oms.order.domain.entity.AddressInfo
import com.walmart.oms.order.domain.entity.CustomerContactInfo
import com.walmart.oms.order.domain.entity.OmsOrderItem
import com.walmart.oms.order.domain.entity.SchedulingInfo
import com.walmart.oms.order.valueobject.*
import com.walmart.oms.order.valueobject.events.FmsOrderValueObject
import com.walmart.oms.order.valueobject.mappers.OMSToFMSValueObjectMapper
import spock.lang.Specification

class FmsOrderCreatedEventListenerTest extends Specification {

    OrderCreatedEventListener fmsOrderCreatedEventListener = Mock()
    FmsOrderApplicationService applicationService = Mock()
    OmsOrder testOmsOrder
    FmsOrderValueObject fmsOrderValueObject

    def setup() {
        fmsOrderCreatedEventListener = new OrderCreatedEventListener(fmsOrderApplicationService: applicationService)
        String sourceOrderId = UUID.randomUUID().toString()
        String vendorOrderId = UUID.randomUUID().toString()
        testOmsOrder = OmsOrder.builder()
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
                .scheduleNumber(testOmsOrder.marketPlaceInfo.vendor.nextOSN())
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

        testOmsOrderItem.enrichItemWithCatalogItemData(
                CatalogItem.builder().upcNumbers(["40404404"]).skuId()
                        .price("2.5")
                        .salesUnit("EACH")
                        .pickerDesc("Asda Biscuits")
                        .build())

        testOmsOrder.addItem(testOmsOrderItem)

        HashMap catalogData = new HashMap()

        CatalogItem catalogItem = CatalogItem.builder()
                .cin("464646")
                .upcNumbers(Arrays.asList("535353535"))
                .build()
        catalogData.put("464646", catalogItem)
        fmsOrderValueObject = OMSToFMSValueObjectMapper.INSTANCE.convertOMSOrderToFMSValueObject(testOmsOrder)
    }

    def "Test valid oms order created event"() {
        given:
        DomainEvent domainEvent = new DomainEvent.EventBuilder(DomainEventType.OMS_ORDER_CREATED, "An order was created in oms domain")
                .from(Domain.OMS)
                .to(Domain.FMS)
                .addMessage(fmsOrderValueObject)
                .build()
        when:
        fmsOrderCreatedEventListener.listen(domainEvent)
        then:
        1 * applicationService.createAndProcessFulfillmentOrder(_ as CreateFmsOrderCommand) >> { CreateFmsOrderCommand _createFMSOrderCommand ->
            assert _createFMSOrderCommand.data.orderInfo.sourceOrderId == fmsOrderValueObject.sourceOrderId
            assert _createFMSOrderCommand.data.orderInfo.vertical == fmsOrderValueObject.vertical
            assert _createFMSOrderCommand.data.orderInfo.storeId == fmsOrderValueObject.storeId
            assert _createFMSOrderCommand.data.orderInfo.pickupLocationId == fmsOrderValueObject.pickupLocationId
            assert _createFMSOrderCommand.data.orderInfo.deliveryDate == fmsOrderValueObject.deliveryDate
            assert _createFMSOrderCommand.data.orderInfo.tenant == fmsOrderValueObject.tenant
            assert _createFMSOrderCommand.data.orderInfo.authStatus == fmsOrderValueObject.authStatus
            assert _createFMSOrderCommand.data.marketPlaceInfo.vendor == fmsOrderValueObject.marketPlaceInfo.vendor
            assert _createFMSOrderCommand.data.marketPlaceInfo.vendorOrderId == fmsOrderValueObject.marketPlaceInfo.vendorOrderId
            assert _createFMSOrderCommand.data.priceInfo.webOrderTotal == fmsOrderValueObject.priceInfo.orderTotal
            assert _createFMSOrderCommand.data.addressInfo.county == fmsOrderValueObject.addressInfo.county
            assert _createFMSOrderCommand.data.addressInfo.country == fmsOrderValueObject.addressInfo.country
            assert _createFMSOrderCommand.data.addressInfo.city == fmsOrderValueObject.addressInfo.city
            assert _createFMSOrderCommand.data.addressInfo.addressType == fmsOrderValueObject.addressInfo.addressType
            assert _createFMSOrderCommand.data.addressInfo.addressOne == fmsOrderValueObject.addressInfo.addressOne
            assert _createFMSOrderCommand.data.addressInfo.addressTwo == fmsOrderValueObject.addressInfo.addressTwo
            assert _createFMSOrderCommand.data.addressInfo.addressThree == fmsOrderValueObject.addressInfo.addressThree
            assert _createFMSOrderCommand.data.addressInfo.latitude == fmsOrderValueObject.addressInfo.latitude
            assert _createFMSOrderCommand.data.addressInfo.longitude == fmsOrderValueObject.addressInfo.longitude
            assert _createFMSOrderCommand.data.addressInfo.postalCode == fmsOrderValueObject.addressInfo.postalCode
            assert _createFMSOrderCommand.data.addressInfo.state == fmsOrderValueObject.addressInfo.state
            assert _createFMSOrderCommand.data.schedulingInfo.vanId == fmsOrderValueObject.schedulingInfo.vanId
            assert _createFMSOrderCommand.data.schedulingInfo.tripId == fmsOrderValueObject.schedulingInfo.tripId
            assert _createFMSOrderCommand.data.schedulingInfo.scheduleNumber == fmsOrderValueObject.schedulingInfo.scheduleNumber
            assert fmsOrderValueObject.schedulingInfo.scheduleNumber != null
            assert _createFMSOrderCommand.data.schedulingInfo.scheduleNumber.startsWith(fmsOrderValueObject.marketPlaceInfo.vendor.code)
            String osn = _createFMSOrderCommand.data.schedulingInfo.scheduleNumber
            Long osnLong = Long.parseLong(osn.substring(1, osn.size()))
            assert osnLong != null
            assert osnLong >= 100 && osnLong <= 9999
            assert _createFMSOrderCommand.data.schedulingInfo.scheduleNumber == fmsOrderValueObject.schedulingInfo.scheduleNumber
            assert _createFMSOrderCommand.data.schedulingInfo.doorStepTime == fmsOrderValueObject.schedulingInfo.doorStepTime
            assert _createFMSOrderCommand.data.schedulingInfo.loadNumber == fmsOrderValueObject.schedulingInfo.loadNumber
            assert _createFMSOrderCommand.data.schedulingInfo.slotEndTime == fmsOrderValueObject.schedulingInfo.plannedDueTime
            assert _createFMSOrderCommand.data.schedulingInfo.slotStartTime == fmsOrderValueObject.schedulingInfo.plannedDueTime
            assert _createFMSOrderCommand.data.contactinfo.lastName == fmsOrderValueObject.contactInfo.lastName
            assert _createFMSOrderCommand.data.contactinfo.firstName == fmsOrderValueObject.contactInfo.firstName
            assert _createFMSOrderCommand.data.contactinfo.phoneNumberOne == fmsOrderValueObject.contactInfo.phoneNumberOne
            assert _createFMSOrderCommand.data.contactinfo.phoneNumberTwo == fmsOrderValueObject.contactInfo.phoneNumberTwo
            assert _createFMSOrderCommand.data.contactinfo.mobileNumber == fmsOrderValueObject.contactInfo.mobileNumber
            assert _createFMSOrderCommand.data.contactinfo.email == fmsOrderValueObject.contactInfo.email
//            assert _createFMSOrderCommand.data.contactinfo.customerId == fmsOrderValueObject.contactInfo.
//            assert _createFMSOrderCommand.data.contactinfo.middleName == fmsOrderValueObject.contactInfo.mid
            assert _createFMSOrderCommand.data.contactinfo.title == fmsOrderValueObject.contactInfo.title
            assert _createFMSOrderCommand.data.items[0].unitPrice == fmsOrderValueObject.fmsOrderItemvalueObjectList[0].unitPrice
            assert _createFMSOrderCommand.data.items[0].quantity == fmsOrderValueObject.fmsOrderItemvalueObjectList[0].quantity
            assert _createFMSOrderCommand.data.items[0].itemId == fmsOrderValueObject.fmsOrderItemvalueObjectList[0].skuId
            assert _createFMSOrderCommand.data.items[0].consumerItemNumber == fmsOrderValueObject.fmsOrderItemvalueObjectList[0].cin
            assert _createFMSOrderCommand.data.items[0].weight == fmsOrderValueObject.fmsOrderItemvalueObjectList[0].weight
            assert _createFMSOrderCommand.data.items[0].salesUnit == fmsOrderValueObject.fmsOrderItemvalueObjectList[0].salesUnit
            assert _createFMSOrderCommand.data.items[0].unitOfMeasurement == fmsOrderValueObject.fmsOrderItemvalueObjectList[0].uom
            assert _createFMSOrderCommand.data.items[0].pickerItemDescription == fmsOrderValueObject.fmsOrderItemvalueObjectList[0].itemDescription
            assert _createFMSOrderCommand.data.items[0].imageURL == fmsOrderValueObject.fmsOrderItemvalueObjectList[0].imageUrl
            assert _createFMSOrderCommand.data.items[0].upcs[0] == fmsOrderValueObject.fmsOrderItemvalueObjectList[0].upcs[0].upc
//            assert _createFMSOrderCommand.data.== fmsOrderValueObject.fmsOrderItemvalueObjectList[0].uom
//            assert _createFMSOrderCommand.data.items[0].minIdealDayValue == fmsOrderValueObject.fmsOrderItemvalueObjectList[0].uom
//            assert _createFMSOrderCommand.data.items[0].nilPickQty == fmsOrderValueObject.fmsOrderItemvalueObjectList[0].uom
//            assert _createFMSOrderCommand.data.items[0].pickerItemDescription == fmsOrderValueObject.fmsOrderItemvalueObjectList[0].uom
//            assert _createFMSOrderCommand.data.items[0].sellbyDateRequired == fmsOrderValueObject.fmsOrderItemvalueObjectList[0].uom
//            assert _createFMSOrderCommand.data.items[0].temperatureZone == fmsOrderValueObject.fmsOrderItemvalueObjectList[0].uom
//            assert _createFMSOrderCommand.data.items[0]. == fmsOrderValueObject.fmsOrderItemvalueObjectList[0].itemDescription
//            assert _createFMSOrderCommand.data.items[0].maxIdealDayValue == fmsOrderValueObject.fmsOrderItemvalueObjectList[0].
//            assert _createFMSOrderCommand.data.items[0].imageURL == fmsOrderValueObject.fmsOrderItemvalueObjectList[0].


        }

    }

}
