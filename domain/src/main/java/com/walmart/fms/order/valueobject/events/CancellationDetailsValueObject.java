package com.walmart.fms.order.valueobject.events;

import lombok.Data;

@Data
public class CancellationDetailsValueObject {

  private String cancelledReasonCode;
  private String cancelledReasonDescription;
  private String cancelledBy;
}
