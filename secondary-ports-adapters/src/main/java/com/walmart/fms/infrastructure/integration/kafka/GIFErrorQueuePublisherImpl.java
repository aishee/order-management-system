package com.walmart.fms.infrastructure.integration.kafka;

import com.walmart.common.infrastructure.integration.kafka.AbstractKafkaProducer;
import com.walmart.fms.infrastructure.integration.kafka.config.FmsKafkaProducerConfig;
import com.walmart.fms.kafka.GIFErrorQueuePublisher;
import java.util.Properties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

@Service
@Slf4j
public class GIFErrorQueuePublisherImpl extends AbstractKafkaProducer
    implements GIFErrorQueuePublisher {

  private FmsKafkaProducerConfig fmsKafkaProducerConfig;

  @Autowired
  protected GIFErrorQueuePublisherImpl(FmsKafkaProducerConfig fmsKafkaProducerConfig) {
    super(fmsKafkaProducerConfig.getNumThreads());
    this.fmsKafkaProducerConfig = fmsKafkaProducerConfig;
    Properties properties = fmsKafkaProducerConfig.getGifErrorProducerProperties();
    this.kafkaSender = createProducer(properties);
  }

  @Override
  public Flux<Boolean> publishMessageToCancelErrorQueue(String msg) {
    String topic = fmsKafkaProducerConfig.getGifOrderCancelErrorProducerTopic();
    return sendOutboundAsyncMessage(topic, msg);
  }

  @Override
  public Flux<Boolean> publishMessageToDeliverErrorQueue(String msg) {
    String topic = fmsKafkaProducerConfig.getGifOrderDeliverErrorProducerTopic();
    return sendOutboundAsyncMessage(topic, msg);
  }

  @Override
  public Flux<Boolean> publishMessageToUpdateErrorQueue(String msg) {
    String topic = fmsKafkaProducerConfig.getGifOrderUpdateErrorProducerTopic();
    return sendOutboundAsyncMessage(topic, msg);
  }
}
