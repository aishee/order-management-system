package com.walmart.common.metrics

import com.walmart.common.domain.valueobject.MetricsValueObject
import io.micrometer.core.instrument.Counter
import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.Timer
import spock.lang.Specification

class MetricServiceSpec extends Specification {
    Counter counter = Mock()
    Timer timer = Mock()
    MeterRegistry meterRegistry = Mock()
    MetricService metricService

    def setup() {
        metricService = new MetricService(
                meterRegistry: meterRegistry
        )
    }

    def "incrementCounterByType Success"() {
        given:
        meterRegistry.counter(_, _, _, _, _) >> counter
        when:
        metricService.incrementCounterByType(MetricConstants.MetricCounters.PRIMARY_PORT_INVOCATION.getCounter(), "sample")
        then:
        noExceptionThrown()
    }

    def "incrementCounterByType Exception"() {
        given:
        meterRegistry.counter(_, _, _, _, _) >> { throw new Exception() }
        when:
        metricService.incrementCounterByType(MetricConstants.MetricCounters.PRIMARY_PORT_INVOCATION.getCounter(), "sample")
        then:
        noExceptionThrown()
    }

    def "recordPrimaryPortMetrics Success status OK"() {
        given:
        meterRegistry.timer(_, _, _, _, _, _, _, _, _) >> timer
        when:
        metricService.recordPrimaryPortMetrics(3543L, "OK")
        then:
        noExceptionThrown()
    }

    def "recordPrimaryPortMetrics Success status 200"() {
        given:
        meterRegistry.timer(_, _, _, _, _, _, _, _, _) >> timer
        when:
        metricService.recordPrimaryPortMetrics(3543L, "200")
        then:
        noExceptionThrown()
    }

    def "recordPrimaryPortMetrics Exception"() {
        given:
        meterRegistry.timer(_, _, _, _, _, _, _, _, _) >> { throw new Exception() }
        when:
        metricService.recordPrimaryPortMetrics(3543L, "200")
        then:
        noExceptionThrown()
    }

    def "recordSecondaryPortMetrics Success"() {
        given:
        meterRegistry.timer(_, _, _, _, _) >> timer
        when:
        metricService.recordSecondaryPortMetrics(3543L, "sampleMethodName")
        then:
        noExceptionThrown()
    }

    def "recordSecondaryPortMetrics Exception"() {
        given:
        meterRegistry.timer(_, _, _, _, _) >> { new Exception() }
        when:
        metricService.recordSecondaryPortMetrics(3543L, "sampleMethodName")
        then:
        noExceptionThrown()
    }

    def "incrementExceptionCounterByType Success"() {
        given:
        meterRegistry.counter(_, _, _, _, _) >> counter
        when:
        metricService.incrementExceptionCounterByType(MetricConstants.MetricCounters.UBER_INVOCATION.getCounter(), "sample", "OMSBadRequestException")
        then:
        noExceptionThrown()
    }

    def "incrementExceptionCounterByType Exception"() {
        given:
        meterRegistry.counter(_, _, _, _, _) >> { throw new Exception() }
        when:
        metricService.incrementExceptionCounterByType(MetricConstants.MetricCounters.UBER_INVOCATION.getCounter(), "sample", "OMSBadRequestException")
        then:
        noExceptionThrown()
    }

    def "recordExecutionTime Success"() {
        given:
        meterRegistry.timer(_, _, _, _, _, _, _) >> timer
        when:
        metricService.recordExecutionTime(getMetricsValueObjectSuccess(MetricConstants.MetricTypes.UBER_ACCEPT_ORDER.getType()))
        then:
        noExceptionThrown()
    }

    def "recordExecutionTime Exception"() {
        given:
        meterRegistry.timer(_, _, _, _, _, _, _) >> { new Exception() }
        when:
        metricService.recordExecutionTime(getMetricsValueObjectException(MetricConstants.MetricTypes.UBER_ACCEPT_ORDER.getType()))
        then:
        noExceptionThrown()
    }

    private MetricsValueObject getMetricsValueObjectSuccess(String type) {
        return MetricsValueObject.builder()
                .counterName(MetricConstants.MetricCounters.UBER_INVOCATION.getCounter())
                .totalExecutionTime(1132L)
                .type(type)
                .isSuccess(true)
                .status("200")
                .build();
    }

    private MetricsValueObject getMetricsValueObjectException(String type) {
        return MetricsValueObject.builder()
                .counterName(MetricConstants.MetricCounters.UBER_EXCEPTION.getCounter())
                .totalExecutionTime(1132L)
                .type(type)
                .isSuccess(false)
                .status("400")
                .build();
    }
}
