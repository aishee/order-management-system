package com.walmart.common.domain.event.processing;

import com.walmart.common.domain.BaseEntity;
import java.util.Optional;
import org.springframework.util.Assert;

/**
 * An abstraction which traces all outbound interactions. The trace records are persisted for audit
 * and replays.
 */
public interface EgressEventTracer<T extends BaseEntity, R> {

  /**
   * Retrieves an {@link EgressEvent} from the repository.
   *
   * @param event an instance of {@link EgressEvent}
   * @return a {@link Optional}<{@link EgressEvent}<T, R>>
   */
  EgressEvent<T, R> get(EgressEvent<T, R> event);

  /**
   * @param event an instance of {@link EgressEvent}
   * @return a copy of the saved {@link EgressEvent}
   */
  EgressEvent<T, R> save(EgressEvent<T, R> event);

  /**
   * Max number of retries that can be performed on an event.
   *
   * @return the max number of retries possible as an int.
   */
  int maxRetries();

  /**
   * Trace any change happened on the {@link EgressEvent}
   *
   * @param event An {@link EgressEvent} which needs tracing.
   */
  default EgressEvent<T, R> trace(EgressEvent<T, R> event) {
    Assert.notNull(event, "Event must not be null !!");
    EgressEvent<T, R> savedCopy = get(event);
    if (savedCopy != null) {
      if (savedCopy.markedAsError()) {
        throw new IllegalStateException("Event is in error state, no tracing permitted !!");
      } else if (savedCopy.isFailed()) {
        if (savedCopy.getRetries() < maxRetries()) {
          savedCopy.copy(event);
          savedCopy.tryAgain();
        } else {
          savedCopy.markAsError();
        }
      } else {
        savedCopy.copy(event);
      }
      savedCopy = save(savedCopy);
    } else {
      savedCopy = save(event);
    }
    return savedCopy;
  }
}
