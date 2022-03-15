package com.walmart.common.infrastructure.config;

import com.walmart.common.infrastructure.ApiAction;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import javax.annotation.PostConstruct;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ResiliencyLoggingListenerConfig {

  @Autowired protected CircuitBreakerRegistry circuitBreakerRegistry;

  @PostConstruct
  public void initialize() {

    ApiAction.getAllCircuitBreakers()
        .forEach(
            circuitBreaker ->
                attachLoggingListener(
                    circuitBreakerRegistry.circuitBreaker(circuitBreaker.name())));
  }

  protected void attachLoggingListener(@NonNull CircuitBreaker circuitBreaker) {
    String circuitBreakerName = circuitBreaker.getName();
    circuitBreaker
        .getEventPublisher()
        .onSuccess(
            event -> {
              if (log.isDebugEnabled()) {
                log.debug(
                    "{} Circuit Breaker: Successfully sent request {}", circuitBreakerName, event);
              }
            })
        .onError(
            event ->
                log.error(
                    "{} Circuit Breaker: Error from remote service Event:{}",
                    circuitBreakerName,
                    event))
        .onReset(event -> log.info("{} Circuit Breaker: reset {}", circuitBreakerName, event))
        .onStateTransition(
            event ->
                log.info(
                    "{} Circuit Breaker: State Machine Transition {}", circuitBreakerName, event));
  }
}
