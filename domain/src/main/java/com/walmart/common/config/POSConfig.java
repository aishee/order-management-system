package com.walmart.common.config;

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
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

@Getter
@ToString
@Slf4j
@Configuration(configName = "oms-pos-config")
@AllArgsConstructor
@NoArgsConstructor
public class POSConfig extends BaseCCMConfig {

  @Property(propertyName = "carrierBagWin")
  @DefaultValue.String("50373659")
  private String carrierBagWin;

  @Property(propertyName = "carrierBagUpc")
  @DefaultValue.String("5057172723966")
  private String carrierBagUpc;

  @Property(propertyName = "carrierBagDepartmentID")
  @DefaultValue.String("69")
  private String carrierBagDepartmentID;

  @Override
  protected String getConfigName() {
    return "oms-pos-config";
  }

  @PostRefresh
  @Override
  protected void configChanged(
      String configName, List<ChangeLog> changeLogs, ConfigurationContext context) {
    super.configChanged(configName, changeLogs, context);
  }
}
