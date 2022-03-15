package com.walmart.oms.dto;

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
public class OmsOrderResponse {

  @JsonProperty("data")
  private OmsOrderResponseData data;

  @JsonProperty("errors")
  private List<OmsError> errors;

  @Data
  @Builder
  public static class OmsOrderResponseData implements Serializable {

    @NotBlank
    @JsonProperty("order")
    private OmsOrderDto order;
  }

  @Data
  @AllArgsConstructor
  @Builder
  public static class OmsError implements Serializable {

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
