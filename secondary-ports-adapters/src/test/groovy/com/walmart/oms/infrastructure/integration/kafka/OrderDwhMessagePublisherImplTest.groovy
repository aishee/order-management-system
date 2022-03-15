package com.walmart.oms.infrastructure.integration.kafka

import com.walmart.oms.infrastructure.configuration.OmsKafkaProducerConfig
import reactor.core.publisher.Flux
import reactor.kafka.sender.KafkaSender
import reactor.kafka.sender.SenderResult
import reactor.test.StepVerifier
import spock.lang.Specification

class OrderDwhMessagePublisherImplTest extends Specification {
    OrderDwhMessagePublisherImpl dwhMessagePublisher
    KafkaSender<String, String> kafkaSender = Mock()
    SenderResult<String> senderResult = Mock()
    OmsKafkaProducerConfig omsKafkaProducerConfig = Mock()

    def setup() {
        Properties props = new Properties()
        String topic = "testTopic"
        props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer")
        props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer")
        props.put("topic", topic)
        props.put("bootstrap.servers", "bootstrap:servers1")
        omsKafkaProducerConfig.getNumThreads() >> 10
        omsKafkaProducerConfig.getDwhConfigProperties() >> props
        dwhMessagePublisher = new OrderDwhMessagePublisherImpl(omsKafkaProducerConfig)
    }

    def testPublishMessageSuccess() {
        given:
        dwhMessagePublisher.@kafkaSender = kafkaSender
        String topic = "testTopic"
        String msg = "testMsg"
        Flux<SenderResult<String>> senderResultFlux = Flux.just(senderResult)
        when:
        Flux<Boolean> response = dwhMessagePublisher.publishMessage(topic, msg)
        then:
        StepVerifier.create(response).expectNext(true).verifyComplete()
        kafkaSender.send(_) >> senderResultFlux
        dwhMessagePublisher.close()
        noExceptionThrown()
    }

    def testPublishMessageFailure() {
        given:
        dwhMessagePublisher.@kafkaSender = kafkaSender
        String topic = "testTopic"
        String msg = "testMsg"
        Exception ex = new Exception()
        Flux<SenderResult<String>> senderResultFlux = Flux.error(
                { -> ex })
        when:
        Flux<Boolean> response = dwhMessagePublisher.publishMessage(topic, msg)
        then:
        StepVerifier.create(response).expectError(ex as Class<? extends Throwable>).verify()
        1 * kafkaSender.send(_) >> senderResultFlux
        dwhMessagePublisher.close()
        noExceptionThrown()
    }

    def testNullKafkaSender() {
        given:
        String topic = "testTopic"
        String msg = "testMsg"
        dwhMessagePublisher.@kafkaSender = null
        when:
        Flux<Boolean> response = dwhMessagePublisher.publishMessage(topic, msg)
        then:
        StepVerifier.create(response).expectNext(false).verifyComplete()
        noExceptionThrown()
    }

    def testCloseSender() {
        given:
        String topic = "testTopic"
        dwhMessagePublisher.@kafkaSender = kafkaSender
        when:
        dwhMessagePublisher.close()
        then:
        noExceptionThrown()
    }
}
