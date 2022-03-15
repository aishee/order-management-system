package com.walmart.fms.infrastructure.integration.jms.config;

public class JmsEndPointConfig {

  private String endpointUrl;

  private int retryCount;

  private int concurencyCount;

  public String getEndpointUrl() {
    return endpointUrl;
  }

  public void setEndpointUrl(String endpointUrl) {
    this.endpointUrl = endpointUrl;
  }

  public int getRetryCount() {
    return retryCount;
  }

  public void setRetryCount(int retryCount) {
    this.retryCount = retryCount;
  }

  public int getConcurencyCount() {
    return concurencyCount;
  }

  public void setConcurencyCount(int concurencyCount) {
    this.concurencyCount = concurencyCount;
  }
}
