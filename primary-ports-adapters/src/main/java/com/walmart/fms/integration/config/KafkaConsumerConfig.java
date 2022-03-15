package com.walmart.fms.integration.config;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.Properties;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Getter
@Setter
@Slf4j
public class KafkaConsumerConfig {

  private KafkaConfig inboundConsumerConfig;

  private KafkaConfig failureProducerConfig;

  private int retryablePolicyCount = 3;

  private int retryablePolicySleepTime = 100;

  private Integer concurrency = 10;

  private boolean enabled = true;

  private boolean autoStartup = true;

  private String securityProtocol;

  private String sslTruststoreLocation;

  private String sslKeystoreLocation;

  private String sslTruststorePassword;

  private String sslKeystorePassword;

  private String sslKeyPassword;

  @JsonIgnore private Properties failureProducerConfigProperty;

  public Properties getFailureProducerConfigProps() {
    if (failureProducerConfigProperty == null) {
      failureProducerConfigProperty = new Properties();
      failureProducerConfigProperty.put("key.serializer", failureProducerConfig.getKeySerializer());
      failureProducerConfigProperty.put(
          "value.serializer", failureProducerConfig.getValueSerializer());
      failureProducerConfigProperty.put("topic", failureProducerConfig.getTopic());
      failureProducerConfigProperty.put(
          "bootstrap.servers", failureProducerConfig.getBootStrapServers());
      failureProducerConfigProperty.put(
          "request.timeout.ms", failureProducerConfig.getRequestTimeout());
    }
    return failureProducerConfigProperty;
  }
}
