package com.walmart.marketplace.infrastructure.gateway.uber;

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

/** CCM Managed Bean for Uber Gateway Configurations Maps to CCM config "UberConfig" */
@Configuration(configName = "UberConfig")
@Getter
@Slf4j
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UberServiceConfiguration extends BaseCCMConfig {

  @DefaultValue.String("https://api.uber.com")
  @Property(propertyName = "uberBaseUri")
  private String uberBaseUri;

  @DefaultValue.String("/v2/eats/order/{uber_order_id}")
  @Property(propertyName = "uberGetOrderUri")
  private String uberGetOrderUri;

  @DefaultValue.String("uber")
  @Property(propertyName = "oauth2ClientRegistrationId")
  private String oauth2ClientRegistrationId;

  @DefaultValue.String("/v1/eats/orders/{uber_order_id}/accept_pos_order")
  @Property(propertyName = "uberAcceptOrderUri")
  private String uberAcceptOrderUri;

  @DefaultValue.String("/v1/eats/orders/{uber_order_id}/deny_pos_order")
  @Property(propertyName = "uberDenyOrderUri")
  private String uberDenyOrderUri;

  @DefaultValue.String("/v1/eats/orders/{uber_order_id}/cancel")
  @Property(propertyName = "uberCancelOrderUri")
  private String uberCancelOrderUri;

  @DefaultValue.String("/v2/eats/orders/{uber_order_id}/cart")
  @Property(propertyName = "uberPatchCartUri")
  private String uberPatchCartUri;

  @DefaultValue.String("/v2/eats/stores/{store_id}/menus/items/{item_id}")
  @Property(propertyName = "uberUpdateItemUri")
  private String uberUpdateItemUri;

  @DefaultValue.Int(1000)
  @Property(propertyName = "connectionTimeout")
  private int connTimeout;

  @DefaultValue.Int(2000)
  @Property(propertyName = "readTimeout")
  private int readTimeout;

  @DefaultValue.Int(2000)
  @Property(propertyName = "writeTimeout")
  private int writeTimeout;

  @DefaultValue.Int(50)
  @Property(propertyName = "maxConnectionCount")
  private int maxConnectionCount;

  @DefaultValue.Int(30000)
  @Property(propertyName = "pendingAcquireTimeoutMs")
  private int pendingAcquireTimeoutMs;

  @DefaultValue.Int(30000)
  @Property(propertyName = "idleTimeoutMs")
  private int idleTimeoutMs;

  @DefaultValue.Boolean(true)
  @Property(propertyName = "loggingEnabled")
  private boolean loggingEnabled;

  @DefaultValue.String("VDP7fHBmxDxoaAItmFhsk2i3wrHpFZpb")
  @Property(propertyName = "clientId")
  private String clientId;

  @DefaultValue.String("")
  @Property(propertyName = "clientSecret")
  private String clientSecret;

  @DefaultValue.List({"eats.order", "eats.store", "eats.store.orders.read", "eats.store.orders.cancel", "eats.report"})
  @Property(propertyName = "scopes")
  private List<String> scopes;

  @DefaultValue.String("https://login.uber.com/oauth/v2/token")
  @Property(propertyName = "accessTokenUri")
  private String accessTokenUri;

  @DefaultValue.Int(30)
  @Property(propertyName = "defaultDeliveryTimeInMinutes")
  private int defaultDeliveryTimeInMinutes;

  @DefaultValue.String("/v1/eats/report")
  @Property(propertyName = "uberReportUri")
  private String uberReportUri;

  @DefaultValue.String("/v1/eats/stores")
  @Property(propertyName = "uberStoreUri")
  private String uberStoreUri;

  @DefaultValue.Int(2)
  @Property(propertyName = "uber.report.day-to-end")
  private int dayToEnd;

  @DefaultValue.Int(400)
  @Property(propertyName = "uber.store.query.limit")
  private int uberStoreQueryLimit;

  @DefaultValue.Int(10)
  @Property(propertyName = "uber.thread-pool-size")
  private int threadPoolSize;

  @Override
  protected String getConfigName() {
    return "UberConfig";
  }

  @PostRefresh
  @Override
  protected void configChanged(
      String configName, List<ChangeLog> changeLogs, ConfigurationContext context) {
    super.configChanged(configName, changeLogs, context);
  }
}
