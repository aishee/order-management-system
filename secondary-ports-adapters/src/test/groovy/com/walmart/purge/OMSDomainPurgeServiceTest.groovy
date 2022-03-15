package com.walmart.purge

import com.walmart.purge.repository.OmsDomainPurgeRepository
import reactor.core.publisher.Mono
import spock.lang.Specification

import java.util.concurrent.Callable

class OMSDomainPurgeServiceTest extends Specification {
    OmsDomainPurgeRepository omsDomainPurgeRepository = Mock()
    OmsDomainPurgeService domainPurgeService;

    def setup() {
        domainPurgeService = new OmsDomainPurgeService(omsDomainPurgeRepository)
    }

    def "Test Egress Event Purge True"() {
        given:
        omsDomainPurgeRepository.purgeEgressEvents(1000) >> prepare(true)

        when:
        Mono<Boolean> result = domainPurgeService.purgeEgressEvents(1000)

        then:
        result.block()
    }

    def "Test Egress Event Purge False"() {
        given:
        omsDomainPurgeRepository.purgeEgressEvents(1000) >> prepare(false)

        when:
        Mono<Boolean> result = domainPurgeService.purgeEgressEvents(1000)

        then:
        !result.block()
    }

    def "Test Market Place Order Purge True"() {
        given:
        omsDomainPurgeRepository.purgeMarketPlaceOrder(1000) >> prepare(true)

        when:
        Mono<Boolean> result = domainPurgeService.purgeMarketPlaceOrder(1000)

        then:
        result.block()
    }

    def "Test Market Place Order Purge False"() {
        given:
        omsDomainPurgeRepository.purgeMarketPlaceOrder(1000) >> prepare(false)

        when:
        Mono<Boolean> result = domainPurgeService.purgeMarketPlaceOrder(1000)

        then:
        !result.block()
    }

    def "Test Fulfilment Order Purge True"() {
        given:
        omsDomainPurgeRepository.purgeFulfilmentOrder(1000) >> prepare(true)

        when:
        Mono<Boolean> result = domainPurgeService.purgeFulfilmentOrder(1000)

        then:
        result.block()
    }

    def "Test Fulfilment Order Purge False"() {
        given:
        omsDomainPurgeRepository.purgeFulfilmentOrder(1000) >> prepare(false)

        when:
        Mono<Boolean> result = domainPurgeService.purgeFulfilmentOrder(1000)

        then:
        !result.block()
    }

    def "Test Oms Order Purge True"() {
        given:
        omsDomainPurgeRepository.purgeOmsOrder(1000) >> prepare(true)

        when:
        Mono<Boolean> result = domainPurgeService.purgeOmsOrder(1000)

        then:
        result.block()
    }

    def "Test Oms Order Purge False"() {
        given:
        omsDomainPurgeRepository.purgeOmsOrder(1000) >> prepare(false)

        when:
        Mono<Boolean> result = domainPurgeService.purgeOmsOrder(1000)

        then:
        !result.block()
    }

    def "Test Marketplace Event Purge True"() {
        given:
        omsDomainPurgeRepository.purgeMarketPlaceEvent(1000) >> prepare(true)

        when:
        Mono<Boolean> result = domainPurgeService.purgeMarketPlaceEvent(1000)

        then:
        result.block()
    }

    def "Test Marketplace Event Purge False"() {
        given:
        omsDomainPurgeRepository.purgeMarketPlaceEvent(1000) >> prepare(false)

        when:
        Mono<Boolean> result = domainPurgeService.purgeMarketPlaceEvent(1000)

        then:
        !result.block()
    }

    private Mono<Boolean> prepare(Boolean status) {
        return Mono.fromCallable(new Callable<Boolean>() {
            @Override
            Boolean call() throws Exception {
                return status
            }
        })
    }
}
