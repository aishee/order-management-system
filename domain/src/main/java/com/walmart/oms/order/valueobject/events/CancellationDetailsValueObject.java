package com.walmart.oms.order.valueobject.events;

import com.walmart.common.domain.type.CancellationSource;
import lombok.Data;

@Data
public class CancellationDetailsValueObject {

  private String cancelledReasonCode;

  private CancellationSource cancelledBy;

  private String cancelledReasonDescription;
}