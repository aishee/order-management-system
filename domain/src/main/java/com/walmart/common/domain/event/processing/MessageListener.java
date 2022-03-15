package com.walmart.common.domain.event.processing;

public interface MessageListener<T extends Message> {

  void listen(T message);
}
