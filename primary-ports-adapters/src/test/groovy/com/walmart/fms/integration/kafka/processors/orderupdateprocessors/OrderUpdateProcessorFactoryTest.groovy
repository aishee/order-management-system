package com.walmart.fms.integration.kafka.processors.orderupdateprocessors

import com.walmart.fms.domain.error.exception.FMSBadRequestException
import spock.lang.Specification

class OrderUpdateProcessorFactoryTest extends Specification {

    PickCompletedProcessor pickCompletedProcessor = Mock()
    PickStartedProcessor pickStartedProcessor = Mock()
    OrderConfirmProcessor orderConfirmProcessor = Mock()
    OrderUpdateProcessorFactory orderUpdateProcessorFactory

    def setup() {
        orderUpdateProcessorFactory = new OrderUpdateProcessorFactory(
                "pickCompletedProcessor": pickCompletedProcessor,
                "pickStartedProcessor": pickStartedProcessor,
                "orderConfirmProcessor": orderConfirmProcessor
        )
    }

    def "Factory class Pick Complete Header"() {
        when:
        GIFOrderUpdateEventProcessor gifOrderUpdateEventProcessor = orderUpdateProcessorFactory.getOrderUpdateEventProcessor("UpdateOrderFulfillmentStatus.updateOrderPickedStatus")

        then:
        gifOrderUpdateEventProcessor == pickCompletedProcessor
    }

    def "Factory class Pick Start Header"() {
        when:
        GIFOrderUpdateEventProcessor gifOrderUpdateEventProcessor = orderUpdateProcessorFactory.getOrderUpdateEventProcessor("UpdateOrderFulfillmentStatus.updateOrderPickingBeginStatus")

        then:
        gifOrderUpdateEventProcessor == pickStartedProcessor
    }

    def "Factory class Order Confirm Header"() {
        when:
        GIFOrderUpdateEventProcessor gifOrderUpdateEventProcessor = orderUpdateProcessorFactory.getOrderUpdateEventProcessor("UpdateOrderFulfillmentStatus.updateOrderFulfillmentBeginStatus")

        then:
        gifOrderUpdateEventProcessor == orderConfirmProcessor
    }

    def "Invalid Header"() {
        when:
        orderUpdateProcessorFactory.getOrderUpdateEventProcessor("Invalid Header")

        then:
        thrown FMSBadRequestException
    }
}
