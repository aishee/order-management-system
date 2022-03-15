package com.walmart.oms.order.valueobject;

import static java.util.Objects.isNull;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class CatalogItem implements Serializable {

  private String skuId;

  private String cin;

  private List<String> upcNumbers;

  private Boolean isBundle;

  private String brand;

  private String itemName;

  private String name;

  private String pickerDesc;

  private String salesUnit;

  private String largeImageURL;

  private String smallImageURL;

  private List<String> untraitedStores;

  private String pricePerUom;

  private Integer minIdealDayValue;

  private Integer maxIdealDayValue;

  private Boolean isSellByDateRequired;

  private String weight;

  private String replenishUnitIndicator;

  public BigDecimal getUnformattedUnitPrice() {
    if (onSale && !isNull(salePrice)) {
      return BigDecimal.valueOf(Double.parseDouble(salePrice.substring(1)));
    }
    if (!isNull(price)) {
      return BigDecimal.valueOf(Double.parseDouble(price.substring(1)));
    } else {
      return BigDecimal.ZERO;
    }
  }

  private String price;

  private boolean onSale;

  private String salePrice;
}
