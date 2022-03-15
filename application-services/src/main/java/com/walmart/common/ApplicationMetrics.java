package com.walmart.common;

import com.walmart.common.domain.valueobject.MetricsValueObject;
import com.walmart.common.metrics.MetricConstants;
import org.springframework.http.HttpStatus;

/**
 * This abstract class define exception and success counter constants and help to record metrics.
 */
public abstract class ApplicationMetrics {

  protected abstract MetricConstants.MetricCounters getMetricsExceptionCounterName();

  protected abstract MetricConstants.MetricCounters getMetricsCounterName();

  protected abstract MetricConstants.MetricTypes getMetricsType();

  /**
   * @param startTime {@code start time of execution}
   * @param status {@link HttpStatus}
   * @return {@link MetricsValueObject}
   */
  protected MetricsValueObject getMetricsValueObject(long startTime, HttpStatus status) {
    boolean isSuccess = status.is2xxSuccessful();
    MetricConstants.MetricCounters metricsCounter =
        isSuccess ? getMetricsCounterName() : getMetricsExceptionCounterName();
    long duration = System.currentTimeMillis() - startTime;
    return MetricsValueObject.builder()
        .counterName(metricsCounter.getCounter())
        .totalExecutionTime(duration)
        .type(getMetricsType().getType())
        .isSuccess(isSuccess)
        .status(String.valueOf(status))
        .build();
  }
}
