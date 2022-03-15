package com.walmart.oms.infrastructure.gateway.iro;

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

@Configuration(configName = "CatalogService")
@Getter
@Setter
@ToString
@Slf4j
public class IROServiceConfiguration extends BaseCCMConfig {

  @DefaultValue.String("http://iro-service-prod-private.walmart.com/asda-iro-service/catalogitem")
  @Property(propertyName = "iroServiceUri")
  private String iroServiceUri;

  @DefaultValue.Int(1000)
  @Property(propertyName = "connectionTimeout")
  private int connTimeout;

  @DefaultValue.Int(2000)
  @Property(propertyName = "readTimeout")
  private int readTimeout;

  @DefaultValue.Boolean(true)
  @Property(propertyName = "loggingEnabled")
  private boolean loggingEnabled;

  @DefaultValue.Int(60)
  @Property(propertyName = "iroItemBatchSize")
  private int iroItemBatchSize;

  @DefaultValue.String("ods_store_item")
  @Property(propertyName = "iroConsumerContract")
  private String iroConsumerContract;

  @DefaultValue.String("ods")
  @Property(propertyName = "iroRequestOrigin")
  private String iroRequestOrigin;

  @DefaultValue.String("Price Drop")
  @Property(propertyName = "priceDrop")
  private String priceDrop;

  @DefaultValue.Boolean(true)
  @Property(propertyName = "newPriceDropTagEnabled")
  private boolean newPriceDropTagEnabled;

  @DefaultValue.Int(10)
  @Property(propertyName = "iro.thread-pool-size")
  private int threadPoolSize;

  @Override
  protected String getConfigName() {
    return "CatalogService";
  }

  @PostRefresh
  @Override
  protected void configChanged(
      String configName, List<ChangeLog> changeLogs, ConfigurationContext context) {
    super.configChanged(configName, changeLogs, context);
  }
}
