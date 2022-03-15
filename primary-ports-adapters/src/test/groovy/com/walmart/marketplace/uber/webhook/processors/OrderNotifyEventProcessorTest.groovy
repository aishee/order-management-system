package com.walmart.marketplace.uber.webhook.processors

import com.walmart.common.metrics.MetricService
import com.walmart.marketplace.MarketPlaceApplicationService
import com.walmart.marketplace.commands.CreateMarketPlaceOrderFromAdapterCommand
import com.walmart.marketplace.commands.WebHookEventCommand
import com.walmart.marketplace.converter.RequestToCommandMapper
import com.walmart.marketplace.order.domain.entity.MarketPlaceEvent
import com.walmart.marketplace.order.domain.entity.type.Vendor
import com.walmart.marketplace.uber.dto.UberWebHookRequest
import spock.lang.Specification

import java.time.Instant

class OrderNotifyEventProcessorTest extends Specification {
    OrderNotifyEventProcessor orderNotifyEventProcessor

    RequestToCommandMapper mapper = Mock()
    MarketPlaceApplicationService marketPlaceApplicationService = Mock()
    MetricService metricService = Mock()

    def setup() {
        orderNotifyEventProcessor = new OrderNotifyEventProcessor(
                mapper: mapper,
                marketPlaceApplicationService: marketPlaceApplicationService,
                metricService: metricService
        )
    }

    def "Test Success scenario"() {
        given:
        String eventId = UUID.randomUUID().toString()
        Vendor vendor = Vendor.UBEREATS
        String eventType = "orders.notification"
        String resourceUrl = "http://localhost:8080"

        UberWebHookRequest webHookRequest = new UberWebHookRequest()
        webHookRequest.setEventId(eventId)
        webHookRequest.setEventType(eventType)
        webHookRequest.setResourceHref(resourceUrl)
        webHookRequest.setEventTime(Instant.now().toEpochMilli())
        MarketPlaceEvent marketPlaceEvent = new MarketPlaceEvent(eventId, eventId, resourceUrl, eventType, eventId, vendor)
        CreateMarketPlaceOrderFromAdapterCommand createMarketPlaceOrderFromAdapterCommand = CreateMarketPlaceOrderFromAdapterCommand.builder()
                .resourceUrl(resourceUrl)
                .externalOrderId(eventId)
                .vendor(vendor)
                .build()
        WebHookEventCommand webHookEventCommand = WebHookEventCommand.builder().eventType(eventType).externalOrderId(eventId).resourceURL(resourceUrl).sourceEventId(eventId).build()

        when:
        orderNotifyEventProcessor.processWebhookRequest(webHookRequest)

        then:
        1 * marketPlaceApplicationService.captureWebHookEvent(_ as WebHookEventCommand) >> { WebHookEventCommand _webHookEventCommand ->
            assert _webHookEventCommand.eventType == eventType
            assert _webHookEventCommand.externalOrderId == eventId
            assert _webHookEventCommand.resourceURL == resourceUrl
            return marketPlaceEvent
        }

        1 * mapper.createWebHookCommand(webHookRequest) >> webHookEventCommand

        1 * mapper.createMarketPlaceOrderCmd(_ as String, _ as String, _ as Vendor) >> { String _externalOrderId, String _resourceUrl, Vendor _vendor ->
            assert _externalOrderId == eventId
            assert _resourceUrl == resourceUrl
            assert _vendor == vendor
            return createMarketPlaceOrderFromAdapterCommand
        }

        1 * marketPlaceApplicationService.createAndProcessMarketPlaceOrder(_ as CreateMarketPlaceOrderFromAdapterCommand) >> { CreateMarketPlaceOrderFromAdapterCommand _createMarketPlaceOrderFromAdapterCommand ->
            assert _createMarketPlaceOrderFromAdapterCommand.resourceUrl == resourceUrl
            assert _createMarketPlaceOrderFromAdapterCommand.externalOrderId == eventId
            assert _createMarketPlaceOrderFromAdapterCommand.vendor == vendor
        }
    }
}
