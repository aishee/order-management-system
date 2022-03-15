package com.walmart.fms.kafka.processors

import com.walmart.common.metrics.MetricService
import com.walmart.fms.domain.error.exception.FMSBadRequestException
import com.walmart.fms.domain.error.exception.FMSThirdPartyException
import com.walmart.fms.eventprocessors.FmsDeliveredCommandService
import com.walmart.fms.integration.config.FMSKafkaConsumersConfig
import com.walmart.fms.integration.config.KafkaConfig
import com.walmart.fms.integration.config.KafkaConsumerConfig
import com.walmart.fms.integration.config.ReactiveKafkaConsumerFactory
import com.walmart.fms.integration.kafka.processors.GIFDeliveryOrderConsumer
import com.walmart.fms.integration.xml.beans.uods.UpdateOrderDispensedStatusRequest
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

class GIFDeliveryOrderConsumerTest extends Specification {
    FmsDeliveredCommandService fmsDeliveredCommandService = Mock()
    private JAXBContext orderUodsJaxbContext
    GIFDeliveryOrderConsumer gifDeliveryOrderConsumer = new GIFDeliveryOrderConsumer()
    private String topic = "WMT.UKGR.FULFILLMENT.ORDER_STATUS_DELIVERED"
    private KafkaConsumerConfig kafkaConsumerConfig
    FMSKafkaConsumersConfig fmsKafkaConsumersConfig = Mock()
    ReactiveKafkaConsumerFactory kafkaConsumerFactory = Mock()
    RetryRegistry retryRegistry = Mock()
    MetricService metricService = Mock()
    ReceiverOffset receiverOffset = Mock()
    Retry retry
    private final String KAFKA_RETRY_CONFIG = "GIFDELIVERYCONSUMER"
    GIFErrorQueuePublisher gifErrorQueuePublisher = Mock()

    def setup() {
        gifDeliveryOrderConsumer.fmsDeliveredCommandService = fmsDeliveredCommandService
        orderUodsJaxbContext = JAXBContextUtil.getJAXBContext(UpdateOrderDispensedStatusRequest.class)
        gifDeliveryOrderConsumer.@orderUodsJaxbContext = orderUodsJaxbContext
        gifDeliveryOrderConsumer.@fmsKafkaConsumersConfig = fmsKafkaConsumersConfig
        gifDeliveryOrderConsumer.reactiveKafkaConsumerFactory = kafkaConsumerFactory
        gifDeliveryOrderConsumer.retryRegistry = retryRegistry
        gifDeliveryOrderConsumer.metricService = metricService
        RetryConfig retryConfig = RetryConfig.custom().maxAttempts(3)
                .waitDuration(Duration.ofMillis(500))
                .retryExceptions(FMSThirdPartyException.class, OMSThirdPartyException.class)
                .build()
        retry = RetryRegistry.of(retryConfig).retry(KAFKA_RETRY_CONFIG)
        gifDeliveryOrderConsumer.@gifErrorQueuePublisher = gifErrorQueuePublisher
    }

    def testPublishToErrorQueue() {
        given:
        String msg = "testMsg"
        when:
        gifDeliveryOrderConsumer.publishToErrorQueue(msg)
        then:
        1 * gifErrorQueuePublisher.publishMessageToDeliverErrorQueue(_)
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
        String xmlString = this.getClass().getResource('/fms/processors/OrderDeliveredMsg.xml').text
        ConsumerRecord<String, String> consumerRecord = new ConsumerRecord<>(topic, 1, 123L, "key", xmlString)
        ReceiverRecord<String, String> receiverRecord = new ReceiverRecord<>(consumerRecord, receiverOffset)
        Flux<ReceiverRecord<String, String>> incomingFlux = Flux.just(receiverRecord).take(1)
        FmsOrder fmsOrder = new FmsOrder()
        retryRegistry.retry(_) >> retry

        when:
        gifDeliveryOrderConsumer.init()

        then:
        1 * fmsKafkaConsumersConfig.getOrderDeliveryKafkaConsumerConfig() >> kafkaConsumerConfig
        1 * kafkaConsumerFactory.createReactiveKafkaConsumer(_, _) >> incomingFlux
        1 * fmsDeliveredCommandService.deliverOrder(_) >> fmsOrder
        noExceptionThrown()
    }

    def testDeliveryOrderAccept() {
        given:
        String xmlString = this.getClass().getResource('/fms/processors/OrderDeliveredMsg.xml').text
        ConsumerRecord<String, String> consumerRecord = new ConsumerRecord<>(topic, 1, 123L, "key", xmlString)
        ReceiverRecord<String, String> receiverRecord = new ReceiverRecord<>(consumerRecord, null)
        FmsOrder fmsOrder = new FmsOrder()

        when:
        gifDeliveryOrderConsumer.accept(receiverRecord)

        then:
        1 * fmsDeliveredCommandService.deliverOrder(_) >> fmsOrder
        noExceptionThrown()
    }

    def testDeliveryOrderAcceptFMSBadRequestException() {
        given:
        String xmlString = this.getClass().getResource('/fms/processors/OrderDeliveredMsg.xml').text
        ConsumerRecord<String, String> consumerRecord = new ConsumerRecord<>(topic, 1, 123L, "key", xmlString)
        ReceiverRecord<String, String> receiverRecord = new ReceiverRecord<>(consumerRecord, null)
        fmsDeliveredCommandService.deliverOrder(_) >> { throw new FMSBadRequestException() }

        when:
        gifDeliveryOrderConsumer.accept(receiverRecord)

        then:
        thrown(FMSBadRequestException)
    }
}
