package com.walmart.common.infrastructure.integration.events.tracing;

import com.walmart.common.domain.BaseEntity;
import com.walmart.common.domain.event.processing.EgressEvent;
import com.walmart.common.domain.event.processing.EgressEventTracer;
import com.walmart.common.domain.repository.EgressEventTracerRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component("tracer")
public class EgressEventTracerImpl<T extends BaseEntity, R> implements EgressEventTracer<T, R> {

  @Autowired private EgressEventTracerRepository repository;

  private int maxRetries = 5;

  @Override
  public EgressEvent<T, R> get(EgressEvent<T, R> event) {
    return repository.get(event.getDomainModelId(), event.getDomain(), event.getName());
  }

  @Override
  public EgressEvent<T, R> save(EgressEvent<T, R> event) {
    return repository.save(event);
  }

  @Override
  public int maxRetries() {
    return maxRetries;
  }
}
