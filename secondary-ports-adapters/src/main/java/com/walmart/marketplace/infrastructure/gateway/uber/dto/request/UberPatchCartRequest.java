package com.walmart.marketplace.infrastructure.gateway.uber.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import java.io.Serializable;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
@AllArgsConstructor
@Builder
public class UberPatchCartRequest implements Serializable {

  private List<FulfillmentIssue> fulfillmentIssues;

  @Data
  @JsonIgnoreProperties(ignoreUnknown = true)
  @JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
  @AllArgsConstructor
  @Builder
  public static class FulfillmentIssue implements Serializable {
    private FulfillmentIssueType fulfillmentIssueType;

    @JsonInclude(Include.NON_NULL)
    private FulfillmentActionType fulfillmentActionType;

    private Item rootItem;

    @JsonInclude(Include.NON_NULL)
    private ItemAvailabilityInfo itemAvailabilityInfo;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
    @AllArgsConstructor
    @Builder
    public static class Item implements Serializable {
      private String instanceId;

      public static FulfillmentIssue.Item getFulfillmentItem(String instanceId) {
        return FulfillmentIssue.Item.builder().instanceId(instanceId).build();
      }
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
    @AllArgsConstructor
    @Builder
    public static class ItemAvailabilityInfo implements Serializable {
      private int itemsAvailable;

      public static ItemAvailabilityInfo getFulfillmentItemAvailability(int itemsAvailable) {
        return ItemAvailabilityInfo.builder().itemsAvailable(itemsAvailable).build();
      }
    }
  }
}
