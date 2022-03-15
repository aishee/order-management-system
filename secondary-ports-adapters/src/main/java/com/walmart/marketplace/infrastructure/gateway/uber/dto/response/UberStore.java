package com.walmart.marketplace.infrastructure.gateway.uber.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import java.io.Serializable;
import java.util.List;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * This is a simple response class for Uber Store
 *
 * @apiNote https://api.uber.com/v1/eats/stores
 */
@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class UberStore implements Serializable {

  private String nextKey;

  private List<Store> stores;

  public List<String> getStoreIds() {
    return this.stores.stream().map(Store::getStoreId).collect(Collectors.toList());
  }

  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  @JsonIgnoreProperties(ignoreUnknown = true)
  @JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
  public static class Store implements Serializable {
    private String name;
    private String storeId;
    private String partnerStoreId;
  }
}
