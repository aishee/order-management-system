package com.walmart.common.domain.event.processing;

import com.walmart.common.domain.BaseEntity;
import java.util.Optional;
import org.springframework.util.Assert;

/** Produces a message to a destination */
public interface Interactor<T extends BaseEntity, R, W> {
  /**
   * Produces a message to the destination Asynchronously.
   *
   * @param event An {@link EgressEvent}
   */
  default Optional<EventResponse<W>> interact(EgressEvent<T, R> event) {
    Assert.notNull(event, "event cannot be null !!");
    Assert.notNull(event.getMessage(), "message cannot be null !!");
    EventResponse<W> response;
    try {
      response = call(event);
      event.markAsProduced();
    } catch (Exception exception) {
      event.markAsFailed();
      throw exception;
    }
    return Optional.ofNullable(response);
  }

  /**
   * Call the underlying infrastructure call, which will host any client specific logic required for
   * this interaction.
   *
   * @param event event An {@link EgressEvent}
   * @param <T> Type of the domain model.
   * @param <R> Type of the integration object.
   * @return An instance of {@link EventResponse}
   */
  EventResponse<W> call(EgressEvent<T, R> event);
}
