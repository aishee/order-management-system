package com.walmart.fms.infrastructure.integration.kafka

import com.walmart.fms.infrastructure.integration.kafka.config.FmsKafkaProducerConfig
import reactor.core.publisher.Flux
import reactor.kafka.sender.KafkaSender
import reactor.kafka.sender.SenderResult
import reactor.test.StepVerifier
import spock.lang.Specification

class GIFErrorQueuePublisherImplTest extends Specification {
    GIFErrorQueuePublisherImpl gifErrorQueuePublisher
    FmsKafkaProducerConfig fmsKafkaProducerConfig = Mock()
    KafkaSender<String, String> kafkaSender = Mock()
    SenderResult<String> senderResult = Mock()

    def setup() {
        Properties props = new Properties()
        String topic = "testTopic"
        props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer")
        props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer")
        props.put("topic", topic)
        props.put("bootstrap.servers", "bootstrap:serversDLQ")
        fmsKafkaProducerConfig.getNumThreads() >> 10
        fmsKafkaProducerConfig.getGifErrorProducerProperties() >> props
        gifErrorQueuePublisher = new GIFErrorQueuePublisherImpl(fmsKafkaProducerConfig)
        gifErrorQueuePublisher.@fmsKafkaProducerConfig = fmsKafkaProducerConfig
    }

    def testPublishCancelMessageSuccess() {
        given:
        gifErrorQueuePublisher.@kafkaSender = kafkaSender
        String topic = "testTopic"
        String msg = "testMsg"
        Flux<SenderResult<String>> senderResultFlux = Flux.just(senderResult)
        when:
        Flux<Boolean> response = gifErrorQueuePublisher.publishMessageToCancelErrorQueue(msg)
        then:
        1 * fmsKafkaProducerConfig.getGifOrderCancelErrorProducerTopic() >> topic
        StepVerifier.create(response).expectNext(true).verifyComplete()
        kafkaSender.send(_) >> senderResultFlux
        gifErrorQueuePublisher.close()
        noExceptionThrown()
    }

    def testPublishDeliverMessageSuccess() {
        given:
        gifErrorQueuePublisher.@kafkaSender = kafkaSender
        String topic = "testTopic"
        String msg = "testMsg"
        Flux<SenderResult<String>> senderResultFlux = Flux.just(senderResult)
        when:
        Flux<Boolean> response = gifErrorQueuePublisher.publishMessageToDeliverErrorQueue(msg)
        then:
        1 * fmsKafkaProducerConfig.getGifOrderDeliverErrorProducerTopic() >> topic
        StepVerifier.create(response).expectNext(true).verifyComplete()
        kafkaSender.send(_) >> senderResultFlux
        gifErrorQueuePublisher.close()
        noExceptionThrown()
    }

    def testPublishUpdateMessageSuccess() {
        given:
        gifErrorQueuePublisher.@kafkaSender = kafkaSender
        String topic = "testTopic"
        String msg = "testMsg"
        Flux<SenderResult<String>> senderResultFlux = Flux.just(senderResult)
        when:
        Flux<Boolean> response = gifErrorQueuePublisher.publishMessageToUpdateErrorQueue(msg)
        then:
        1 * fmsKafkaProducerConfig.getGifOrderUpdateErrorProducerTopic() >> topic
        StepVerifier.create(response).expectNext(true).verifyComplete()
        kafkaSender.send(_) >> senderResultFlux
        gifErrorQueuePublisher.close()
        noExceptionThrown()
    }

    def testCloseSender() {
        given:
        String topic = "testTopic"
        when:
        gifErrorQueuePublisher.close()
        then:
        noExceptionThrown()
    }
}
