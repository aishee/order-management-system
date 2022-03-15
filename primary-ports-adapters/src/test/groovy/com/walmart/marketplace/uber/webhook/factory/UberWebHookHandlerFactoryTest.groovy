package com.walmart.marketplace.uber.webhook.factory

import com.walmart.marketplace.uber.webhook.processors.OrderCancelEventProcessor
import com.walmart.marketplace.uber.webhook.processors.OrderNotifyEventProcessor
import com.walmart.marketplace.uber.webhook.processors.ReportDownloadEventProcessor
import com.walmart.marketplace.uber.webhook.processors.UberWebHookEventProcessor
import com.walmart.marketplace.uber.webhook.processors.WebhookEventType
import com.walmart.oms.domain.error.exception.OMSBadRequestException
import spock.lang.Specification

class UberWebHookHandlerFactoryTest extends Specification {
    UberWebHookHandlerFactory uberWebHookHandlerFactory

    private OrderCancelEventProcessor orderCancelEventProcessor = Mock()
    private OrderNotifyEventProcessor orderNotifyEventProcessor = Mock()
    private ReportDownloadEventProcessor reportDownloadEventProcessor = Mock()

    def setup() {
        uberWebHookHandlerFactory = new UberWebHookHandlerFactory(
                orderNotifyEventProcessor: orderNotifyEventProcessor,
                orderCancelEventProcessor: orderCancelEventProcessor,
                reportDownloadEventProcessor: reportDownloadEventProcessor
        )
    }

    def "Test UberWebHook for Order Notify event"() {

        given:
        String eventType = WebhookEventType.ORDER_NOTIFY_EVENT_TYPE.getEventType()

        when:
        UberWebHookEventProcessor uberWebHookEventProcessor = uberWebHookHandlerFactory.getUberWebHookEventProcessor(eventType)

        then:
        uberWebHookEventProcessor != null
        noExceptionThrown()
    }

    def "Test UberWebHook for Order Cancel event"() {

        given:
        String eventType = WebhookEventType.ORDER_CANCEL_EVENT_TYPE.getEventType()

        when:
        UberWebHookEventProcessor uberWebHookEventProcessor = uberWebHookHandlerFactory.getUberWebHookEventProcessor(eventType)

        then:
        uberWebHookEventProcessor != null
        noExceptionThrown()
    }

    def "Test UberWebHook for Report Download event"() {

        given:
        String eventType = WebhookEventType.REPORT_SUCCESS_EVENT_TYPE.getEventType()

        when:
        UberWebHookEventProcessor uberWebHookEventProcessor = uberWebHookHandlerFactory.getUberWebHookEventProcessor(eventType)

        then:
        uberWebHookEventProcessor != null
        noExceptionThrown()
    }

    def "Test UberWebHook for Unknown event"() {

        given:
        String eventType = "not.valid"

        when:
        uberWebHookHandlerFactory.getUberWebHookEventProcessor(eventType)

        then:
        thrown(OMSBadRequestException)
    }
}
