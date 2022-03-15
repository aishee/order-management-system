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
import com.walmart.oms.commands.PickCompleteCommand
import com.walmart.oms.commands.extensions.OrderInfo
import com.walmart.oms.domain.error.exception.OMSBadRequestException
import com.walmart.oms.order.aggregateroot.OmsOrder
import com.walmart.oms.order.domain.entity.OmsOrderItem
import com.walmart.oms.order.domain.entity.PickedItem
import com.walmart.oms.order.domain.entity.PickedItemUpc
import com.walmart.oms.order.factory.OmsOrderFactory
import com.walmart.oms.order.repository.IOmsOrderRepository
import com.walmart.oms.order.valueobject.ItemPriceInfo
import com.walmart.oms.order.valueobject.MarketPlaceInfo
import com.walmart.oms.order.valueobject.Money
import com.walmart.oms.order.valueobject.Picker
import spock.lang.Specification

class OmsPickCompleteCommandServiceTest extends Specification {

    String testCin = "4647474"

    OmsPickCompleteCommandService omsPickCompleteCommandService

    OmsOrderFactory omsOrderFactory = Mock()

    IOmsOrderRepository omsOrderRepository = Mock()

    DomainEventPublisher omsDomainEventPublisher = Mock()

    String sourceOrderId = UUID.randomUUID().toString()

    String vendorOrderId = UUID.randomUUID().toString()

    OmsOrder testOmsOrder

    def setup() {

        omsPickCompleteCommandService = new OmsPickCompleteCommandService(
                omsOrderRepository: omsOrderRepository,
                omsOrderFactory: omsOrderFactory,
                omsDomainEventPublisher: omsDomainEventPublisher)

    }

    def "Test processing of pick complete command"() {

        given:
        testOmsOrder = MockOmsOrder()
        PickCompleteCommand pickCompleteCommand = buildPickCompleteCommand()
        omsOrderFactory.getOmsOrderBySourceOrder(_ as String, _ as Tenant, _ as Vertical) >> testOmsOrder
        omsOrderFactory.createPickedItem(_ as String, _ as String, _ as String, _ as String, _ as List) >> mockPickedItem()

        when:
        omsPickCompleteCommandService.pickCompleteOrder(pickCompleteCommand)

        then:

        1 * omsOrderRepository.save(_ as OmsOrder) >> { OmsOrder _omsOrder ->
            assert _omsOrder.sourceOrderId == sourceOrderId
            assert _omsOrder.getOrderItemList().get(0).getPickedItem().getOrderedCin().equalsIgnoreCase(testCin)
            assert _omsOrder.getOrderItemList().get(0).getPickedItem().getPickedItemUpcList() != null
            assert _omsOrder.getOrderItemList().get(0).getPickedItem().getPickedItemUpcList().get(0).getQuantity() == 1
            assert _omsOrder.getOrderItemList().get(0).getPickedItem().getQuantity() == 2
            assert _omsOrder.getOrderItemList().get(0).getPickedItem().pickedItemPriceInfo.unitPrice == new Money(2.5, Currency.GBP)
            assert _omsOrder.getOrderState() == "PICK_COMPLETE"
            return testOmsOrder
        }

        1 * omsDomainEventPublisher.publish(_ as DomainEvent, _ as String) >> { DomainEvent _domainEvent, String _destination ->
            assert _destination == "OMS_ORDER_UPDATES"
            assert _domainEvent.source == Domain.OMS
            assert _domainEvent.key == testOmsOrder.id
            assert _domainEvent.name == DomainEventType.OMS_ORDER_PICK_COMPLETE
            assert _domainEvent.message != null
            Optional<MarketPlaceOrderValueObject> valueObject = _domainEvent.createObjectFromJson(MarketPlaceOrderValueObject.class)
            assert testOmsOrder.sourceOrderId == valueObject.get().sourceOrderId
            assert testOmsOrder.orderState == valueObject.get().orderState
            assert testOmsOrder.storeId == valueObject.get().storeId
            assert testOmsOrder.marketPlaceInfo.vendorOrderId == valueObject.get().vendorOrderId
            assert testOmsOrder.marketPlaceInfo.vendor == valueObject.get().vendorId

        }

    }

    def "Test processing of Partial Pick Complete command"() {
        given:
        testOmsOrder = MockOmsOrder()
        PickCompleteCommand pickCompleteCommand = buildPickCompleteCommand()
        omsOrderFactory.getOmsOrderBySourceOrder(_ as String, _ as Tenant, _ as Vertical) >> testOmsOrder
        omsOrderFactory.createPickedItem(_ as String, _ as String, _ as String, _ as String, _ as List) >> mockPartialNilPickedItem()
        when:
        omsPickCompleteCommandService.pickCompleteOrder(pickCompleteCommand)
        then:
        1 * omsOrderRepository.save(_ as OmsOrder) >> { OmsOrder _omsOrder ->
            assert _omsOrder.sourceOrderId == sourceOrderId
            assert _omsOrder.getOrderItemList().get(0).getPickedItem().getOrderedCin().equalsIgnoreCase(testCin)
            assert _omsOrder.getOrderItemList().get(0).getPickedItem().getPickedItemUpcList() != null
            assert _omsOrder.getOrderItemList().get(0).getPickedItem().getPickedItemUpcList().get(0).getQuantity() == 0
            assert _omsOrder.getOrderItemList().get(0).getPickedItem().getQuantity() == 1
            assert _omsOrder.getOrderItemList().get(0).getPickedItem().pickedItemPriceInfo.unitPrice == new Money(2.5, Currency.GBP)
            assert _omsOrder.getOrderState() == "PICK_COMPLETE"
            return testOmsOrder
        }

    }

    def "Test processing Pick Complete with Existing Cancelled Order"() {
        given:
        testOmsOrder = MockOmsCancelOrder()
        PickCompleteCommand pickCompleteCommand = buildPickCompleteCommand()
        omsOrderFactory.getOmsOrderBySourceOrder(_ as String, _ as Tenant, _ as Vertical) >> testOmsOrder
        omsOrderFactory.createPickedItem(_ as String, _ as String, _ as String, _ as String, _ as List) >> mockPartialNilPickedItem()
        when:
        omsPickCompleteCommandService.pickCompleteOrder(pickCompleteCommand)
        then:
        thrown(OMSBadRequestException)
    }

    def "Test processing Pick Complete with Existing Delivered Order"() {
        given:
        testOmsOrder = MockOmsDeliveredOrder()
        PickCompleteCommand pickCompleteCommand = buildPickCompleteCommand()
        omsOrderFactory.getOmsOrderBySourceOrder(_ as String, _ as Tenant, _ as Vertical) >> testOmsOrder
        omsOrderFactory.createPickedItem(_ as String, _ as String, _ as String, _ as String, _ as List) >> mockPartialNilPickedItem()
        when:
        omsPickCompleteCommandService.pickCompleteOrder(pickCompleteCommand)
        then:
        omsOrderRepository.save(_ as OmsOrder) >> { OmsOrder _omsOrder ->
            assert _omsOrder.sourceOrderId == sourceOrderId
            assert _omsOrder.getOrderItemList().get(0).getPickedItem().getOrderedCin().equalsIgnoreCase(testCin)
            assert _omsOrder.getOrderItemList().get(0).getPickedItem().getPickedItemUpcList() != null
            assert _omsOrder.getOrderItemList().get(0).getPickedItem().getPickedItemUpcList().get(0).getQuantity() == 1
            assert _omsOrder.getOrderItemList().get(0).getPickedItem().getQuantity() == 2
            assert _omsOrder.getOrderItemList().get(0).getPickedItem().pickedItemPriceInfo.unitPrice == new Money(2.5, Currency.GBP)
            assert _omsOrder.getOrderState() == OmsOrder.OrderStatus.DELIVERED
            return testOmsOrder
        }
    }

    PickedItemUpc mockPickedItemUpc() {
        return PickedItemUpc.builder()
                .id(UUID.randomUUID().toString())
                .quantity(1)
                .upc("27272727272")
                .uom("E")
                .storeUnitPrice(new Money(2.45, Currency.GBP))
                .win("112")
                .build()

    }

    PickedItemUpc mockPickedItemUpcTwo() {
        return PickedItemUpc.builder()
                .id(UUID.randomUUID().toString())
                .quantity(1)
                .upc("27272727273")
                .uom("E")
                .storeUnitPrice(new Money(2.5, Currency.GBP))
                .win("113")
                .build()
    }

    PickedItem mockPickedItem() {
        return PickedItem.builder()
                .omsOrderItem(OmsOrderItem.builder()
                        .id(sourceOrderId)
                        .quantity(2)
                        .omsOrder(testOmsOrder)
                        .cin("4647474")
                        .itemDescription("test description")
                        .salesUnit("EACH")
                        .uom("E")
                        .skuId("1234")
                        .itemPriceInfo(getItemPriceInfo())
                        .build())
                .pickedItemDescription("test description")
                .id(UUID.randomUUID().toString())
                .departmentID("112")
                .orderedCin("4647474")
                .picker(new Picker("testPicker"))
                .pickedItemUpcList(Arrays.asList(mockPickedItemUpc(), mockPickedItemUpcTwo())).build()

    }

    private static ItemPriceInfo getItemPriceInfo() {
        return new ItemPriceInfo(new Money(BigDecimal.valueOf(2.5), Currency.GBP), new Money(BigDecimal.valueOf(5.0), Currency.GBP))
    }

    PickedItem mockPartialNilPickedItem() {
        return PickedItem.builder()
                .omsOrderItem(OmsOrderItem.builder()
                        .id(sourceOrderId)
                        .quantity(2)
                        .omsOrder(testOmsOrder)
                        .cin("4647474")
                        .itemDescription("test description")
                        .salesUnit("EACH")
                        .uom("E")
                        .skuId("1234")
                        .itemPriceInfo(getItemPriceInfo())
                        .build())
                .pickedItemDescription("test description")
                .id(UUID.randomUUID().toString())
                .departmentID(null)
                .orderedCin("4647474")
                .picker(new Picker("testPicker"))
                .pickedItemUpcList(Arrays.asList(PickedItemUpc.builder()
                        .id(UUID.randomUUID().toString())
                        .quantity(0)
                        .upc("27272727272")
                        .uom("E")
                        .storeUnitPrice(new Money(2.45, Currency.GBP))
                        .win("112")
                        .build(), PickedItemUpc.builder()
                        .id(UUID.randomUUID().toString())
                        .quantity(1)
                        .upc("27272727273")
                        .uom("E")
                        .storeUnitPrice(new Money(2.5, Currency.GBP))
                        .win("113")
                        .build())).build()

    }

    PickCompleteCommand buildPickCompleteCommand() {
        OrderInfo orderInfo = OrderInfo.builder().sourceOrderId(sourceOrderId)
                .storeId("4401")
                .spokeStoreId("4401")
                .deliveryDate(new Date())
                .vertical(Vertical.MARKETPLACE)
                .tenant(Tenant.ASDA)
                .build()

        List<PickCompleteCommand.PickedItemInfo> pickedItemInfoList = Arrays.asList(PickCompleteCommand.PickedItemInfo.builder()
                .cin("4647474")
                .departmentId("124")
                .pickedBy("testpicker")
                .pickedItemDescription("test picked item description")
                .pickedItemUpcs(Arrays.asList(PickCompleteCommand.PickedItemUpc.builder()
                        .win("112")
                        .weight(0.0)
                        .upc("27272727272")
                        .unitPrice(BigDecimal.valueOf(2.5))
                        .pickedQuantity(2).uom("E").build())).build())

        return PickCompleteCommand.builder().data(PickCompleteCommand.OmsOrderData.builder().orderInfo(orderInfo)
                .pickedItems(pickedItemInfoList).build()).build()
    }

    OmsOrder MockOmsDeliveredOrder() {
        OmsOrder omsOrder = MockOmsOrder();
        omsOrder.orderState = OmsOrder.OrderStatus.DELIVERED
        return omsOrder;
    }

    OmsOrder MockOmsCancelOrder() {
        OmsOrder omsOrder = MockOmsOrder();
        omsOrder.orderState = OmsOrder.OrderStatus.CANCELLED
        return omsOrder;
    }

    OmsOrder MockOmsOrder() {

        String testCin = "4647474"
        OmsOrder omsOrder = OmsOrder.builder()
                .tenant(Tenant.ASDA)
                .vertical(Vertical.MARKETPLACE)
                .orderState("RECD_AT_STORE")
                .deliveryDate(new Date())
                .sourceOrderId(sourceOrderId)
                .storeOrderId("123456789")
                .storeId("4401")
                .fulfillmentType(FulfillmentType.INSTORE_PICKUP)
                .marketPlaceInfo(new MarketPlaceInfo(Vendor.UBEREATS, vendorOrderId))
                .spokeStoreId("4401")
                .build()

        omsOrder.addItem(OmsOrderItem.builder().quantity(2)
                .itemDescription("test description")
                .cin(testCin)
                .omsOrder(omsOrder)
                .itemPriceInfo(getItemPriceInfo())
                .id(UUID.randomUUID().toString())
                .build())

        return omsOrder
    }


}