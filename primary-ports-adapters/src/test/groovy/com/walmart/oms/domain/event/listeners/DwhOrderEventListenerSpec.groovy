package com.walmart.oms.domain.event.listeners

import com.walmart.common.infrastructure.config.KafkaProducerConfig
import com.walmart.oms.domain.event.messages.DwhOrderEventMessage
import com.walmart.oms.infrastructure.configuration.OmsKafkaProducerConfig
import com.walmart.oms.kafka.OrderDwhMessagePublisher
import com.walmart.oms.order.gateway.orderservice.OrdersEvent
import reactor.core.publisher.Flux
import spock.lang.Specification

class DwhOrderEventListenerSpec extends Specification {
    OmsKafkaProducerConfig omsKafkaProducerConfig = Mock()
    OrderDwhMessagePublisher orderDwhMessagePublisher = Mock()
    DwhOrderEventListener dwhOrderEventListener

    def setup() {
        dwhOrderEventListener = new DwhOrderEventListener(
                "omsKafkaProducerConfig": omsKafkaProducerConfig,
                "orderDwhMessagePublisher": orderDwhMessagePublisher,
                )
    }

    def "test order dwh Order Event Listener listener"() {
        given:
        KafkaProducerConfig mock = new KafkaProducerConfig();
        mock.setAcks("all");
        mock.setBatchSize(10);
        mock.setBootStrapServers("mock1,mock2,mock3,mock4");
        mock.setTopic("mock_topic");
        mock.setCompressionType("lz4");
        mock.setBufferMemory(123);
        mock.setKeySerializer("mocked");
        mock.setValueSerializer("mocked");
        mock.setLingerMs(111);
        mock.setRetries(1);

        omsKafkaProducerConfig.getConfigForTopic(_ as String) >> { String _topicName ->
            return mock
        }
        DwhOrderEventMessage message = mockMessage()

        when:
        dwhOrderEventListener.listen(message)

        then:
        1 * orderDwhMessagePublisher.publishMessage(_ as String, _ as String) >> Flux.just(Boolean.TRUE)
    }

    def "test order dwh Order Event Listener listener producerConfig null"() {
        given:
        KafkaProducerConfig mock = new KafkaProducerConfig();
        mock.setAcks("all");
        mock.setBatchSize(10);
        mock.setBootStrapServers("mock1,mock2,mock3,mock4");
        mock.setTopic("mock_topic");
        mock.setCompressionType("lz4");
        mock.setBufferMemory(123);
        mock.setKeySerializer("mocked");
        mock.setValueSerializer("mocked");
        mock.setLingerMs(111);
        mock.setRetries(1);

        omsKafkaProducerConfig.getConfigForTopic(_ as String) >> null

        DwhOrderEventMessage message = mockMessage()

        when:
        dwhOrderEventListener.listen(message)

        then:
        0 * orderDwhMessagePublisher.publishMessage(_ as String, _ as String) >> Flux.just(Boolean.TRUE)
    }

    private static DwhOrderEventMessage mockMessage() {
        return DwhOrderEventMessage.builder().storeOrderId("1111")
                .omsOrderOrdersEvent(mockOmsOrderOrdersEvent()).build()
    }

    private static OrdersEvent<com.walmart.services.oms.order.common.model.OmsOrder> mockOmsOrderOrdersEvent() {
        return new OrdersEvent<com.walmart.services.oms.order.common.model.OmsOrder>()
    }
}
