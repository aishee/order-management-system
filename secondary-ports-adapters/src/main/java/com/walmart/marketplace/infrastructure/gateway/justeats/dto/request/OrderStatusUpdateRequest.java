package com.walmart.marketplace.infrastructure.gateway.justeats.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class OrderStatusUpdateRequest {

  @JsonProperty("happenedAt")
  private LocalDateTime happenedAt;

  @JsonProperty("errorCode")
  private DenialErrorCode errorCode;

  @JsonProperty("errorMessage")
  private String errorMessage;
}
