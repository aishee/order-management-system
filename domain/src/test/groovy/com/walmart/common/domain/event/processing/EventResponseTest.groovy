package com.walmart.common.domain.event.processing

import spock.lang.Specification

class EventResponseTest extends Specification {
    EventResponse eventResponse


    def setup() {
        eventResponse = new EventResponse()
    }

    def "Test EventResponse Parameterized Constructor"() {
        given:
        EgressEvent egressEvent = new EgressEvent()
        egressEvent.markAsProduced()
        when:
        EventResponse eventResponse1 = new EventResponse(egressEvent, null)
        then:
        eventResponse1.getEvent() != null
        eventResponse1.isSuccess()
        !eventResponse1.isError()
        eventResponse1.getResponse() == null
        noExceptionThrown()
    }

    def "Test AddEvent"() {
        given:
        EgressEvent egressEvent = new EgressEvent()
        egressEvent.markAsProduced()
        when:
        eventResponse.addEvent(egressEvent)
        then:
        noExceptionThrown()
    }
}
