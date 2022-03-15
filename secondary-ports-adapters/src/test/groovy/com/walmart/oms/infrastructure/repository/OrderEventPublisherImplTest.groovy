package com.walmart.oms.infrastructure.repository

import com.walmart.common.domain.event.processing.EventGeneratorService
import com.walmart.common.domain.type.Currency
import com.walmart.common.domain.type.FulfillmentType
import com.walmart.common.domain.type.Tenant
import com.walmart.common.domain.type.Vertical
import com.walmart.marketplace.order.domain.entity.type.Vendor
import com.walmart.oms.domain.event.messages.DwhOrderEventMessage
import com.walmart.oms.infrastructure.configuration.OmsOrderConfig
import com.walmart.oms.order.aggregateroot.OmsOrder
import com.walmart.oms.order.domain.entity.*
import com.walmart.oms.order.valueobject.*
import spock.lang.Specification

class OrderEventPublisherImplTest extends Specification {

    OrderUpdateEventPublisher orderUpdateEventPublisher
    EventGeneratorService eventGeneratorService = Mock()
    OmsOrderConfig omsOrderConfig = Mock()

    def setup() {
        orderUpdateEventPublisher = new OrderUpdateEventPublisherImpl(
                eventGeneratorService: eventGeneratorService,
                omsOrderConfig: omsOrderConfig)
    }

    def "If enabled , Successfully publishing the Spring event"() {
        given:
        OmsOrder omsOrder = mockOmsOrder()
        omsOrderConfig.isPublishOrderUpdateEvent() >> {
            return true
        }
        when:
        orderUpdateEventPublisher.emitOrderUpdateEvent(omsOrder)
        then:
        1 * omsOrderConfig.isPublishOrderUpdateEvent() >> {
            return true
        }
        1 * eventGeneratorService.publishApplicationEvent(_ as DwhOrderEventMessage)
    }

    def "If enabled and failed while publishing the Spring event"() {
        given:
        OmsOrder omsOrder = Mock()
        omsOrderConfig.isPublishOrderUpdateEvent() >> {
            return true
        }
        when:
        orderUpdateEventPublisher.emitOrderUpdateEvent(omsOrder)
        then:
        0 * eventGeneratorService.publishApplicationEvent(_ as DwhOrderEventMessage)
    }

    def "If not enabled, by passing the spring event generation"() {
        given:
        omsOrderConfig.isPublishOrderUpdateEvent() >> {
            return true
        }
        OmsOrder omsOrder = mockOmsOrder()
        when:
        orderUpdateEventPublisher.emitOrderUpdateEvent(omsOrder)
        then:
        1 * omsOrderConfig.isPublishOrderUpdateEvent() >> {
            return false
        }
        0 * eventGeneratorService.publishApplicationEvent(_ as DwhOrderEventMessage)
    }

    OmsOrder mockOmsOrder() {
        OmsOrder testOmsOrder = OmsOrder.builder()
                .vertical(Vertical.ASDAGR)
                .tenant(Tenant.ASDA)
                .storeId("4401")
                .sourceOrderId(UUID.randomUUID().toString())
                .storeOrderId("123456789")
                .pickupLocationId("4401")
                .spokeStoreId("4401")
                .deliveryDate(new Date())
                .fulfillmentType(FulfillmentType.INSTORE_PICKUP)
                .marketPlaceInfo(MarketPlaceInfo.builder().vendor(Vendor.UBEREATS).vendorOrderId(UUID.randomUUID().toString()).build())
                .orderState("RECD_AT_STORE")
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
                .plannedDueTime(new Date()).build())

        testOmsOrder.addContactInfo(CustomerContactInfo.builder()
                .order(testOmsOrder)
                .fullName(new FullName(null, "John", null, "Doe"))
                .phoneNumberOne(new TelePhone("0123456789")).build())

        OmsOrderBundledItem omsOrderBundledItem = new OmsOrderBundledItem(new OmsOrderItem(), "iafd", 2, 1, "agdd", "dsads", "c1")

        testOmsOrder.addItem(OmsOrderItem.builder()
                .omsOrder(testOmsOrder)
                .cin("464646")
                .itemPriceInfo(new ItemPriceInfo(new Money(BigDecimal.valueOf(2.5), Currency.GBP), new Money(BigDecimal.valueOf(5.0), Currency.GBP)))
                .quantity(2)
                .salesUnit("EACH")
                .uom("E")
                .bundledItemList(Arrays.asList(omsOrderBundledItem))
                .build())
        OmsOrderItem omsOrderItem = new OmsOrderItem()

        PickedItemUpc pickedItemUpc = PickedItemUpc.builder().uom("12").upc("12").win("23").build()
        PickedItem pickedItem = PickedItem.builder()
                .omsOrderItem(omsOrderItem).quantity(10).pickedItemUpcList(Arrays.asList(pickedItemUpc))
                .departmentID("1").orderedCin("1")
                .pickedItemDescription("this is test item")
                .build()
        SubstitutedItem substitutedItem = SubstitutedItem.builder()
                .quantity(1).department("123")
                .consumerItemNumber("12222")
                .upcs(Arrays.asList(SubstitutedItemUpc.builder().uom("u").upc("sdfad").build()))
                .description("description")
                .substitutedItemPriceInfo(SubstitutedItemPriceInfo.builder()
                        .totalPrice(BigDecimal.TEN).unitPrice(BigDecimal.ONE)
                        .build())
                .build()
        substitutedItem.addPickedItem(pickedItem)
        List<SubstitutedItem> list = Arrays.asList(substitutedItem)
        pickedItem.substitutedItems = list

        omsOrderItem.pickedItem = pickedItem
        testOmsOrder.orderItemList = Arrays.asList(omsOrderItem)



        return testOmsOrder
    }


}
