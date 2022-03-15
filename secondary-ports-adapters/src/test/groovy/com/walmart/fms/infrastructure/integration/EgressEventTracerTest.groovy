package com.walmart.fms.infrastructure.integration

import com.walmart.common.infrastructure.integration.events.tracing.EgressEventTracerImpl
import com.walmart.common.domain.event.processing.EgressEvent
import com.walmart.common.domain.event.processing.EgressEventTracer
import com.walmart.common.domain.repository.EgressEventTracerRepository
import spock.lang.Shared
import spock.lang.Specification

import static com.walmart.common.domain.event.processing.EgressEvent.EgressStatus.*
import static com.walmart.fms.EgressEventsAndMappers.an_event_with

class EgressEventTracerTest extends Specification {

    EgressEventTracer eventTracer
    EgressEventTracerRepository repository = Mock()
    @Shared
    int max_re_tries = 5

    def setup() {
        eventTracer = new EgressEventTracerImpl(repository: repository, maxRetries: max_re_tries)
    }

    def "When null event is passed for tracing, throw illegal argument exception"() {
        when: "Trace is called with a null event object"
        eventTracer.trace(null)
        then: "Must throw IllegalArgumentException"
        thrown(IllegalArgumentException)
    }

    def "When an attempt is made to trace an event which is in error state, throw illegal state exception"() {
        when: "Trace is called with a null event object"
        eventTracer.trace(an_event_with("123456", 0, INITIAL))
        then: "Must throw IllegalArgumentException"
        1 * repository.get(*_) >> { _ ->
            return an_event_with("1234514", 5, ERROR)
        }
        thrown(IllegalStateException)
    }

    def "Test tracing egress events"() {
        when: "Trace is called on the initial event"
        EgressEvent result = eventTracer.trace(event)
        then: "A call to save the event must happen and the saved copy must be in initial state"
        1 * repository.get(*_) >> { _ ->
            return savedEvent
        }
        1 * repository.save(_ as EgressEvent) >> { EgressEvent _event ->
            return _event
        }
        assert result.status == expectedStatus
        assert result.retries == expectedRetries
        where:
        event                                         | savedEvent                                     | expectedStatus   | expectedRetries
        an_event_with("123456", 0, INITIAL)           | null                                           | INITIAL          | 0
        an_event_with("123457", 0, READY_TO_PUBLISH)  | null                                           | READY_TO_PUBLISH | 0
        an_event_with("123458", 0, PRODUCED)          | null                                           | PRODUCED         | 0
        an_event_with("123459", 0, FAILED)            | null                                           | FAILED           | 0
        an_event_with("1234510", 0, INITIAL)          | an_event_with("1234510", 0, FAILED)            | INITIAL          | 1
        an_event_with("1234510", 0, READY_TO_PUBLISH) | an_event_with("1234510", 0, FAILED)            | READY_TO_PUBLISH | 1
        an_event_with("1234510", 0, PRODUCED)         | an_event_with("1234510", 0, FAILED)            | PRODUCED         | 1
        an_event_with("1234510", 0, INITIAL)          | an_event_with("1234510", 3, FAILED)            | INITIAL          | 4
        an_event_with("1234510", 0, FAILED)           | an_event_with("1234510", 3, INITIAL)           | FAILED           | 3
        an_event_with("1234510", 0, FAILED)           | an_event_with("1234510", 3, READY_TO_PUBLISH)  | FAILED           | 3
        an_event_with("1234510", 0, PRODUCED)         | an_event_with("1234510", 3, READY_TO_PUBLISH)  | PRODUCED         | 3
        an_event_with("1234514", 0, INITIAL)          | an_event_with("1234514", max_re_tries, FAILED) | ERROR            | max_re_tries
    }


}
