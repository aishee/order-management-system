package com.walmart.oms.infrastructure.gateway.tax;

import com.walmart.common.config.BaseCCMConfig;
import io.strati.configuration.annotation.Configuration;
import io.strati.configuration.annotation.DefaultValue;
import io.strati.configuration.annotation.PostRefresh;
import io.strati.configuration.annotation.Property;
import io.strati.configuration.context.ConfigurationContext;
import io.strati.configuration.listener.ChangeLog;
import java.util.List;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Configuration(configName = "CalculatorTaxService")
@Getter
@Slf4j
public class CalculatorTaxServiceConfiguration extends BaseCCMConfig {

  @DefaultValue.String("https://api.wal-mart.com/si/boftm/calculation-api/order/tax/calculate")
  @Property(propertyName = "taxServiceUri")
  private String taxServiceUri;

  @DefaultValue.Int(1000)
  @Property(propertyName = "connectionTimeout")
  private int connTimeout;

  @DefaultValue.Int(2000)
  @Property(propertyName = "readTimeout")
  private int readTimeout;

  @DefaultValue.Boolean(true)
  @Property(propertyName = "loggingEnabled")
  private boolean loggingEnabled;

  @DefaultValue.Boolean(true)
  @Property(propertyName = "isReverseCalculation")
  private Boolean isReverseCalculation;

  @DefaultValue.String("05e193aa-221e-4ebc-a3c5-79eb6a7af015")
  @Property(propertyName = "taxClientId")
  private String taxClientId;

  @DefaultValue.String("dG0eD4rT1bF5qM3sV7mW3yL7uR7yC7qG5fF0nD2cV1kV2lD5eW")
  @Property(propertyName = "taxClientSecret")
  private String taxClientSecret;

  @DefaultValue.Int(10)
  @Property(propertyName = "tax.client.thread-pool-size")
  private int threadPoolSize;

  @Override
  protected String getConfigName() {
    return "CalculatorTaxService";
  }

  @PostRefresh
  @Override
  protected void configChanged(
      String configName, List<ChangeLog> changeLogs, ConfigurationContext context) {
    super.configChanged(configName, changeLogs, context);
  }
}
