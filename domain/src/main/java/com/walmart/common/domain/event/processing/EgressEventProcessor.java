package com.walmart.common.domain.event.processing;

import com.walmart.common.domain.BaseEntity;
import java.util.concurrent.ExecutorService;
import java.util.function.Consumer;
import org.springframework.util.Assert;

/** An event processor for all the {@link EgressEvent} */
public interface EgressEventProcessor<T extends BaseEntity, R, W> {
  /**
   * Process the {@link EgressEvent}
   *
   * @param event An egress event {@link EgressEvent}x
   */
  default void processAsync(EgressEvent<T, R> event) {
    Assert.notNull(event, "event cannot be null !!!");
    ExecutorService executor = getExecutorService();
    process(executor, event, this::processCallback);
  }

  /**
   * Process the egress event in a child thread using an {@link ExecutorService}
   *
   * @param executorService an {@link ExecutorService}
   * @param event An egress event {@link EgressEvent}
   * @param callback A callback handle for post processing.
   */
  default void process(
      ExecutorService executorService,
      EgressEvent<T, R> event,
      Consumer<EventResponse<W>> callback) {
    executorService.submit(
        () -> {
          EventResponse<W> result = execute(event);
          callback.accept(result);
        });
  }

  /**
   * Process the {@link EgressEvent} synchronously.
   *
   * @param event An egress event {@link EgressEvent}
   * @return the EventResponse for the interaction.
   */
  default EventResponse<W> process(EgressEvent<T, R> event) {
    Assert.notNull(event, "event cannot be null !!!");
    return execute(event);
  }

  /**
   * Prepares the payload execute the event processing.
   *
   * @param event An egress event {@link EgressEvent}
   * @return an Event response object.
   */
  EventResponse<W> execute(EgressEvent<T, R> event);

  /**
   * Performs any callback actions.
   *
   * @param response An egress event response {@link EventResponse}
   */
  void processCallback(EventResponse<W> response);

  /**
   * Returns a reference to the interactor which is configured.
   *
   * @return An {@link Interactor}
   */
  Interactor<T, R, W> getInteractor();

  /**
   * Returns a reference to the executor service.
   *
   * @return an {@link ExecutorService}
   */
  ExecutorService getExecutorService();
}
