package com.walmart.common.domain.messaging;

import com.walmart.common.domain.messaging.exception.DomainEventPublishingException;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.util.Assert;

/** A domain event publisher which publishes a domain event to another domain. */
public interface DomainEventPublisher {
  /**
   * Publishes a message to a specific domain.
   *
   * @param event An object of type {@link DomainEvent}
   * @param destination A string indicating the destination of the domain event.
   */
  default void publish(DomainEvent event, String destination) {
    Assert.notNull(event, "Event object cannot be null");
    Assert.notNull(destination, "Destination cannot be null");
    try {
      getJmsTemplate().convertAndSend(destination, event);
    } catch (Exception e) {
      throw new DomainEventPublishingException(e);
    }
  }

  JmsTemplate getJmsTemplate();
}
