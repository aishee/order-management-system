package com.walmart.common.domain.event.processing;

/** An Event going out from a domain. */
public interface IEgressEvent {

  /** Applies the mapping to convert T to R */
  void applyMapping();

  /** converts the mapped object to an xml string and assigns it to message object. */
  void createXmlMessage();

  /** converts the mapped object to an json string and assigns it to message object. */
  void createJsonMessage();

  /** @return true if the mappedObject is not null otherwise returns false */
  boolean isMappingApplied();

  /** Mark the event as produced. */
  void markAsProduced();

  /** Mark the event as failed. */
  void markAsFailed();

  /** Mark the event as READY_TO_PUBLISH */
  void markAsReadyToPublish();

  /** Mark the event as ERROR */
  void markAsError();

  /** Mark the event as INITIAL */
  void markAsInitial();

  /** Increment the retry count. */
  int tryAgain();

  /**
   * Returns true if this event is in failed status.
   *
   * @return true if the event is in failed state.
   */
  boolean isFailed();

  /** @return true if the event is in error state. */
  boolean markedAsError();
}
