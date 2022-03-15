package com.walmart.common.infrastructure.config

import com.walmart.common.infrastructure.ApiAction
import io.github.resilience4j.circuitbreaker.CircuitBreaker
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry
import spock.lang.Specification

class ResiliencyLoggingListenerConfigTest extends Specification {

    ResiliencyLoggingListenerConfig resiliencyLoggingListenerConfig
    CircuitBreakerRegistry circuitBreakerRegistry = Mock()

    def setup() {

        resiliencyLoggingListenerConfig = new ResiliencyLoggingListenerConfig(
                circuitBreakerRegistry: CircuitBreakerRegistry.ofDefaults()
        )
    }

    def "All circuit breakers are Initialized Successfully"() {
        given:
        CircuitBreaker pysipypCircuitBreaker = resiliencyLoggingListenerConfig
                .circuitBreakerRegistry.circuitBreaker(ApiAction.CircuitBreaker.PYSIPYP.name())
        CircuitBreaker taxCircuitBreaker = resiliencyLoggingListenerConfig
                .circuitBreakerRegistry.circuitBreaker(ApiAction.CircuitBreaker.TAX.name())
        CircuitBreaker iroCircuitBreaker = resiliencyLoggingListenerConfig
                .circuitBreakerRegistry.circuitBreaker(ApiAction.CircuitBreaker.IRO.name())
        CircuitBreaker uberCartCircuitBreaker = resiliencyLoggingListenerConfig
                .circuitBreakerRegistry.circuitBreaker(ApiAction.CircuitBreaker.UBER_CART_CIRCUIT_BREAKER.name())

        CircuitBreaker uberOrderUpdateCircuitBreaker = resiliencyLoggingListenerConfig
                .circuitBreakerRegistry.circuitBreaker(ApiAction.CircuitBreaker.UBER_ORDER_UPDATE_CIRCUIT_BREAKER.name())

        CircuitBreaker uberBatchCircuitBreaker = resiliencyLoggingListenerConfig
                .circuitBreakerRegistry.circuitBreaker(ApiAction.CircuitBreaker.UBER_BATCH_CIRCUIT_BREAKER.name())


        when:
        resiliencyLoggingListenerConfig.initialize()

        then:
        pysipypCircuitBreaker != null
        pysipypCircuitBreaker.getEventPublisher().hasConsumers() == true

        taxCircuitBreaker != null
        taxCircuitBreaker.getEventPublisher().hasConsumers() == true

        iroCircuitBreaker != null
        iroCircuitBreaker.getEventPublisher().hasConsumers() == true

        uberCartCircuitBreaker != null
        uberCartCircuitBreaker.getEventPublisher().hasConsumers() == true

        uberOrderUpdateCircuitBreaker != null
        uberOrderUpdateCircuitBreaker.getEventPublisher().hasConsumers() == true

        uberBatchCircuitBreaker != null
        uberBatchCircuitBreaker.getEventPublisher().hasConsumers() == true
    }
}
