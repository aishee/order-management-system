package com.walmart.common.domain.event.processing;

import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * A container which encapsulate the raw response for a sync call to a gateway and also provides
 * methods to deserialize the raw XML or Json message to an object. All methods are exception safe.
 */
@NoArgsConstructor
public class EventResponse<W> implements IEventResponse {
  /** The event for which the response got generated. */
  @Getter private EgressEvent<?, ?> event;
  /** The response object. */
  @Getter private W response;

  public EventResponse(EgressEvent<?, ?> event, W response) {
    this.response = response;
    this.event = event.makeLiteCopy();
  }

  @Override
  public boolean isSuccess() {
    return this.event.getStatus() == EgressEvent.EgressStatus.PRODUCED;
  }

  @Override
  public void addEvent(EgressEvent event) {
    this.event = event.makeLiteCopy();
  }
}
