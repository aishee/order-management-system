package com.walmart.purge.repository

import com.walmart.oms.domain.error.exception.OMSThirdPartyException
import com.walmart.purge.configuration.OmsPurgeConfig
import reactor.core.publisher.Mono
import spock.lang.Specification

import javax.persistence.EntityManager
import javax.persistence.StoredProcedureQuery
import java.lang.reflect.Field

class OMSDomainPurgeRepositoryTest extends Specification {
    EntityManager entityManager = Mock();
    OmsPurgeConfig omsPurgeConfig = Mock()
    OmsDomainPurgeRepository domainPurgeRepository;

    def setup() {
        domainPurgeRepository = new OmsDomainPurgeRepository(entityManager: entityManager, omsPurgeConfig: omsPurgeConfig)
    }

    def "Test Init"() {
        given:
        omsPurgeConfig.getTtlSeconds() >> 10

        when:
        domainPurgeRepository.initElasticPool()

        then:
        Field elasticPoolField = domainPurgeRepository.getClass().getDeclaredField("elasticPool");
        elasticPoolField.setAccessible(true)
        elasticPoolField.get(domainPurgeRepository) != null
    }

    def "Test Egress Event Purge True"() {
        given:
        omsPurgeConfig.getTtlSeconds() >> 10
        domainPurgeRepository.initElasticPool()
        StoredProcedureQuery query = Mock()
        omsPurgeConfig.getEgressEventProcedure() >> "OMSCORE.EGRESS_EVENTS_PROCDR"
        entityManager.createStoredProcedureQuery(_ as String) >> query
        query.execute() >> true

        when:
        Mono<Boolean> result = domainPurgeRepository.purgeEgressEvents(1000)

        then:
        result.block()
    }

    def "Test Egress Event Purge False"() {
        given:
        omsPurgeConfig.getTtlSeconds() >> 10
        domainPurgeRepository.initElasticPool()
        StoredProcedureQuery query = Mock()
        omsPurgeConfig.getEgressEventProcedure() >> "OMSCORE.EGRESS_EVENTS_PROCDR"
        entityManager.createStoredProcedureQuery(_ as String) >> query

        when:
        Mono<Boolean> result = domainPurgeRepository.purgeEgressEvents(1000)

        then:
        !result.block()
    }

    def "Test Market Place Order Purge True"() {
        given:
        omsPurgeConfig.getTtlSeconds() >> 10
        domainPurgeRepository.initElasticPool()
        StoredProcedureQuery query = Mock()
        omsPurgeConfig.getMarketplaceOrderProcedure() >> "OMSCORE.MARKET_PLACE_ORDER_PROCDR"
        entityManager.createStoredProcedureQuery(_ as String) >> query
        query.execute() >> true

        when:
        Mono<Boolean> result = domainPurgeRepository.purgeMarketPlaceOrder(1000)

        then:
        result.block()
    }

    def "Test Market Place Order Purge False"() {
        given:
        omsPurgeConfig.getTtlSeconds() >> 10
        domainPurgeRepository.initElasticPool()
        StoredProcedureQuery query = Mock()
        omsPurgeConfig.getMarketplaceOrderProcedure() >> "OMSCORE.MARKET_PLACE_ORDER_PROCDR"
        entityManager.createStoredProcedureQuery(_ as String) >> query

        when:
        Mono<Boolean> result = domainPurgeRepository.purgeMarketPlaceOrder(1000)

        then:
        !result.block()
    }

    def "Test Fulfilment Order Purge True"() {
        given:
        omsPurgeConfig.getTtlSeconds() >> 10
        domainPurgeRepository.initElasticPool()
        StoredProcedureQuery query = Mock()
        omsPurgeConfig.getFulfilmentOrderProcedure() >> "OMSCORE.FULFILLMENT_ORDER_PROCDR"
        entityManager.createStoredProcedureQuery(_ as String) >> query
        query.execute() >> true

        when:
        Mono<Boolean> result = domainPurgeRepository.purgeFulfilmentOrder(1000)

        then:
        result.block()
    }

    def "Test Fulfilment Order Purge False"() {
        given:
        omsPurgeConfig.getTtlSeconds() >> 10
        domainPurgeRepository.initElasticPool()
        StoredProcedureQuery query = Mock()
        omsPurgeConfig.getFulfilmentOrderProcedure() >> "OMSCORE.FULFILLMENT_ORDER_PROCDR"
        entityManager.createStoredProcedureQuery(_ as String) >> query

        when:
        Mono<Boolean> result = domainPurgeRepository.purgeFulfilmentOrder(1000)

        then:
        !result.block()
    }

    def "Test Oms Order Purge True"() {
        given:
        omsPurgeConfig.getTtlSeconds() >> 10
        domainPurgeRepository.initElasticPool()
        StoredProcedureQuery query = Mock()
        omsPurgeConfig.getOmsOrderProcedure() >> "OMSCORE.OMS_ORDER_PROCDR"
        entityManager.createStoredProcedureQuery(_ as String) >> query
        query.execute() >> true

        when:
        Mono<Boolean> result = domainPurgeRepository.purgeOmsOrder(1000)

        then:
        result.block()
    }

    def "Test Oms Order Purge False"() {
        given:
        omsPurgeConfig.getTtlSeconds() >> 10
        domainPurgeRepository.initElasticPool()
        StoredProcedureQuery query = Mock()
        omsPurgeConfig.getOmsOrderProcedure() >> "OMSCORE.OMS_ORDER_PROCDR"
        entityManager.createStoredProcedureQuery(_ as String) >> query

        when:
        Mono<Boolean> result = domainPurgeRepository.purgeOmsOrder(1000)

        then:
        !result.block()
    }

    def "Test Marketplace Event Purge True"() {
        given:
        omsPurgeConfig.getTtlSeconds() >> 10
        domainPurgeRepository.initElasticPool()
        StoredProcedureQuery query = Mock()
        omsPurgeConfig.getMarketplaceEventProcedure() >> "OMSCORE.MARKET_PLACE_EVENTS_PROCDR"
        entityManager.createStoredProcedureQuery(_ as String) >> query
        query.execute() >> true

        when:
        Mono<Boolean> result = domainPurgeRepository.purgeMarketPlaceEvent(1000)

        then:
        result.block()
    }

    def "Test Marketplace Event Purge False"() {
        given:
        omsPurgeConfig.getTtlSeconds() >> 10
        domainPurgeRepository.initElasticPool()
        StoredProcedureQuery query = Mock()
        omsPurgeConfig.getMarketplaceEventProcedure() >> "OMSCORE.MARKET_PLACE_EVENTS_PROCDR"
        entityManager.createStoredProcedureQuery(_ as String) >> query

        when:
        Mono<Boolean> result = domainPurgeRepository.purgeMarketPlaceEvent(1000)

        then:
        !result.block()
    }

    def "Test Exceptionally"() {
        given:
        omsPurgeConfig.getTtlSeconds() >> 10
        domainPurgeRepository.initElasticPool()
        StoredProcedureQuery query = Mock()
        omsPurgeConfig.getMarketplaceEventProcedure() >> "OMSCORE.MARKET_PLACE_EVENTS_PROCDR"
        entityManager.createStoredProcedureQuery(_ as String) >> query
        query.execute() >> { throw new Exception("Any Exception") }

        when:
        domainPurgeRepository.purgeMarketPlaceEvent(1000).block()

        then:
        thrown(OMSThirdPartyException)
    }

    def "Test Exceptionally Null Query"() {
        given:
        omsPurgeConfig.getTtlSeconds() >> 10
        domainPurgeRepository.initElasticPool()
        omsPurgeConfig.getMarketplaceEventProcedure() >> "OMSCORE.MARKET_PLACE_EVENTS_PROCDR"

        when:
        domainPurgeRepository.purgeMarketPlaceEvent(1000).block()

        then:
        thrown(OMSThirdPartyException)
    }
}
