package com.walmart.marketplace.justeats.webhook

import com.walmart.config.WebHandlerConfiguration
import com.walmart.marketplace.justeats.factory.JustEatsEventProcessorFactory
import com.walmart.marketplace.justeats.processors.JustEatsOrderNotifyEventProcessor
import com.walmart.marketplace.justeats.request.JustEatsWebHookRequest
import spock.lang.Specification

import java.util.concurrent.ExecutorService

class JustEatsWebHookHandlerTest extends Specification {

    JustEatsWebHookHandler justeatsWebHookHandler
    ExecutorService webHookHandlerService = Mock()
    JustEatsEventProcessorFactory justeatsWebHookHandlerFactory = Mock()

    def setup() {
        WebHandlerConfiguration webHandlerConfiguration = new WebHandlerConfiguration(10)

        justeatsWebHookHandler = new JustEatsWebHookHandler(
                webHandlerConfiguration: webHandlerConfiguration,
                justEatsWebHookHandlerFactory: justeatsWebHookHandlerFactory
        )
        justeatsWebHookHandler.setup()
    }

    def "Test JustEatsWebHook for Order Creation event"() {

        given:
        String eventId = UUID.randomUUID().toString()
        JustEatsWebHookRequest webHookRequest = new JustEatsWebHookRequest()
        webHookRequest.setId(eventId)
        JustEatsOrderNotifyEventProcessor justEatsOrderNotifyEventProcessor = Mock()

        when:
        justeatsWebHookHandler.justEatsWebHook(webHookRequest)

        then:
        1 * justeatsWebHookHandlerFactory.getJustEatsWebHookEventProcessor() >> {
            return justEatsOrderNotifyEventProcessor
        }
    }

}