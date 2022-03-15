package com.walmart.common.domain.repository

import com.walmart.common.domain.event.processing.EgressEvent
import spock.lang.Specification

class EgressEventTracerRepositorySpec extends Specification {

    public String domainUniqueId = UUID.randomUUID().toString()

    IEgressEventTraceSqlServerRepo egressEventTraceSqlServerRepo = Mock()
    EgressEventTracerRepository egressEventTracerRepository
    EgressEvent egressEvent = Mock()


    def setup() {
        egressEventTracerRepository = new EgressEventTracerRepositoryImp(
                egressEventTraceSqlServerRepo: egressEventTraceSqlServerRepo)

    }

    def "Check Error for get EgressEvent from Repository mandatory parameters: domainUniqueId,domain,eventName"() {
        when:
        egressEventTracerRepository.get(domainUniqueId, domain, eventName)
        then:
        thrown(IllegalArgumentException)
        where:
        domainUniqueId | domain | eventName
        null           | null   | null
        null           | "FMS"  | null
        null           | null   | "Order Download"
        null           | "FMS"  | "Order Download"
        "12345"        | "FMS"  | null
        "12345"        | null   | "Order Download"
    }


    def "Check Success for get EgressEvent from Repository mandatory parameters: domainUniqueId,domain,eventName"() {
        when:
        egressEventTracerRepository.get(domainUniqueId, domain, eventName)
        then:
        1 * egressEventTraceSqlServerRepo
                .findByDomainModelIdAndDomainAndName(domainUniqueId, domain, eventName) >> egressEvent
        where:
        domainUniqueId | domain | eventName
        "12345"        | "FMS"  | "Order Download"
    }

    def "test get EgressEvent with blank domainModelId"() {
        given:
        String domainModelId = ""
        when:
        egressEventTracerRepository.get(domainModelId)
        then:
        thrown(IllegalArgumentException)
    }

    def "test get EgressEvent with valid domainModelId"() {
        given:
        String domainModelId = "testId"
        when:
        egressEventTracerRepository.get(domainModelId)
        then:
        1 * egressEventTraceSqlServerRepo.findByDomainModelId(_) >> egressEvent
        noExceptionThrown()
    }

    def "test Save EgressEvent"() {
        when:
        egressEventTracerRepository.save(egressEvent)
        then:
        1 * egressEventTraceSqlServerRepo.save(egressEvent) >> egressEvent
    }
}
