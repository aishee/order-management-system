package com.walmart.oms.integration.listeners

import com.walmart.common.domain.event.processing.EventGeneratorService
import com.walmart.common.domain.messaging.DomainEvent
import com.walmart.common.domain.type.CancellationSource
import com.walmart.common.domain.type.DomainEventType
import com.walmart.common.domain.type.Domain
import com.walmart.common.domain.type.Tenant
import com.walmart.common.domain.type.Vertical
import com.walmart.marketplace.order.domain.entity.type.Vendor
import com.walmart.oms.commands.DeliveredOrderCommand
import com.walmart.oms.commands.OmsCancelOrderCommand
import com.walmart.oms.commands.OrderConfirmationCommand
import com.walmart.oms.commands.PickCompleteCommand
import com.walmart.oms.eventprocessors.OmsDeliveredCommandService
import com.walmart.oms.eventprocessors.OmsOrderConfirmationCommandService
import com.walmart.oms.eventprocessors.OmsPickCompleteCommandService
import com.walmart.oms.eventprocessors.OmsCancelOrderCommandService
import com.walmart.oms.order.aggregateroot.OmsOrder
import com.walmart.oms.order.valueobject.MarketPlaceInfo
import com.walmart.oms.order.valueobject.events.*
import spock.lang.Specification

class FmsOrderUpdatesListenerTest extends Specification {

    FmsOrderUpdatesListener fmsOrderUpdatesListener

    OmsOrderConfirmationCommandService orderConfirmationCommandService = Mock()

    OmsDeliveredCommandService omsDeliveredCommandService = Mock()

    EventGeneratorService eventGeneratorService = Mock()

    OmsCancelOrderCommandService omsStoreCancelledCommandService = Mock()

    OmsPickCompleteCommandService omsPickCompleteCommandService = Mock()

    Domain source = Domain.FMS
    Domain destination = Domain.OMS
    String sourceOrderId


    def setup() {

        sourceOrderId = UUID.randomUUID().toString()
        fmsOrderUpdatesListener = new FmsOrderUpdatesListener(
                orderConfirmationCommandService: orderConfirmationCommandService,
                omsDeliveredCommandService: omsDeliveredCommandService,
                omsCancelOrderCommandService: omsStoreCancelledCommandService,
                eventGeneratorService: eventGeneratorService,
                omsPickCompleteCommandService: omsPickCompleteCommandService)

    }

    def "Testing Listen with order confirmation event name"() {

        given:
        DomainEvent orderConfirmationEvent = new DomainEvent.EventBuilder<FmsOrderValueObject>(DomainEventType.FMS_ORDER_CONFIRM, "FMS_ORDER_CONFIRM")
                .from(source)
                .to(destination)
                .addMessage(new FmsOrderValueObject(
                        sourceOrderId: sourceOrderId,
                        storeId: "4401",
                        deliveryDate: new Date(),
                        tenant: Tenant.ASDA,
                        vertical: Vertical.MARKETPLACE)
                )
                .build()

        when:
        fmsOrderUpdatesListener.listen(orderConfirmationEvent)

        then:
        1 * orderConfirmationCommandService.orderConfirmedAtStore(_ as OrderConfirmationCommand) >> { OrderConfirmationCommand _orderConfirmationCommand ->
            assert _orderConfirmationCommand.getSourceOrderId() == sourceOrderId
            assert _orderConfirmationCommand.getTenant() == Tenant.ASDA
            assert _orderConfirmationCommand.getVertical() == Vertical.MARKETPLACE

        }

    }

    def "Testing Listen with Order delivered event name"() {

        given:
        DomainEvent orderDeliveredEvent = new DomainEvent.EventBuilder<FmsOrderValueObject>(DomainEventType.FMS_ORDER_DELIVERED, "FMS_ORDER_DELIVERED")
                .from(source)
                .to(destination)
                .addMessage(new FmsOrderValueObject(
                        sourceOrderId: sourceOrderId,
                        storeId: "4401",
                        deliveryDate: new Date(),
                        tenant: Tenant.ASDA,
                        vertical: Vertical.MARKETPLACE)
                )
                .build()

        when:
        fmsOrderUpdatesListener.listen(orderDeliveredEvent)

        then:
        1 * omsDeliveredCommandService.deliverOrder(_ as DeliveredOrderCommand) >> { DeliveredOrderCommand _deliveredOrderCommand ->
            assert _deliveredOrderCommand.getData().getSourceOrderId() == sourceOrderId
            assert _deliveredOrderCommand.getData().getTenant() == Tenant.ASDA
            assert _deliveredOrderCommand.getData().getVertical() == Vertical.MARKETPLACE
            assert _deliveredOrderCommand.getData().getStoreId() == "4401"

        }

    }

    def "Testing Listen with Order cancelled event name"() {

        given:
        DomainEvent orderCancelledEvent = new DomainEvent.EventBuilder<FmsOrderValueObject>(DomainEventType.FMS_ORDER_CANCELLED , "FMS_ORDER_CANCELLED")
                .from(source)
                .to(destination)
                .addMessage(new FmsOrderValueObject(
                        sourceOrderId: sourceOrderId,
                        storeId: "4401",
                        deliveryDate: new Date(),
                        tenant: Tenant.ASDA,
                        vertical: Vertical.MARKETPLACE,
                        cancellationDetails: new CancellationDetailsValueObject(
                                cancelledBy: CancellationSource.STORE,
                                cancelledReasonCode: "CANCELLED_AT_STORE")
                )
                )
                .build()
        OmsOrder cancelledOrder = mockOmsOrder()

        when:
        fmsOrderUpdatesListener.listen(orderCancelledEvent)

        then:
        1 * omsStoreCancelledCommandService.cancelOrder(_ as OmsCancelOrderCommand) >> { OmsCancelOrderCommand _storeCancelledOrderCommand ->
            assert _storeCancelledOrderCommand.getSourceOrderId() == sourceOrderId
            assert _storeCancelledOrderCommand.getTenant() == Tenant.ASDA
            assert _storeCancelledOrderCommand.getVertical() == Vertical.MARKETPLACE
            assert _storeCancelledOrderCommand.getCancellationDetails().cancelledReasonCode == "CANCELLED_AT_STORE"
            return cancelledOrder
        }

    }

    def "Testing Listen with Order Pick complete event name"() {

        given:
        List<FmsOrderItemvalueObject> fmsOrderItemvalueObjectList = Arrays.asList(FmsOrderItemvalueObject.builder()
                .itemDescription("Test description")
                .quantity(1)
                .unitPrice(2.5)
                .cin("45677")
                .pickedItem(FmsPickedItemValueObject.builder()
                        .unitPrice(2.5)
                        .quantity(1)
                        .orderedCin("45677")
                        .pickerUserName("testuser")
                        .pickedItemDescription("test picked description")
                        .pickedItemUpcList(Arrays.asList(FmsPickedItemUpcVo.builder()
                                .quantity(2)
                                .storeUnitPrice(3.0)
                                .uom("E")
                                .upc("4637373")
                                .win("363636").build())).build()).build())

        Date dateofDelivery = new Date()
        DomainEvent orderPickCompleteEvent = new DomainEvent.EventBuilder<FmsOrderValueObject>(DomainEventType.FMS_ORDER_PICK_COMPLETE, "FMS_ORDER_PICK_COMPLETE")
                .from(source)
                .to(destination)
                .addMessage(new FmsOrderValueObject(
                        sourceOrderId: sourceOrderId,
                        storeId: "4401",
                        deliveryDate: dateofDelivery,
                        tenant: Tenant.ASDA,
                        vertical: Vertical.MARKETPLACE,
                        fmsOrderItemvalueObjectList: fmsOrderItemvalueObjectList)
                )
                .build()

        when:
        fmsOrderUpdatesListener.listen(orderPickCompleteEvent)

        then:
        1 * omsPickCompleteCommandService.pickCompleteOrder(_ as PickCompleteCommand) >> { PickCompleteCommand _pickCompleteCommand ->
            assert _pickCompleteCommand.getData().getOrderInfo().sourceOrderId == sourceOrderId
            assert _pickCompleteCommand.getData().getOrderInfo().tenant == Tenant.ASDA
            assert _pickCompleteCommand.getData().getOrderInfo().vertical == Vertical.MARKETPLACE
            assert _pickCompleteCommand.getData().getOrderInfo().getStoreId() == "4401"
            assert _pickCompleteCommand.getData().getOrderInfo().getDeliveryDate() == dateofDelivery
            assert _pickCompleteCommand.getData().pickedItems[0].cin == "45677"
            assert _pickCompleteCommand.getData().pickedItems[0].pickedBy == "testuser"
            assert _pickCompleteCommand.getData().pickedItems[0].pickedItemDescription == "test picked description"
            assert _pickCompleteCommand.getData().pickedItems[0].pickedItemUpcs.size() == 1
            assert _pickCompleteCommand.getData().pickedItems[0].pickedItemUpcs[0].pickedQuantity == 2
            assert _pickCompleteCommand.getData().pickedItems[0].pickedItemUpcs[0].unitPrice == 3.0
            assert _pickCompleteCommand.getData().pickedItems[0].pickedItemUpcs[0].upc == "4637373"
            assert _pickCompleteCommand.getData().pickedItems[0].pickedItemUpcs[0].win == "363636"
            return new OmsOrder()
        }

    }

    private OmsOrder mockOmsOrder() {
        new OmsOrder().builder()
                .storeId("5755")
                .storeOrderId("1234")
                .deliveryDate(new Date())
                .sourceOrderId("123455555")
                .marketPlaceInfo(new MarketPlaceInfo(Vendor.UBEREATS, "testId"))

                .vertical(Vertical.ASDAGR).build()
    }


}