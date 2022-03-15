package com.walmart.fms.infrastructure.integration.jms.config;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.walmart.common.config.BaseCCMConfig;
import io.strati.configuration.annotation.Configuration;
import io.strati.configuration.annotation.PostInit;
import io.strati.configuration.annotation.PostRefresh;
import io.strati.configuration.annotation.Property;
import io.strati.configuration.context.ConfigurationContext;
import io.strati.configuration.listener.ChangeLog;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

@Slf4j
@Configuration(configName = "jms-client-producer-config")
public class JmsProducerClientConfig extends BaseCCMConfig {

  private static final ObjectMapper mapper =
      new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
  private static final String GIF_CLIENT_CONFIG_NAME = "GIF";
  private final Map<String, JmsClientConfig> configMap = new HashMap<>();
  @Property(propertyName = "maasMQ.gif.client.config.json")
  private String gifJasonClientConfig;
  @Property(propertyName = "maasMQGifClientPassword")
  private String gifClientPassword;

  @PostInit
  public void postInit(String configName, ConfigurationContext context) {
    refreshProps(configName);
  }

  @PostRefresh
  @Override
  public void configChanged(
      String configName, List<ChangeLog> changes, ConfigurationContext context) {
    super.configChanged(configName, changes, context);
    refreshProps(configName);
  }

  private void refreshProps(String configName) {
    JmsClientConfig gifJmsClientConfig = convertJsonToConfig(this.gifJasonClientConfig, configName);
    gifJmsClientConfig.setPassword(this.gifClientPassword);
    configMap.put(GIF_CLIENT_CONFIG_NAME, gifJmsClientConfig);
  }

  private JmsClientConfig convertJsonToConfig(String configJason, String configName) {
    JmsClientConfig jmsClientConfig = new JmsClientConfig();
    if (StringUtils.isNotEmpty(configJason)) {
      try {
        jmsClientConfig = mapper.readValue(configJason, JmsClientConfig.class);
      } catch (Exception e) {
        log.info("Exception Occured While Parsing the ConfigName={} Exception={}", configName, e);
      }
    }
    return jmsClientConfig;
  }

  public String getGifJasonClientConfig() {
    return gifJasonClientConfig;
  }

  public void setGifJasonClientConfig(String gifJasonClientConfig) {
    this.gifJasonClientConfig = gifJasonClientConfig;
  }

  public JmsClientConfig getGifClientConfig() {
    return configMap.get(GIF_CLIENT_CONFIG_NAME);
  }

  @Override
  protected String getConfigName() {
    return "jms-client-producer-config";
  }
}
