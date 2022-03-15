package com.walmart.marketplace.infrastructure.gateway.uber.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
@AllArgsConstructor
@Builder
public class UberUpdateItemRequest implements Serializable {
  private SuspensionInfo suspensionInfo;

  @Getter
  @JsonIgnoreProperties(ignoreUnknown = true)
  @JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
  @AllArgsConstructor
  @Builder
  public static class SuspensionInfo implements Serializable {
    private Suspension suspension;

    @Getter
    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
    @AllArgsConstructor
    @Builder
    public static class Suspension implements Serializable {
      private int suspendUntil;
      private String reason;
    }
  }
}
