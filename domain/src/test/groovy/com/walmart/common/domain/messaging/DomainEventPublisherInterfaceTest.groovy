package com.walmart.common.domain.messaging

import com.walmart.common.domain.messaging.exception.DomainEventPublishingException
import org.springframework.jms.core.JmsTemplate
import spock.lang.Specification

class DomainEventPublisherInterfaceTest extends Specification {

    JmsTemplate jmsTemplate = Mock()
    String destination = "destination"

    def "Test Publish Success"() {
        given:
        DomainEvent domainEvent = Mock()
        DomainEventPublisher domainEventPublisher = Spy(DomainEventPublisher) {
            getJmsTemplate() >> jmsTemplate
        }
        when:
        domainEventPublisher.publish(domainEvent, destination)
        then:
        noExceptionThrown()
    }

    def "Test Publish with DomainEventPublishingException"() {
        given:
        DomainEvent domainEvent = Mock()
        DomainEventPublisher domainEventPublisher = Spy(DomainEventPublisher) {
            getJmsTemplate() >> { throw new DomainEventPublishingException("exception") }
        }
        when:
        domainEventPublisher.publish(domainEvent, destination)
        then:
        thrown(DomainEventPublishingException)
    }

    def "Test Publish with throwable DomainEventPublishingException"() {
        given:
        DomainEvent domainEvent = Mock()
        Throwable throwable = new Throwable("EXCEPTION")
        DomainEventPublisher domainEventPublisher = Spy(DomainEventPublisher) {
            getJmsTemplate() >> { throw new DomainEventPublishingException(throwable) }
        }
        when:
        domainEventPublisher.publish(domainEvent, destination)
        then:
        thrown(DomainEventPublishingException)
    }
}
