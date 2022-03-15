package com.walmart.fms.kafka;

import reactor.core.publisher.Flux;

public interface GIFErrorQueuePublisher {

  Flux<Boolean> publishMessageToCancelErrorQueue(String msg);

  Flux<Boolean> publishMessageToDeliverErrorQueue(String msg);

  Flux<Boolean> publishMessageToUpdateErrorQueue(String msg);
}
