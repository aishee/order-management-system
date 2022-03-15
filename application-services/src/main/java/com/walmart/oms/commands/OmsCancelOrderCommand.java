package com.walmart.oms.commands;

import com.walmart.common.domain.type.CancellationSource;
import com.walmart.common.domain.type.Tenant;
import com.walmart.common.domain.type.Vertical;
import com.walmart.common.domain.valueobject.CancellationDetails;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class OmsCancelOrderCommand {

  private String sourceOrderId;

  private CancellationDetails cancellationDetails;

  private Tenant tenant;

  private Vertical vertical;

  public CancellationSource getCancellationSource() {
    return cancellationDetails.getCancelledBy();
  }
}