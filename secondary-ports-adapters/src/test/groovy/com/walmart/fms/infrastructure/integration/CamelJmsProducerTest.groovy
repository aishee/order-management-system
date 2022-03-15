package com.walmart.fms.infrastructure.integration

import com.walmart.common.infrastructure.integration.events.interactors.CamelJmsInteractor
import com.walmart.common.domain.event.processing.EgressEvent
import com.walmart.common.domain.event.processing.Interactor
import org.apache.camel.CamelContext
import org.apache.camel.ProducerTemplate
import spock.lang.Specification

import static com.walmart.common.domain.event.processing.EgressEvent.EgressStatus.*
import static com.walmart.fms.EgressEventsAndMappers.an_event_with
import static com.walmart.fms.EgressEventsAndMappers.initial_event_retries_0

class CamelJmsProducerTest extends Specification {

    Interactor messageProducer
    CamelContext camelContext = Mock()
    ProducerTemplate producerTemplate = Mock()

    def setup() {
        messageProducer = new CamelJmsInteractor(camelContext: camelContext, producerTemplate: producerTemplate)
        camelContext.createProducerTemplate() >> producerTemplate
    }

    def "Test error: throw error when null event and when the message is missing."() {
        when:
        messageProducer.interact(event)
        then:
        thrown(IllegalArgumentException)
        where:
        event                            | _
        null                             | _
        initial_event_retries_0("12345") | _
    }

    def "Test valid event with message:event status must change to PRODUCED "() {
        given:
        EgressEvent event = an_event_with("12345", 0, READY_TO_PUBLISH)
        when:
        messageProducer.interact(event)
        then:
        1 * producerTemplate.sendBody(_ as String, _ as String) >> { String uri, String message ->
            assert event.destination == uri
            assert event.message == message
        }
        assert event.status == PRODUCED
    }

    def "Test valid event with message: if there is an error while sending message then event must be marked as failed"() {
        given:
        EgressEvent event = an_event_with("12345", 0, READY_TO_PUBLISH)
        when:
        messageProducer.interact(event)
        then:
        1 * producerTemplate.sendBody(_ as String, _ as String) >> { String uri, String message ->
            throw new IllegalArgumentException("dummy throw")
        }
        thrown(IllegalArgumentException)
        assert event.status == FAILED
    }

}
