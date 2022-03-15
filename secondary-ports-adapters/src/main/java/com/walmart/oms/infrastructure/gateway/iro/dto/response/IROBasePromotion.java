package com.walmart.oms.infrastructure.gateway.iro.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class IROBasePromotion implements Serializable {

  @JsonProperty("item_promo_type")
  private String itemPromoType;
}
