package com.walmart.fms.order.valueobject;

import com.walmart.common.domain.AssertionConcern;
import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@Getter
@NoArgsConstructor
public class ItemCatalogInfo extends AssertionConcern implements Serializable {

  @Builder
  public ItemCatalogInfo(
      String salesUnit,
      String unitOfMeasurement,
      String pickerItemDescription,
      String imageUrl,
      Integer minIdealDays,
      Integer maxIdealDays,
      String temperatureZone,
      boolean isSellbyDateRequired) {

    this.assertArgumentNotEmpty(pickerItemDescription, "Picker Item Description cannot ");

    this.salesUnit = salesUnit;
    this.unitOfMeasurement = unitOfMeasurement;
    this.pickerItemDescription = pickerItemDescription;
    this.imageUrl = imageUrl;
    this.minIdealDays = minIdealDays;
    this.maxIdealDays = maxIdealDays;
    this.temperatureZone = temperatureZone;
    this.isSellbyDateRequired = isSellbyDateRequired;
  }

  @Column(name = "SALES_UNIT")
  private String salesUnit;

  @Column(name = "UNIT_OF_MEASUREMENT")
  private String unitOfMeasurement;

  @Column(name = "PICKER_ITEM_DESCRIPTION")
  private String pickerItemDescription;

  @Column(name = "IMAGE_URL")
  private String imageUrl;

  @Column(name = "MIN_IDEAL_DAY_VALUE")
  private Integer minIdealDays;

  @Column(name = "MAX_IDEAL_DAY_VALUE")
  private Integer maxIdealDays;

  @Column(name = "TEMPERATURE_ZONE")
  private String temperatureZone;

  @Column(name = "IS_SELLBY_DATE_REQUIRED")
  private boolean isSellbyDateRequired;
}
