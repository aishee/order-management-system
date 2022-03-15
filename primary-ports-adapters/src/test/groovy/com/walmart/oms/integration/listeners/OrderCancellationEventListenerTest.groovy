package com.walmart.oms.integration.listeners

import com.walmart.common.domain.event.processing.EventGeneratorService
import com.walmart.common.domain.messaging.DomainEvent
import com.walmart.common.domain.type.*
import com.walmart.marketplace.order.aggregateroot.MarketPlaceOrder
import com.walmart.marketplace.order.domain.entity.type.Vendor
import com.walmart.marketplace.order.domain.valueobject.*
import com.walmart.marketplace.order.domain.valueobject.mappers.MarketPlaceOrderToValueObjectMapper
import com.walmart.oms.commands.OmsCancelOrderCommand
import com.walmart.oms.domain.event.messages.OrderCancelledDomainEventMessage
import com.walmart.oms.domain.mapper.OmsDomainToEventMessageMapper
import com.walmart.oms.eventprocessors.OmsCancelOrderCommandService
import com.walmart.oms.order.aggregateroot.OmsOrder
import com.walmart.oms.order.valueobject.MarketPlaceInfo
import com.walmart.oms.order.valueobject.events.CancellationDetailsValueObject
import spock.lang.Specification

class OrderCancellationEventListenerTest extends Specification {

    OrderCancellationEventListener orderCancellationEventListener

    OmsCancelOrderCommandService omsStoreCancelledCommandService = Mock()
    EventGeneratorService eventGeneratorService = Mock()

    String cancelledOrderState = "CANCELLED"
    String storeId = "4401"
    String externalItemId
    String externalOrderId
    String sourceOrderId
    Vendor vendor = Vendor.UBEREATS;
    String customerFirstName = "John"
    String customerLastName = "Doe"
    MarketPlaceOrder marketPlaceOrder
    Domain source = Domain.MARKETPLACE
    Domain destination = Domain.OMS
    Date orderDueDate = new Date()
    MarketPlaceOrderValueObject marketPlaceOrderValueObject
    String instanceId

    def setup() {
        orderCancellationEventListener = new OrderCancellationEventListener(
                omsCancelOrderCommandService: omsStoreCancelledCommandService,
                eventGeneratorService: eventGeneratorService)

        externalItemId = UUID.randomUUID().toString()
        externalOrderId = UUID.randomUUID().toString()
        sourceOrderId = UUID.randomUUID().toString()
        instanceId = UUID.randomUUID().toString()
        ItemIdentifier itemIdentifier = ItemIdentifier.builder().itemId(externalItemId).itemType("CIN").build()
        MarketPlaceItemPriceInfo marketPlaceItemPriceInfo = MarketPlaceItemPriceInfo.builder().totalPrice(1.0).baseTotalPrice(1.0).unitPrice(1.0).baseUnitPrice(1.0).build()

        MarketPlaceOrderContactInfo marketPlaceOrderContactInfo = MarketPlaceOrderContactInfo.builder().firstName(customerFirstName).lastName(customerLastName).build()
        MarketPlaceOrderPaymentInfo marketPlaceOrderPaymentInfo = MarketPlaceOrderPaymentInfo.builder().
                total(new Money(BigDecimal.valueOf(2.0), Currency.GBP)).build()

        marketPlaceOrder = MarketPlaceOrder.builder()
                .id(sourceOrderId)
                .vendorOrderId(externalOrderId)
                .orderDueTime(orderDueDate)
                .orderState(cancelledOrderState)
                .marketPlaceItems([])
                .sourceModifiedDate(new Date())
                .storeId(storeId).vendorId(vendor)
                .marketPlaceOrderContactInfo(marketPlaceOrderContactInfo)
                .paymentInfo(marketPlaceOrderPaymentInfo)
                .build()
        marketPlaceOrder.addMarketPlaceItem(externalItemId, externalItemId, "test item", 1, instanceId, itemIdentifier, marketPlaceItemPriceInfo, SubstitutionOption.DO_NOT_SUBSTITUTE)
        marketPlaceOrderValueObject = MarketPlaceOrderToValueObjectMapper.INSTANCE.modelToValueObject(marketPlaceOrder)
        CancellationDetailsValueObject cancellationDetailsValueObject = new CancellationDetailsValueObject();
        cancellationDetailsValueObject.setCancelledBy(CancellationSource.VENDOR)
        cancellationDetailsValueObject.setCancelledReasonCode("VENDOR")
        marketPlaceOrderValueObject.setCancellationDetails(cancellationDetailsValueObject)
    }

    def "Test Listen"() {
        given:

        DomainEvent marketPlaceOrderCancellationEvent = new DomainEvent.EventBuilder<MarketPlaceOrderValueObject>(DomainEventType.OMS_ORDER_CANCELLED, "Description")
                .from(source)
                .to(destination)
                .addMessage(marketPlaceOrderValueObject)
                .build()
        OmsOrder cancelledOrder = mockOmsOrder()
        OrderCancelledDomainEventMessage orderCancelledDomainEventMessage = mockCancelledEventMessage()
        omsStoreCancelledCommandService.cancelOrder(_ as OmsCancelOrderCommand) >> cancelledOrder
        OmsDomainToEventMessageMapper.mapToOrderCancelledDomainEventMessage(cancelledOrder) >> orderCancelledDomainEventMessage

        when:
        orderCancellationEventListener.listen(marketPlaceOrderCancellationEvent)

        then:
        1 * omsStoreCancelledCommandService.cancelOrder(_ as OmsCancelOrderCommand) >> { OmsCancelOrderCommand _omsCancelOrderCommand ->
            assert _omsCancelOrderCommand.getCancellationDetails() != null
            assert _omsCancelOrderCommand.sourceOrderId == sourceOrderId
            return cancelledOrder
        }

        1 * eventGeneratorService.publishApplicationEvent(_ as OrderCancelledDomainEventMessage)
    }

    private static OmsOrder mockOmsOrder() {
        new OmsOrder().builder()
                .storeId("5755")
                .storeOrderId("1234")
                .deliveryDate(new Date())
                .sourceOrderId("123455555")
                .marketPlaceInfo(
                        new MarketPlaceInfo(Vendor.UBEREATS, "testId")
                )
                .vertical(Vertical.ASDAGR).build()
    }

    private static OrderCancelledDomainEventMessage mockCancelledEventMessage() {
        new OrderCancelledDomainEventMessage().builder()
                .storeId("5755")
                .storeOrderId("1234")
                .vendorOrderId("123445")
                .sourceOrderId("123455555")
                .vertical(Vertical.ASDAGR).build()
    }
}