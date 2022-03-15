package com.walmart.fms.infrastructure.integration.jms.gateway.store

import com.walmart.common.domain.type.SubstitutionOption
import com.walmart.fms.infrastructure.integration.gateway.store.StoreGateway
import com.walmart.fms.infrastructure.integration.gateway.store.StoreGatewayConfig
import com.walmart.fms.infrastructure.integration.gateway.store.dto.forceordercancellation.UpdateFulfillmentOrderRequest
import com.walmart.fms.infrastructure.integration.gateway.store.dto.orderdownload.Customer
import com.walmart.fms.infrastructure.integration.gateway.store.dto.orderdownload.MessageHeader
import com.walmart.fms.infrastructure.integration.gateway.store.dto.orderdownload.PlaceFulfillmentOrderRequest
import com.walmart.fms.infrastructure.integration.jms.PublishToMessagingQueue
import com.walmart.fms.infrastructure.integration.jms.config.JmsEndPointConfig
import com.walmart.fms.infrastructure.integration.jms.config.JmsProducerEndpointConfig
import com.walmart.fms.infrastructure.integration.mapper.OrderDownloadMapper
import com.walmart.fms.order.aggregateroot.FmsOrder
import com.walmart.fms.order.domain.entity.FmsCustomerContactInfo
import com.walmart.fms.order.domain.entity.FmsOrderItem
import com.walmart.fms.order.domain.entity.FmsSchedulingInfo
import com.walmart.fms.order.valueobject.*
import spock.lang.Specification

class StoreGatewayTest extends Specification {

    StoreGateway storeGateway = Mock()
    OrderDownloadMapper orderDownloadMapper = Mock()
    JmsProducerEndpointConfig jmsProducerEndpointConfig = Mock()
    PublishToMessagingQueue publishToMessagingQueue = Mock()
    StoreGatewayConfig gatewayConfig = new StoreGatewayConfig(2, 2, 10, 30)


    def setup() {
        storeGateway = new StoreGateway(
                orderDownloadMapper: orderDownloadMapper,
                jmsProducerEndpointConfig: jmsProducerEndpointConfig,
                publishToMessagingQueue: publishToMessagingQueue,
                gatewayConfig: gatewayConfig

        )
        storeGateway.initComponent()

    }

    def "Empty FmsOrder model in OrderDownload"() {

        given:
        FmsOrder fmsOrder = null

        when:
        Boolean result = storeGateway.sendMarketPlaceOrderDownload(fmsOrder)

        then:
        !result
    }

    def "Valid FmsOrder model in OrderDownload"() {

        given:
        FmsOrder fmsOrder = createFmsOrder()
        PlaceFulfillmentOrderRequest mapperMock = MockOrderDownloadMapper()
        orderDownloadMapper.toPlaceFulfillmentOrderRequest(fmsOrder) >> { return mapperMock }
        jmsProducerEndpointConfig.getgifOrderDownloadConfig() >> MockConfig()

        when:
        Boolean result = storeGateway.sendMarketPlaceOrderDownload(fmsOrder)

        then:
        result
    }


    def "Empty FmsOrder model in OrderDownloadAsync"() {

        given:
        FmsOrder fmsOrder = null

        when:
        storeGateway.sendMarketPlaceOrderDownloadAsync(fmsOrder).get()

        then:
        thrown(IllegalArgumentException)
    }

    def "Valid FmsOrder model in OrderDownloadAsync"() {

        given:
        FmsOrder fmsOrder = createFmsOrder()
        PlaceFulfillmentOrderRequest mapperMock = MockOrderDownloadMapper()
        orderDownloadMapper.toPlaceFulfillmentOrderRequest(fmsOrder) >> { return mapperMock }
        jmsProducerEndpointConfig.getgifOrderDownloadConfig() >> MockConfig()

        when:
        Boolean result = storeGateway.sendMarketPlaceOrderDownloadAsync(fmsOrder).get()

        then:
        result
    }

    def "Empty FmsOrder model in Force OrderCancellation"() {

        given:
        FmsOrder fmsOrder = null

        when:
        Boolean result = storeGateway.sendMarketPlaceForceOrderCancellation(fmsOrder)

        then:
        !result
    }

    def "Valid FmsOrder model in Force OrderCancellation"() {

        given:
        FmsOrder fmsOrder = createFmsOrder()
        jmsProducerEndpointConfig.getGifForceOrderCancelConfig() >> MockConfig()

        when:
        Boolean result = storeGateway.sendMarketPlaceForceOrderCancellation(fmsOrder)

        then:
        result
    }

    def "Validate mapping and construction of Force OrderCancellation XML from FMS_Order"() {

        given:
        FmsOrder fmsOrder = createFmsOrder()
        jmsProducerEndpointConfig.getGifForceOrderCancelConfig() >> MockConfig()

        when:
        UpdateFulfillmentOrderRequest forceOrderCancellationRequestXML = storeGateway.constructMarketPlaceForceOrderCancellationRequestXML(fmsOrder)

        then:
        forceOrderCancellationRequestXML.messageHeader != null
        forceOrderCancellationRequestXML.messageHeader.subId == "SUB-ASDA-UFO-V1"
        forceOrderCancellationRequestXML.messageHeader.cnsmrId == "CON-ASDA-HOS-V1"
        forceOrderCancellationRequestXML.messageHeader.tranId == fmsOrder.storeOrderId
        forceOrderCancellationRequestXML.messageHeader.version == "2.5"

        forceOrderCancellationRequestXML.messageBody != null
        forceOrderCancellationRequestXML.messageBody.customerOrder.orderHeader.orderNumber == fmsOrder.storeOrderId.toBigDecimal()
        forceOrderCancellationRequestXML.messageBody.customerOrder.fulfillmentOrder.get(0).orderHeader.orderNumber == fmsOrder.storeOrderId.toBigDecimal()
        forceOrderCancellationRequestXML.messageBody.customerOrder.fulfillmentOrder.get(0).orderType == "GRP"
        forceOrderCancellationRequestXML.messageBody.customerOrder.fulfillmentOrder.get(0).status.code == "CAN"
        forceOrderCancellationRequestXML.messageBody.customerOrder.fulfillmentOrder.get(0).status.eventTime != null
        forceOrderCancellationRequestXML.messageBody.customerOrder.fulfillmentOrder.get(0).status.description == "CANCELLED"
        forceOrderCancellationRequestXML.messageBody.customerOrder.fulfillmentOrder.get(0).node.location.countryCode == "GB"
        forceOrderCancellationRequestXML.messageBody.customerOrder.fulfillmentOrder.get(0).node.nodeID == fmsOrder.storeId.toLong()
        forceOrderCancellationRequestXML.messageBody.customerOrder.originatingNode.location.countryCode == "GB"
        forceOrderCancellationRequestXML.messageBody.customerOrder.originatingNode.nodeID == 4715
    }

    def "Validate mapping and construction of PFO Order Line XML from FmsOrderItem with Substitution allowed"() {

        given:
        FmsOrderItem fmsOrderItem = mockFmsOrderItemWithSubstitution(SubstitutionOption.SUBSTITUTE)
        jmsProducerEndpointConfig.getgifOrderDownloadConfig() >> MockConfig()

        when:
        PlaceFulfillmentOrderRequest.MessageBody.CustomerOrderInfo.CustomerOrder.FulfillmentOrders.OrderLines orderLines = storeGateway.buildPfoRequestOrderLine(fmsOrderItem, 1)

        then:
        orderLines.isSubstitutionAllowed
        orderLines.isOrderByQuantity
        orderLines.getLineNumber() == 1L
        orderLines.getPrice().getAmount().getValue() == 10
    }

    def "Validate mapping and construction of PFO Order Line XML from FmsOrderItem with Substitution Not allowed"() {

        given:
        FmsOrderItem fmsOrderItem = mockFmsOrderItemWithSubstitution(SubstitutionOption.DO_NOT_SUBSTITUTE)
        jmsProducerEndpointConfig.getgifOrderDownloadConfig() >> MockConfig()

        when:
        PlaceFulfillmentOrderRequest.MessageBody.CustomerOrderInfo.CustomerOrder.FulfillmentOrders.OrderLines orderLines = storeGateway.buildPfoRequestOrderLine(fmsOrderItem, 1)

        then:
        !orderLines.isSubstitutionAllowed
        orderLines.isOrderByQuantity
        orderLines.getLineNumber() == 1L
        orderLines.getPrice().getAmount().getValue() == 10
    }

    def "test sendMarketPlaceOrderDownload Exception"() {

        given:
        FmsOrder fmsOrder = createFmsOrder()

        when:
        Boolean result = storeGateway.sendMarketPlaceOrderDownload(fmsOrder)

        then:
        !result
    }

    def "test sendMarketPlaceForceOrderCancellation Exception"() {

        given:
        FmsOrder fmsOrder = createFmsOrder()

        when:
        Boolean result = storeGateway.sendMarketPlaceForceOrderCancellation(fmsOrder)

        then:
        !result
    }

    def "test before destroy"() {
        when:
        storeGateway.beforeDestroy()

        then:
        0 * storeGateway.beforeDestroy()
    }

    FmsOrder createFmsOrder() {
        return new FmsOrder(
                priceInfo: new OrderPriceInfo(60, 0.0, 0.0, 0.4),
                sourceOrderId: "1234567",
                storeId: "5555",
                contactInfo: new FmsCustomerContactInfo(
                        fullName: new FullName(
                                firstName: "Barath",
                                lastName: "NaravulaLoganathan"
                        ),
                        email: new EmailAddress(
                                address: "barath@walmart.com"
                        )
                ),
                schedulingInfo: new FmsSchedulingInfo(
                        slotStartTime: new Date(),
                        slotEndTime: new Date(),
                        orderDueTime: new Date()
                ),
                authStatus: "AUTH_SUCCESS",
                fmsOrderItems: [new FmsOrderItem(
                        consumerItemNumber: "123",
                        substitutionOption: SubstitutionOption.SUBSTITUTE,
                        itemPriceInfo: new ItemPriceInfo(
                                unitPrice: new Money(
                                        amount: 10
                                )
                        ),
                        catalogInfo: new ItemCatalogInfo(
                                imageUrl: "https://image.com",
                                pickerItemDescription: "pickDescr_Item",
                                isSellbyDateRequired: true,
                                minIdealDays: 1,
                                maxIdealDays: 1
                        ),
                        upcInfo: new ItemUpcInfo(['1243', '4312'])

                )],
                storeOrderId: "4127"
        )
    }

    FmsOrderItem mockFmsOrderItemWithSubstitution(SubstitutionOption substitutionOption) {
        return new FmsOrderItem(
                consumerItemNumber: "123",
                substitutionOption: substitutionOption,
                itemPriceInfo: new ItemPriceInfo(
                        unitPrice: new Money(
                                amount: 10
                        )
                ),
                catalogInfo: new ItemCatalogInfo(
                        imageUrl: "https://image.com",
                        pickerItemDescription: "pickDescr_Item",
                        isSellbyDateRequired: true,
                        minIdealDays: 1,
                        maxIdealDays: 1
                ),
                upcInfo: new ItemUpcInfo(['1243', '4312'])
        )
    }


    PlaceFulfillmentOrderRequest MockOrderDownloadMapper() {
        return new PlaceFulfillmentOrderRequest(
                messageHeader: new MessageHeader(
                        tranId: "1234567"
                ),
                messageBody: new PlaceFulfillmentOrderRequest.MessageBody(
                        customerOrderInfo: new PlaceFulfillmentOrderRequest.MessageBody.CustomerOrderInfo(
                                customerInfo: new PlaceFulfillmentOrderRequest.MessageBody.CustomerOrderInfo.CustomerInfo(
                                        customerDetails: new PlaceFulfillmentOrderRequest.MessageBody.CustomerOrderInfo.CustomerInfo.CustomerDetails(
                                                customer: new Customer(
                                                        id: "98765",
                                                        firstName: "Barath",
                                                        lastName: "NaravulaLoganathan"
                                                )
                                        )
                                )
                        )
                )
        )
    }

    JmsEndPointConfig MockConfig() {
        return new JmsEndPointConfig(
                endpointUrl: "MaasMqConsumerComponent:queue:DEV.QUEUE.3?concurrentConsumers=5",
                retryCount: "1",
                concurencyCount: "2"
        )
    }
}
