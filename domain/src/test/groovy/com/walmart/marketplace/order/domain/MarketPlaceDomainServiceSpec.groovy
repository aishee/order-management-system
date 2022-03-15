package com.walmart.marketplace.order.domain

import com.walmart.common.domain.event.processing.EventGeneratorService
import com.walmart.common.domain.messaging.DomainEvent
import com.walmart.common.domain.messaging.DomainEventPublisher
import com.walmart.common.domain.messaging.exception.DomainEventPublishingException
import com.walmart.common.domain.type.CancellationSource
import com.walmart.common.domain.type.Currency
import com.walmart.common.domain.type.DomainEventType
import com.walmart.common.domain.type.Domain
import com.walmart.common.domain.type.SubstitutionOption
import com.walmart.marketplace.domain.event.messages.MarketPlaceOrderCancelMessage
import com.walmart.marketplace.order.aggregateroot.MarketPlaceOrder
import com.walmart.marketplace.order.configuration.MarketPlaceOrderConfig
import com.walmart.marketplace.order.domain.entity.type.Vendor
import com.walmart.marketplace.order.domain.uber.MarketPlaceReportRequest
import com.walmart.marketplace.order.domain.uber.ReportType
import com.walmart.marketplace.order.domain.valueobject.ItemIdentifier
import com.walmart.marketplace.order.domain.valueobject.MarketPlaceOrderContactInfo
import com.walmart.marketplace.order.domain.valueobject.MarketPlaceOrderPaymentInfo
import com.walmart.marketplace.order.domain.valueobject.MarketPlaceItemPriceInfo
import com.walmart.marketplace.order.domain.valueobject.MarketPlaceOrderValueObject
import com.walmart.marketplace.order.domain.valueobject.Money
import com.walmart.marketplace.order.gateway.IMarketPlaceGatewayFinder
import com.walmart.marketplace.order.repository.IMarketPlaceGateWay
import com.walmart.marketplace.order.repository.IMarketPlaceRepository
import com.walmart.oms.domain.error.exception.OMSBadRequestException
import com.walmart.common.domain.valueobject.CancellationDetails
import spock.lang.Specification

import java.time.LocalDate

class MarketPlaceDomainServiceSpec extends Specification {

    String externalOrderId = UUID.randomUUID().toString()

    IMarketPlaceGatewayFinder marketPlaceGatewayFinder = Mock()

    IMarketPlaceRepository marketPlaceOrdRepository = Mock()

    MarketPlaceOrderConfig config

    MarketPlaceDomainService marketPlaceDomainService

    IMarketPlaceGateWay marketPlaceGateWay = Mock()

    DomainEventPublisher domainEventPublisher = Mock()

    EventGeneratorService eventGeneratorService = Mock()

    def setup() {
        config = getConfiguration()
        marketPlaceDomainService = new MarketPlaceDomainService(
                marketPlaceGatewayFinder: marketPlaceGatewayFinder,
                marketPlaceOrdRepository: marketPlaceOrdRepository,
                domainEventPublisher: domainEventPublisher,
                marketplaceOrderConfig: config,
                eventGeneratorService: eventGeneratorService)
    }

    private static MarketPlaceOrderConfig getConfiguration() {
        return new MarketPlaceOrderConfig(
                allowedRunningOrders: 100,
                inProgressStates: "CREATED,ACCEPTED,RECD_AT_STORE"
        )
    }

    def "Test ProcessMarketPlaceOrder within threshold"() {

        given:
        String itemId = UUID.randomUUID().toString()
        Vendor vendor = Vendor.UBEREATS
        String createdOrderState = "CREATED"
        String acceptedOrderState = "ACCEPTED"
        String storeId = "4401"
        String externalItemId = UUID.randomUUID().toString()
        String customerFirstName = "John"
        String customerLastName = "Doe"
        String instanceId = UUID.randomUUID().toString()

        marketPlaceOrdRepository.getInProgressOrderCount(_ as List, _ as String) >> 10

        ItemIdentifier itemIdentifier = ItemIdentifier.builder().itemId(externalItemId).itemType("CIN").build()
        MarketPlaceItemPriceInfo marketPlaceItemPriceInfo = MarketPlaceItemPriceInfo.builder().totalPrice(1.0).baseTotalPrice(1.0).unitPrice(1.0).baseUnitPrice(1.0).build()

        MarketPlaceOrderContactInfo marketPlaceOrderContactInfo = MarketPlaceOrderContactInfo.builder().firstName(customerFirstName).lastName(customerLastName).build()
        MarketPlaceOrderPaymentInfo marketPlaceOrderPaymentInfo = MarketPlaceOrderPaymentInfo.builder()
                .bagFee(new Money(new BigDecimal(1.0), Currency.GBP))
                .tax(new Money(new BigDecimal(2.0), Currency.GBP))
                .total(new Money(new BigDecimal(100.0), Currency.GBP))
                .totalFee(new Money(new BigDecimal(1.0), Currency.GBP))
                .totalFeeTax(new Money(new BigDecimal(0.5), Currency.GBP))
                .subTotal(new Money(new BigDecimal(95.5), Currency.GBP))
                .build()

        MarketPlaceOrder marketPlaceOrder = MarketPlaceOrder.builder()
                .vendorOrderId(externalOrderId)
                .orderDueTime(new Date())
                .orderState(createdOrderState)
                .marketPlaceItems([])
                .sourceModifiedDate(new Date())
                .storeId(storeId).vendorId(vendor)
                .paymentInfo(marketPlaceOrderPaymentInfo)
                .marketPlaceOrderContactInfo(marketPlaceOrderContactInfo).build()

        marketPlaceOrder.addMarketPlaceItem(itemId, externalItemId, "test item", 1, instanceId, itemIdentifier, marketPlaceItemPriceInfo, SubstitutionOption.DO_NOT_SUBSTITUTE)


        when:
        marketPlaceDomainService.processMarketPlaceOrder(marketPlaceOrder)

        then:
        1 * marketPlaceOrdRepository.getInProgressOrderCount(_ as List, _ as String) >> { List _orderStates, String _storeId ->
            assert _orderStates.size() == 3
            assert _orderStates.get(0) == "CREATED"
            assert _orderStates.get(1) == "ACCEPTED"
            assert _orderStates.get(2) == "RECD_AT_STORE"
            return 10
        }
        1 * marketPlaceGatewayFinder.getMarketPlaceGateway(_ as Vendor) >> { Vendor _vendor ->
            assert _vendor == vendor
            return marketPlaceGateWay
        }
        1 * marketPlaceGateWay.acceptOrder(_ as MarketPlaceOrder) >> { MarketPlaceOrder _marketPlaceOrder ->
            assert _marketPlaceOrder.vendorOrderId == externalOrderId
            assert _marketPlaceOrder.storeId == storeId
            return true
        }
        1 * marketPlaceOrdRepository.save(_ as MarketPlaceOrder) >> { MarketPlaceOrder _marketPlaceOrder ->
            assert _marketPlaceOrder.orderState == acceptedOrderState
        }
        1 * domainEventPublisher.publish(_ as DomainEvent, _ as String) >> { DomainEvent _domainEvent, String _destination ->
            assert _destination == "ORDER_CREATED"
            assert _domainEvent.source == Domain.MARKETPLACE
            assert _domainEvent.key == marketPlaceOrder.id
            assert _domainEvent.name == DomainEventType.MARKET_PLACE_ORDER_CREATED
            assert _domainEvent.message != null
            MarketPlaceOrderValueObject _valueObject = _domainEvent.createObjectFromJson(MarketPlaceOrderValueObject.class).get()
            assert _valueObject != null
            assert marketPlaceOrder.storeId == _valueObject.storeId
            assert marketPlaceOrder.vendorOrderId == _valueObject.vendorOrderId
            assert marketPlaceOrder.vendorId == _valueObject.vendorId
            assert marketPlaceOrder.sourceModifiedDate == _valueObject.sourceModifiedDate
            assert marketPlaceOrder.orderDueTime == _valueObject.orderDueTime
            assert marketPlaceOrder.orderState == _valueObject.orderState
            assert marketPlaceOrder.storeId == _valueObject.storeId
            assert _valueObject.contactInfo != null
            assert marketPlaceOrder.marketPlaceOrderContactInfo.firstName == _valueObject.contactInfo.firstName
            assert marketPlaceOrder.marketPlaceOrderContactInfo.lastName == _valueObject.contactInfo.lastName
            assert _valueObject.marketPlaceOrderPaymentInfo != null
            assert marketPlaceOrder.marketPlaceOrderPaymentInfo.bagFee.amount == _valueObject.marketPlaceOrderPaymentInfo.bagFee.amount
            assert marketPlaceOrder.marketPlaceOrderPaymentInfo.bagFee.currency == _valueObject.marketPlaceOrderPaymentInfo.bagFee.currency
            assert marketPlaceOrder.marketPlaceOrderPaymentInfo.subTotal.amount == _valueObject.marketPlaceOrderPaymentInfo.subTotal.amount
            assert marketPlaceOrder.marketPlaceOrderPaymentInfo.subTotal.currency == _valueObject.marketPlaceOrderPaymentInfo.subTotal.currency
            assert marketPlaceOrder.marketPlaceOrderPaymentInfo.tax.amount == _valueObject.marketPlaceOrderPaymentInfo.tax.amount
            assert marketPlaceOrder.marketPlaceOrderPaymentInfo.tax.currency == _valueObject.marketPlaceOrderPaymentInfo.tax.currency
            assert marketPlaceOrder.marketPlaceOrderPaymentInfo.total.amount == _valueObject.marketPlaceOrderPaymentInfo.total.amount
            assert marketPlaceOrder.marketPlaceOrderPaymentInfo.total.currency == _valueObject.marketPlaceOrderPaymentInfo.total.currency
            assert marketPlaceOrder.marketPlaceOrderPaymentInfo.totalFee.amount == _valueObject.marketPlaceOrderPaymentInfo.totalFee.amount
            assert marketPlaceOrder.marketPlaceOrderPaymentInfo.totalFee.currency == _valueObject.marketPlaceOrderPaymentInfo.totalFee.currency
            assert marketPlaceOrder.marketPlaceOrderPaymentInfo.totalFeeTax.amount == _valueObject.marketPlaceOrderPaymentInfo.totalFeeTax.amount
            assert marketPlaceOrder.marketPlaceOrderPaymentInfo.totalFeeTax.currency == _valueObject.marketPlaceOrderPaymentInfo.totalFeeTax.currency
            MarketPlaceOrderValueObject.Item _item = _valueObject.items[0]
            assert marketPlaceOrder.marketPlaceItems[0].externalItemId == _item.externalItemId
            assert marketPlaceOrder.marketPlaceItems[0].itemDescription == _item.itemDescription
            assert marketPlaceOrder.marketPlaceItems[0].quantity == _item.quantity
            assert marketPlaceOrder.marketPlaceItems[0].itemIdentifier.itemId == _item.itemIdentifier.itemId
            assert marketPlaceOrder.marketPlaceItems[0].itemIdentifier.itemType == _item.itemIdentifier.itemType
            assert marketPlaceOrder.marketPlaceItems[0].marketPlacePriceInfo.unitPrice == _item.itemPriceInfo.unitPrice
            assert marketPlaceOrder.marketPlaceItems[0].marketPlacePriceInfo.baseTotalPrice == _item.itemPriceInfo.baseTotalPrice
            assert marketPlaceOrder.marketPlaceItems[0].marketPlacePriceInfo.baseUnitPrice == _item.itemPriceInfo.baseUnitPrice
            assert _domainEvent.getHeaderValueForKey("OSN", String.class).isPresent()
        }

    }


    def "Test publishing exception while processing market place order"() {
        given:
        String itemId = UUID.randomUUID().toString()
        Vendor vendor = Vendor.UBEREATS
        String createdOrderState = "CREATED"
        String acceptedOrderState = "ACCEPTED"
        String storeId = "4401"
        String externalItemId = UUID.randomUUID().toString()
        String customerFirstName = "John"
        String customerLastName = "Doe"
        String instanceId = UUID.randomUUID().toString()

        marketPlaceOrdRepository.getInProgressOrderCount(_ as List, _ as String) >> 10

        ItemIdentifier itemIdentifier = ItemIdentifier.builder().itemId(externalItemId).itemType("CIN").build()
        MarketPlaceItemPriceInfo marketPlaceItemPriceInfo = MarketPlaceItemPriceInfo.builder().totalPrice(1.0).baseTotalPrice(1.0).unitPrice(1.0).baseUnitPrice(1.0).build()

        MarketPlaceOrderContactInfo marketPlaceOrderContactInfo = MarketPlaceOrderContactInfo.builder().firstName(customerFirstName).lastName(customerLastName).build()

        MarketPlaceOrder marketPlaceOrder = MarketPlaceOrder.builder()
                .vendorOrderId(externalOrderId)
                .orderDueTime(new Date())
                .orderState(createdOrderState)
                .marketPlaceItems([])
                .sourceModifiedDate(new Date())
                .storeId(storeId).vendorId(vendor)
                .marketPlaceOrderContactInfo(marketPlaceOrderContactInfo).build()

        marketPlaceOrder.addMarketPlaceItem(itemId, externalItemId, "test item", 1, instanceId, itemIdentifier, marketPlaceItemPriceInfo, SubstitutionOption.DO_NOT_SUBSTITUTE)
        domainEventPublisher.publish(_ as DomainEvent, _ as String) >> { DomainEvent _domainEvent, String _destination ->
            throw new DomainEventPublishingException("Error while publishing message")
        }

        when:
        marketPlaceDomainService.processMarketPlaceOrder(marketPlaceOrder)

        then:
        1 * marketPlaceGatewayFinder.getMarketPlaceGateway(_ as Vendor) >> { Vendor _vendor ->
            assert _vendor == vendor
            return marketPlaceGateWay
        }
        1 * marketPlaceGateWay.acceptOrder(_ as MarketPlaceOrder) >> { MarketPlaceOrder _marketPlaceOrder ->
            assert _marketPlaceOrder.vendorOrderId == externalOrderId
            assert _marketPlaceOrder.storeId == storeId
            return true
        }
        1 * marketPlaceOrdRepository.save(_ as MarketPlaceOrder) >> { MarketPlaceOrder _marketPlaceOrder ->
            assert _marketPlaceOrder.orderState == acceptedOrderState
        }
        def e = thrown(DomainEventPublishingException)
        e.message == "Error while publishing message"

    }

    def "Test ProcessMarketPlaceOrder outside the threshold"() {

        given:
        String externalOrderId = UUID.randomUUID().toString()
        String itemId = UUID.randomUUID().toString()
        Vendor vendor = Vendor.UBEREATS
        String createdOrderState = "CREATED"
        String rejectedState = "REJECTED"
        String storeId = "4401"
        String externalItemId = UUID.randomUUID().toString()
        String customerFirstName = "John"
        String customerLastName = "Doe"
        String instanceId = UUID.randomUUID().toString()

        marketPlaceOrdRepository.getInProgressOrderCount(_ as List, _ as String) >> 10
        config.setAllowedRunningOrders(5)

        ItemIdentifier itemIdentifier = ItemIdentifier.builder().itemId(externalItemId).itemType("CIN").build()
        MarketPlaceItemPriceInfo marketPlaceItemPriceInfo = MarketPlaceItemPriceInfo.builder().totalPrice(1.0).baseTotalPrice(1.0).unitPrice(1.0).baseUnitPrice(1.0).build()

        MarketPlaceOrderContactInfo marketPlaceOrderContactInfo = MarketPlaceOrderContactInfo.builder().firstName(customerFirstName).lastName(customerLastName).build()

        MarketPlaceOrder marketPlaceOrder = MarketPlaceOrder.builder()
                .vendorOrderId(externalOrderId)
                .orderDueTime(new Date())
                .orderState(createdOrderState)
                .marketPlaceItems([])
                .sourceModifiedDate(new Date())
                .storeId(storeId).vendorId(vendor)
                .marketPlaceOrderContactInfo(marketPlaceOrderContactInfo).build()

        marketPlaceOrder.addMarketPlaceItem(itemId, externalItemId, "test item", 1, instanceId, itemIdentifier, marketPlaceItemPriceInfo, SubstitutionOption.DO_NOT_SUBSTITUTE)


        when:
        marketPlaceDomainService.processMarketPlaceOrder(marketPlaceOrder)

        then:
        1 * marketPlaceGatewayFinder.getMarketPlaceGateway(_ as Vendor) >> { Vendor _vendor ->
            assert _vendor == vendor
            return marketPlaceGateWay
        }
        1 * marketPlaceGateWay.rejectOrder(_ as MarketPlaceOrder, _ as String) >> { MarketPlaceOrder _marketPlaceOrder, String reason ->
            assert _marketPlaceOrder.vendorOrderId == externalOrderId
            assert _marketPlaceOrder.storeId == storeId
            return true
        }
        1 * marketPlaceOrdRepository.save(_ as MarketPlaceOrder) >> { MarketPlaceOrder _marketPlaceOrder ->
            assert _marketPlaceOrder.orderState == rejectedState
        }

        0 * domainEventPublisher.publish(_ as DomainEvent, _ as String)

    }

    def "Test cancel with valid order"() {

        given:
        String externalOrderId = UUID.randomUUID().toString()
        String itemId = UUID.randomUUID().toString()
        Vendor vendor = Vendor.UBEREATS
        String createdOrderState = "CREATED"
        String rejectedState = "REJECTED"
        String storeId = "4401"
        String externalItemId = UUID.randomUUID().toString()
        String customerFirstName = "John"
        String customerLastName = "Doe"
        String instanceId = UUID.randomUUID().toString()

        ItemIdentifier itemIdentifier = ItemIdentifier.builder().itemId(externalItemId).itemType("CIN").build()
        MarketPlaceItemPriceInfo marketPlaceItemPriceInfo = MarketPlaceItemPriceInfo.builder().totalPrice(1.0).baseTotalPrice(1.0).unitPrice(1.0).baseUnitPrice(1.0).build()

        MarketPlaceOrderContactInfo marketPlaceOrderContactInfo = MarketPlaceOrderContactInfo.builder().firstName(customerFirstName).lastName(customerLastName).build()

        MarketPlaceOrder marketPlaceOrder = MarketPlaceOrder.builder()
                .vendorOrderId(externalOrderId)
                .orderDueTime(new Date())
                .orderState(createdOrderState)
                .marketPlaceItems([])
                .sourceModifiedDate(new Date())
                .storeId(storeId).vendorId(vendor)
                .marketPlaceOrderContactInfo(marketPlaceOrderContactInfo).build()

        marketPlaceOrder.addMarketPlaceItem(itemId, externalItemId, "test item", 1, instanceId, itemIdentifier, marketPlaceItemPriceInfo, SubstitutionOption.DO_NOT_SUBSTITUTE)

        marketPlaceGatewayFinder.getMarketPlaceGateway(_ as Vendor) >> marketPlaceGateWay
        CancellationDetails cancellationDetails = CancellationDetails.builder().cancelledBy(CancellationSource.STORE).build()

        when:
        marketPlaceDomainService.cancelOrder(marketPlaceOrder, cancellationDetails)

        then:
        1 * marketPlaceOrdRepository.save(_ as MarketPlaceOrder) >> { MarketPlaceOrder _marketPlaceOrder ->
            assert _marketPlaceOrder.orderState == "CANCELLED"
            assert _marketPlaceOrder.vendorOrderId == externalOrderId

        }

        1 * eventGeneratorService.publishApplicationEvent(_ as MarketPlaceOrderCancelMessage) >> { MarketPlaceOrderCancelMessage _marketPlaceOrderCancelMessage ->
            assert _marketPlaceOrderCancelMessage.cancellationSource == CancellationSource.STORE
            assert _marketPlaceOrderCancelMessage.vendorOrderId == externalOrderId
            assert _marketPlaceOrderCancelMessage.testOrder == false
            assert _marketPlaceOrderCancelMessage.vendor == Vendor.UBEREATS
        }

        0 * domainEventPublisher.publish(_ as DomainEvent, _ as String)

    }

    def "Test cancel with cancellation source as VENDOR order"() {

        given:
        marketPlaceGatewayFinder.getMarketPlaceGateway(_ as Vendor) >> marketPlaceGateWay
        CancellationDetails cancellationDetails = CancellationDetails.builder().cancelledBy(CancellationSource.VENDOR).build()

        when:
        marketPlaceDomainService.cancelOrder(getMarketPlaceOrder("PICK_COMPLETE"), cancellationDetails)

        then:
        1 * marketPlaceOrdRepository.save(_ as MarketPlaceOrder) >> { MarketPlaceOrder _marketPlaceOrder ->
            assert _marketPlaceOrder.orderState == "CANCELLED"
            assert _marketPlaceOrder.vendorOrderId == externalOrderId
        }

        1 * eventGeneratorService.publishApplicationEvent(_ as MarketPlaceOrderCancelMessage) >> { MarketPlaceOrderCancelMessage _marketPlaceOrderCancelMessage ->
            assert _marketPlaceOrderCancelMessage.cancellationSource == CancellationSource.VENDOR
            assert _marketPlaceOrderCancelMessage.vendorOrderId == externalOrderId
        }

        1 * domainEventPublisher.publish(_ as DomainEvent, _ as String)

    }

    def "Test cancel with already delivered  order"() {

        given:
        String externalOrderId = UUID.randomUUID().toString()
        String itemId = UUID.randomUUID().toString()
        Vendor vendor = Vendor.UBEREATS
        String deliveredOrderState = "DELIVERED"
        String rejectedState = "REJECTED"
        String storeId = "4401"
        String externalItemId = UUID.randomUUID().toString()
        String customerFirstName = "John"
        String customerLastName = "Doe"
        String instanceId = UUID.randomUUID().toString()

        ItemIdentifier itemIdentifier = ItemIdentifier.builder().itemId(externalItemId).itemType("CIN").build()
        MarketPlaceItemPriceInfo marketPlaceItemPriceInfo = MarketPlaceItemPriceInfo.builder().totalPrice(1.0).baseTotalPrice(1.0).unitPrice(1.0).baseUnitPrice(1.0).build()

        MarketPlaceOrderContactInfo marketPlaceOrderContactInfo = MarketPlaceOrderContactInfo.builder().firstName(customerFirstName).lastName(customerLastName).build()

        MarketPlaceOrder marketPlaceOrder = MarketPlaceOrder.builder()
                .vendorOrderId(externalOrderId)
                .orderDueTime(new Date())
                .orderState(deliveredOrderState)
                .marketPlaceItems([])
                .sourceModifiedDate(new Date())
                .storeId(storeId).vendorId(vendor)
                .marketPlaceOrderContactInfo(marketPlaceOrderContactInfo).build()

        marketPlaceOrder.addMarketPlaceItem(itemId, externalItemId, "test item", 1, instanceId, itemIdentifier, marketPlaceItemPriceInfo, SubstitutionOption.DO_NOT_SUBSTITUTE)

        marketPlaceGatewayFinder.getMarketPlaceGateway(_ as Vendor) >> marketPlaceGateWay
        CancellationDetails cancellationDetails = CancellationDetails.builder().cancelledBy(CancellationSource.STORE).build()


        when:
        marketPlaceDomainService.cancelOrder(marketPlaceOrder, cancellationDetails)

        then:

        thrown(OMSBadRequestException.class)

        0 * domainEventPublisher.publish(_ as DomainEvent, _ as String)

    }

    def "Test Report Invocation Successfully"() {
        given:
        MarketPlaceReportRequest marketPlaceReportRequest = getMarketPlaceReportRequest()

        when:
        marketPlaceDomainService.invokeMarketPlaceReport(marketPlaceReportRequest)

        then:
        1 * marketPlaceGatewayFinder.getMarketPlaceGateway(_ as Vendor) >> { Vendor vendor ->
            assert vendor == Vendor.UBEREATS
            return marketPlaceGateWay
        }
    }

    def "Save marketplace order Successfully"() {
        given:
        MarketPlaceOrder order = getMarketPlaceOrder("CREATED")

        when:
        marketPlaceDomainService.pickCompleteMarketPlaceOrder(order)

        then:
        1 * marketPlaceOrdRepository.save(_ as MarketPlaceOrder) >> { MarketPlaceOrder marketPlaceOrder ->
            assert marketPlaceOrder.orderState == "PICK_COMPLETE"
            return marketPlaceOrder
        }
    }

    private MarketPlaceItemPriceInfo getMarketPlaceItemPriceInfo() {
        return MarketPlaceItemPriceInfo.builder().totalPrice(1.0).baseTotalPrice(1.0).unitPrice(1.0).baseUnitPrice(1.0).build()
    }

    private MarketPlaceOrderContactInfo getMarketPlaceOrderContactInfo() {
        return MarketPlaceOrderContactInfo.builder().firstName("John").lastName("Doe").build()
    }

    private MarketPlaceOrder getMarketPlaceOrder(String orderState) {
        return MarketPlaceOrder.builder()
                .vendorOrderId(externalOrderId)
                .orderDueTime(new Date())
                .orderState(orderState)
                .sourceModifiedDate(new Date())
                .storeId("4401").vendorId(Vendor.UBEREATS)
                .marketPlaceOrderContactInfo(getMarketPlaceOrderContactInfo()).build()
    }

    MarketPlaceReportRequest getMarketPlaceReportRequest() {
        return MarketPlaceReportRequest.builder()
                .reportType(ReportType.DOWNTIME_REPORT)
                .startDate(LocalDate.now().minusDays(15))
                .endDate(LocalDate.now().minusDays(5))
                .vendor(Vendor.UBEREATS)
                .build()
    }
}