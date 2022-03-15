package com.walmart.oms.infrastructure.integration.kafka;

import com.walmart.common.infrastructure.integration.kafka.AbstractKafkaProducer;
import com.walmart.oms.infrastructure.configuration.OmsKafkaProducerConfig;
import com.walmart.oms.kafka.OrderDwhMessagePublisher;
import java.util.Properties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

@Service
public class OrderDwhMessagePublisherImpl extends AbstractKafkaProducer
    implements OrderDwhMessagePublisher {

  @Autowired
  public OrderDwhMessagePublisherImpl(OmsKafkaProducerConfig omsKafkaProducerConfig) {
    super(omsKafkaProducerConfig.getNumThreads());
    Properties properties = omsKafkaProducerConfig.getDwhConfigProperties();
    this.kafkaSender = createProducer(properties);
  }

  @Override
  public Flux<Boolean> publishMessage(String topic, String msg) {
    return sendOutboundAsyncMessage(topic, msg);
  }
}
