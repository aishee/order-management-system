package com.walmart.purge

import com.walmart.common.domain.valueobject.MetricsValueObject
import com.walmart.common.metrics.MetricService
import com.walmart.purge.configuration.OmsPurgeConfig
import reactor.core.publisher.Mono
import spock.lang.Specification

import java.util.function.Supplier

class OMSPurgeServiceTest extends Specification {
    OmsDomainPurgeService omsDomainPurgeService = Mock()
    MetricService metricService = Mock()
    OmsPurgeConfig omsPurgeConfig = Mock()
    OmsPurgeService omsPurgeService

    def setup() {
        omsPurgeService = new OmsPurgeService(omsDomainPurgeService, metricService)
        omsPurgeService.setOmsPurgeConfig(omsPurgeConfig)
    }

    def "Test Purge Historical Data Success"() {
        given:
        Mono<Boolean> resultMono = Mono.fromSupplier(new Supplier<Boolean>() {
            @Override
            Boolean get() {
                return true
            }
        })
        omsPurgeConfig.getDayToSub() >> 1000
        omsDomainPurgeService.purgeEgressEvents(1000) >> resultMono
        omsDomainPurgeService.purgeMarketPlaceOrder(1000) >> resultMono
        omsDomainPurgeService.purgeFulfilmentOrder(1000) >> resultMono
        omsDomainPurgeService.purgeOmsOrder(1000) >> resultMono
        omsDomainPurgeService.purgeMarketPlaceEvent(1000) >> resultMono

        when:
        Mono<String> purgeStatus = omsPurgeService.purgeHistoricalData()

        then:
        purgeStatus.block() != null && purgeStatus.block() == "Purge request is accepted. System will execute purge based on input days."
    }

    def "Test Purge Historical Data Fail"() {
        given:
        MetricsValueObject metricsValueObject = Mock()
        metricService.recordExecutionTime(metricsValueObject) >> "12345"
        omsPurgeConfig.getDayToSub() >> 1000
        omsDomainPurgeService.purgeEgressEvents(1000) >> Mono.error(new Exception("Any Exception"))

        when:
        omsPurgeService.purgeHistoricalData().block()

        then:
        thrown(RuntimeException)
    }
}
