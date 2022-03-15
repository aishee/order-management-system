package com.walmart.marketplace.infrastructure.gateway.justeats.config;

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
import lombok.extern.slf4j.Slf4j;

/** CCM Managed Bean for JustEats Gateway Configurations Maps to CCM config "JustEatsConfig" */
@Configuration(configName = "JustEatsConfig")
@Getter
@Slf4j
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JustEatsServiceConfiguration extends BaseCCMConfig {

  @DefaultValue.String("https://order-injection-status-updater.flyt-platform.com")
  @Property(propertyName = "order.status.update.base.uri")
  private String orderStatusUpdateBaseUri;

  @DefaultValue.String("/order/{order_id}/sent-to-pos-success")
  @Property(propertyName = "accept.order.api")
  private String acceptOrderApiUrl;

  @DefaultValue.String("/order/{order_id}/sent-to-pos-failed")
  @Property(propertyName = "deny.order.api")
  private String denyOrderApiUrl;

  @Property(propertyName = "order.status.update.api.key")
  private String orderStatusUpdateApiKey;

  @DefaultValue.String("https://api.flytplatform.com")
  @Property(propertyName = "item.update.base.uri")
  private String itemUpdateBaseUri;

  @DefaultValue.String("/item-availability")
  @Property(propertyName = "update.item.uri")
  private String itemAvailabilityApiUrl;

  @Property(propertyName = "item.availability.api.key")
  private String itemAvailabilityApiKey;

  @DefaultValue.Int(5000)
  @Property(propertyName = "read.timeout")
  private int readTimeout;

  @DefaultValue.Int(5000)
  @Property(propertyName = "conn.timeout")
  private int connTimeout;

  @DefaultValue.Int(5000)
  @Property(propertyName = "write.timeout")
  private int writeTimeout;

  @DefaultValue.Int(30000)
  @Property(propertyName = "pending.acquire.timeout")
  private int pendingAcquireTimeoutMs;

  @DefaultValue.Int(50)
  @Property(propertyName = "max.connection.timeout")
  private int maxConnectionCount;

  @DefaultValue.Boolean(true)
  @Property(propertyName = "logging.enabled")
  private boolean loggingEnabled;

  @DefaultValue.Int(30000)
  @Property(propertyName = "idle.timeout")
  private int idleTimeoutMs;

  @Property(propertyName = "clientSecret")
  private String clientSecret;

  @DefaultValue.Int(10)
  @Property(propertyName = "thread.pool-size")
  private int threadPoolSize;

  @Override
  protected String getConfigName() {
    return "JustEatsConfig";
  }

  @PostRefresh
  @Override
  protected void configChanged(
      String configName, List<ChangeLog> changeLogs, ConfigurationContext context) {
    super.configChanged(configName, changeLogs, context);
  }
}
