package com.walmart.fms.order.valueobject.events;

import lombok.Data;

@Data
public class FmsSubstitutedItemUpcValueObject {

  private String upc;
  private String uom;
}
