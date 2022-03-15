package com.walmart.fms.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import java.io.Serializable;
import javax.validation.Valid;
import lombok.Builder;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class CreateFmsOrderRequest {

  @Valid private CreateFmsOrderRequest.CreateFmsOrderRequestData data;

  public CreateFmsOrderRequest(
      @JsonProperty(value = "data", required = true)
          CreateFmsOrderRequest.CreateFmsOrderRequestData data) {
    this.data = data;
  }

  @Data
  @JsonIgnoreProperties(ignoreUnknown = true)
  @Builder
  public static class CreateFmsOrderRequestData implements Serializable {

    private FmsOrderDto order;
  }
}
