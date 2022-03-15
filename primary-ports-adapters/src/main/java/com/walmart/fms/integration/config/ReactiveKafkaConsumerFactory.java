package com.walmart.fms.integration.config;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.config.SslConfigs;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import reactor.core.publisher.Flux;
import reactor.kafka.receiver.KafkaReceiver;
import reactor.kafka.receiver.ReceiverOptions;
import reactor.kafka.receiver.ReceiverRecord;

@Configuration
@Slf4j
@EnableKafka
public class ReactiveKafkaConsumerFactory {

  /**
   * Creates reactiveKafkaReceiver.
   *
   * @return Reactive stream of ReceiverRecord.
   */
  public <T> Flux<ReceiverRecord<String, T>> createReactiveKafkaConsumer(
      KafkaConsumerConfig kafkaConsumerConfig, Class<T> messageType) {
    KafkaConfig kafkaConfig = kafkaConsumerConfig.getInboundConsumerConfig();
    log.info("creating kafka receiver for topic:[{}]", kafkaConfig.getTopic());
    return KafkaReceiver.create(kafkaReceiverOptions(kafkaConsumerConfig, kafkaConfig, messageType))
        .receive();
  }

  /**
   * create kafka configuration.
   *
   * @return Consumer configuration.
   */
  private <T> Map<String, Object> kafkaConsumerConfiguration(
      KafkaConsumerConfig kafkaConsumerConfig, KafkaConfig kafkaConfig, Class<T> messageType) {
    Map<String, Object> configProp = new HashMap<>();
    configProp.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, kafkaConfig.getKeyDeserializer());
    configProp.put(
        ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, kafkaConfig.getValueDeserializer());
    configProp.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, kafkaConfig.isAutoCommit());
    configProp.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, kafkaConfig.getSessionTimeout());
    configProp.put(ConsumerConfig.REQUEST_TIMEOUT_MS_CONFIG, kafkaConfig.getRequestTimeout());
    configProp.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, kafkaConfig.getAutoOffset());
    configProp.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, kafkaConfig.getMaxPollRecords());
    configProp.put(
        ConsumerConfig.HEARTBEAT_INTERVAL_MS_CONFIG, kafkaConfig.getHeartBeatIntervalMs());
    configProp.put(ConsumerConfig.GROUP_ID_CONFIG, kafkaConfig.getGroupId());
    configProp.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaConfig.getBootStrapServers());
    configProp.put(JsonDeserializer.VALUE_DEFAULT_TYPE, messageType);
    // for secured connection, add configuration.
    if (kafkaConfig.isSecuredCluster()) {
      configProp.put(
          CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, kafkaConsumerConfig.getSecurityProtocol());
      configProp.put(
          SslConfigs.SSL_TRUSTSTORE_LOCATION_CONFIG,
          kafkaConsumerConfig.getSslTruststoreLocation());
      configProp.put(
          SslConfigs.SSL_TRUSTSTORE_PASSWORD_CONFIG,
          kafkaConsumerConfig.getSslTruststorePassword());
      configProp.put(
          SslConfigs.SSL_KEYSTORE_LOCATION_CONFIG, kafkaConsumerConfig.getSslKeystoreLocation());
      configProp.put(
          SslConfigs.SSL_KEYSTORE_PASSWORD_CONFIG, kafkaConsumerConfig.getSslKeystorePassword());
      configProp.put(SslConfigs.SSL_KEY_PASSWORD_CONFIG, kafkaConsumerConfig.getSslKeyPassword());
    }
    return configProp;
  }

  /**
   * create kafkaReceiverOptions.
   *
   * @return ReceiverOptions.
   */
  private <T> ReceiverOptions<String, T> kafkaReceiverOptions(
      KafkaConsumerConfig kafkaConsumerConfig, KafkaConfig kafkaConfig, Class<T> messageType) {
    Map<String, Object> kafkaConsumerConfigurationMap =
        kafkaConsumerConfiguration(kafkaConsumerConfig, kafkaConfig, messageType);

    ReceiverOptions<String, T> receiverOptions =
        ReceiverOptions.create(kafkaConsumerConfigurationMap);

    return receiverOptions
        .subscription(Collections.singletonList(kafkaConfig.getTopic()))
        .addAssignListener(partitions -> log.info("onPartitionsAssigned={}", partitions))
        .addRevokeListener(partitions -> log.info("onPartitionsRevoked={}", partitions));
  }
}
