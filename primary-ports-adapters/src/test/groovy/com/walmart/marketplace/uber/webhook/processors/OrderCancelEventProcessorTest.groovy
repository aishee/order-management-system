package com.walmart.marketplace.uber.webhook.processors

import com.walmart.common.domain.type.CancellationSource
import com.walmart.common.metrics.MetricService
import com.walmart.marketplace.MarketPlaceApplicationService
import com.walmart.marketplace.commands.CancelMarketPlaceOrderCommand
import com.walmart.marketplace.commands.WebHookEventCommand
import com.walmart.marketplace.converter.RequestToCommandMapper
import com.walmart.marketplace.order.aggregateroot.MarketPlaceOrder
import com.walmart.marketplace.order.domain.entity.MarketPlaceEvent
import com.walmart.marketplace.order.domain.entity.type.Vendor
import com.walmart.marketplace.uber.dto.UberWebHookRequest
import com.walmart.common.domain.valueobject.CancellationDetails
import spock.lang.Specification

import java.time.Instant

class OrderCancelEventProcessorTest extends Specification {
    OrderCancelEventProcessor orderCancelEventProcessor

    MetricService metricService = Mock()
    RequestToCommandMapper mapper = Mock()
    MarketPlaceApplicationService marketPlaceApplicationService = Mock()

    def setup() {
        orderCancelEventProcessor = new OrderCancelEventProcessor(
                mapper: mapper,
                marketPlaceApplicationService: marketPlaceApplicationService,
                metricService: metricService
        )
    }

    def "Test success scenario"() {
        given:
        Vendor vendor = Vendor.UBEREATS
        String eventType = "orders.cancel"
        String resourceUrl = "http://localhost:8080"
        String id = UUID.randomUUID().toString()

        UberWebHookRequest webHookRequest = new UberWebHookRequest()
        webHookRequest.setEventId(id)
        webHookRequest.setEventType(eventType)
        webHookRequest.setResourceHref(resourceUrl)
        webHookRequest.setEventTime(Instant.now().toEpochMilli())
        CancelMarketPlaceOrderCommand cancelMarketPlaceOrderCommand = CancelMarketPlaceOrderCommand.builder()
                .sourceOrderId(id)
                .vendor(Vendor.UBEREATS)
                .cancellationDetails(CancellationDetails.builder()
                        .cancelledReasonCode("VENDOR")
                        .cancelledBy(CancellationSource.VENDOR)
                        .cancelledReasonDescription("CANCELLED BY VENDOR")
                        .build())
                .build()

        MarketPlaceEvent marketPlaceEvent = new MarketPlaceEvent(id, id, resourceUrl, eventType, id, vendor)
        WebHookEventCommand webHookEventCommand = WebHookEventCommand.builder().eventType(eventType).externalOrderId(id).resourceURL(resourceUrl).sourceEventId(id).build()

        when:
        orderCancelEventProcessor.processWebhookRequest(webHookRequest)

        then:
        1 * marketPlaceApplicationService.captureWebHookEvent(_ as WebHookEventCommand) >> { WebHookEventCommand _webHookEventCommand ->
            assert _webHookEventCommand.eventType == eventType
            assert _webHookEventCommand.externalOrderId == id
            assert _webHookEventCommand.resourceURL == resourceUrl
            return marketPlaceEvent
        }
        1 * marketPlaceApplicationService.cancelOrder(_ as CancelMarketPlaceOrderCommand) >> { CancelMarketPlaceOrderCommand _cancelMarketPlaceOrderCommand ->
            assert _cancelMarketPlaceOrderCommand.sourceOrderId == id
            assert _cancelMarketPlaceOrderCommand.vendor == vendor
            assert _cancelMarketPlaceOrderCommand.getCancellationDetails().cancelledReasonCode == "VENDOR"
            assert _cancelMarketPlaceOrderCommand.getCancellationDetails().cancelledBy == CancellationSource.VENDOR
            return MarketPlaceOrder.builder()
                    .id(id)
                    .vendorOrderId(id)
                    .build()
        }

        1 * mapper.createWebHookCommand(webHookRequest) >> webHookEventCommand

        1 * marketPlaceApplicationService.getOrder(_ as String) >> { String _vendorOrderId ->
            return MarketPlaceOrder.builder()
                    .id(id)
                    .vendorOrderId(id)
                    .build()
        }
        1 * mapper.createMarketPlaceCancelCommand(_ as String, _ as String, _ as String, _ as String, _ as Vendor) >> { String _sourceOrderId, String _cancelledReasonCode, String _cancelledReasonDesc, String _resourceUrl, Vendor _vendor ->
            assert _sourceOrderId == id
            assert _resourceUrl == resourceUrl
            assert _vendor == vendor
            return cancelMarketPlaceOrderCommand
        }
    }

    def "When marketplaceOrder is not present"() {
        given:
        String eventId = UUID.randomUUID().toString()
        Vendor vendor = Vendor.UBEREATS
        String eventType = "orders.cancel"
        String resourceUrl = "http://localhost:8080"
        String id = UUID.randomUUID().toString()

        CancelMarketPlaceOrderCommand cancelMarketPlaceOrderCommand = CancelMarketPlaceOrderCommand.builder()
                .sourceOrderId(id)
                .cancellationDetails(CancellationDetails.builder()
                        .cancelledReasonCode("VENDOR")
                        .cancelledBy(CancellationSource.VENDOR)
                        .cancelledReasonDescription("CANCELLED BY VENDOR")
                        .build())
                .build()
        MarketPlaceEvent marketPlaceEvent = new MarketPlaceEvent(eventId, eventId, resourceUrl, eventType, eventId, vendor)
        UberWebHookRequest webHookRequest = new UberWebHookRequest()
        webHookRequest.setEventId(eventId)
        webHookRequest.setEventType(eventType)
        webHookRequest.setResourceHref(resourceUrl)
        webHookRequest.setEventTime(Instant.now().toEpochMilli())
        WebHookEventCommand webHookEventCommand = WebHookEventCommand.builder().eventType(eventType).externalOrderId(eventId).resourceURL(resourceUrl).sourceEventId(eventId).build()

        when:
        orderCancelEventProcessor.processWebhookRequest(webHookRequest)

        then:
        1 * marketPlaceApplicationService.captureWebHookEvent(_ as WebHookEventCommand) >> { WebHookEventCommand _webHookEventCommand ->
            assert _webHookEventCommand.eventType == eventType
            assert _webHookEventCommand.externalOrderId == eventId
            assert _webHookEventCommand.resourceURL == resourceUrl
            return marketPlaceEvent
        }

        1 * mapper.createWebHookCommand(webHookRequest) >> webHookEventCommand

        1 * marketPlaceApplicationService.getOrder(_ as String) >> { String _vendorOrderId ->
            return null
        }
        0 * mapper.createMarketPlaceCancelCommand(_ as String, _ as String, _ as String, _ as String, _ as Vendor) >> { String _sourceOrderId, String _cancelledReasonCode, String _cancelledReasonDesc, String _resourceUrl, Vendor _vendor ->
            assert _sourceOrderId == id
            assert _resourceUrl == resourceUrl
            assert _vendor == vendor
            return cancelMarketPlaceOrderCommand
        }
    }
}