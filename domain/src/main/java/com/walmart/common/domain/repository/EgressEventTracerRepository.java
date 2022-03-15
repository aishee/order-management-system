package com.walmart.common.domain.repository;

import com.walmart.common.domain.event.processing.EgressEvent;

/** A repository for EgressEvents */
public interface EgressEventTracerRepository {
  /**
   * Retrieves event object from repository
   *
   * @param domainModelUniqueId
   * @param domain
   * @param eventName
   * @return a copy of {@link EgressEvent}
   */
  EgressEvent get(String domainModelUniqueId, String domain, String eventName);

  /**
   * Retrieves event object from repository
   *
   * @param domainModelUniqueId
   * @return a copy of {@link EgressEvent}
   */
  EgressEvent get(String domainModelUniqueId);

  /**
   * Save the event to repository
   *
   * @param event
   * @return saved copy of {@link EgressEvent}
   */
  EgressEvent save(EgressEvent event);
}
