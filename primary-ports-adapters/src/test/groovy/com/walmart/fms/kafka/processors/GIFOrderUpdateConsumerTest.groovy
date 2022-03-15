package com.walmart.fms.kafka.processors

import com.walmart.common.metrics.MetricService
import com.walmart.fms.domain.error.exception.FMSBadRequestException
import com.walmart.fms.domain.error.exception.FMSThirdPartyException
import com.walmart.fms.integration.config.FMSKafkaConsumersConfig
import com.walmart.fms.integration.config.KafkaConfig
import com.walmart.fms.integration.config.KafkaConsumerConfig
import com.walmart.fms.integration.config.ReactiveKafkaConsumerFactory
import com.walmart.fms.integration.kafka.processors.GIFOrderUpdateConsumer
import com.walmart.fms.integration.kafka.processors.orderupdateprocessors.OrderConfirmProcessor
import com.walmart.fms.integration.kafka.processors.orderupdateprocessors.OrderUpdateProcessorFactory
import com.walmart.fms.kafka.GIFErrorQueuePublisher
import com.walmart.fms.order.aggregateroot.FmsOrder
import com.walmart.oms.domain.error.exception.OMSThirdPartyException
import io.github.resilience4j.retry.Retry
import io.github.resilience4j.retry.RetryConfig
import io.github.resilience4j.retry.RetryRegistry
import org.apache.kafka.clients.consumer.ConsumerRecord
import reactor.core.publisher.Flux
import reactor.kafka.receiver.ReceiverOffset
import reactor.kafka.receiver.ReceiverRecord
import spock.lang.Specification

import java.time.Duration

class GIFOrderUpdateConsumerTest extends Specification {
    private OrderUpdateProcessorFactory orderUpdateProcessorFactory = Mock()
    GIFOrderUpdateConsumer gifOrderUpdateConsumer = new GIFOrderUpdateConsumer()
    FMSKafkaConsumersConfig fmsKafkaConsumersConfig = Mock()
    private KafkaConsumerConfig kafkaConsumerConfig
    private String topic = "WMT.UKGR.FULFILLMENT.ORDER_STATUS_UPDATES"
    OrderConfirmProcessor orderConfirmProcessor = Mock()
    ReactiveKafkaConsumerFactory kafkaConsumerFactory = Mock()
    RetryRegistry retryRegistry = Mock()
    MetricService metricService = Mock()
    ReceiverOffset receiverOffset = Mock()
    Retry retry
    private final String KAFKA_RETRY_CONFIG = "GIFUPDATECONSUMER"
    GIFErrorQueuePublisher gifErrorQueuePublisher = Mock()

    def setup() {
        gifOrderUpdateConsumer.orderUpdateProcessorFactory = orderUpdateProcessorFactory
        gifOrderUpdateConsumer.@fmsKafkaConsumersConfig = fmsKafkaConsumersConfig
        gifOrderUpdateConsumer.reactiveKafkaConsumerFactory = kafkaConsumerFactory
        gifOrderUpdateConsumer.retryRegistry = retryRegistry
        gifOrderUpdateConsumer.metricService = metricService
        RetryConfig retryConfig = RetryConfig.custom().maxAttempts(3)
                .waitDuration(Duration.ofMillis(500))
                .retryExceptions(FMSThirdPartyException.class, OMSThirdPartyException.class)
                .build()
        retry = RetryRegistry.of(retryConfig).retry(KAFKA_RETRY_CONFIG)
        gifOrderUpdateConsumer.@gifErrorQueuePublisher = gifErrorQueuePublisher
    }

    def testInit() {
        given:
        kafkaConsumerConfig = new KafkaConsumerConfig()
        kafkaConsumerConfig.setEnabled(true)
        KafkaConfig kafkaConfig = new KafkaConfig()
        kafkaConfig.setTopic(topic)
        kafkaConfig.setBootStrapServers("bootstrap:servers")
        kafkaConsumerConfig.setInboundConsumerConfig(kafkaConfig)
        String xmlString = this.getClass().getResource('/fms/processors/OrderUpdateMsg.xml').text
        ConsumerRecord<String, String> consumerRecord = new ConsumerRecord<>(topic, 1, 123L, "key", xmlString)
        ReceiverRecord<String, String> receiverRecord = new ReceiverRecord<>(consumerRecord, receiverOffset)
        Flux<ReceiverRecord<String, String>> incomingFlux = Flux.just(receiverRecord).take(1)
        FmsOrder fmsOrder = new FmsOrder()
        retryRegistry.retry(_) >> retry

        when:
        gifOrderUpdateConsumer.init()

        then:
        1 * fmsKafkaConsumersConfig.getOrderUpdatesKafkaConsumerConfig() >> kafkaConsumerConfig
        1 * kafkaConsumerFactory.createReactiveKafkaConsumer(_, _) >> incomingFlux
        noExceptionThrown()
    }

    def testPublishToErrorQueue() {
        given:
        String msg = "testMsg"
        when:
        gifOrderUpdateConsumer.publishToErrorQueue(msg)
        then:
        1 * gifErrorQueuePublisher.publishMessageToUpdateErrorQueue(_)
        noExceptionThrown()
    }

    def testAcceptSuccess() {
        given:
        String xmlString = this.getClass().getResource('/fms/processors/OrderUpdateMsg.xml').text
        ConsumerRecord<String, String> consumerRecord = new ConsumerRecord<>(topic, 1, 123L, "key", xmlString)
        ReceiverRecord<String, String> receiverRecord = new ReceiverRecord<>(consumerRecord, null)

        when:
        gifOrderUpdateConsumer.accept(receiverRecord)

        then:
        1 * orderUpdateProcessorFactory.getOrderUpdateEventProcessor(_) >> orderConfirmProcessor
    }

    def testAcceptNullHeaderError() {
        given:
        String xmlString = this.getClass().getResource('/fms/processors/OrderUpdateFailure.xml').text
        ConsumerRecord<String, String> consumerRecord = new ConsumerRecord<>(topic, 1, 123L, "key", xmlString)
        ReceiverRecord<String, String> receiverRecord = new ReceiverRecord<>(consumerRecord, null)

        when:
        gifOrderUpdateConsumer.accept(receiverRecord)

        then:
        0 * orderUpdateProcessorFactory.getOrderUpdateEventProcessor(_) >> orderConfirmProcessor
        thrown(FMSBadRequestException.class)
    }
}
