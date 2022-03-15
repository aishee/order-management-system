package com.walmart.common.domain.event.processing


import spock.lang.Specification

class InteractorInterfaceTest extends Specification {

    EventResponse eventResponse = Mock()

    def "Test interact success"() {
        given:
        EgressEvent.Builder builder = new EgressEvent.Builder("name", "description")
        builder = builder.withMessage("testMessage")
        EgressEvent egressEvent = new EgressEvent(builder)
        Interactor interactor = Spy(Interactor) {
            call(_) >> eventResponse
        }
        when:
        interactor.interact(egressEvent)
        then:
        noExceptionThrown()
    }

    def "Test interact with Exception"() {
        given:
        EgressEvent.Builder builder = new EgressEvent.Builder("name", "description")
        builder = builder.withMessage("testMessage")
        EgressEvent egressEvent = new EgressEvent(builder)
        Interactor interactor = Spy(Interactor) {
            call(_) >> { throw new Exception("EXCEPTION") }
        }
        when:
        interactor.interact(egressEvent)
        then:
        thrown(Exception)
    }
}
