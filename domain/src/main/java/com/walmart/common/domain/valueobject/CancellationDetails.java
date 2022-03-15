package com.walmart.common.domain.valueobject;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.walmart.common.domain.type.CancellationSource;
import java.io.Serializable;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class CancellationDetails implements Serializable {
  String cancelledReasonCode;

  CancellationSource cancelledBy;

  String cancelledReasonDescription;

}