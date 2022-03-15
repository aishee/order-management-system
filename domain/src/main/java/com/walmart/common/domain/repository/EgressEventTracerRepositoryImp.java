package com.walmart.common.domain.repository;

import com.walmart.common.domain.event.processing.EgressEvent;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class EgressEventTracerRepositoryImp implements EgressEventTracerRepository {
  @Autowired IEgressEventTraceSqlServerRepo egressEventTraceSqlServerRepo;

  @Override
  public EgressEvent get(String domainModelId, String domain, String eventName) {
    if (StringUtils.isNotBlank(domainModelId)
        && StringUtils.isNotBlank(domain)
        && StringUtils.isNotBlank(eventName)) {
      return egressEventTraceSqlServerRepo.findByDomainModelIdAndDomainAndName(
          domainModelId, domain, eventName);
    } else {
      throw new IllegalArgumentException();
    }
  }

  @Override
  public EgressEvent get(String domainModelId) {
    if (StringUtils.isNotBlank(domainModelId)) {
      return egressEventTraceSqlServerRepo.findByDomainModelId(domainModelId);
    } else {
      throw new IllegalArgumentException();
    }
  }

  @Override
  public EgressEvent save(EgressEvent event) {
    return egressEventTraceSqlServerRepo.save(event);
  }
}
