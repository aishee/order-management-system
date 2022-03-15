package com.walmart.oms.infrastructure.gateway.iro.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class IROPromotionInfo implements Serializable {

  @JsonProperty("rollback")
  private IROPromotionRollback rollback;

  @JsonProperty("base_promotion")
  private IROBasePromotion iroBasePromotion;

  @lombok.Data
  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class IROPromotionRollback implements Serializable {

    @JsonProperty("was_price")
    private String wasPrice;
  }
}
