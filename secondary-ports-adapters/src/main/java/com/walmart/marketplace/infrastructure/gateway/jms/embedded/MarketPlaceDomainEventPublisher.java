package com.walmart.marketplace.infrastructure.gateway.jms.embedded;

import com.walmart.common.domain.messaging.DomainEventPublisher;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;

@Component("domainEventPublisher")
@Slf4j
public class MarketPlaceDomainEventPublisher implements DomainEventPublisher {

  @Autowired JmsTemplate jmsTemplate;

  public JmsTemplate getJmsTemplate() {
    return jmsTemplate;
  }
}
