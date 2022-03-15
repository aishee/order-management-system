package com.walmart.fms.kafka.processors

import com.walmart.common.metrics.MetricService
import com.walmart.fms.domain.error.exception.FMSBadRequestException
import com.walmart.fms.domain.error.exception.FMSThirdPartyException
import com.walmart.fms.eventprocessors.FmsCancelledCommandService
import com.walmart.fms.integration.config.FMSKafkaConsumersConfig
import com.walmart.fms.integration.config.KafkaConfig
import com.walmart.fms.integration.config.KafkaConsumerConfig
import com.walmart.fms.integration.config.ReactiveKafkaConsumerFactory
import com.walmart.fms.integration.kafka.processors.GIFCancelOrderConsumer
import com.walmart.fms.integration.xml.beans.cfo.CancelFulfillmentOrderRequest
import com.walmart.fms.kafka.GIFErrorQueuePublisher
import com.walmart.fms.order.aggregateroot.FmsOrder
import com.walmart.oms.domain.error.exception.OMSThirdPartyException
import com.walmart.util.JAXBContextUtil
import io.github.resilience4j.retry.Retry
import io.github.resilience4j.retry.RetryConfig
import io.github.resilience4j.retry.RetryRegistry
import org.apache.kafka.clients.consumer.ConsumerRecord
import reactor.core.publisher.Flux
import reactor.kafka.receiver.ReceiverOffset
import reactor.kafka.receiver.ReceiverRecord
import spock.lang.Specification

import javax.xml.bind.JAXBContext
import java.time.Duration

class GIFCancelOrderConsumerTest extends Specification {
    GIFCancelOrderConsumer gifCancelOrderConsumer = new GIFCancelOrderConsumer()
    FmsCancelledCommandService fmsCancelledCommandService = Mock()
    FMSKafkaConsumersConfig fmsKafkaConsumersConfig = Mock()
    private JAXBContext orderCancelJaxbContext
    private KafkaConsumerConfig kafkaConsumerConfig
    private String topic = "WMT.UKGR.FULFILLMENT.ORDER_STATUS_CANCEL"
    ReactiveKafkaConsumerFactory kafkaConsumerFactory = Mock()
    RetryRegistry retryRegistry = Mock()
    MetricService metricService = Mock()
    ReceiverOffset receiverOffset = Mock()
    Retry retry
    private final String KAFKA_RETRY_CONFIG = "GIFCANCELCONSUMER"
    GIFErrorQueuePublisher gifErrorQueuePublisher = Mock()

    def setup() {
        gifCancelOrderConsumer.fmsCancelledCommandService = fmsCancelledCommandService
        orderCancelJaxbContext = JAXBContextUtil.getJAXBContext(CancelFulfillmentOrderRequest.class)
        gifCancelOrderConsumer.@orderCancelJaxbContext = orderCancelJaxbContext
        gifCancelOrderConsumer.@fmsKafkaConsumersConfig = fmsKafkaConsumersConfig
        gifCancelOrderConsumer.reactiveKafkaConsumerFactory = kafkaConsumerFactory
        gifCancelOrderConsumer.retryRegistry = retryRegistry
        gifCancelOrderConsumer.metricService = metricService
        gifCancelOrderConsumer.@gifErrorQueuePublisher = gifErrorQueuePublisher
        RetryConfig retryConfig = RetryConfig.custom().maxAttempts(3)
                .waitDuration(Duration.ofMillis(500))
                .retryExceptions(FMSThirdPartyException.class, OMSThirdPartyException.class)
                .build()
        retry = RetryRegistry.of(retryConfig).retry(KAFKA_RETRY_CONFIG)
    }

    def testDLQPublishFailure() {
        given:
        kafkaConsumerConfig = new KafkaConsumerConfig()
        kafkaConsumerConfig.setEnabled(true)
        KafkaConfig kafkaConfig = new KafkaConfig()
        kafkaConfig.setTopic(topic)
        kafkaConfig.setBootStrapServers("bootstrap:servers")
        kafkaConsumerConfig.setInboundConsumerConfig(kafkaConfig)

        KafkaConfig failureKafkaConfig = new KafkaConfig()
        failureKafkaConfig.setBootStrapServers("bootstrap:failureServers");
        failureKafkaConfig.setTopic("testTopic")
        kafkaConsumerConfig.setFailureProducerConfig(failureKafkaConfig)

        String xmlString = this.getClass().getResource('/fms/processors/OrderCancelMsg.xml').text
        ConsumerRecord<String, String> consumerRecord =
                new ConsumerRecord<>(topic, 1, 123L, "key", xmlString)
        ReceiverRecord<String, String> receiverRecord = new ReceiverRecord<>(consumerRecord, receiverOffset)
        Flux<ReceiverRecord<String, String>> incomingFlux = Flux.just(receiverRecord).take(1)
        fmsCancelledCommandService.cancelOrder(_) >> { throw new FMSBadRequestException() }
        retryRegistry.retry(_) >> retry

        when:
        gifCancelOrderConsumer.init()

        then:
        1 * fmsCancelledCommandService.cancelOrder(_) >> { throw new FMSBadRequestException("message") }
        1 * fmsKafkaConsumersConfig.getOrderCancellationKafkaConsumerConfig() >> kafkaConsumerConfig
        1 * kafkaConsumerFactory.createReactiveKafkaConsumer(_, _) >> incomingFlux
        1 * metricService.recordPrimaryPortMetrics(_, _)
        1 * metricService.incrementCounterByType(_, _)
        1 * gifErrorQueuePublisher.publishMessageToCancelErrorQueue(_) >> Flux.just(false)
    }

    def testDLQPublishSuccess() {
        given:
        kafkaConsumerConfig = new KafkaConsumerConfig()
        kafkaConsumerConfig.setEnabled(true)
        KafkaConfig kafkaConfig = new KafkaConfig()
        kafkaConfig.setTopic(topic)
        kafkaConfig.setBootStrapServers("bootstrap:servers")
        kafkaConsumerConfig.setInboundConsumerConfig(kafkaConfig)

        KafkaConfig failureKafkaConfig = new KafkaConfig()
        failureKafkaConfig.setBootStrapServers("bootstrap:failureServers");
        failureKafkaConfig.setTopic("testTopic")
        kafkaConsumerConfig.setFailureProducerConfig(failureKafkaConfig)

        String xmlString = this.getClass().getResource('/fms/processors/OrderCancelMsg.xml').text
        ConsumerRecord<String, String> consumerRecord =
                new ConsumerRecord<>(topic, 1, 123L, "key", xmlString)
        ReceiverRecord<String, String> receiverRecord = new ReceiverRecord<>(consumerRecord, receiverOffset)
        Flux<ReceiverRecord<String, String>> incomingFlux = Flux.just(receiverRecord).take(1)
        fmsCancelledCommandService.cancelOrder(_) >> { throw new FMSBadRequestException() }
        retryRegistry.retry(_) >> retry

        when:
        gifCancelOrderConsumer.init()

        then:
        1 * fmsCancelledCommandService.cancelOrder(_) >> { throw new FMSBadRequestException("FAILED TO PUBLISH") }
        1 * fmsKafkaConsumersConfig.getOrderCancellationKafkaConsumerConfig() >> kafkaConsumerConfig
        1 * kafkaConsumerFactory.createReactiveKafkaConsumer(_, _) >> incomingFlux
        1 * metricService.recordPrimaryPortMetrics(_, _)
        1 * metricService.incrementCounterByType(_, _)
        1 * gifErrorQueuePublisher.publishMessageToCancelErrorQueue(_) >> Flux.just(true)
    }

    def testDisabledConsumer() {
        given:
        kafkaConsumerConfig = new KafkaConsumerConfig()
        kafkaConsumerConfig.setEnabled(false)

        when:
        gifCancelOrderConsumer.init()

        then:
        1 * fmsKafkaConsumersConfig.getOrderCancellationKafkaConsumerConfig() >> kafkaConsumerConfig
        noExceptionThrown()
    }

    def testInitializeSuccess() {
        given:
        kafkaConsumerConfig = new KafkaConsumerConfig()
        kafkaConsumerConfig.setEnabled(true)
        KafkaConfig kafkaConfig = new KafkaConfig()
        kafkaConfig.setTopic(topic)
        kafkaConfig.setBootStrapServers("bootstrap:servers")
        kafkaConsumerConfig.setInboundConsumerConfig(kafkaConfig)
        String xmlString = this.getClass().getResource('/fms/processors/OrderCancelMsg.xml').text
        ConsumerRecord<String, String> consumerRecord = new ConsumerRecord<>(topic, 1, 123L, "key", xmlString)
        ReceiverRecord<String, String> receiverRecord = new ReceiverRecord<>(consumerRecord, receiverOffset)

        Flux<ReceiverRecord<String, String>> incomingFlux = Flux.just(receiverRecord).take(1)
        FmsOrder fmsOrder = new FmsOrder()

        when:
        gifCancelOrderConsumer.initialize(kafkaConsumerConfig)

        then:
        2 * retryRegistry.retry(_) >> retry
        1 * kafkaConsumerFactory.createReactiveKafkaConsumer(_, _) >> incomingFlux
        1 * fmsCancelledCommandService.cancelOrder(_) >> fmsOrder
        1 * metricService.recordPrimaryPortMetrics(_, _)
        0 * metricService.incrementCounterByType(_, _)
        noExceptionThrown()
    }

    def testInit() {
        given:
        kafkaConsumerConfig = new KafkaConsumerConfig()
        kafkaConsumerConfig.setEnabled(true)
        KafkaConfig kafkaConfig = new KafkaConfig()
        kafkaConfig.setTopic(topic)
        kafkaConfig.setBootStrapServers("bootstrap:servers")
        kafkaConsumerConfig.setInboundConsumerConfig(kafkaConfig)
        String xmlString = this.getClass().getResource('/fms/processors/OrderCancelMsg.xml').text
        ConsumerRecord<String, String> consumerRecord = new ConsumerRecord<>(topic, 1, 123L, "key", xmlString)
        ReceiverRecord<String, String> receiverRecord = new ReceiverRecord<>(consumerRecord, receiverOffset)

        Flux<ReceiverRecord<String, String>> incomingFlux = Flux.just(receiverRecord).take(1)
        FmsOrder fmsOrder = new FmsOrder()
        retryRegistry.retry(_) >> retry

        when:
        gifCancelOrderConsumer.init()

        then:
        1 * fmsKafkaConsumersConfig.getOrderCancellationKafkaConsumerConfig() >> kafkaConsumerConfig
        1 * kafkaConsumerFactory.createReactiveKafkaConsumer(_, _) >> incomingFlux
        1 * fmsCancelledCommandService.cancelOrder(_) >> fmsOrder
        noExceptionThrown()
    }

    def testInitWithJAXBException() {
        given:
        kafkaConsumerConfig = new KafkaConsumerConfig()
        kafkaConsumerConfig.setEnabled(true)
        KafkaConfig kafkaConfig = new KafkaConfig()
        kafkaConfig.setTopic(topic)
        kafkaConfig.setBootStrapServers("bootstrap:servers")
        kafkaConsumerConfig.setInboundConsumerConfig(kafkaConfig)
        String xmlString = this.getClass().getResource('/fms/processors/OrderCancelJaxbError.xml').text
        ConsumerRecord<String, String> consumerRecord =
                new ConsumerRecord<>(topic, 1, 123L, "key", xmlString)
        ReceiverRecord<String, String> receiverRecord = new ReceiverRecord<>(consumerRecord, receiverOffset)

        Flux<ReceiverRecord<String, String>> incomingFlux = Flux.just(receiverRecord).take(1)
        retryRegistry.retry(_) >> retry

        when:
        gifCancelOrderConsumer.init()

        then:
        1 * fmsKafkaConsumersConfig.getOrderCancellationKafkaConsumerConfig() >> kafkaConsumerConfig
        1 * kafkaConsumerFactory.createReactiveKafkaConsumer(_, _) >> incomingFlux
        noExceptionThrown()
    }

    def testInitWithFmsBadRequestException() {
        given:
        kafkaConsumerConfig = new KafkaConsumerConfig()
        kafkaConsumerConfig.setEnabled(true)
        KafkaConfig kafkaConfig = new KafkaConfig()
        kafkaConfig.setTopic(topic)
        kafkaConfig.setBootStrapServers("bootstrap:servers")
        kafkaConsumerConfig.setInboundConsumerConfig(kafkaConfig)
        String xmlString = this.getClass().getResource('/fms/processors/OrderCancelMsg.xml').text
        ConsumerRecord<String, String> consumerRecord =
                new ConsumerRecord<>(topic, 1, 123L, "key", xmlString)
        ReceiverRecord<String, String> receiverRecord = new ReceiverRecord<>(consumerRecord, receiverOffset)
        Flux<ReceiverRecord<String, String>> incomingFlux = Flux.just(receiverRecord).take(1)
        fmsCancelledCommandService.cancelOrder(_) >> { throw new FMSBadRequestException() }
        retryRegistry.retry(_) >> retry


        when:
        gifCancelOrderConsumer.init()

        then:
        1 * fmsCancelledCommandService.cancelOrder(_) >> { throw new FMSBadRequestException() }
        1 * fmsKafkaConsumersConfig.getOrderCancellationKafkaConsumerConfig() >> kafkaConsumerConfig
        1 * kafkaConsumerFactory.createReactiveKafkaConsumer(_, _) >> incomingFlux
        1 * metricService.recordPrimaryPortMetrics(_, _)
        1 * metricService.incrementCounterByType(_, _)
    }

    def testInitWithOnError() {
        given:
        kafkaConsumerConfig = new KafkaConsumerConfig()
        kafkaConsumerConfig.setEnabled(true)
        KafkaConfig kafkaConfig = new KafkaConfig()
        kafkaConfig.setTopic(topic)
        kafkaConfig.setBootStrapServers("bootstrap:servers")
        kafkaConsumerConfig.setInboundConsumerConfig(kafkaConfig)
        String xmlString = this.getClass().getResource('/fms/processors/OrderCancelMsg.xml').text
        ConsumerRecord<String, String> consumerRecord =
                new ConsumerRecord<>(topic, 1, 123L, "key", xmlString)
        ReceiverRecord<String, String> receiverRecord = new ReceiverRecord<>(consumerRecord, receiverOffset)
        Flux<ReceiverRecord<String, String>> incomingFlux = Flux.just(receiverRecord).take(1)
        fmsCancelledCommandService.cancelOrder(_) >> { throw new FMSBadRequestException() }
        retryRegistry.retry(_) >> retry


        when:
        gifCancelOrderConsumer.init()

        then:
        1 * fmsKafkaConsumersConfig.getOrderCancellationKafkaConsumerConfig() >> kafkaConsumerConfig
        1 * kafkaConsumerFactory.createReactiveKafkaConsumer(_, _) >> incomingFlux
        1 * metricService.incrementCounterByType(_, _) >> { throw new Exception() }
    }


    def testInitWithExceptionForRetry() {
        given:
        kafkaConsumerConfig = new KafkaConsumerConfig()
        kafkaConsumerConfig.setEnabled(true)
        KafkaConfig kafkaConfig = new KafkaConfig()
        kafkaConfig.setTopic(topic)
        kafkaConfig.setBootStrapServers("bootstrap:servers")
        kafkaConsumerConfig.setInboundConsumerConfig(kafkaConfig)
        String xmlString = this.getClass().getResource('/fms/processors/OrderCancelMsg.xml').text
        ConsumerRecord<String, String> consumerRecord = new ConsumerRecord<>(topic, 1, 123L, "key", xmlString)
        ReceiverRecord<String, String> receiverRecord = new ReceiverRecord<>(consumerRecord, receiverOffset)

        Flux<ReceiverRecord<String, String>> incomingFlux = Flux.just(receiverRecord).take(1)
        FmsOrder fmsOrder = new FmsOrder()
        retryRegistry.retry(_) >> retry

        when:
        gifCancelOrderConsumer.init()

        then:
        1 * fmsKafkaConsumersConfig.getOrderCancellationKafkaConsumerConfig() >> kafkaConsumerConfig
        1 * kafkaConsumerFactory.createReactiveKafkaConsumer(_, _) >> incomingFlux
        3 * fmsCancelledCommandService.cancelOrder(_) >> { throw new Exception() }
        noExceptionThrown()
    }

    def testInitWithNullOffset() {
        given:
        kafkaConsumerConfig = new KafkaConsumerConfig()
        kafkaConsumerConfig.setEnabled(true)
        KafkaConfig kafkaConfig = new KafkaConfig()
        kafkaConfig.setTopic(topic)
        kafkaConfig.setBootStrapServers("bootstrap:servers")
        kafkaConsumerConfig.setInboundConsumerConfig(kafkaConfig)
        String xmlString = this.getClass().getResource('/fms/processors/OrderCancelMsg.xml').text
        ConsumerRecord<String, String> consumerRecord = new ConsumerRecord<>(topic, 1, 123L, "key", xmlString)
        ReceiverRecord<String, String> receiverRecord = new ReceiverRecord<>(consumerRecord, null)

        Flux<ReceiverRecord<String, String>> incomingFlux = Flux.just(receiverRecord).take(1)
        FmsOrder fmsOrder = new FmsOrder()
        retryRegistry.retry(_) >> retry

        when:
        gifCancelOrderConsumer.init()

        then:
        1 * fmsKafkaConsumersConfig.getOrderCancellationKafkaConsumerConfig() >> kafkaConsumerConfig
        1 * kafkaConsumerFactory.createReactiveKafkaConsumer(_, _) >> incomingFlux
        1 * fmsCancelledCommandService.cancelOrder(_) >> fmsOrder
        noExceptionThrown()
    }


    def testCancelOrderAcceptSuccess() {
        given:
        String xmlString = this.getClass().getResource('/fms/processors/OrderCancelMsg.xml').text
        ConsumerRecord<String, String> consumerRecord = new ConsumerRecord<>(topic, 1, 123L, "key", xmlString)
        ReceiverRecord<String, String> receiverRecord = new ReceiverRecord<>(consumerRecord, null)
        FmsOrder fmsOrder = new FmsOrder()

        when:
        gifCancelOrderConsumer.accept(receiverRecord)

        then:
        1 * fmsCancelledCommandService.cancelOrder(_) >> fmsOrder
        noExceptionThrown()
    }

    def testCancelOrderAcceptFMSBadRequestException() {
        given:
        String xmlString = this.getClass().getResource('/fms/processors/OrderCancelMsg.xml').text
        String topic = "WMT.UKGR.FULFILLMENT.ORDER_STATUS_CANCEL"
        ConsumerRecord<String, String> consumerRecord = new ConsumerRecord<>(topic, 1, 123L, "key", xmlString)
        ReceiverRecord<String, String> receiverRecord = new ReceiverRecord<>(consumerRecord, null)
        fmsCancelledCommandService.cancelOrder(_) >> { throw new FMSBadRequestException() }

        when:
        gifCancelOrderConsumer.accept(receiverRecord)

        then:
        thrown(FMSBadRequestException)
    }
}
