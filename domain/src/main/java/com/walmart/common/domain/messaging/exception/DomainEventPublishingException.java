package com.walmart.common.domain.messaging.exception;

public class DomainEventPublishingException extends RuntimeException {

  public DomainEventPublishingException(String message) {
    super(message);
  }

  public DomainEventPublishingException(Throwable cause) {
    super(cause);
  }
}
