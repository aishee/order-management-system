package com.walmart.oms.infrastructure.gateway.iro.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import lombok.NoArgsConstructor;

@lombok.Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class IROResponse implements Serializable {

  @JsonProperty("data")
  private Data data;

  public List<IRORootItems> getItems() {
    return data.getItems();
  }

  @lombok.Data
  @NoArgsConstructor
  public static class Data implements Serializable {
    @JsonProperty("uber_item")
    private UberItem uberItem;

    public List<IRORootItems> getItems() {
      return uberItem.getItems();
    }

    public List<String> getInvalidItemSkuIds() {
      return uberItem.getInvalidItemSkuIds();
    }
  }

  @lombok.Data
  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class UberItem implements Serializable {

    @JsonProperty("items")
    private List<IRORootItems> items;

    @JsonProperty("overall_invalid_items")
    private OverallInvalidItems invalidItemIds = new OverallInvalidItems();

    public List<String> getInvalidItemSkuIds() {
      return invalidItemIds.getInvalidItems();
    }
  }

  @lombok.Data
  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class OverallInvalidItems implements Serializable {

    @JsonProperty("invalid_items")
    private List<String> invalidItems = new ArrayList<>();

    @JsonProperty("invalid_prices")
    private List<String> invalidPrices = new ArrayList<>();
  }

  @JsonIgnore
  public List<String> getInvalidItems() {
    return data.getInvalidItemSkuIds();
  }

  @JsonIgnore
  public boolean containsInvalidItems() {
    return !data.getInvalidItemSkuIds().isEmpty();
  }
}
