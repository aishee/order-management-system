package com.walmart.common.infrastructure.config

import io.github.resilience4j.retry.Retry
import io.github.resilience4j.retry.RetryRegistry
import spock.lang.Specification

class RetryLoggingListenerConfigSpec extends Specification {

    RetryLoggingListenerConfig retryLoggingListenerConfig
    RetryRegistry retryRegistry = Mock()

    def setup() {

        retryLoggingListenerConfig = new RetryLoggingListenerConfig(
                retryRegistry: RetryRegistry.ofDefaults()
        )
    }

    def "All retry clients are Initialized Successfully"() {
        given:
        Retry pysipypRetry = retryLoggingListenerConfig
                .retryRegistry.retry(RetryLoggingListenerConfig.RetryClients.PYSIPYP.name())
        Retry taxRetry = retryLoggingListenerConfig
                .retryRegistry.retry(RetryLoggingListenerConfig.RetryClients.TAX.name())
        Retry iroRetry = retryLoggingListenerConfig
                .retryRegistry.retry(RetryLoggingListenerConfig.RetryClients.IRO.name())
        Retry uberRetry = retryLoggingListenerConfig
                .retryRegistry.retry(RetryLoggingListenerConfig.RetryClients.UBER.name())

        when:
        retryLoggingListenerConfig.initialize()

        then:
        pysipypRetry != null
        pysipypRetry.getEventPublisher().hasConsumers() == true

        taxRetry != null
        taxRetry.getEventPublisher().hasConsumers() == true

        iroRetry != null
        taxRetry.getEventPublisher().hasConsumers() == true

        uberRetry != null
        uberRetry.getEventPublisher().hasConsumers() == true
    }
}
