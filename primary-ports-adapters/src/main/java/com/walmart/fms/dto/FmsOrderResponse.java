package com.walmart.fms.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.walmart.oms.domain.error.ErrorType;
import java.io.Serializable;
import java.util.List;
import javax.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@Setter
public class FmsOrderResponse {

  @JsonProperty("data")
  private FmsOrderResponseData data;

  @JsonProperty("errors")
  private List<FmsError> errors;

  @Data
  @Builder
  public static class FmsOrderResponseData implements Serializable {

    @NotBlank
    @JsonProperty("order")
    private FmsOrderDto order;
  }

  @Data
  @AllArgsConstructor
  @Builder
  public static class FmsError implements Serializable {

    @JsonProperty("error_code")
    private ErrorType errorCode;

    @JsonProperty("error_message")
    private String errorMessage;

    @JsonProperty("code")
    private ErrorType code;

    @JsonProperty("type")
    private String type;

    @JsonProperty("message")
    private String message;
  }
}
