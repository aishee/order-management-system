package com.walmart.fms.commands;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class FmsDeliveredOrderCommand {

  private FmsDeliveredOrderCommandData data;

  @Getter
  @Builder
  @AllArgsConstructor
  public static class FmsDeliveredOrderCommandData {

    private String storeOrderId;
  }
}
