package com.walmart.oms.infrastructure.configuration;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.walmart.common.config.BaseCCMConfig;
import com.walmart.common.infrastructure.config.KafkaProducerConfig;
import io.strati.configuration.annotation.Configuration;
import io.strati.configuration.annotation.DefaultValue;
import io.strati.configuration.annotation.Ignore;
import io.strati.configuration.annotation.PostInit;
import io.strati.configuration.annotation.Property;
import io.strati.configuration.context.ConfigurationContext;
import io.strati.libs.logging.kafka.common.errors.InvalidConfigurationException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/** This a configuration for kafka producers. */
@Slf4j
@Configuration(configName = "oms-kafka-producer-config")
public class OmsKafkaProducerConfig extends BaseCCMConfig {

  private static final ObjectMapper OBJECT_MAPPER =
      new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
  private static final String DWH_CONFIG_NAME = "order.services.config";

  @Ignore private final Map<String, KafkaProducerConfig> configMap = new HashMap<>();

  /** The json string of producer configurations. */
  @Getter
  @Setter
  @Property(propertyName = "order.services.config")
  private String orderServiceConfigString;

  @Getter
  @Setter
  @Property(propertyName = "oms.producer.threads")
  @DefaultValue.Int(10)
  private int numThreads;

  @Getter
  @Setter
  @Property(propertyName = "ssl.truststore.location")
  private String sslTruststoreLocation;

  @Getter
  @Setter
  @Property(propertyName = "ssl.keystore.location")
  private String sslKeystoreLocation;

  @Getter
  @Setter
  @Property(propertyName = "ssl.truststore.password")
  private String sslTruststorePassword;

  @Getter
  @Setter
  @Property(propertyName = "ssl.key.password")
  private String sslKeyPassword;

  @Getter
  @Setter
  @Property(propertyName = "ssl.keystore.password")
  private String sslKeystorePassword;

  @Getter
  @Setter
  @Property(propertyName = "security.protocol")
  private String securityProtocol;

  @Getter
  @Setter
  @Property(propertyName = "secured.cluster")
  @DefaultValue.Boolean(false)
  private boolean securedCluster;

  public Properties getDwhConfigProperties() {
    if (configMap.containsKey(DWH_CONFIG_NAME)) {
      return configMap.get(DWH_CONFIG_NAME).createProperties();
    }
    return new Properties();
  }

  public KafkaProducerConfig getConfigForTopic(String propertyName) {
    return configMap.get(propertyName);
  }

  @PostInit
  public void postInit(String configName, ConfigurationContext context) {
    loadConfigMap();
  }

  /** Load the configuration in to a map. */
  private void loadConfigMap() {
    try {
      KafkaProducerConfig kafkaProducerConfig =
          OBJECT_MAPPER.readValue(orderServiceConfigString, KafkaProducerConfig.class);
      kafkaProducerConfig.setSecuredCluster(securedCluster);
      kafkaProducerConfig.setSecurityProtocol(securityProtocol);
      kafkaProducerConfig.setSslTruststoreLocation(sslTruststoreLocation);
      kafkaProducerConfig.setSslTruststorePassword(sslTruststorePassword);
      kafkaProducerConfig.setSslKeyPassword(sslKeyPassword);
      kafkaProducerConfig.setSslKeystoreLocation(sslKeystoreLocation);
      kafkaProducerConfig.setSslKeystorePassword(sslKeystorePassword);
      configMap.put(DWH_CONFIG_NAME, kafkaProducerConfig);
    } catch (Exception ex) {
      log.error(
          String.format(
              "Exception occurred while parsing orderServiceConfig json = %s",
              orderServiceConfigString),
          ex);
      throw new InvalidConfigurationException("Invalid oms-kafka-producer-config", ex);
    }
  }

  @Override
  protected String getConfigName() {
    return "oms-kafka-producer-config";
  }
}
