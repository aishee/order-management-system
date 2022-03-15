package com.walmart.fms.infrastructure.integration.jms.gateway.store

import com.walmart.common.domain.event.processing.EgressEvent
import com.walmart.common.domain.event.processing.Interactor
import com.walmart.fms.infrastructure.integration.gateway.store.GifStoreGateway
import com.walmart.fms.infrastructure.integration.jms.config.JmsEndPointConfig
import com.walmart.fms.infrastructure.integration.jms.config.JmsProducerEndpointConfig
import com.walmart.fms.order.aggregateroot.FmsOrder
import com.walmart.fms.order.gateway.StoreEvents
import spock.lang.Specification

import java.util.function.Consumer

import static com.walmart.fms.FmsMockOrderFactory.give_me_a_valid_market_place_order
import static com.walmart.fms.mapper.StoreEventsMapperFactory.getMapper

class GifStoreGatewayTest extends Specification {

    GifStoreGateway gifStoreGateway
    JmsEndPointConfig orderDownloadEndpoint = new JmsEndPointConfig(endpointUrl: "direct.12345.queue.PFO")
    JmsEndPointConfig cancelOrderEndpoint = new JmsEndPointConfig(endpointUrl: "direct.12345.queue.CFO")
    Interactor camelJmsProducer = Mock()
    def config = Spy(JmsProducerEndpointConfig) {
        getgifOrderDownloadConfig() >> orderDownloadEndpoint
        getGifForceOrderCancelConfig() >> cancelOrderEndpoint
    }

    def setup() {
        gifStoreGateway = Spy(new GifStoreGateway(jmsProducerEndpointConfig: config, camelJmsProducer: camelJmsProducer))
    }

    def "Throw IllegalArgumentException when null fms order is passed for Confirmation"() {
        when:
        gifStoreGateway.sendOrderConfirmation(null)
        then:
        thrown(IllegalArgumentException)
    }

    def "Throw IllegalArgumentException when null fms order is passed for Cancellation"() {
        when:
        gifStoreGateway.sendOrderCancellation(null)
        then:
        thrown(IllegalArgumentException)
    }

    def "sendOrderConfirmation:: When valid order is passed ->  prepare EgressEvent and call publishEvent()"() {
        when:
        gifStoreGateway.sendOrderConfirmation(give_me_a_valid_market_place_order())
        then:
        1 * gifStoreGateway.processAsync(_ as EgressEvent) >> { EgressEvent _event ->
            _event != null
            _event.model != null
            _event.isMappingApplied() == null
            _event.name == StoreEvents.PFO.name()
            _event.description == GifStoreGateway.ORDER_CONFIRMATION_MESSAGE_TO_STORE
            _event.destination == orderDownloadEndpoint.endpointUrl
            _event.mappingFunction == getMapper(StoreEvents.PFO)
        }
    }

    def "sendOrderCancellation:: When valid order is passed -> prepare EgressEvent for cancellation and call publishEvent()"() {
        when:
        gifStoreGateway.sendOrderCancellation(give_me_a_valid_market_place_order())
        then:
        1 * gifStoreGateway.processAsync(_ as EgressEvent) >> { EgressEvent _event ->
            _event != null
            _event.model != null
            _event.isMappingApplied() == null
            _event.name == StoreEvents.UFO.name()
            _event.description == GifStoreGateway.ORDER_CANCELLATION_MESSAGE_TO_STORE
            _event.destination == cancelOrderEndpoint.endpointUrl
            _event.mappingFunction == getMapper(StoreEvents.UFO)

        }

    }

    def "maxPoolSize:: test"() {
        when:
        int maxPoolSize = gifStoreGateway.maxPoolSize()

        then:
        maxPoolSize == 10
    }

    def "getInteractor:: test"() {
        when:
        Interactor camelJmsProducer = gifStoreGateway.getInteractor()

        then:
        camelJmsProducer != null
    }

    def "getActionByCode:: test"() {
        given:
        String eventName = "UFO"

        when:
        Optional<Consumer<FmsOrder>> methodCodeMap = gifStoreGateway.getActionByCode(eventName)

        then:
        methodCodeMap.isPresent()
    }
}
