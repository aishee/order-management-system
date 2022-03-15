package com.walmart.marketplace.order.configuration;

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
import lombok.extern.slf4j.Slf4j;

@Getter
@Setter
@Slf4j
@Configuration(configName = "MarketPlaceOrderConfig")
public class MarketPlaceOrderConfig extends BaseCCMConfig {

  @DefaultValue.Int(1000)
  @Property(propertyName = "allowedRunningOrders")
  private int allowedRunningOrders;

  @Property(propertyName = "inProgressStates")
  private String inProgressStates;

  @Override
  protected String getConfigName() {
    return "MarketPlaceOrderConfig";
  }

  @PostRefresh
  @Override
  protected void configChanged(
      String configName, List<ChangeLog> changeLogs, ConfigurationContext context) {
    super.configChanged(configName, changeLogs, context);
  }
}
