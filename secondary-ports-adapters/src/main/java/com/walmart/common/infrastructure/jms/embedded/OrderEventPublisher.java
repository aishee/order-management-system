package com.walmart.common.infrastructure.jms.embedded;

import com.walmart.common.domain.messaging.DomainEventPublisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;

@Component("orderEventPublisher")
public class OrderEventPublisher implements DomainEventPublisher {

  @Autowired JmsTemplate jmsTemplate;

  public JmsTemplate getJmsTemplate() {
    return jmsTemplate;
  }
}
