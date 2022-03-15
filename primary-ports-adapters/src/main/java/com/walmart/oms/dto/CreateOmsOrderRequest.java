package com.walmart.oms.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class CreateOmsOrderRequest implements Serializable {

  @Valid
  @NotNull
  @JsonProperty(value = "data")
  private CreateOmsOrderRequest.CreateOmsOrderRequestData data;

  public CreateOmsOrderRequest(
      @JsonProperty(value = "data", required = true)
          CreateOmsOrderRequest.CreateOmsOrderRequestData data) {
    this.data = data;
  }

  @Data
  @JsonIgnoreProperties(ignoreUnknown = true)
  @Builder
  public static class CreateOmsOrderRequestData implements Serializable {

    private OmsOrderDto order;
  }
}
