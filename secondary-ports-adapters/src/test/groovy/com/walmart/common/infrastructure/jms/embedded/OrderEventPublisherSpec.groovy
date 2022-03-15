package com.walmart.common.infrastructure.jms.embedded


import org.springframework.jms.core.JmsTemplate
import spock.lang.Specification

class OrderEventPublisherSpec extends Specification {

    OrderEventPublisher orderEventPublisher
    JmsTemplate jmsTemplate = Mock()

    def setup() {
        orderEventPublisher = new OrderEventPublisher("jmsTemplate": jmsTemplate)
    }

    def "dependency check"() {
        when:
        JmsTemplate fetchedJmsTemplate = orderEventPublisher.getJmsTemplate()

        then:
        fetchedJmsTemplate == jmsTemplate
    }
}
