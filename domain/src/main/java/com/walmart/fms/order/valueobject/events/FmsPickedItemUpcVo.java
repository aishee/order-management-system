package com.walmart.fms.order.valueobject.events;

import java.math.BigDecimal;
import lombok.Data;

@Data
public class FmsPickedItemUpcVo {

  private String upc;

  private String uom;

  private String win;

  private long quantity;

  private double weight;

  private BigDecimal storeUnitPrice;
}
