package com.walmart.fms.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.walmart.common.domain.type.SubstitutionOption;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.PositiveOrZero;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class FmsOrderItemDTO implements Serializable {

  @NotNull private String itemId;

  private String consumerItemNumber;

  private String unitOfMeasurement;

  private String salesUnit;

  @PositiveOrZero private long quantity;

  @PositiveOrZero private long nilPickQty;

  @PositiveOrZero private Integer minIdealDayValue;

  @PositiveOrZero private Integer maxIdealDayValue;

  private boolean isSellbyDateRequired;

  private double weight;

  private BigDecimal unitPrice;

  private String imageURL;

  private String pickerItemDescription;

  private String temperatureZone;

  private SubstitutionOption substitutionOption;

  private FmsPickedItemDTO pickedItem;

  public SubstitutionOption getSubstitutionOption() {
    return Optional.ofNullable(substitutionOption).orElse(SubstitutionOption.DO_NOT_SUBSTITUTE);
  }

  @JsonProperty("upcs")
  @NotNull
  @NotEmpty
  private List<FmsOrderItemUpc> orderItemUpcs;

  @Data
  @Builder
  @JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
  public static class FmsOrderItemUpc implements Serializable {

    @NotNull private String upc;
  }

  @Data
  @Builder
  @JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
  public static class FmsPickedItemDTO implements Serializable {

    private String departmentID;

    @NotNull private String consumerItemNumber;

    private String walmartItemNumber;

    private long quantity;

    private double weight;

    private String pickedItemDescription;

    private Date sellByDate;

    private String pickedBy;

    private BigDecimal unitPrice;

    private List<FmsPickedItemUpcDTO> pickedItemUpcs;
  }

  @Data
  @Builder
  @JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
  public static class FmsPickedItemUpcDTO implements Serializable {

    @NotNull private String upc;

    private String unitOfMeasurement;

    private String walmartItemNumber;

    private long quantity;

    private double weight;

    private BigDecimal storeUnitPrice;
  }
}
