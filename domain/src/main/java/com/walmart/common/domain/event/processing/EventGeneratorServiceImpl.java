package com.walmart.common.domain.event.processing;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class EventGeneratorServiceImpl implements EventGeneratorService {

  private final ApplicationEventPublisher applicationEventPublisher;

  @Override
  public <T extends Message> void publishApplicationEvent(T message) {
    applicationEventPublisher.publishEvent(message);
    log.info("ApplicationEvent published successfully for message : {}", message.getClass());
  }
}
