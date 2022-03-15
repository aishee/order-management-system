package com.walmart.common.domain.event.processing


import spock.lang.Specification

import java.util.concurrent.ExecutorService

class EgressEventProcessorInterfaceTest extends Specification {
    ExecutorService executorService = Mock()
    Interactor interactor = Mock()
    EventResponse eventResponse = Mock()

    def "Test ProcessAsync"() {
        given:
        EgressEventProcessor egressEventProcessor = Spy(EgressEventProcessor) {
            getExecutorService() >> executorService
            getInteractor() >> interactor
            process(_) >> eventResponse
        }
        EgressEvent.Builder builder = new EgressEvent.Builder("name", "description")
        EgressEvent egressEvent = new EgressEvent(builder)
        when:
        egressEventProcessor.processAsync(egressEvent)
        then:
        noExceptionThrown()
    }

    def "Test Process With EgressEvent"() {
        given:
        EgressEvent.Builder builder = new EgressEvent.Builder("name", "description")
        EgressEvent egressEvent = new EgressEvent(builder)
        EgressEventProcessor egressEventProcessor1 = Spy(EgressEventProcessor) {
            getExecutorService() >> executorService
            getInteractor() >> interactor
            execute(_) >> eventResponse
        }
        when:
        egressEventProcessor1.process(egressEvent)
        then:
        noExceptionThrown()
    }
}
