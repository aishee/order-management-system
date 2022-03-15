package com.walmart.oms.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.walmart.common.domain.type.SubstitutionOption;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class OmsOrderItemDTO implements Serializable {

  @JsonProperty("sku_id")
  private String skuId;

  @JsonProperty("item_description")
  private String itemDescription;

  @JsonProperty("quantity")
  @Positive
  private long quantity;

  @JsonProperty("weight")
  private double weight;

  @JsonProperty("unit_price")
  @Positive
  private BigDecimal unitPrice;

  @JsonProperty("temperature_zone")
  @NotNull
  private String temperatureZone;

  @JsonProperty("uom")
  @NotNull
  private String uom;

  @JsonProperty("sales_unit")
  @NotNull
  private String salesUnit;

  @JsonProperty("image_url")
  private String imageURL;

  @JsonProperty("consumer_item_num")
  @NotNull
  private String consumerItemNumber;

  @JsonProperty("substitution_option")
  private SubstitutionOption substitutionOption;

  @JsonProperty("upcs")
  @NotNull
  @NotEmpty
  private List<OmsOrderItemUpc> orderItemUpcs;

  @JsonProperty("vendor_unit_price")
  @NotNull
  @NotEmpty
  private BigDecimal vendorUnitPrice;

  @JsonProperty("vendor_total_price")
  @NotNull
  @NotEmpty
  private BigDecimal vendorTotalPrice;

  @JsonProperty("picked_item")
  private PickedItemDto pickedItem;

  public SubstitutionOption getSubstitutionOption() {
    return Optional.ofNullable(substitutionOption)
        .orElse(SubstitutionOption.DO_NOT_SUBSTITUTE);
  }

  @Data
  @Builder
  public static class PickedItemDto implements Serializable {

    @JsonProperty("department_id")
    private String departmentId;

    @JsonProperty("consumer_item_num")
    @NotNull
    private String consumerItemNum;

    @JsonProperty("quantity")
    private long quantity;

    @JsonProperty("weight")
    private double weight;

    @JsonProperty("picked_item_description")
    private String pickedItemDescription;

    @JsonProperty("sell_by_date")
    private Date sellByDate;

    @JsonProperty("picked_by")
    private String pickedBy;

    @JsonProperty("unit_price")
    private BigDecimal unitPrice;

    @JsonProperty("adjusted_price_ex_vat")
    private BigDecimal adjustedPriceExVat;

    @JsonProperty("adjusted_price")
    private BigDecimal adjustedPrice;

    @JsonProperty("web_adjusted_price")
    private BigDecimal webAdjustedPrice;

    @JsonProperty("vat_amount")
    private BigDecimal vatAmount;

    @JsonProperty("picked_item_upcs")
    private List<PickedItemUpcDto> pickedItemUpcs;
  }

  @Data
  @Builder
  public static class PickedItemUpcDto implements Serializable {

    @JsonProperty("upc")
    @NotNull
    private String upc;

    @JsonProperty("unit_of_measurement")
    private String unitOfMeasurement;

    @JsonProperty("walmart_item_number")
    private String walmartItemNumber;

    @JsonProperty("quantity")
    private long quantity;

    @JsonProperty("weight")
    private double weight;

    @JsonProperty("store_unit_price")
    private BigDecimal storeUnitPrice;
  }

  @Data
  @Builder
  public static class OmsOrderItemUpc implements Serializable {

    @JsonProperty("upc")
    @NotNull
    private String upc;
  }
}
