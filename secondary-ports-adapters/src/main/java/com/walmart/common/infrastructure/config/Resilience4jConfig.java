package com.walmart.common.infrastructure.config;

import io.strati.configuration.annotation.Configuration;
import io.strati.configuration.annotation.DefaultValue;
import io.strati.configuration.annotation.Property;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

@Configuration(configName = "Resilience4jConfig")
@Getter
@Setter
@ToString
@Slf4j
public class Resilience4jConfig {

  @DefaultValue.Int(5)
  @Property(propertyName = "circuitbreaker.default.minimumNumberOfCalls")
  private int minimumNumberOfCalls;

  @DefaultValue.Int(10)
  @Property(propertyName = "circuitbreaker.default.slidingWindowSize")
  private int slidingWindowSize;

  @DefaultValue.Int(3)
  @Property(propertyName = "circuitbreaker.default.permittedNumberOfCallsInHalfOpenState")
  private int permittedNumberOfCallsInHalfOpenState;

  @DefaultValue.Boolean(false)
  @Property(propertyName = "circuitbreaker.default.allowHealthIndicatorToFail")
  private boolean allowHealthIndicatorToFail;

  @DefaultValue.Int(3)
  @Property(propertyName = "retry.default.maxRetryAttempts")
  private int maxRetryAttempts;

  @DefaultValue.Int(500)
  @Property(propertyName = "retry.default.waitDuration")
  private int retryWaitDuration;

  @DefaultValue.Int(50)
  @Property(propertyName = "circuitbreaker.default.failureRateThreshold")
  private int failureRateThreshold;

  @DefaultValue.Int(100)
  @Property(propertyName = "bulkhead.default.maxConcurrentCalls")
  private int bulkheadMaxConcurrentCalls;

  @DefaultValue.Int(10)
  @Property(propertyName = "retry.default.scheduledThreadPoolSize")
  private int retryScheduledThreadPoolSize;
}
