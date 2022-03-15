package com.walmart.oms.order.valueobject.events;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class FmsOrderItemUpcValueObject {

  private String upc;
}
