package com.walmart.marketplace.infrastructure.gateway.jms.embedded


import org.springframework.jms.core.JmsTemplate
import spock.lang.Specification

class MarketPlaceDomainEventPublisherSpec extends Specification {

    MarketPlaceDomainEventPublisher marketPlaceDomainEventPublisher
    JmsTemplate jmsTemplate = Mock()

    def setup() {
        marketPlaceDomainEventPublisher = new MarketPlaceDomainEventPublisher("jmsTemplate": jmsTemplate)
    }

    def "dependency check"() {
        when:
        JmsTemplate fetchedJmsTemplate = marketPlaceDomainEventPublisher.getJmsTemplate()

        then:
        fetchedJmsTemplate == jmsTemplate
    }
}
