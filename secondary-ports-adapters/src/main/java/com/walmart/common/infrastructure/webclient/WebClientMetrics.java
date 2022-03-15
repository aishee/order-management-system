package com.walmart.common.infrastructure.webclient;

import com.walmart.common.domain.valueobject.MetricsValueObject;
import org.springframework.http.HttpStatus;

public interface WebClientMetrics {

  String getMetricsExceptionCounterName();

  String getMetricsCounterName();

  default MetricsValueObject getMetricsValueObject(String type, long duration, String status) {
    boolean isSuccess =
        status != null && (String.valueOf(HttpStatus.OK.value()).equalsIgnoreCase(status.trim()));
    return MetricsValueObject.builder()
        .counterName(getMetricsCounterName())
        .totalExecutionTime(duration)
        .type(type)
        .isSuccess(isSuccess)
        .status(status)
        .build();
  }
}
