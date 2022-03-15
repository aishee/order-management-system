package com.walmart.fms.eventprocessors

import com.walmart.common.domain.event.processing.EventGeneratorService
import com.walmart.common.domain.messaging.DomainEvent
import com.walmart.common.domain.messaging.DomainEventPublisher
import com.walmart.common.domain.type.*
import com.walmart.fms.commands.FmsPickCompleteCommand
import com.walmart.fms.commands.extensions.OrderInfo
import com.walmart.fms.domain.error.exception.FMSBadRequestException
import com.walmart.fms.order.aggregateroot.FmsOrder
import com.walmart.fms.order.domain.entity.*
import com.walmart.fms.order.factory.FmsOrderFactory
import com.walmart.fms.order.repository.IFmsOrderRepository
import com.walmart.fms.order.valueobject.*
import com.walmart.fms.order.valueobject.events.FmsOrderValueObject
import com.walmart.marketplace.order.domain.entity.type.Vendor
import spock.lang.Specification

class FmsPickCompleteCommandServiceTest extends Specification {

    String testCin = "4647474"
    FmsPickCompleteCommandService fmsPickCompleteCommandService
    FmsOrderFactory fmsOrderFactory = Mock()
    IFmsOrderRepository fmsOrderRepository = Mock()

    DomainEventPublisher fmsDomainEventPublisher = Mock()
    EventGeneratorService eventGeneratorService = Mock()

    String storeOrderId = UUID.randomUUID().toString()

    String vendorOrderId = UUID.randomUUID().toString()

    FmsOrder testFmsOrder

    def setup() {

        fmsPickCompleteCommandService = new FmsPickCompleteCommandService(fmsOrderFactory,
                fmsOrderRepository,
                fmsDomainEventPublisher,
                eventGeneratorService)

    }

    def "Test processing of pick complete command"() {

        given:
        testFmsOrder = MockFmsOrder()
        FmsPickCompleteCommand pickCompleteCommand = buildPickCompleteCommand()
        fmsOrderFactory.getFmsOrderByStoreOrder(_ as String) >> testFmsOrder
        fmsOrderFactory.createPickedItemUpc(_ as long, _ as BigDecimal, _ as String, _ as String, _ as String) >> mockPickedItemUpc()
        fmsOrderFactory.createPickedItem(_ as String, _ as String, _ as String, _ as String, _ as List) >> mockPickedItem()

        when:
        fmsPickCompleteCommandService.pickCompleteOrder(pickCompleteCommand)

        then:

        1 * fmsOrderRepository.save(_ as FmsOrder) >> { FmsOrder _fmsOrder ->
            assert _fmsOrder.storeOrderId == storeOrderId
            assert _fmsOrder.getFmsOrderItems().get(0).getPickedItem().getCin().equalsIgnoreCase(testCin)
            assert _fmsOrder.getFmsOrderItems().get(0).getPickedItem().getPickedItemUpcList() != null
            assert _fmsOrder.getFmsOrderItems().get(0).getPickedItem().getPickedItemUpcList().get(0).getQuantity() == 2
            return testFmsOrder
        }

        1 * fmsDomainEventPublisher.publish(_ as DomainEvent, _ as String) >> { DomainEvent _domainEvent, String _destination ->
            assert _destination == "FMS_ORDER_UPDATES"
            assert _domainEvent.source == Domain.FMS
            assert _domainEvent.key == testFmsOrder.id
            assert _domainEvent.name == DomainEventType.FMS_ORDER_PICK_COMPLETE
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
            assert testFmsOrder.fmsOrderItems[0].itemPriceInfo.unitPrice.amount == valueObject.get().fmsOrderItemvalueObjectList[0].unitPrice

        }

    }

    def "Test processing of pick complete command with existing Cancelled Status Order"() {

        given:
        testFmsOrder = MockFmsCancelOrder()
        FmsPickCompleteCommand pickCompleteCommand = buildPickCompleteCommandMoreQuantity()
        fmsOrderFactory.getFmsOrderByStoreOrder(_ as String) >> testFmsOrder
        fmsOrderFactory.createPickedItemUpc(_ as long, _ as BigDecimal, _ as String, _ as String, _ as String) >> mockPickedItemUpcQuantityMore()
        fmsOrderFactory.createPickedItem(_ as String, _ as String, _ as String, _ as String, _ as List) >> mockPickedItemMoreQuantity()

        when:
        fmsPickCompleteCommandService.pickCompleteOrder(pickCompleteCommand)

        then:
        thrown(FMSBadRequestException)
    }

    def "Test processing of pick complete command with existing PICK_COMPLETE Status Order"() {

        given:
        testFmsOrder = MockFmsPickCompleteOrder()
        FmsPickCompleteCommand pickCompleteCommand = buildPickCompleteCommandMoreQuantity()
        fmsOrderFactory.getFmsOrderByStoreOrder(_ as String) >> testFmsOrder
        fmsOrderFactory.createPickedItemUpc(_ as long, _ as BigDecimal, _ as String, _ as String, _ as String) >> mockPickedItemUpcQuantityMore()
        fmsOrderFactory.createPickedItem(_ as String, _ as String, _ as String, _ as String, _ as List) >> mockPickedItemMoreQuantity()

        when:
        fmsPickCompleteCommandService.pickCompleteOrder(pickCompleteCommand)

        then:
        thrown(FMSBadRequestException)
    }

    def "Test processing of pick complete command with existing Delivered Status Order"() {

        given:
        testFmsOrder = MockFmsDeliveredOrder()
        FmsPickCompleteCommand pickCompleteCommand = buildPickCompleteCommandMoreQuantity()
        fmsOrderFactory.getFmsOrderByStoreOrder(_ as String) >> testFmsOrder
        fmsOrderFactory.createPickedItemUpc(_ as long, _ as BigDecimal, _ as String, _ as String, _ as String) >> mockPickedItemUpcQuantityMore()
        fmsOrderFactory.createPickedItem(_ as String, _ as String, _ as String, _ as String, _ as List) >> mockPickedItemMoreQuantity()

        when:
        fmsPickCompleteCommandService.pickCompleteOrder(pickCompleteCommand)

        then:
        thrown(FMSBadRequestException)
    }


    def "All Nil Picks Pickup Command"() {

        given:
        testFmsOrder = MockFmsOrder()
        FmsPickCompleteCommand pickCompleteCommand = buildNilPickCompleteCommand()
        fmsOrderFactory.getFmsOrderByStoreOrder(_ as String) >> testFmsOrder
        fmsOrderFactory.createPickedItemUpc(_ as long, _ as BigDecimal, _ as String, _ as String, _ as String) >> mockPickedItemUpc()
        fmsOrderFactory.createPickedItem(_ as String, _ as String, _ as String, _ as String, _ as List) >> mockNilPickedItem()

        when:
        fmsPickCompleteCommandService.pickCompleteOrder(pickCompleteCommand)

        then:

        1 * fmsOrderRepository.save(_ as FmsOrder) >> { FmsOrder _fmsOrder ->
            assert _fmsOrder.storeOrderId == storeOrderId
            assert _fmsOrder.getFmsOrderItems().get(0).getPickedItem().getCin().equalsIgnoreCase(testCin)
            assert _fmsOrder.getFmsOrderItems().get(0).getPickedItem().getPickedItemUpcList() != null
            assert _fmsOrder.getFmsOrderItems().get(0).getPickedItem().getPickedItemUpcList().size() == 1
            assert _fmsOrder.getFmsOrderItems().get(0).getPickedItem().getPickedItemUpcList().get(0).getQuantity() == 0
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
        }
    }

    def "Pick Complete Without Existing Orders"() {

        given:
        testFmsOrder = MockFmsNullOrders()
        FmsPickCompleteCommand pickCompleteCommand = buildOrderItemsNullPickCompleteCommand()
        fmsOrderFactory.getFmsOrderByStoreOrder(_ as String) >> new FmsOrder("INITIAL")
        fmsOrderFactory.createPickedItemUpc(_ as long, _ as BigDecimal, _ as String, _ as String, _ as String) >> null
        fmsOrderFactory.createPickedItem(_ as String, _ as String, _ as String, _ as String, _ as List) >> null

        when:
        fmsPickCompleteCommandService.pickCompleteOrder(pickCompleteCommand)

        then:
        thrown(com.walmart.fms.domain.error.exception.FMSBadRequestException.class)
    }

    def "Test processing of pick complete command Cancel Order"() {

        given:
        testFmsOrder = mockFmsOrderCancelOrder()
        FmsPickCompleteCommand pickCompleteCommand = buildPickCompleteCommand()
        fmsOrderFactory.getFmsOrderByStoreOrder(_ as String) >> testFmsOrder
        fmsOrderFactory.createPickedItemUpc(_ as long, _ as BigDecimal, _ as String, _ as String, _ as String) >> mockPickedItemUpc()
        fmsOrderFactory.createPickedItem(_ as String, _ as String, _ as String, _ as String, _ as List) >> mockNilPickedItem()
        when:
        fmsPickCompleteCommandService.pickCompleteOrder(pickCompleteCommand)

        then:

        1 * fmsOrderRepository.save(_ as FmsOrder) >> { FmsOrder _fmsOrder ->
            assert _fmsOrder.storeOrderId == storeOrderId
            assert _fmsOrder.getFmsOrderItems().get(0).getPickedItem().getCin().equalsIgnoreCase(testCin)
            assert _fmsOrder.getFmsOrderItems().get(0).getPickedItem().getPickedItemUpcList() != null
            assert _fmsOrder.getFmsOrderItems().get(0).getPickedItem().getPickedItemUpcList().get(0).getQuantity() == 0
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
            assert testFmsOrder.fmsOrderItems[0].itemPriceInfo.unitPrice.amount == valueObject.get().fmsOrderItemvalueObjectList[0].unitPrice
        }

    }

    FmsPickedItemUpc mockPickedItemUpc() {
        return FmsPickedItemUpc.builder()
                .id(UUID.randomUUID().toString())
                .quantity(2)
                .upc("27272727272")
                .uom("E")
                .storeUnitPrice(new Money(2.5, Currency.GBP))
                .win("112")
                .build()
    }

    FmsPickedItemUpc mockPickedItemUpcQuantityMore() {
        return FmsPickedItemUpc.builder()
                .id(UUID.randomUUID().toString())
                .quantity(4)
                .upc("27272727272")
                .uom("E")
                .storeUnitPrice(new Money(2.5, Currency.GBP))
                .win("112")
                .build()
    }

    FmsPickedItem mockPickedItem() {
        FmsPickedItem fmsPickedItem = FmsPickedItem.builder()
                .quantity(2)
                .fmsOrderItem(mockFmsItem(fmsOrder))
                .pickedItemDescription("test description")
                .id(UUID.randomUUID().toString())
                .departmentID("112")
                .cin("4647474")
                .picker(new Picker("testPicker"))
                .build()

        fmsPickedItem.addPickedItemUpcList(Arrays.asList(FmsPickedItemUpc.builder()
                .id(UUID.randomUUID().toString())
                .quantity(2)
                .upc("27272727272")
                .uom("E")
                .storeUnitPrice(new Money(2.5, Currency.GBP))
                .win("112")
                .build()))

        return fmsPickedItem
    }

    FmsSubstitutedItem mockSubstitutedItem() {

        List<FmsSubstitutedItemUpc> upcs = Arrays.asList(FmsSubstitutedItemUpc.builder()
                .upc("27272727272")
                .uom("E")
                .build())

        FmsSubstitutedItem fmsSubstitutedItem = FmsSubstitutedItem.builder()
                .description("ABCD Substitute item")
                .quantity(2L)
                .weight(2.2)
                .walmartItemNumber("123")
                .consumerItemNumber("1212")
                .department("34")
                .substitutedItemPriceInfo(SubstitutedItemPriceInfo.builder().unitPrice(BigDecimal.TEN)
                        .totalPrice(BigDecimal.valueOf(20L)).build())
                .upcs(Arrays.asList(FmsSubstitutedItemUpc.builder().upc("a").uom("b").build()))
                .build()

        return fmsSubstitutedItem
    }

    FmsPickedItem mockPickedItemMoreQuantity() {
        FmsPickedItem fmsPickedItem = FmsPickedItem.builder()
                .quantity(3)
                .fmsOrderItem(FmsOrderItem.builder()
                        .id(UUID.randomUUID().toString())
                        .quantity(4)
                        .fmsOrder(testFmsOrder)
                        .consumerItemNumber("4647474")
                        .itemPriceInfo(new ItemPriceInfo(new Money(BigDecimal.valueOf(2.5), Currency.GBP)))
                        .substitutionOption(SubstitutionOption.DO_NOT_SUBSTITUTE)
                        .build())
                .pickedItemDescription("test description")
                .id(UUID.randomUUID().toString())
                .departmentID("112")
                .cin("4647474")
                .picker(new Picker("testPicker"))
                .build()

        fmsPickedItem.addPickedItemUpcList(Arrays.asList(FmsPickedItemUpc.builder()
                .id(UUID.randomUUID().toString())
                .quantity(3)
                .upc("27272727272")
                .uom("E")
                .storeUnitPrice(new Money(2.5, Currency.GBP))
                .win("112")
                .build()))

        return fmsPickedItem
    }

    FmsPickedItem mockNilPickedItem() {
        FmsPickedItem fmsPickedItem = FmsPickedItem.builder()
                .quantity(0)
                .fmsOrderItem(mockFmsItem(fmsOrder))
                .pickedItemDescription("test description")
                .id(UUID.randomUUID().toString())
                .departmentID(null)
                .cin("4647474")
                .picker(new Picker("testPicker"))
                .build()

        fmsPickedItem.addPickedItemUpcList(Arrays.asList(FmsPickedItemUpc.builder()
                .id(UUID.randomUUID().toString())
                .quantity(0)
                .upc("27272727272")
                .uom("E")
                .storeUnitPrice(new Money(2.5, Currency.GBP))
                .win("112")
                .build()))

        return fmsPickedItem
    }

    FmsPickCompleteCommand buildPickCompleteCommand() {
        OrderInfo orderInfo = getOrderInfo()
        List<FmsPickCompleteCommand.PickedItemInfo> pickedItemInfoList = getPickedItemInfoList()
        return getFmsPickCompleteCommand(orderInfo, pickedItemInfoList)
    }

    FmsPickCompleteCommand buildPickCompleteCommandForSubstitution() {
        OrderInfo orderInfo = getOrderInfo()
        List<FmsPickCompleteCommand.PickedItemInfo> pickedItemInfoList = getPickedItemInfoListForSubstitution()
        return getFmsPickCompleteCommand(orderInfo, pickedItemInfoList)
    }

    private FmsPickCompleteCommand getFmsPickCompleteCommand(OrderInfo orderInfo, List<FmsPickCompleteCommand.PickedItemInfo> pickedItemInfoList) {
        return FmsPickCompleteCommand.builder().data(FmsPickCompleteCommand.FmsOrderData.builder().orderInfo(orderInfo)
                .pickedItems(pickedItemInfoList).build()).build()
    }

    FmsPickCompleteCommand buildPickCompleteCommandMoreQuantity() {

        OrderInfo orderInfo = getOrderInfo()
        List<FmsPickCompleteCommand.PickedItemInfo> pickedItemInfoList = Arrays.asList(FmsPickCompleteCommand.PickedItemInfo.builder()
                .cin("4647474")
                .departmentId("124")
                .pickedBy("TestPicker")
                .pickedItemDescription("test picked item description")
                .pickedItemUpcs(Arrays.asList(FmsPickCompleteCommand.PickedItemUpc.builder()
                        .win("112")
                        .upc("27272")
                        .unitPrice(BigDecimal.valueOf(2.5))
                        .weight(0.0)
                        .pickedQuantity(4).uom("E").build())).build())

        return getFmsPickCompleteCommand(orderInfo, pickedItemInfoList)
    }

    private OrderInfo getOrderInfo() {
        return OrderInfo.builder().sourceOrderId("444333")
                .storeOrderId(storeOrderId)
                .storeId("4401")
                .deliveryDate(new Date())
                .vertical(Vertical.MARKETPLACE)
                .tenant(Tenant.ASDA)
                .vendor(Vendor.TESTVENDOR.toString())
                .build()
    }

    FmsPickCompleteCommand buildOrderItemsNullPickCompleteCommand() {
        OrderInfo orderInfo = getOrderInfoForCancelled()
        List<FmsPickCompleteCommand.PickedItemInfo> pickedItemInfoList = null
        return getFmsPickCompleteCommand(orderInfo, pickedItemInfoList)
    }

    FmsPickCompleteCommand buildNilPickCompleteCommand() {
        OrderInfo orderInfo = getOrderInfoForCancelled()
        List<FmsPickCompleteCommand.PickedItemInfo> pickedItemInfoList = getPickedItemInfoList()
        return getFmsPickCompleteCommand(orderInfo, pickedItemInfoList)
    }

    private OrderInfo getOrderInfoForCancelled() {
        return OrderInfo.builder().sourceOrderId("444333")
                .storeOrderId(storeOrderId)
                .storeId("4401")
                .orderStatus("CANCELLED")
                .cancelledReasonCode("NIL PICKED")
                .deliveryDate(new Date())
                .vertical(Vertical.MARKETPLACE)
                .tenant(Tenant.ASDA)
                .vendor(Vendor.TESTVENDOR.toString())
                .build()
    }

    private List<FmsPickCompleteCommand.PickedItemInfo> getPickedItemInfoList() {
        return Arrays.asList(FmsPickCompleteCommand.PickedItemInfo.builder()
                .cin("4647474")
                .departmentId("124")
                .pickedBy("TestPicker")
                .pickedItemDescription("test picked item description")
                .pickedItemUpcs(getPickedItemUpc()).build())
    }

    private List<FmsPickCompleteCommand.PickedItemInfo> getPickedItemInfoListForSubstitution() {
        return Arrays.asList(FmsPickCompleteCommand.PickedItemInfo.builder()
                .cin("4647474")
                .departmentId("124")
                .pickedBy("TestPicker")
                .pickedItemDescription("test picked item description")
                .pickedItemUpcs(getPickedItemUpc())
                .substitutedItemInfoList(Arrays.asList(FmsPickCompleteCommand.SubstitutedItemInfo.builder()
                        .description("ABCD Substitute item")
                        .quantity(2L)
                        .weight(2.2)
                        .walmartItemNumber("123")
                        .consumerItemNumber("1212")
                        .department("34")
                        .unitPrice(BigDecimal.TEN)
                        .upcs(Arrays.asList(FmsPickCompleteCommand.SubstitutedItemUpc.builder().uom("EACH").upc("2222").build()))
                        .build()
                ))
                .build()

        )
    }

    private List<FmsPickCompleteCommand.PickedItemUpc> getPickedItemUpc() {
        Arrays.asList(FmsPickCompleteCommand.PickedItemUpc.builder()
                .win("112")
                .upc("27272")
                .unitPrice(BigDecimal.valueOf(2.5))
                .weight(0.0)
                .pickedQuantity(2).uom("E").build())
    }

    FmsOrder MockFmsOrder() {
        FmsOrder fmsOrder = getFmsOrder()
        fmsOrder.addItem(mockFmsItem(fmsOrder))
        return fmsOrder
    }

    FmsOrder mockFmsOrderCancelOrder() {
        FmsOrder fmsOrder = getFmsOrder()
        fmsOrder.addItem(mockFmsItemCancelOrder(fmsOrder))
        return fmsOrder
    }

    FmsOrder MockFmsOrderForSubstitution() {
        FmsOrder fmsOrder = getFmsOrder()
        FmsOrderItem fmsItem = FmsOrderItem.builder()
                .fmsOrder(fmsOrder)
                .consumerItemNumber("4647474")
                .itemPriceInfo(new ItemPriceInfo(new Money(BigDecimal.valueOf(2.5), Currency.GBP)))
                .quantity(2)
                .substitutionOption(SubstitutionOption.SUBSTITUTE)
                .build()
        fmsItem.addCatalogInfo(mockItemCatalogInfo())
        fmsItem.addUpcInfo(mockFmsItemUpcInfo())
        fmsOrder.addItem(fmsItem)
        return fmsOrder
    }

    FmsOrder MockFmsCancelOrder() {
        FmsOrder fmsOrder = getFmsOrder()
        fmsOrder.addItem(mockFmsItem(fmsOrder))
        fmsOrder.orderState = FmsOrder.OrderStatus.CANCELLED.getName()
        return fmsOrder
    }

    FmsOrder MockFmsPickCompleteOrder() {
        FmsOrder fmsOrder = getFmsOrder()
        fmsOrder.addItem(mockFmsItem(fmsOrder))
        fmsOrder.orderState = FmsOrder.OrderStatus.PICK_COMPLETE.getName()
        return fmsOrder
    }

    FmsOrder MockFmsDeliveredOrder() {
        FmsOrder fmsOrder = getFmsOrder()
        fmsOrder.addItem(mockFmsItem(fmsOrder))
        fmsOrder.orderState = FmsOrder.OrderStatus.DELIVERED
        return fmsOrder
    }


    FmsOrder MockFmsNullOrders() {
        FmsOrder fmsOrder = getFmsOrder()
        fmsOrder.addItem(null)
        return fmsOrder
    }

    private FmsOrder getFmsOrder() {
        return FmsOrder.builder()
                .tenant(Tenant.ASDA)
                .vertical(Vertical.MARKETPLACE)
                .orderState(FmsOrder.OrderStatus.RECEIVED_AT_STORE.getName())
                .deliveryDate(new Date())
                .sourceOrderId("333333")
                .storeOrderId(storeOrderId)
                .storeId("4401")
                .marketPlaceInfo(new MarketPlaceInfo(Vendor.UBEREATS, vendorOrderId))
                .build()
    }

    private static ItemCatalogInfo mockItemCatalogInfo() {
        return new ItemCatalogInfo("EACH", "E", "Asda Fresh ",
                "https://i.groceries.asda.com/image.jpg", 3,
                4, "Ambient", true)


    }

    private static FmsOrderItem mockFmsItem(FmsOrder fmsOrder) {
        FmsOrderItem fmsItem = FmsOrderItem.builder()
                .fmsOrder(fmsOrder)
                .consumerItemNumber("4647474")
                .itemPriceInfo(new ItemPriceInfo(new Money(BigDecimal.valueOf(2.5), Currency.GBP)))
                .quantity(2)
                .substitutionOption(SubstitutionOption.DO_NOT_SUBSTITUTE)
                .build()
        fmsItem.addCatalogInfo(mockItemCatalogInfo())
        fmsItem.addUpcInfo(mockFmsItemUpcInfo())
        return fmsItem

    }

    private static FmsOrderItem mockFmsItemCancelOrder(FmsOrder fmsOrder) {
        FmsOrderItem fmsItem = FmsOrderItem.builder()
                .fmsOrder(fmsOrder)
                .consumerItemNumber("4647474")
                .itemPriceInfo(new ItemPriceInfo(new Money(BigDecimal.valueOf(2.5), Currency.GBP)))
                .quantity(2)
                .substitutionOption(SubstitutionOption.CANCEL_ENTIRE_ORDER)
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

    def "Test processing of pick complete command for substitutions"() {

        given:
        testFmsOrder = MockFmsOrderForSubstitution()
        FmsPickCompleteCommand pickCompleteCommand = buildPickCompleteCommandForSubstitution()
        fmsOrderFactory.getFmsOrderByStoreOrder(_ as String) >> testFmsOrder
        fmsOrderFactory.createPickedItemUpc(_ as long, _ as BigDecimal, _ as String, _ as String, _ as String) >> mockPickedItemUpc()
        fmsOrderFactory.createPickedItem(_ as String, _ as String, _ as String, _ as String, _ as List) >> mockPickedItem()
        fmsOrderFactory.createSubstitutedItem(_, _, _, _, _, _, _, _) >> mockSubstitutedItem()

        when:
        fmsPickCompleteCommandService.pickCompleteOrder(pickCompleteCommand)

        then:

        1 * fmsOrderRepository.save(_ as FmsOrder) >> { FmsOrder _fmsOrder ->
            assert _fmsOrder.storeOrderId == storeOrderId
            assert _fmsOrder.getFmsOrderItems().get(0).getPickedItem().getCin().equalsIgnoreCase(testCin)
            assert _fmsOrder.getFmsOrderItems().get(0).getPickedItem().getPickedItemUpcList() != null
            assert _fmsOrder.getFmsOrderItems().get(0).getPickedItem().getPickedItemUpcList().get(0).getQuantity() == 2
            return testFmsOrder
        }

        1 * fmsDomainEventPublisher.publish(_ as DomainEvent, _ as String) >> { DomainEvent _domainEvent, String _destination ->
            assert _destination == "FMS_ORDER_UPDATES"
            assert _domainEvent.source == Domain.FMS
            assert _domainEvent.key == testFmsOrder.id
            assert _domainEvent.name == DomainEventType.FMS_ORDER_PICK_COMPLETE
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
            assert testFmsOrder.fmsOrderItems[0].itemPriceInfo.unitPrice.amount == valueObject.get().fmsOrderItemvalueObjectList[0].unitPrice
            assert testFmsOrder.fmsOrderItems[0].pickedItem.substitutedItems.size() == 1
            assert testFmsOrder.fmsOrderItems[0].pickedItem.substitutedItems.get(0).getDepartment() == "34"
            assert testFmsOrder.fmsOrderItems[0].pickedItem.substitutedItems.get(0).getConsumerItemNumber() == "1212"
            assert testFmsOrder.fmsOrderItems[0].pickedItem.substitutedItems.get(0).getWalmartItemNumber() == "123"
            assert testFmsOrder.fmsOrderItems[0].pickedItem.substitutedItems.get(0).getQuantity() == 2
            assert testFmsOrder.fmsOrderItems[0].pickedItem.substitutedItems.get(0).getDescription() == "ABCD Substitute item"

        }

    }

}
