package com.walmart.marketplace.infrastructure.gateway.justeats;

import com.walmart.common.infrastructure.webclient.BaseWebClient;
import com.walmart.common.metrics.MetricConstants;
import com.walmart.marketplace.infrastructure.gateway.justeats.config.JustEatsServiceConfiguration;
import io.strati.configuration.annotation.ManagedConfiguration;

public abstract class JustEatsBaseWebClient extends BaseWebClient {

  protected static final String JUST_EATS = "JUST_EATS";
  protected static final String API_KEY_HEADER = "X-Flyt-API-Key";
  protected static final String LOG_PREFIX = "%s JustEatsOrderId=%s";
  protected static final String THREAD_FACTORY_NAME = "JustEats-thread-pool-%d";
  @ManagedConfiguration protected JustEatsServiceConfiguration justEatsServiceConfiguration;

  @Override
  protected int getReadTimeout() {
    return justEatsServiceConfiguration.getReadTimeout();
  }

  @Override
  public String getMetricsCounterName() {
    return MetricConstants.MetricCounters.JUST_EATS_INVOCATION.getCounter();
  }

  @Override
  protected int getConnTimeout() {
    return justEatsServiceConfiguration.getConnTimeout();
  }

  @Override
  public boolean isLogEnabled() {
    return justEatsServiceConfiguration.isLoggingEnabled();
  }

  @Override
  protected String getClientName() {
    return JUST_EATS;
  }

  @Override
  public String getMetricsExceptionCounterName() {
    return MetricConstants.MetricCounters.JUST_EATS_EXCEPTION.getCounter();
  }
}
