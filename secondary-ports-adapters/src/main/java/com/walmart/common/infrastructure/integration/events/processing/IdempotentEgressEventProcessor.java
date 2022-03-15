package com.walmart.common.infrastructure.integration.events.processing;

import static com.walmart.common.domain.event.processing.EgressEvent.MessageFormat.XML;

import com.walmart.common.domain.BaseEntity;
import com.walmart.common.domain.event.processing.EgressEvent;
import com.walmart.common.domain.event.processing.EgressEventProcessor;
import com.walmart.common.domain.event.processing.EgressEventTracer;
import com.walmart.common.domain.event.processing.EventResponse;
import com.walmart.common.domain.event.processing.Interactor;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.annotation.PreDestroy;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

/**
 * An idempotent event processor. It uses {@link EgressEventTracer} to trace the {@link EgressEvent}
 * at crucial transformations and saves the snapshot, so even if processEgressEvent() is called
 * multiple times the processing takes in to account the saved snapshot and its values before
 * re-processing.
 *
 * @see EgressEventTracer
 * @see EgressEvent
 * @see Interactor
 */
@Slf4j
@Component
@NoArgsConstructor
public abstract class IdempotentEgressEventProcessor<T extends BaseEntity, R, W>
    implements EgressEventProcessor<T, R, W> {

  private static final int DEFAULT_MAX_POOL_SIZE = 10;
  /** An instance of the executor service. */
  ExecutorService executorService;
  /** An instance of {@link EgressEventTracer} */
  @Autowired private EgressEventTracer<T, R> tracer;

  /**
   * Constructor with a tracer.
   *
   * @param tracer
   */
  protected IdempotentEgressEventProcessor(EgressEventTracer<T, R> tracer) {
    Assert.notNull(tracer, "Tracer cannot be null !!!");
    this.tracer = tracer;
  }

  /**
   * Constructor with executor and tracer.
   *
   * @param tracer
   * @param executorService
   */
  protected IdempotentEgressEventProcessor(
      EgressEventTracer<T, R> tracer, ExecutorService executorService) {
    Assert.notNull(tracer, "Tracer cannot be null !!!");
    Assert.notNull(executorService, "executorService cannot be null !!!");
    this.tracer = tracer;
    this.executorService = executorService;
  }

  @PreDestroy
  private void shutDownExecutor() {
    Optional.ofNullable(executorService).ifPresent(ExecutorService::shutdown);
  }

  /**
   * Returns the maximum number of threads that can be present in the pool.
   *
   * @return max pool size as an integer.
   */
  protected int maxPoolSize() {
    return DEFAULT_MAX_POOL_SIZE;
  }

  @Override
  public ExecutorService getExecutorService() {
    if (executorService == null) {
      executorService = Executors.newFixedThreadPool(maxPoolSize());
    }
    return executorService;
  }

  /**
   * Helper method to determine the message type and create either xml or json message.
   *
   * @param event An egress event {@link EgressEvent}t
   */
  protected void preparePayload(EgressEvent<T, R> event) {
    event.applyMapping();
    if (event.getFormat() == XML) {
      event.createXmlMessage();
    } else {
      event.createJsonMessage();
    }
  }

  @Override
  public void processCallback(EventResponse<W> response) {
    Assert.notNull(response, "response cannot be null !!");
    Assert.notNull(response.getEvent(), "event cannot be null !!");
    log.info(
        "Event processed name={}, id={}, status={}, retries={}, domain={}",
        response.getEvent().getName(),
        response.getEvent().getDomainModelId(),
        response.getEvent().getStatus(),
        response.getEvent().getRetries(),
        response.getEvent().getDomain());
  }

  @Override
  public EventResponse<W> execute(EgressEvent<T, R> event) {
    Assert.notNull(event, "event cannot be null !!!");
    EventResponse<W> response;
    Optional<EventResponse<W>> responseOptional = Optional.empty();
    try {
      preparePayload(event);
      responseOptional = getInteractor().interact(event);
    } catch (Exception ex) {
      log.error(
          "Error while publishing event. name={}, destination={}",
          event.getName(),
          event.getDestination(),
          ex);
    } finally {
      response = responseOptional.orElseGet(EventResponse::new);
      markFailureAsErrorForAuditEvent(event);
      response.addEvent(event);
      tracer.trace(event);
    }
    return response;
  }

  private void markFailureAsErrorForAuditEvent(EgressEvent<T, R> event) {
    if (event.isFailed()) {
      if (event.isJustAudit()) {
        event.markAsError();
      } else {
        event.markAsFailed();
      }
    }
  }
}
