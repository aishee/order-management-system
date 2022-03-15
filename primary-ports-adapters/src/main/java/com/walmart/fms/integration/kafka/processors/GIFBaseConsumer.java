package com.walmart.fms.integration.kafka.processors;

import com.walmart.common.domain.type.Domain;
import com.walmart.common.metrics.MetricConstants.MetricCounters;
import com.walmart.common.metrics.MetricService;
import com.walmart.fms.domain.error.ErrorType;
import com.walmart.fms.domain.error.exception.FMSBadRequestException;
import com.walmart.fms.domain.error.exception.FMSThirdPartyException;
import com.walmart.fms.integration.config.KafkaConsumerConfig;
import com.walmart.fms.integration.config.ReactiveKafkaConsumerFactory;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryRegistry;
import java.util.function.Supplier;
import javax.xml.bind.JAXBException;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import reactor.core.publisher.Flux;
import reactor.kafka.receiver.ReceiverRecord;

@Slf4j
public abstract class GIFBaseConsumer {

  private static final String FMS_CONSUMER_SUCCESS_RESPONSE_CODE = "SUCCESS";
  @Autowired protected ReactiveKafkaConsumerFactory reactiveKafkaConsumerFactory;
  @Autowired protected RetryRegistry retryRegistry;
  @Autowired protected MetricService metricService;

  public void initialize(KafkaConsumerConfig kafkaConsumerConfig) {
    if ((kafkaConsumerConfig == null) || !kafkaConsumerConfig.isEnabled()) {
      return;
    }

    addLoggerForRetry(retryRegistry.retry(getRetryConsumerName()));
    reactiveKafkaConsumerFactory
        // create reactive kafka receiver
        .createReactiveKafkaConsumer(kafkaConsumerConfig, String.class)
        // process message once it is received from kafka topic.
        .doOnNext(this::messageProcessingWithRetry)
        .onErrorContinue(
            (throwable, kafkaRecord) -> this.onError(throwable, kafkaRecord, kafkaConsumerConfig))
        .doOnError(
            exception ->
                log.error(
                    String.format("Exception while consuming message for %s = ", getClassName()),
                    exception))
        .subscribe();
  }

  private void messageProcessingWithRetry(ReceiverRecord<String, String> kafkaMessage) {
    Supplier<Void> acceptSupplier = () -> process(kafkaMessage);

    Supplier<Void> retryDecoratedSupplier =
        Retry.decorateSupplier(retryRegistry.retry(getRetryConsumerName()), acceptSupplier);

    retryDecoratedSupplier.get();
  }

  private Void process(ReceiverRecord<String, String> kafkaMessage) {
    String message = kafkaMessage.value();
    initMDC(getClassName(), Domain.FMS.getDomainName());
    String status = FMS_CONSUMER_SUCCESS_RESPONSE_CODE;
    long startTime = System.currentTimeMillis();
    try {
      accept(kafkaMessage);
    } catch (JAXBException jaxbException) {
      status = ErrorType.INVALID_REQUEST_EXCEPTION.name();
      log.error(
          String.format("%s Message Failed with invalid structure : %s", getClassName(), message),
          jaxbException);
      throw new FMSBadRequestException(jaxbException.getMessage());
    } catch (FMSBadRequestException fmsBadRequestException) {
      status = ErrorType.INVALID_REQUEST_EXCEPTION.name();
      log.error(
          String.format("%s Message Failed While Consuming With The  Error : ", getClassName()),
          fmsBadRequestException);
      throw fmsBadRequestException;
    } catch (Exception exception) {
      status = ErrorType.INTERNAL_SERVICE_EXCEPTION.name();
      log.error(
          String.format("%s Message Failed While Consuming With The  Error : ", getClassName()),
          exception);
      throw new FMSThirdPartyException(exception.getMessage());
    } finally {
      kafkaMessage.receiverOffset().commit();
      log.info(
          "action={} processing message completed , metricServices {} ",
          getClassName(),
          metricService.recordPrimaryPortMetrics(System.currentTimeMillis() - startTime, status));
      MDC.clear();
    }
    return null;
  }

  protected abstract void accept(ReceiverRecord<String, String> kafkaMessage) throws JAXBException;

  private void onError(
      Throwable throwable, Object kafkaRecord, KafkaConsumerConfig kafkaConsumerConfig) {
    metricService.incrementCounterByType(
        MetricCounters.PRIMARY_PORT_EXCEPTION_COUNTER.getCounter(),
        throwable.getClass().getSimpleName());
    log.error(
        String.format(
            "%s error in processing GIF message=%s error=", getClassName(), kafkaRecord.toString()),
        throwable);

    if (ConsumerRecord.class.isAssignableFrom(kafkaRecord.getClass())
        && (kafkaConsumerConfig.getFailureProducerConfig() != null)) {
      ConsumerRecord<String, String> consumerRecordForDLQ =
          (ConsumerRecord<String, String>) kafkaRecord;
      publishToErrorQueue(consumerRecordForDLQ.value()).subscribe();
    }
  }

  protected abstract Flux<Boolean> publishToErrorQueue(String msg);

  protected String getClassName() {
    return this.getClass().getSimpleName();
  }

  protected void addLoggerForRetry(@NonNull Retry retry) {
    String retryName = retry.getName();
    retry
        .getEventPublisher()
        .onError(
            event ->
                log.error(
                    "{} Retry: Error in event: {} last exception being :{}",
                    getClassName(),
                    retryName,
                    event.getLastThrowable()));
  }

  protected abstract String getRetryConsumerName();

  private void initMDC(String api, String domain) {
    MDC.put("api", api);
    MDC.put("domain", domain);
  }
}
