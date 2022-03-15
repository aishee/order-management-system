package com.walmart.common.domain.valueobject;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
@AllArgsConstructor
public class MetricsValueObject {

  private String counterName;
  private String type;
  private long totalExecutionTime;
  private String status;
  private boolean isSuccess;
}
