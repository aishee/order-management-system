package com.walmart.marketplace.integration.listeners

import com.walmart.common.domain.messaging.DomainEvent
import com.walmart.common.domain.type.CancellationSource
import com.walmart.common.domain.type.Domain
import com.walmart.common.domain.type.DomainEventType
import com.walmart.marketplace.commands.CancelMarketPlaceOrderCommand
import com.walmart.marketplace.commands.MarketPlaceDeliveredOrderCommand
import com.walmart.marketplace.commands.MarketPlaceOrderConfirmationCommand
import com.walmart.marketplace.commands.MarketPlacePickCompleteCommand
import com.walmart.marketplace.eventprocessors.MarketPlaceDeliveredCommandService
import com.walmart.marketplace.eventprocessors.MarketPlaceOrderConfirmationService
import com.walmart.marketplace.eventprocessors.MarketPlacePickCompleteCommandService
import com.walmart.marketplace.eventprocessors.MarketPlaceStoreCancelCommandService
import com.walmart.marketplace.order.domain.entity.type.Vendor
import com.walmart.marketplace.order.domain.valueobject.MarketPlaceOrderValueObject
import com.walmart.oms.order.valueobject.events.CancellationDetailsValueObject
import spock.lang.Specification

class OmsOrderUpdatesListenerTest extends Specification {

    OmsOrderUpdatesListener omsOrderUpdatesListener

    MarketPlaceOrderConfirmationService marketPlaceOrderConfirmationService = Mock()

    MarketPlaceDeliveredCommandService marketPlaceDeliveredCommandService = Mock()

    MarketPlaceStoreCancelCommandService marketPlaceStoreCancelCommandService = Mock()

    MarketPlacePickCompleteCommandService marketPlacePickCompleteCommandService = Mock()

    Domain source = Domain.OMS
    Domain destination = Domain.MARKETPLACE
    String sourceOrderId = UUID.randomUUID().toString()
    String vendorOrderId = UUID.randomUUID().toString()


    def setup() {
        omsOrderUpdatesListener = new OmsOrderUpdatesListener(
                marketPlaceOrderConfirmationService: marketPlaceOrderConfirmationService,
                marketPlaceDeliveredCommandService: marketPlaceDeliveredCommandService,
                marketPlaceStoreCancelCommandService: marketPlaceStoreCancelCommandService,
                marketPlacePickCompleteCommandService: marketPlacePickCompleteCommandService)
    }

    def "Testing Listen with order confirmation event name"() {

        given:
        DomainEvent orderConfirmationEvent = new DomainEvent.EventBuilder<MarketPlaceOrderValueObject>(DomainEventType.OMS_ORDER_CONFIRM, "OMS_ORDER_CONFIRM")
                .from(source)
                .to(destination)
                .addMessage(new MarketPlaceOrderValueObject(
                        sourceOrderId: sourceOrderId,
                        storeId: "4401",
                        vendorId: Vendor.UBEREATS,
                        vendorOrderId: vendorOrderId)
                )
                .build()

        when:
        omsOrderUpdatesListener.listen(orderConfirmationEvent)

        then:
        1 * marketPlaceOrderConfirmationService.orderConfirmedAtStore(_ as MarketPlaceOrderConfirmationCommand) >> { MarketPlaceOrderConfirmationCommand _marketPlaceOrderConfirmationCommand ->
            assert _marketPlaceOrderConfirmationCommand.data.sourceOrderId == sourceOrderId
        }

    }

    def "Testing Listen with Order delivered event name"() {

        given:
        DomainEvent orderDeliveredEvent = new DomainEvent.EventBuilder<MarketPlaceOrderValueObject>(DomainEventType.OMS_ORDER_DELIVERED, "OMS_ORDER_DELIVERED")
                .from(source)
                .to(destination)
                .addMessage(new MarketPlaceOrderValueObject(
                        sourceOrderId: sourceOrderId,
                        storeId: "4401",
                        vendorId: Vendor.UBEREATS,
                        vendorOrderId: vendorOrderId)
                )
                .build()

        when:
        omsOrderUpdatesListener.listen(orderDeliveredEvent)

        then:
        1 * marketPlaceDeliveredCommandService.deliverOrder(_ as MarketPlaceDeliveredOrderCommand) >> { MarketPlaceDeliveredOrderCommand _marketPlaceDeliveredOrderCommand ->
            assert _marketPlaceDeliveredOrderCommand.getData().getSourceOrderId() == sourceOrderId
        }

    }

    def "Testing Listen with Order cancelled event name"() {

        given:
        DomainEvent orderCancelledEvent = new DomainEvent.EventBuilder<MarketPlaceOrderValueObject>(DomainEventType.OMS_ORDER_CANCELLED, "OMS_ORDER_CANCELLED")
                .from(source)
                .to(destination)
                .addMessage(new MarketPlaceOrderValueObject(
                        sourceOrderId: sourceOrderId,
                        storeId: "4401",
                        vendorId: Vendor.UBEREATS,
                        vendorOrderId: vendorOrderId,
                        cancellationDetails: new CancellationDetailsValueObject(
                                cancelledBy: "STORE",
                                cancelledReasonCode: "STORE")
                )
                )
                .build()

        when:
        omsOrderUpdatesListener.listen(orderCancelledEvent)

        then:
        1 * marketPlaceStoreCancelCommandService.cancelOrder(_ as CancelMarketPlaceOrderCommand) >> { CancelMarketPlaceOrderCommand _cancelMarketPlaceOrderCommand ->
            assert _cancelMarketPlaceOrderCommand.getSourceOrderId() == sourceOrderId
            assert _cancelMarketPlaceOrderCommand.getCancellationDetails().cancelledReasonCode == "STORE"
            assert _cancelMarketPlaceOrderCommand.getCancellationDetails().cancelledBy == CancellationSource.STORE

        }

    }

    def "Testing Listen with Order Pick complete event name"() {
        given:
        DomainEvent orderPickCompleteEvent = new DomainEvent.EventBuilder<MarketPlaceOrderValueObject>(DomainEventType.OMS_ORDER_PICK_COMPLETE, "OMS_ORDER_PICK_COMPLETE")
                .from(source)
                .to(destination)
                .addMessage(getMarketPlaceOrderValueObject())
                .build()

        when:
        omsOrderUpdatesListener.listen(orderPickCompleteEvent)

        then:
        1 * marketPlacePickCompleteCommandService.pickCompleteOrder(_ as MarketPlacePickCompleteCommand) >> { MarketPlacePickCompleteCommand _marketPlacePickCompleteCommand ->
            assert _marketPlacePickCompleteCommand.getData().sourceOrderId == sourceOrderId

        }
    }

    private MarketPlaceOrderValueObject getMarketPlaceOrderValueObject() {
        return new MarketPlaceOrderValueObject(
                sourceOrderId: sourceOrderId,
                storeId: "4401",
                vendorId: Vendor.UBEREATS,
                vendorOrderId: vendorOrderId,
                items: getMarketPlaceOrderValueObjectItems(),
                nilPicks: Arrays.asList("e2066983-7793-4017-ac06-74785bfeff15"),
                partialPicks: new HashMap<String, Integer>())
    }

    private static MarketPlaceOrderValueObject.PickedItem getPickedItem(long quantity) {
        MarketPlaceOrderValueObject.PickedItem pickedItem = new MarketPlaceOrderValueObject.PickedItem()
        pickedItem.setItemId("testItemId")
        pickedItem.setPickedQuantity(quantity)
        return pickedItem
    }

    private static ArrayList<MarketPlaceOrderValueObject.Item> getMarketPlaceOrderValueObjectItems() {
        ArrayList<MarketPlaceOrderValueObject.Item> list = new ArrayList<>();
        MarketPlaceOrderValueObject.Item item1 = new MarketPlaceOrderValueObject.Item(
                externalItemId: "e2066983-7793-4017-ac06-74785bfeff15",
                itemDescription: "Nescafe Dolce Gusto Espresso Pods",
                quantity: 1,
                pickedItem: getPickedItem(1),
                itemIdentifier: getItemIdentifier(),
                itemPriceInfo: getItemPriceInfo()
        )
        MarketPlaceOrderValueObject.Item item2 = new MarketPlaceOrderValueObject.Item(
                externalItemId: "e2066983-7793-4017-ac06-74785bfeff15",
                itemDescription: "Nescafe Dolce Gusto Espresso Pods",
                quantity: 3,
                pickedItem: getPickedItem(3),
                itemIdentifier: getItemIdentifier(),
                itemPriceInfo: getItemPriceInfo(),
        )
        list.add(item1)
        list.add(item2)
        return list
    }

    private static MarketPlaceOrderValueObject.ItemPriceInfo getItemPriceInfo() {
        return new MarketPlaceOrderValueObject.ItemPriceInfo(
                unitPrice: 3.77,
                baseTotalPrice: 7.44,
                baseUnitPrice: 3.77,
                totalPrice: 0
        )
    }

    private static MarketPlaceOrderValueObject.ItemIdentifier getItemIdentifier() {
        return new MarketPlaceOrderValueObject.ItemIdentifier(
                itemId: "11605",
                itemType: "CIN"
        )
    }
}