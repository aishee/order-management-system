package com.walmart.common.domain.messaging;

public interface DomainEventListener {
  void listen(DomainEvent event);
}
