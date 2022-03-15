package com.walmart.oms.infrastructure.configuration;

import com.walmart.common.config.BaseCCMConfig;
import io.strati.configuration.annotation.Configuration;
import io.strati.configuration.annotation.DefaultValue;
import io.strati.configuration.annotation.PostRefresh;
import io.strati.configuration.annotation.Property;
import io.strati.configuration.context.ConfigurationContext;
import io.strati.configuration.listener.ChangeLog;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

@Getter
@Setter
@ToString
@Slf4j
@Configuration(configName = "oms.order.config")
public class OmsOrderConfig extends BaseCCMConfig {

  @DefaultValue.Boolean(false)
  @Property(propertyName = "enable.order.update.message.publish")
  private boolean publishOrderUpdateEvent;

  @DefaultValue.Int(50)
  @Property(propertyName = "max.order.fetch.limit")
  private int maxOrderFetchLimit;

  @Override
  protected String getConfigName() {
    return "oms.order.config";
  }

  @PostRefresh
  @Override
  protected void configChanged(
      String configName, List<ChangeLog> changeLogs, ConfigurationContext context) {
    super.configChanged(configName, changeLogs, context);
  }
}
