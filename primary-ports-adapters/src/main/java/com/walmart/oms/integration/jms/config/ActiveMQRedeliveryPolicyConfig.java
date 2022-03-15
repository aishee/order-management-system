package com.walmart.oms.integration.jms.config;

import com.walmart.common.config.BaseCCMConfig;
import io.strati.configuration.annotation.Configuration;
import io.strati.configuration.annotation.DefaultValue;
import io.strati.configuration.annotation.PostRefresh;
import io.strati.configuration.annotation.Property;
import io.strati.configuration.context.ConfigurationContext;
import io.strati.configuration.listener.ChangeLog;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Getter
@Setter
@Slf4j
@Configuration(configName = "activemq-redelivery-policy-config")
@AllArgsConstructor
@NoArgsConstructor
public class ActiveMQRedeliveryPolicyConfig extends BaseCCMConfig {

  @Property(propertyName = "maximum-redeliveries")
  @DefaultValue.Int(3)
  private int maximumRedeliveries;

  @Override
  protected String getConfigName() {
    return "activemq-redelivery-policy-config";
  }

  @PostRefresh
  @Override
  protected void configChanged(
      String configName, List<ChangeLog> changeLogs, ConfigurationContext context) {
    super.configChanged(configName, changeLogs, context);
  }
}
