package com.walmart.oms.infrastructure.gateway.iro.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder(toBuilder = true)
public class IRORequest implements Serializable {
  @JsonProperty("item_ids")
  private List<String> itemIds;

  @JsonProperty("item_id_type")
  private String itemIdType;

  @JsonProperty("store_id")
  private String storeId;

  @JsonProperty("ship_on_date")
  private String shipOnDate;

  @JsonProperty("consumer_contract")
  private String consumerContract;

  @JsonProperty("request_origin")
  private String requestOrigin;
}
