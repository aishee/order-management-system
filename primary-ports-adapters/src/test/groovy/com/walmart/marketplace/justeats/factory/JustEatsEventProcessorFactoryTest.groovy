package com.walmart.marketplace.justeats.factory

import com.walmart.marketplace.justeats.processors.JustEatsOrderNotifyEventProcessor
import com.walmart.marketplace.justeats.processors.JustEatsWebHookEventProcessor
import spock.lang.Specification

class JustEatsEventProcessorFactoryTest extends Specification {

    JustEatsOrderNotifyEventProcessor justEatsOrderNotifyEventProcessor = Mock()

    JustEatsEventProcessorFactory justEatsEventProcessorFactory

    def setup() {
        justEatsEventProcessorFactory = new JustEatsEventProcessorFactory(justEatsOrderNotifyEventProcessor)
    }

    def factoryOrderNotify() {
        when:
        JustEatsWebHookEventProcessor justEatsWebHookEventProcessor = justEatsEventProcessorFactory.getJustEatsWebHookEventProcessor()

        then:
        justEatsWebHookEventProcessor == (justEatsOrderNotifyEventProcessor)
    }

}
