package com.walmart.common.domain.event.processing


import spock.lang.Specification

class EgressEventTracerInterfaceTest extends Specification {
    EgressEventTracer egressEventTracer
    EgressEvent egressEvent

    def setup() {
        EgressEvent.Builder builder = new EgressEvent.Builder("name", "description")
        egressEvent = new EgressEvent(builder)
        egressEventTracer = Spy(EgressEventTracer) {
            get(_) >> { _ ->
                return egressEvent
            }

            save(_) >> { _ ->
                return egressEvent
            }
        }
    }

    def "Test trace with Null Event"() {
        when:
        egressEventTracer.trace(null)
        then:
        thrown(IllegalArgumentException)
    }

    def "Test trace success"() {
        given:
        when:
        EgressEvent event = egressEventTracer.trace(egressEvent)
        then:
        event.getName() == "name"
    }

    def "Test trace with event marked as error"() {
        given:
        EgressEvent.Builder builder = new EgressEvent.Builder("name", "description")
        EgressEvent egressEvent1 = new EgressEvent(builder)
        egressEvent1.markAsError()
        EgressEventTracer egressEventTracer1 = Spy(EgressEventTracer) {
            get(_) >> { _ ->
                return egressEvent1
            }

            save(_) >> { _ ->
                return egressEvent1
            }
        }
        when:
        egressEventTracer1.trace(egressEvent1)
        then:
        thrown(IllegalStateException)
    }

    def "Test trace with SavedCopy Failed within Retries"() {
        given:
        EgressEvent.Builder builder = new EgressEvent.Builder("name", "description")
        builder = builder.withRetries(2)
        EgressEvent egressEvent1 = new EgressEvent(builder)
        egressEvent1.markAsFailed()
        EgressEventTracer egressEventTracer1 = Spy(EgressEventTracer) {
            get(_) >> { _ ->
                return egressEvent1
            }

            save(_) >> { _ ->
                return egressEvent1
            }

            maxRetries() >> 5
        }
        when:
        EgressEvent event = egressEventTracer1.trace(egressEvent1)
        then:
        event.getName() == "name"
        event.getDescription() == "description"
    }

    def "Test trace with SavedCopy Failed exceeding Retries"() {
        given:
        EgressEvent.Builder builder = new EgressEvent.Builder("name", "description")
        builder = builder.withRetries(10)
        EgressEvent egressEvent1 = new EgressEvent(builder)
        egressEvent1.markAsFailed()
        EgressEventTracer egressEventTracer1 = Spy(EgressEventTracer) {
            get(_) >> { _ ->
                return egressEvent1
            }

            save(_) >> { _ ->
                return egressEvent1
            }

            maxRetries() >> 5
        }
        when:
        EgressEvent event = egressEventTracer1.trace(egressEvent1)
        then:
        event.markedAsError()
    }

    def "Test trace with Null SavedCopy"() {
        given:
        EgressEvent.Builder builder = new EgressEvent.Builder("name", "description")
        builder = builder.withRetries(10)
        EgressEvent egressEvent1 = new EgressEvent(builder)
        egressEvent1.markAsFailed()
        EgressEventTracer egressEventTracer1 = Spy(EgressEventTracer) {
            get(_) >> { _ ->
                return null
            }

            save(_) >> { _ ->
                return null
            }

            maxRetries() >> 5
        }
        when:
        EgressEvent event = egressEventTracer1.trace(egressEvent1)
        then:
        event == null
    }
}
