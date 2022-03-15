package com.walmart.oms.infrastructure.gateway.iro.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import java.util.List;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class IROItem implements Serializable {

  @JsonProperty("sku_id")
  private String skuId;

  @JsonProperty("cin")
  private String cin;

  @JsonProperty("upc_numbers")
  private List<String> upcNumbers;

  @JsonProperty("brand")
  private String brand;

  @JsonProperty("item_name")
  private String itemName;

  @JsonProperty("name")
  private String name;

  @JsonProperty("picker_desc")
  private String pickerDesc;

  @JsonProperty("sales_unit")
  private String salesUnit;

  @JsonProperty("images")
  private Images images;

  @JsonProperty("untraited_stores")
  private List<String> untraitedStores;

  @JsonProperty("extended_item_info")
  private ExtendedItemInfo extendedItemInfo;

  @JsonProperty("freshness_info")
  private FreshnessInfo freshnessInfo;

  @lombok.Data
  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class Images implements Serializable {

    @JsonProperty("scene7_id")
    private String scene7Id;

    @JsonProperty("scene7_host")
    private String scene7Host;
  }

  @lombok.Data
  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class FreshnessInfo implements Serializable {

    @JsonProperty("min_ideal_day_value")
    private Integer minIdealDayValue;

    @JsonProperty("max_ideal_day_value")
    private Integer maxIdealDayValue;

    @JsonProperty("is_sell_by_date_required")
    private Boolean isSellByDateRequired;
  }

  @lombok.Data
  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class ExtendedItemInfo implements Serializable {

    @JsonProperty("weight")
    private String weight;

    @JsonProperty("replenish_unit_indicator")
    private String replenishUnitIndicator;
  }
}
