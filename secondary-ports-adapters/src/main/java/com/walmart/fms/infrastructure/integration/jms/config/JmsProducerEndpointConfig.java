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
@Configuration(configName = "jms-producer-endpoint-config")
public class JmsProducerEndpointConfig extends BaseCCMConfig {

  private static final ObjectMapper mapper =
      new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
  private static final String GIF_ORDER_DOWNLOAD = "GIF_ORDER_DOWNLOAD";
  private static final String GIF_FORCE_ORDER_CANCEL = "GIF_FORCE_ORDER_CANCEL";
  private final Map<String, JmsEndPointConfig> configMap = new HashMap<>();
  @Property(propertyName = "maasMQ.gif.orderDownload.producer.config.json")
  private String gifOrderDownloadConfig;
  @Property(propertyName = "maasMQ.gif.forceOrderCancel.producer.config.json")
  private String gifforceOrderCancelConfig;

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
    configMap.put(GIF_ORDER_DOWNLOAD, convertJsonToConfig(this.gifOrderDownloadConfig, configName));
    configMap.put(
        GIF_FORCE_ORDER_CANCEL, convertJsonToConfig(this.gifforceOrderCancelConfig, configName));
  }

  private JmsEndPointConfig convertJsonToConfig(String configJason, String configName) {
    JmsEndPointConfig jmsProducerConfig = new JmsEndPointConfig();
    if (StringUtils.isNotEmpty(configJason)) {
      try {
        jmsProducerConfig = mapper.readValue(configJason, JmsEndPointConfig.class);
      } catch (Exception e) {
        log.error("Exception Occured While Parsing the ConfigName={} Exception={}", configName, e);
      }
    }
    return jmsProducerConfig;
  }

  public JmsEndPointConfig getgifOrderDownloadConfig() {
    return configMap.get(GIF_ORDER_DOWNLOAD);
  }

  public JmsEndPointConfig getGifForceOrderCancelConfig() {
    return configMap.get(GIF_FORCE_ORDER_CANCEL);
  }

  @Override
  protected String getConfigName() {
    return "jms-producer-endpoint-config";
  }
}
