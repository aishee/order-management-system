package com.walmart.common.domain.event

import com.walmart.common.domain.event.processing.EventGeneratorServiceImpl
import com.walmart.marketplace.domain.event.messages.MarketPlaceOrderCancelMessage
import org.springframework.context.ApplicationEventPublisher
import spock.lang.Specification

class EventGeneratorServiceImplTest extends Specification {
    EventGeneratorServiceImpl eventGeneratorServiceImpl;
    ApplicationEventPublisher applicationEventPublisher = Mock()

    def setup() {
        eventGeneratorServiceImpl = new EventGeneratorServiceImpl(applicationEventPublisher);
    }

    def "Create a domain message and test event is published successfully"() {
        MarketPlaceOrderCancelMessage message = new MarketPlaceOrderCancelMessage();
        when:
        eventGeneratorServiceImpl.publishApplicationEvent(message);
        then:
        noExceptionThrown()
    }
}
