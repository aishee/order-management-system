package com.walmart.oms.infrastructure.gateway.iro.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class IRORootItems implements Serializable {

  @JsonProperty("item_id")
  private String itemId;

  @JsonProperty("is_bundle")
  private boolean isBundle;

  @JsonProperty("item")
  private IROItem iroItem;

  @JsonProperty("price")
  private IROPrice iroPrice;

  @JsonProperty("promotion_info")
  private List<IROPromotionInfo> iroPromotionInfo;

  public List<IROPromotionInfo> getIroPromotionInfo() {
    return Optional.ofNullable(iroPromotionInfo).orElse(Collections.emptyList());
  }
}
