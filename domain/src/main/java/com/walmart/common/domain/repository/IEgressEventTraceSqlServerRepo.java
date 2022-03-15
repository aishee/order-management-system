package com.walmart.common.domain.repository;

import com.walmart.common.domain.event.processing.EgressEvent;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IEgressEventTraceSqlServerRepo extends JpaRepository<EgressEvent, UUID> {
  EgressEvent findByDomainModelIdAndDomainAndName(
      String domainModelId, String domain, String eventName);

  EgressEvent findByDomainModelId(String domainModelUniqueId);
}
