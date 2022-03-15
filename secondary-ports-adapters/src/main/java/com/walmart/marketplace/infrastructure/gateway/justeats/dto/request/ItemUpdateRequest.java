package com.walmart.marketplace.infrastructure.gateway.justeats.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class ItemUpdateRequest {

  @JsonProperty("restaurant")
  private String restaurant;

  @JsonProperty("happenedAt")
  private String happenedAt;

  @JsonProperty("nextAvailableAt")
  private String nextAvailableAt;

  @JsonProperty("itemReferences")
  private List<String> itemReferences;

  @JsonProperty("event")
  private ItemUpdateEventType event;
}
