package com.walmart.oms.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.walmart.oms.domain.error.exception.OMSBadRequestException;
import java.io.Serializable;
import java.time.LocalDateTime;
import javax.validation.constraints.NotNull;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class OmsOrderReplayRequest implements Serializable {
  @NotNull private LocalDateTime createStartDateTime;

  @NotNull private LocalDateTime createEndDateTime;

  public void validate() {
    if (createStartDateTime.isAfter(createEndDateTime)) {
      throw new OMSBadRequestException(
          "createStartDateTime must be equal or before createEndDateTime.");
    }
  }
}
