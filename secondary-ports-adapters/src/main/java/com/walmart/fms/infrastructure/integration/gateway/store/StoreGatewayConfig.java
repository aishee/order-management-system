package com.walmart.fms.infrastructure.integration.gateway.store;

import com.walmart.common.config.BaseCCMConfig;
import io.strati.configuration.annotation.Configuration;
import io.strati.configuration.annotation.DefaultValue;
import io.strati.configuration.annotation.PostRefresh;
import io.strati.configuration.annotation.Property;
import io.strati.configuration.context.ConfigurationContext;
import io.strati.configuration.listener.ChangeLog;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

/** CCM Managed Bean for Store Gateway Configurations Maps to CCM config "StoreGatewayConfig" */
@Configuration(configName = "StoreGatewayConfig")
@Getter
@Setter
@ToString
@Slf4j
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StoreGatewayConfig extends BaseCCMConfig {

  @DefaultValue.Int(5)
  @Property(propertyName = "poolCoreSize")
  private int poolCoreSize;

  @DefaultValue.Int(30)
  @Property(propertyName = "poolMaxSize")
  private int poolMaxSize;

  @DefaultValue.Int(100)
  @Property(propertyName = "poolQueueSize")
  private int poolQueueSize;

  @DefaultValue.Long(30)
  @Property(propertyName = "poolKeepAliveTime")
  private long poolKeepAliveTime;

  @Override
  protected String getConfigName() {
    return "StoreGatewayConfig";
  }

  @PostRefresh
  @Override
  protected void configChanged(
      String configName, List<ChangeLog> changeLogs, ConfigurationContext context) {
    super.configChanged(configName, changeLogs, context);
  }
}
