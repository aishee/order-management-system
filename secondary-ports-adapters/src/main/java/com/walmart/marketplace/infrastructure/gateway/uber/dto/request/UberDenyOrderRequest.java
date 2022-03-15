package com.walmart.marketplace.infrastructure.gateway.uber.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import java.io.Serializable;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
@AllArgsConstructor
@Builder
public class UberDenyOrderRequest implements Serializable {

  private Reason reason;

  @Data
  @JsonIgnoreProperties(ignoreUnknown = true)
  @AllArgsConstructor
  @Builder
  public static class Reason implements Serializable {

    private String explanation;

    private List<String> outOfStockItems;

    private List<String> invalidItems;
  }
}
