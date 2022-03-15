package com.walmart.fms.integration.listeners

import com.walmart.common.domain.messaging.DomainEvent
import com.walmart.common.domain.type.CancellationSource
import com.walmart.common.domain.type.Currency
import com.walmart.common.domain.type.DomainEventType
import com.walmart.common.domain.type.Domain
import com.walmart.common.domain.type.FulfillmentType
import com.walmart.common.domain.type.Tenant
import com.walmart.common.domain.type.Vertical
import com.walmart.fms.commands.FmsCancelOrderCommand
import com.walmart.fms.eventprocessors.FmsCancelledCommandService
import com.walmart.marketplace.order.domain.entity.type.Vendor
import com.walmart.oms.order.aggregateroot.OmsOrder
import com.walmart.oms.order.domain.entity.AddressInfo
import com.walmart.oms.order.domain.entity.CustomerContactInfo
import com.walmart.oms.order.domain.entity.OmsOrderItem
import com.walmart.oms.order.domain.entity.SchedulingInfo
import com.walmart.oms.order.valueobject.CatalogItem
import com.walmart.oms.order.valueobject.FullName
import com.walmart.oms.order.valueobject.ItemPriceInfo
import com.walmart.oms.order.valueobject.MarketPlaceInfo
import com.walmart.oms.order.valueobject.Money
import com.walmart.oms.order.valueobject.OrderPriceInfo
import com.walmart.oms.order.valueobject.TelePhone
import com.walmart.oms.order.valueobject.events.FmsOrderValueObject
import com.walmart.oms.order.valueobject.mappers.OMSToFMSValueObjectMapper
import spock.lang.Specification

class OrderCancellationEventListenerTest extends Specification {

    OrderCancellationEventListener orderCancellationEventListener

    FmsCancelledCommandService fmsCancelledCommandService = Mock()

    String sourceOrderId = UUID.randomUUID().toString()
    String vendorOrderId = UUID.randomUUID().toString()
    String storeOrderId = "2626276272"
    FmsOrderValueObject fmsOrderValueObject
    OmsOrder testOmsOrder

    def setup() {

        orderCancellationEventListener = new OrderCancellationEventListener(
                fmsCancelledCommandService:fmsCancelledCommandService)

        testOmsOrder = OmsOrder.builder()
                .vertical(Vertical.ASDAGR)
                .tenant(Tenant.ASDA)
                .storeId("4401")
                .sourceOrderId(sourceOrderId)
                .storeOrderId(storeOrderId)
                .pickupLocationId("4401")
                .spokeStoreId("4401")
                .deliveryDate(new Date())
                .fulfillmentType(FulfillmentType.INSTORE_PICKUP)
                .marketPlaceInfo(MarketPlaceInfo.builder().vendor(Vendor.UBEREATS).vendorOrderId(vendorOrderId).build())
                .orderState("CANCELLED")
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


        OmsOrderItem testOmsOrderItem =  OmsOrderItem.builder()
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

    def "Test Cancellation event handling"() {
        given:

        DomainEvent domainEvent = new DomainEvent.EventBuilder(DomainEventType.OMS_ORDER_CANCELLED, "An order was cancelled in oms domain")
                .from(Domain.OMS)
                .to(Domain.FMS)
                .addMessage(fmsOrderValueObject)
                .build()
        when:
        orderCancellationEventListener.listen(domainEvent)

        then:
        1 * fmsCancelledCommandService.cancelOrder(_ as FmsCancelOrderCommand) >> {FmsCancelOrderCommand _fmsCancelOrderCommand ->
            assert _fmsCancelOrderCommand.data.storeOrderId ==  storeOrderId
            assert _fmsCancelOrderCommand.data.cancellationSource == CancellationSource.VENDOR
            assert _fmsCancelOrderCommand.data.cancelledReasonCode == "VENDOR"
        }


    }
}
