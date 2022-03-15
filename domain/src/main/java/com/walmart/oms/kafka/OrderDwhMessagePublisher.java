package com.walmart.oms.kafka;

import reactor.core.publisher.Flux;

public interface OrderDwhMessagePublisher {
  Flux<Boolean> publishMessage(String topic, String msg);
}
