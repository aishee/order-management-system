package com.walmart.fms.infrastructure.integration.jms.config;

import javax.jms.ConnectionFactory;
import lombok.Getter;
import lombok.Setter;

public class CloudMessagingCustomFactory {

  @Getter @Setter public ConnectionFactory connectionFactory;

  public ConnectionFactory getConnectionFactory() {
    return connectionFactory;
  }

  public void setConnectionFactory(ConnectionFactory connectionFactory) {
    this.connectionFactory = connectionFactory;
  }
}
