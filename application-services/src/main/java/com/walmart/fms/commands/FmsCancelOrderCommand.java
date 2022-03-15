package com.walmart.fms.commands;

import com.walmart.common.domain.type.CancellationSource;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class FmsCancelOrderCommand {

  private FmsCancelOrderCommandData data;

  @Getter
  @Builder
  @AllArgsConstructor
  public static class FmsCancelOrderCommandData {

    private String sourceOrderId;

    private String storeOrderId;

    private String cancelledReasonCode;

    private String cancelledReasonDescription;

    private CancellationSource cancellationSource;
  }
}
