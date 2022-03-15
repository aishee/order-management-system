package com.walmart.common.infrastructure.integration.kafka;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import java.util.Properties;
import java.util.concurrent.Executors;
import javax.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;
import reactor.kafka.sender.KafkaSender;
import reactor.kafka.sender.SenderOptions;
import reactor.kafka.sender.SenderRecord;
import reactor.kafka.sender.SenderResult;

@Slf4j
public abstract class AbstractKafkaProducer {

  protected KafkaSender<String, String> kafkaSender;
  private final Scheduler scheduler;

  protected AbstractKafkaProducer(int numThreads) {
    log.info("Initializing Scheduler for: {}", getClassName());
    scheduler =
        Schedulers.fromExecutor(
            Executors.newFixedThreadPool(
                numThreads,
                new ThreadFactoryBuilder().setNameFormat("KAFKA-PRODUCER-THREAD-%d").build()));
  }

  protected Flux<Boolean> sendOutboundAsyncMessage(String topic, String msg) {
    if (kafkaSender == null) {
      log.error(String.format("Kafka Sender is not initialized for topic=%s", topic));
      return Flux.just(false);
    }
    return sendMessage(kafkaSender, topic, msg)
        .onErrorContinue((throwable, kafkaRecord) -> this.onPublishFailure(topic, msg, throwable))
        .doOnError(
            (exception ->
                log.error(
                    String.format(
                        "Exception while Producing the message=%s on topic=%s with error = ",
                        msg, topic),
                    exception)))
        .map(r -> onPublishSuccess(topic, msg));
  }

  private Flux<SenderResult<Integer>> sendMessage(
      KafkaSender<String, String> kafkaSender, String topic, String message) {
    ProducerRecord<String, String> producerRecord = new ProducerRecord<>(topic, message);
    return kafkaSender
        .send(Flux.range(1, 1).map(i -> SenderRecord.create(producerRecord, i)))
        .subscribeOn(scheduler);
  }

  private boolean onPublishSuccess(String topic, String message) {
    log.info("Successfully published the message: {} to the topic: {}", message, topic);
    return true;
  }

  private void onPublishFailure(String topic, String message, Throwable throwable) {
    log.error(
        String.format(
            "Failed to publish the message= %s to topic= %s with error= ", message, topic),
        throwable);
  }

  protected KafkaSender<String, String> createProducer(Properties props) {
    SenderOptions<String, String> senderOptions = SenderOptions.create(props);
    log.info("Creating KafkaSender: {} ", getClassName());
    return KafkaSender.create(senderOptions);
  }

  /** Close the kafkaSender gracefully. */
  @PreDestroy
  public void close() {
    if (kafkaSender != null) {
      log.info("Closing the KafkaSender: {} Gracefully", getClassName());
      kafkaSender.close();
    }
  }

  protected String getClassName() {
    return this.getClass().getSimpleName();
  }
}
