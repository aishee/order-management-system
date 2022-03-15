package com.walmart.oms.order.valueobject.events;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FmsPickedItemUpcVo {

  private String upc;

  private String uom;

  private String win;

  private long quantity;

  private double weight;

  private BigDecimal storeUnitPrice;
}
