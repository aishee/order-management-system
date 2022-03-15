package com.walmart.fms.infrastructure.integration

import com.walmart.common.infrastructure.integration.events.processing.IdempotentEgressEventProcessor
import com.walmart.common.domain.event.processing.EgressEvent
import com.walmart.common.domain.event.processing.EgressEventProcessor
import com.walmart.common.domain.event.processing.EgressEventTracer
import com.walmart.common.domain.event.processing.EventResponse
import com.walmart.fms.DummyInteractor
import com.walmart.fms.order.valueobject.events.FmsOrderValueObject
import spock.lang.Ignore
import spock.lang.Specification
import spock.util.concurrent.BlockingVariable

import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.function.Consumer

import static com.walmart.common.domain.event.processing.EgressEvent.EgressStatus.*
import static com.walmart.fms.EgressEventsAndMappers.*

class IdempotentEgressEventProcessorTest extends Specification {

    EgressEventTracer eventTracer = Mock()

    def setup() {

    }

    def "If null event it received then throw Illegal argument exception"() {
        given:
        EgressEventProcessor processor = Spy(IdempotentEgressEventProcessor, constructorArgs: [eventTracer]) {
            getInteractor() >> new DummyInteractor.NoAckInteractor()
            eventTracer.trace(_ as EgressEvent) >> { EgressEvent _event ->
                return _event
            }
        }
        when:
        processor.processAsync(null)
        then: "Throw exception"
        thrown(IllegalArgumentException)
    }

    def "Process Sync EgressEvent:: null event object"() {
        given:
        EgressEventProcessor processor = Spy(IdempotentEgressEventProcessor, constructorArgs: [eventTracer]) {
            getInteractor() >> new DummyInteractor.NoAckInteractor()
            eventTracer.trace(_ as EgressEvent) >> { EgressEvent _event ->
                return _event
            }
        }
        when:
        processor.process(null)
        then:
        thrown(IllegalArgumentException)
    }

    def "Process Async EgressEvent "() {
        given:
        ExecutorService executorService = Executors.newFixedThreadPool(1)
        EgressEventProcessor processor = Spy(IdempotentEgressEventProcessor, constructorArgs: [eventTracer]) {
            getInteractor() >> interactor
            eventTracer.trace(_ as EgressEvent) >> { EgressEvent _event ->
                return _event
            }
        }
        when:
        def result = new BlockingVariable<EventResponse>()
        processor.process(executorService, event) { e ->
            result.set(e)
        }
        then: "Status must be Failed"
        assert result.get().isError() == expectedIsError
        assert result.get().event.name == event.name
        assert result.get().event.format == event.format
        assert result.get().event.destination == event.destination
        assert result.get().event.domain == event.domain
        assert result.get().event.domainModelId == event.domainModelId
        assert result.get().event.status == expectedStatus

        cleanup:
        executorService.shutdown()
        where:
        event                                       | interactor                            | expectedStatus | expectedIsError
        an_event_with("12345", 0, READY_TO_PUBLISH) | new DummyInteractor.ErrorInteractor() | FAILED         | true
        an_event_with("12345", 0, READY_TO_PUBLISH) | new DummyInteractor.NoAckInteractor() | PRODUCED       | false
    }


    def "Process Sync Egress Event:: valid event with mapping"() {
        given:
        EgressEventProcessor processor = Spy(IdempotentEgressEventProcessor, constructorArgs: [eventTracer]) {
            getInteractor() >> interactor
            eventTracer.trace(_ as EgressEvent) >> { EgressEvent _event ->
                return _event
            }
        }
        when:
        EventResponse response = processor.process(event)
        then:
        assert response != null
        assert response.isSuccess() == success
        if (response.response != null) {
            assert response.getResponse().asType(expectedClass) != null
        }
        assert response.event.status == expectedStatus
        where:
        event                                                  | interactor                                      | expectedStatus | expectedClass             | success
        event_fms_order_pfo()                                  | new DummyInteractor.ACKInteractorWithFmsOrder() | PRODUCED       | FmsOrderValueObject.class | true
        an_event_with("12345", 0, READY_TO_PUBLISH)            | new DummyInteractor.ACKInteractor()             | PRODUCED       | String.class              | true
        an_event_with("12345", 0, READY_TO_PUBLISH)            | new DummyInteractor.ErrorInteractor()           | FAILED         | null                      | false
        an_event_with_audit_with("12345", 0, READY_TO_PUBLISH) | new DummyInteractor.ErrorInteractor()           | ERROR          | null                      | false
    }

/**
 * The below test shows the async nature of the event processing, if enabled the test may fail or succeed based on whether the child thread finishes before
 * the parent thread or not. So this test is Ignored purposefully due to its nondeterministic nature.
 */
    @Ignore
    def "Process EgressEvent: Verify if processing is async"() {
        given:
        EgressEvent event = an_event_with("12345", 0, READY_TO_PUBLISH)
        EgressEventProcessor processor = Spy(IdempotentEgressEventProcessor, constructorArgs: [eventTracer]) {
            getInteractor() >> new DummyInteractor.NoAckInteractor()
            eventTracer.trace(_ as EgressEvent) >> { EgressEvent _event ->
                return _event
            }
        }
        when:
        processor.processAsync(event)
        then:
        1 * processor.process(_ as ExecutorService, _ as EgressEvent, _ as Consumer<EgressEvent>)
        assert event.status == READY_TO_PUBLISH
    }
}
