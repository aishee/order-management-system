package com.walmart.oms.infrastructure.gateway.price;

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

@Configuration(configName = "PYSIPYPService")
@Getter
@Setter
@Slf4j
public class PYSIPYPServiceConfiguration extends BaseCCMConfig {

  @DefaultValue.String("http://asda-groceries-services-pqa.walmart.com/asda-services/rest/order")
  @Property(propertyName = "pysipypServiceUri")
  private String pysipypServiceUri;

  @DefaultValue.String("http://asda-groceries-services-pqa.walmart.com/asda-services/rest/orders/")
  @Property(propertyName = "pysipypServiceUriForOrders")
  private String pysipypServiceUriForOrders;

  @DefaultValue.Int(1000)
  @Property(propertyName = "connectionTimeout")
  private int connTimeout;

  @DefaultValue.Int(2000)
  @Property(propertyName = "readTimeout")
  private int readTimeout;

  @DefaultValue.Boolean(true)
  @Property(propertyName = "loggingEnabled")
  private boolean loggingEnabled;

  @DefaultValue.String("5f7638c6a164a2b2c60eea794916550a")
  @Property(propertyName = "accessCode")
  private String accessCode;

  @DefaultValue.Int(10)
  @Property(propertyName = "pysipyp.thread-pool-size")
  private int threadPoolSize;

  @Override
  protected String getConfigName() {
    return "PYSIPYPService";
  }

  @PostRefresh
  @Override
  protected void configChanged(
      String configName, List<ChangeLog> changeLogs, ConfigurationContext context) {
    super.configChanged(configName, changeLogs, context);
  }
}
