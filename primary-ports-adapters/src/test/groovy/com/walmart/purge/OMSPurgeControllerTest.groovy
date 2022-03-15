package com.walmart.purge

import com.walmart.common.metrics.MetricService
import com.walmart.oms.domain.error.exception.OMSThirdPartyException
import com.walmart.purge.response.OmsPurgeResponse
import reactor.core.publisher.Mono
import spock.lang.Specification

import java.util.concurrent.Callable

class OMSPurgeControllerTest extends Specification {
    OmsPurgeService omsPurgeService = Mock()
    MetricService metricService = Mock()
    OmsPurgeController controller

    def setup() {
        controller = new OmsPurgeController(omsPurgeService, metricService)
    }

    def "Test OMS Purge Successfully"() {
        given:
        Mono<String> resultMono = Mono.fromCallable(new Callable<String>() {
            @Override
            String call() {
                return "Purge completed successfully."
            }
        })
        omsPurgeService.purgeHistoricalData() >> resultMono

        when:
        Mono<OmsPurgeResponse> response = controller.purgeHistoricalData()

        then:
        response.block().getMessage() == "Purge completed successfully."
    }

    def "Test OMS Purge Fail"() {
        given:
        omsPurgeService.purgeHistoricalData() >> { throw new OMSThirdPartyException("Any Exception") }

        when:
        controller.purgeHistoricalData()

        then:
        thrown(OMSThirdPartyException)
    }

}
