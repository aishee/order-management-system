package com.walmart.fms.infrastructure.integration.jms

import org.apache.camel.CamelContext
import org.apache.camel.ProducerTemplate
import spock.lang.Specification

class PublishToMessagingQueueSpec extends Specification {

    CamelContext camelContext = Mock()
    PublishToMessagingQueue publishToMessagingQueue

    def setup() {
        publishToMessagingQueue = new PublishToMessagingQueue(
                "camelContext": camelContext
        )
    }

    def "Triggering Camel Context"() {
        given:
        ProducerTemplate producerTemplate = Mock()
        camelContext.createProducerTemplate() >> { producerTemplate }

        when:
        publishToMessagingQueue.postEventToByEndpointUri("endpoint", "message")

        then:
        1 * producerTemplate.sendBody(_ as String, _ as String)
    }
}
