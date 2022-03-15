package com.walmart.fms.infrastructure.jms.embedded

import org.springframework.jms.core.JmsTemplate
import spock.lang.Specification

class FmsDomainEventPublisherSpec extends Specification {

    FmsDomainEventPublisher fmsDomainEventPublisher
    JmsTemplate jmsTemplate = Mock()

    def setup() {
        fmsDomainEventPublisher = new FmsDomainEventPublisher("jmsTemplate": jmsTemplate)
    }

    def "dependency check"() {
        when:
        JmsTemplate fetchedJmsTemplate = fmsDomainEventPublisher.getJmsTemplate()

        then:
        fetchedJmsTemplate == jmsTemplate
    }

}
