package com.walmart.purge.configuration;

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
@Configuration(configName = "oms-purge-config")
public class OmsPurgeConfig extends BaseCCMConfig {

  @DefaultValue.String("OMSCOREDB.EGRESS_EVENTS_PROCDR")
  @Property(propertyName = "egress.event-procedure")
  private String egressEventProcedure;

  @DefaultValue.String("OMSCOREDB.MARKET_PLACE_ORDER_PROCDR")
  @Property(propertyName = "marketplace.order-procedure")
  private String marketplaceOrderProcedure;

  @DefaultValue.String("OMSCOREDB.FULFILLMENT_ORDER_PROCDR")
  @Property(propertyName = "fulfilment.order-procedure")
  private String fulfilmentOrderProcedure;

  @DefaultValue.String("OMSCOREDB.OMS_ORDER_PROCDR")
  @Property(propertyName = "oms.order-procedure")
  private String omsOrderProcedure;

  @DefaultValue.String("OMSCOREDB.MARKET_PLACE_EVENTS_PROCDR")
  @Property(propertyName = "marketplace.event-procedure")
  private String marketplaceEventProcedure;

  @DefaultValue.Int(10)
  @Property(propertyName = "oms.purge.thread.ttl-seconds")
  private int ttlSeconds;

  @DefaultValue.Int(365)
  @Property(propertyName = "oms.purge.day-to-sub")
  private int dayToSub;

  @Override
  protected String getConfigName() {
    return "oms-purge-config";
  }

  @PostRefresh
  @Override
  protected void configChanged(
      String configName, List<ChangeLog> changeLogs, ConfigurationContext context) {
    super.configChanged(configName, changeLogs, context);
  }
}
