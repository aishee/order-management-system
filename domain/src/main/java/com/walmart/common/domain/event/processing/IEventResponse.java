package com.walmart.common.domain.event.processing;

/** The response object for an egress event. */
public interface IEventResponse {
  /** @return true if there was a failure on the client or server side. */
  default boolean isError() {
    return !isSuccess();
  }

  /** @return true if the interaction was a success. */
  boolean isSuccess();

  /**
   * Adds the event to the response.
   *
   * @param event
   */
  void addEvent(EgressEvent event);
}
