package com.walmart.fms.infrastructure.integration.kafka.config;

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
import org.apache.commons.lang3.StringUtils;

/** This a configuration for kafka producers. */
@Slf4j
@Configuration(configName = "fms-kafka-producer-config")
@Getter
@Setter
public class FmsKafkaProducerConfig extends BaseCCMConfig {

  private static final ObjectMapper mapper =
      new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
  private static final String GIF_ERROR_PRODUCER_CONFIG_NAME = "gif.error.producer.config";

  /** The json string of producer configurations. */
  @Property(propertyName = "gif.error.producer.config")
  private String gifErrorProducerConfig;

  @Property(propertyName = "gif.order.cancel.error.producer.topic")
  private String gifOrderCancelErrorProducerTopic;

  @Property(propertyName = "gif.order.deliver.error.producer.topic")
  private String gifOrderDeliverErrorProducerTopic;

  @Property(propertyName = "gif.order.update.error.producer.topic")
  private String gifOrderUpdateErrorProducerTopic;

  @Property(propertyName = "fms.producer.threads")
  @DefaultValue.Int(10)
  private int numThreads;

  @Property(propertyName = "secured.cluster")
  @DefaultValue.Boolean(false)
  private boolean securedCluster;

  @Property(propertyName = "ssl.truststore.location")
  private String sslTruststoreLocation;

  @Property(propertyName = "ssl.truststore.password")
  private String sslTruststorePassword;

  @Property(propertyName = "ssl.keystore.location")
  private String sslKeystoreLocation;

  @Property(propertyName = "ssl.key.password")
  private String sslKeyPassword;

  @Property(propertyName = "ssl.keystore.password")
  private String sslKeystorePassword;

  @Property(propertyName = "security.protocol")
  private String securityProtocol;

  /** A map of {@link KafkaProducerConfig} to a alphabetic code. */
  @Ignore private Map<String, KafkaProducerConfig> configMap = new HashMap<>();

  public Properties getGifErrorProducerProperties() {
    if (configMap.containsKey(GIF_ERROR_PRODUCER_CONFIG_NAME)) {
      return configMap.get(GIF_ERROR_PRODUCER_CONFIG_NAME).createProperties();
    }
    return new Properties();
  }

  @PostInit
  public void postInit(String configName, ConfigurationContext context) {
    if (StringUtils.isNotEmpty(configName) && configName.equalsIgnoreCase(getConfigName())) {
      loadConfigMap();
    }
  }

  /** Load the configuration in to a map. */
  private void loadConfigMap() {
    try {
      KafkaProducerConfig kafkaProducerConfig =
          mapper.readValue(gifErrorProducerConfig, KafkaProducerConfig.class);
      kafkaProducerConfig.setSecuredCluster(securedCluster);
      kafkaProducerConfig.setSslTruststoreLocation(sslTruststoreLocation);
      kafkaProducerConfig.setSslKeystoreLocation(sslKeystoreLocation);
      kafkaProducerConfig.setSslTruststorePassword(sslTruststorePassword);
      kafkaProducerConfig.setSslKeystorePassword(sslKeystorePassword);
      kafkaProducerConfig.setSslKeyPassword(sslKeyPassword);
      kafkaProducerConfig.setSecurityProtocol(securityProtocol);
      configMap.put(GIF_ERROR_PRODUCER_CONFIG_NAME, kafkaProducerConfig);
    } catch (Exception ex) {
      log.error(
          String.format(
              "Exception occurred while parsing gifErrorProducerConfig json = %s",
              gifErrorProducerConfig),
          ex);
      throw new InvalidConfigurationException("Invalid fms-kafka-producer-config", ex);
    }
  }

  @Override
  protected String getConfigName() {
    return "fms-kafka-producer-config";
  }
}
