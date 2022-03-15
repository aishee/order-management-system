package com.walmart.common.infrastructure.config;

import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryRegistry;
import java.util.Arrays;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class RetryLoggingListenerConfig {

  @Autowired protected RetryRegistry retryRegistry;

  @PostConstruct
  public void initialize() {
    Arrays.stream(RetryClients.values())
        .collect(Collectors.toList())
        .forEach(retryClient -> attachLoggingListener(retryRegistry.retry(retryClient.name())));
  }

  private void attachLoggingListener(@NonNull Retry retry) {
    String retryName = retry.getName();
    retry
        .getEventPublisher()
        .onRetry(event -> log.debug("{} Retrying event: {}", retryName, event))
        .onSuccess(
            event -> log.debug("{} Retry: Successfully sent request event:{}", retryName, event))
        .onError(event -> log.error("{} Retry: Error in event:{}", retryName, event));
  }

  public enum RetryClients {
    IRO,
    UBER,
    PYSIPYP,
    TAX,
    JUST_EATS
  }
}
