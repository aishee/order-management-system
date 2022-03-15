package com.walmart.oms.integration.listeners

import com.walmart.common.domain.messaging.DomainEvent
import com.walmart.common.domain.type.Currency
import com.walmart.common.domain.type.DomainEventType
import com.walmart.common.domain.type.Domain
import com.walmart.common.domain.type.SubstitutionOption
import com.walmart.common.domain.type.Tenant
import com.walmart.common.domain.type.Vertical
import com.walmart.marketplace.order.aggregateroot.MarketPlaceOrder
import com.walmart.marketplace.order.domain.entity.type.Vendor
import com.walmart.marketplace.order.domain.valueobject.ItemIdentifier
import com.walmart.marketplace.order.domain.valueobject.MarketPlaceItemPriceInfo
import com.walmart.marketplace.order.domain.valueobject.MarketPlaceOrderContactInfo
import com.walmart.marketplace.order.domain.valueobject.MarketPlaceOrderPaymentInfo
import com.walmart.marketplace.order.domain.valueobject.MarketPlaceOrderValueObject
import com.walmart.marketplace.order.domain.valueobject.Money
import com.walmart.marketplace.order.domain.valueobject.mappers.MarketPlaceOrderToValueObjectMapper
import com.walmart.oms.OmsOrderApplicationService
import com.walmart.oms.commands.CreateOmsOrderCommand
import com.walmart.oms.integration.exception.DomainEventListenerException
import spock.lang.Specification

class OrderCreatedEventListenerTest extends Specification {

    OrderCreatedEventListener createdEventListener
    OmsOrderApplicationService applicationService = Mock()
    String createdOrderState = "CREATED"
    String storeId = "4401"
    String externalItemId
    String externalOrderId
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
        createdEventListener = new OrderCreatedEventListener(omsOrderApplicationService: applicationService)
        externalItemId = UUID.randomUUID().toString()
        externalOrderId = UUID.randomUUID().toString()
        instanceId = UUID.randomUUID().toString()
        ItemIdentifier itemIdentifier = ItemIdentifier.builder().itemId(externalItemId).itemType("CIN").build()
        MarketPlaceItemPriceInfo marketPlaceItemPriceInfo = MarketPlaceItemPriceInfo.builder().totalPrice(1.0).baseTotalPrice(1.0).unitPrice(1.0).baseUnitPrice(1.0).build()

        MarketPlaceOrderContactInfo marketPlaceOrderContactInfo = MarketPlaceOrderContactInfo.builder().firstName(customerFirstName).lastName(customerLastName).build()
        MarketPlaceOrderPaymentInfo marketPlaceOrderPaymentInfo = MarketPlaceOrderPaymentInfo.builder().
                total(new Money(BigDecimal.valueOf(2.0), Currency.GBP)).build()

        marketPlaceOrder = MarketPlaceOrder.builder()
                .vendorOrderId(externalOrderId)
                .orderDueTime(orderDueDate)
                .orderState(createdOrderState)
                .marketPlaceItems([])
                .sourceModifiedDate(new Date())
                .storeId(storeId).vendorId(vendor)
                .marketPlaceOrderContactInfo(marketPlaceOrderContactInfo)
                .paymentInfo(marketPlaceOrderPaymentInfo).id(UUID.randomUUID().toString())
                .build()
        marketPlaceOrder.addMarketPlaceItem(externalItemId, externalItemId, "test item", 1, instanceId, itemIdentifier, marketPlaceItemPriceInfo, SubstitutionOption.DO_NOT_SUBSTITUTE)
        marketPlaceOrderValueObject= MarketPlaceOrderToValueObjectMapper.INSTANCE.modelToValueObject(marketPlaceOrder)
    }

    def "Test market place order creation event consumption and create oms order"() {
        String osn = vendor.nextOSN()
        given:
        DomainEvent marketPlaceOrderCreatedEvent = new DomainEvent.EventBuilder<MarketPlaceOrderValueObject>(DomainEventType.MARKET_PLACE_ORDER_CREATED, "Description")
                .from(source)
                .to(destination)
                .addMessage(marketPlaceOrderValueObject).addHeader("OSN", osn)
                .build()
        when:
        createdEventListener.listen(marketPlaceOrderCreatedEvent)

        then:
        1 * applicationService.createOmsOrderFromCommand(_ as CreateOmsOrderCommand) >> { CreateOmsOrderCommand _createOmsOrderCommand ->
            assert _createOmsOrderCommand.data.orderInfo.storeId == marketPlaceOrderValueObject.storeId
            assert _createOmsOrderCommand.data.orderInfo.deliveryDate == marketPlaceOrderValueObject.orderDueTime
            assert _createOmsOrderCommand.data.orderInfo.sourceOrderId == marketPlaceOrderValueObject.sourceOrderId
            assert _createOmsOrderCommand.data.marketPlaceInfo.vendorOrderId == marketPlaceOrderValueObject.vendorOrderId
            assert _createOmsOrderCommand.data.marketPlaceInfo.vendor == marketPlaceOrderValueObject.vendorId
            assert _createOmsOrderCommand.data.schedulingInfo.plannedDueTime == marketPlaceOrderValueObject.orderDueTime
            assert _createOmsOrderCommand.data.contactinfo.firstName == marketPlaceOrderValueObject.contactInfo.firstName
            assert _createOmsOrderCommand.data.contactinfo.lastName == marketPlaceOrderValueObject.contactInfo.lastName
            assert _createOmsOrderCommand.data.priceInfo.orderTotal == marketPlaceOrderValueObject.marketPlaceOrderPaymentInfo.total.amount
            assert _createOmsOrderCommand.data.items[0].cin == marketPlaceOrderValueObject.items[0].itemIdentifier.itemId
            assert _createOmsOrderCommand.data.items[0].quantity == marketPlaceOrderValueObject.items[0].quantity
            assert _createOmsOrderCommand.data.items[0].vendorTotalPrice == marketPlaceOrderValueObject.items[0].itemPriceInfo.totalPrice
            assert _createOmsOrderCommand.data.items[0].vendorUnitPrice == marketPlaceOrderValueObject.items[0].itemPriceInfo.unitPrice
            assert _createOmsOrderCommand.data.orderInfo.vertical == Vertical.MARKETPLACE
            assert _createOmsOrderCommand.data.orderInfo.tenant == Tenant.ASDA
            assert _createOmsOrderCommand.data.schedulingInfo.scheduleNumber == osn
        }
    }

    def "Test market place order creation event coming from different source"() {

        given:
        DomainEvent marketPlaceOrderCreatedEvent = new DomainEvent.EventBuilder<MarketPlaceOrderValueObject>(DomainEventType.MARKET_PLACE_ORDER_CREATED, "Description")
                .from(Domain.OMS)
                .to(destination)
                .addMessage(marketPlaceOrderValueObject)
                .build()
        when:
        createdEventListener.listen(marketPlaceOrderCreatedEvent)

        then:
        0 * applicationService.createOmsOrderFromCommand(_ as CreateOmsOrderCommand)
    }

    def "Test market place order creation event with wrong payload"() {
        given:
        DomainEvent marketPlaceOrderCreatedEvent = new DomainEvent.EventBuilder<MarketPlaceOrderValueObject>(DomainEventType.MARKET_PLACE_ORDER_CREATED, "Description")
                .from(source)
                .to(destination)
                .addMessage(new Integer(1000))
                .build()
        when:
        createdEventListener.listen(marketPlaceOrderCreatedEvent)

        then:
        thrown(IllegalArgumentException)
    }

    def "Test market place order creation event with empty payload"() {
        given:
        DomainEvent marketPlaceOrderCreatedEvent = new DomainEvent.EventBuilder<MarketPlaceOrderValueObject>(DomainEventType.MARKET_PLACE_ORDER_CREATED, "Description")
                .from(source)
                .to(destination)
                .addMessage(new MarketPlaceOrderValueObject())
                .build()
        when:
        createdEventListener.listen(marketPlaceOrderCreatedEvent)

        then:
        thrown(DomainEventListenerException)
    }


}
