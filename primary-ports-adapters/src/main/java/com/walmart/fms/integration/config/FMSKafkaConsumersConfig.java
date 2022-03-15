package com.walmart.fms.integration.config;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.walmart.common.config.BaseCCMConfig;
import io.strati.configuration.annotation.Configuration;
import io.strati.configuration.annotation.Ignore;
import io.strati.configuration.annotation.PostInit;
import io.strati.configuration.annotation.PostRefresh;
import io.strati.configuration.annotation.Property;
import io.strati.configuration.context.ConfigurationContext;
import io.strati.configuration.listener.ChangeLog;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

@Getter
@Setter
@Slf4j
@Configuration(configName = "fms-kafka-consumer-config")
public class FMSKafkaConsumersConfig extends BaseCCMConfig {

  private static final String GIF_UOFS_CONFIG_NAME = "GIF-UOFS";
  private static final String GIF_CANCEL_CONFIG_NAME = "GIF-CANCEL";
  private static final String GIF_UODS_CONFIG_NAME = "GIF-UODS";

  private static final ObjectMapper mapper =
      new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

  @Property(propertyName = "fms.kafka.inbound.gifUofs")
  private String gifUofsKafkaConsumerConfig;

  @Property(propertyName = "fms.kafka.inbound.gifCancelFMSOrder")
  private String gifCancelKafkaConsumerConfig;

  @Property(propertyName = "fms.kafka.inbound.gifUods")
  private String gifUodsKafkaConsumerConfig;

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

  @Ignore private Map<String, KafkaConsumerConfig> configMap = new HashMap<>();

  @PostInit
  public void postInit(String configName, ConfigurationContext context) {
    refreshProps(configName);
  }

  public KafkaConsumerConfig getOrderDeliveryKafkaConsumerConfig() {
    return getKafkaConsumerConfigByName(GIF_UODS_CONFIG_NAME);
  }

  public KafkaConsumerConfig getOrderCancellationKafkaConsumerConfig() {
    return getKafkaConsumerConfigByName(GIF_CANCEL_CONFIG_NAME);
  }

  public KafkaConsumerConfig getOrderUpdatesKafkaConsumerConfig() {
    return getKafkaConsumerConfigByName(GIF_UOFS_CONFIG_NAME);
  }

  private KafkaConsumerConfig getKafkaConsumerConfigByName(String name) {
    if (StringUtils.isNotBlank(name)) {
      return this.configMap.get(name.toUpperCase());
    } else {
      return null;
    }
  }

  private void refreshProps(String configName) {
    configMap.put(
        GIF_UOFS_CONFIG_NAME, convertJsonToConfig(this.gifUofsKafkaConsumerConfig, configName));
    configMap.put(
        GIF_CANCEL_CONFIG_NAME, convertJsonToConfig(this.gifCancelKafkaConsumerConfig, configName));
    configMap.put(
        GIF_UODS_CONFIG_NAME, convertJsonToConfig(this.gifUodsKafkaConsumerConfig, configName));
  }

  private KafkaConsumerConfig convertJsonToConfig(String configJason, String configName) {
    KafkaConsumerConfig kafkaConsumerConfig = new KafkaConsumerConfig();
    if (StringUtils.isNotEmpty(configJason)) {
      try {
        kafkaConsumerConfig = mapper.readValue(configJason, KafkaConsumerConfig.class);
        kafkaConsumerConfig.setSslTruststoreLocation(sslTruststoreLocation);
        kafkaConsumerConfig.setSslKeystoreLocation(sslKeystoreLocation);
        kafkaConsumerConfig.setSslTruststorePassword(sslTruststorePassword);
        kafkaConsumerConfig.setSslKeystorePassword(sslKeystorePassword);
        kafkaConsumerConfig.setSslKeyPassword(sslKeyPassword);
        kafkaConsumerConfig.setSecurityProtocol(securityProtocol);
      } catch (Exception e) {
        log.info("Exception Occurred While Parsing the ConfigName={} Exception={}", configName, e);
      }
    }
    return kafkaConsumerConfig;
  }

  @Override
  protected String getConfigName() {
    return "fms-kafka-consumer-config";
  }

  @PostRefresh
  @Override
  protected void configChanged(
      String configName, List<ChangeLog> changeLogs, ConfigurationContext context) {
    super.configChanged(configName, changeLogs, context);
  }
}
