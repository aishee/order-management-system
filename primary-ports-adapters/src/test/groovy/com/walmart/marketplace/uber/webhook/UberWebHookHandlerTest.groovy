package com.walmart.marketplace.uber.webhook

import com.walmart.config.WebHandlerConfiguration
import com.walmart.marketplace.uber.dto.ReportMetaData
import com.walmart.marketplace.uber.dto.UberWebHookRequest
import com.walmart.marketplace.uber.webhook.factory.UberWebHookHandlerFactory
import com.walmart.marketplace.uber.webhook.processors.OrderCancelEventProcessor
import com.walmart.marketplace.uber.webhook.processors.OrderNotifyEventProcessor
import com.walmart.marketplace.uber.webhook.processors.ReportDownloadEventProcessor
import spock.lang.Specification

import java.time.Instant
import java.util.concurrent.ExecutorService

class UberWebHookHandlerTest extends Specification {

    UberWebHookHandler uberWebHookHandler

    ExecutorService uberWebHookHandlerService = Mock()
    UberWebHookHandlerFactory uberWebHookHandlerFactory = Mock()
    OrderNotifyEventProcessor orderNotifyEventProcessor = Mock()
    OrderCancelEventProcessor orderCancelEventProcessor = Mock()
    ReportDownloadEventProcessor reportDownloadEventProcessor = Mock()

    def setup() {
        WebHandlerConfiguration webHandlerConfiguration = new WebHandlerConfiguration(10)
        uberWebHookHandler = new UberWebHookHandler(
                uberWebHandlerConfiguration: webHandlerConfiguration,
                uberWebHookHandlerService: uberWebHookHandlerService,
                uberWebHookHandlerFactory: uberWebHookHandlerFactory
        )
        uberWebHookHandler.setup()
    }

    def "Test UberWebHook for Order Notify event"() {

        given:
        String eventId = UUID.randomUUID().toString()
        String eventType = "orders.notification"
        String resourceUrl = "http://localhost:8080"

        UberWebHookRequest webHookRequest = new UberWebHookRequest()
        webHookRequest.setEventId(eventId)
        webHookRequest.setEventType(eventType)
        webHookRequest.setResourceHref(resourceUrl)
        webHookRequest.setEventTime(Instant.now().toEpochMilli())

        when:
        uberWebHookHandler.uberWebHook(webHookRequest)

        then:
        1 * uberWebHookHandlerFactory.getUberWebHookEventProcessor(_ as String) >> {
            return orderNotifyEventProcessor
        }
    }

    def "Test ProcessUberWebHookEvent for order cancellation event"() {
        given:
        String eventId = UUID.randomUUID().toString()
        String eventType = "orders.cancel"
        String resourceUrl = "http://localhost:8080"

        UberWebHookRequest webHookRequest = new UberWebHookRequest()
        webHookRequest.setEventId(eventId)
        webHookRequest.setEventType(eventType)
        webHookRequest.setResourceHref(resourceUrl)
        webHookRequest.setEventTime(Instant.now().toEpochMilli())

        when:
        uberWebHookHandler.uberWebHook(webHookRequest)

        then:
        1 * uberWebHookHandlerFactory.getUberWebHookEventProcessor(_ as String) >> {
            return orderCancelEventProcessor
        }
    }

    def "Test Uber Webhook event on Uber Report event"() {
        given:
        String eventId = UUID.randomUUID().toString()
        String eventType = "eats.report.success"
        String resourceUrl = "http://localhost:8080"

        ReportMetaData.Section section = new ReportMetaData.Section()
        section.setDownloadUrl("test")
        List<ReportMetaData.Section> sectionList = new ArrayList<>()
        sectionList.add(section)
        ReportMetaData reportMetaData = new ReportMetaData()
        reportMetaData.setSections(sectionList)

        UberWebHookRequest webHookRequest = new UberWebHookRequest()
        webHookRequest.setEventId(eventId)
        webHookRequest.setEventType(eventType)
        webHookRequest.setResourceHref(resourceUrl)
        webHookRequest.setEventTime(Instant.now().toEpochMilli())
        webHookRequest.setReportMetaData(reportMetaData)

        when:
        uberWebHookHandler.uberWebHook(webHookRequest)

        then:
        1 * uberWebHookHandlerFactory.getUberWebHookEventProcessor(_ as String) >> {
            return reportDownloadEventProcessor
        }
    }
}