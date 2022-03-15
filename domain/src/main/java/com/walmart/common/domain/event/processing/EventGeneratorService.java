package com.walmart.common.domain.event.processing;

public interface EventGeneratorService {

  <T extends Message> void publishApplicationEvent(T domainEntity);
}
