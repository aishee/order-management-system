package com.walmart.common.metrics;

import static com.walmart.common.constants.CommonConstants.DOMAIN_KEY;

import com.walmart.common.domain.valueobject.MetricsValueObject;
import com.walmart.common.metrics.MetricConstants.MetricCounters;
import io.micrometer.core.instrument.MeterRegistry;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class MetricService {

  @Autowired private MeterRegistry meterRegistry;

  public void incrementCounterByType(String counterName, String type) {
    try {
      meterRegistry.counter(counterName, MetricConstants.TYPE, type).increment();
    } catch (Exception exception) {
      String message =
          String.format("Exception while recording counter %s", exception.getMessage());
      log.warn(message, exception);
    }
  }

  public String recordPrimaryPortMetrics(long totalExecutionTime, String status) {

    try {
      boolean isSuccess =
          status != null
              && ("OK".equalsIgnoreCase(status.trim()) || status.trim().equalsIgnoreCase("200"));
      meterRegistry
          .timer(
              MetricCounters.PRIMARY_PORT_INVOCATION.getCounter(),
              MetricConstants.API,
              MDC.get(MetricConstants.API) != null ? MDC.get(MetricConstants.API) : "unknown",
              DOMAIN_KEY,
              MDC.get(DOMAIN_KEY) != null ? MDC.get(DOMAIN_KEY) : "unknown",
              MetricConstants.STATUS,
              status,
              MetricConstants.IS_SUCCESS,
              Boolean.toString(isSuccess))
          .record(totalExecutionTime, TimeUnit.MILLISECONDS);
    } catch (Exception exception) {
      log.debug("Exception while recording primary port metrics, " + exception.getMessage());
    }
    return "totalExecutionTime_ms=" + totalExecutionTime;
  }

  public void recordSecondaryPortMetrics(long duration, String methodName) {
    try {
      meterRegistry
          .timer(
              MetricCounters.SECONDARY_PORT_INVOCATION.getCounter(),
              MetricConstants.METHOD_NAME,
              methodName,
              MetricConstants.IS_REPOSITORY,
              Boolean.toString(methodName.contains("Repository")))
          .record(duration, TimeUnit.MILLISECONDS);
    } catch (Exception exception) {
      log.warn("Exception while recording secondary port metrics " + exception.getMessage());
    }
  }

  public void incrementExceptionCounterByType(
      String counterName, String type, String exceptionType) {
    try {
      String message =
          String.format("Incrementing counter %s for %s: %s", counterName, type, exceptionType);
      log.debug(message);
      meterRegistry
          .counter(
              counterName,
              MetricConstants.TYPE,
              type,
              MetricConstants.EXCEPTION_TYPE,
              exceptionType)
          .increment();
    } catch (Exception exception) {
      String message =
          String.format(
              "Exception while recording counter %s : %s", counterName, exception.getMessage());
      log.warn(message, exception);
    }
  }

  public String recordExecutionTime(MetricsValueObject metricsValueObject) {
    try {
      meterRegistry
          .timer(
              metricsValueObject.getCounterName(),
              MetricConstants.TYPE,
              metricsValueObject.getType(),
              MetricConstants.STATUS,
              metricsValueObject.getStatus(),
              MetricConstants.IS_SUCCESS,
              Boolean.toString(metricsValueObject.isSuccess()))
          .record(metricsValueObject.getTotalExecutionTime(), TimeUnit.MILLISECONDS);
    } catch (Exception exception) {
      String message =
          String.format(
              "Exception while recording execution time metrics for counter %s : %s",
              metricsValueObject.getCounterName(), exception.getMessage());
      log.warn(message, exception);
    }
    return "totalExecutionTime_ms=" + metricsValueObject.getTotalExecutionTime();
  }
}
