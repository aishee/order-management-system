package com.walmart.common.infrastructure.config;

import io.github.resilience4j.bulkhead.Bulkhead;
import io.github.resilience4j.bulkhead.BulkheadRegistry;
import java.util.Arrays;
import java.util.stream.Collectors;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class BulkHeadLoggingListenerConfig {

  /**
   * Attaching Logging listener for Bulkhead Registry.
   *
   * @param bulkheadRegistry Instance provided by Resiliency.
   */
  @Autowired
  public BulkHeadLoggingListenerConfig(BulkheadRegistry bulkheadRegistry) {

    Arrays.stream(BulkHeadClients.values())
        .collect(Collectors.toList())
        .forEach(
            bulkHeadClients ->
                attachLoggingListener(bulkheadRegistry.bulkhead(bulkHeadClients.name())));
  }

  private void attachLoggingListener(@NonNull Bulkhead bulkhead) {
    String bulkheadName = bulkhead.getName();
    bulkhead
        .getEventPublisher()
        .onCallFinished(
            event -> log.debug("{} Bulkhead Finished Event found : {}", bulkheadName, event))
        .onCallPermitted(
            event -> log.debug("{} Bulkhead Permitted Event found : {}", bulkheadName, event))
        .onCallRejected(
            event -> log.error("{} Bulkhead Rejected Event found : {}", bulkheadName, event));
  }

  public enum BulkHeadClients {
    IRO,
    UBER,
    PYSIPYP,
    TAX,
    JUST_EATS
  }
}
