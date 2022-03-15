package com.walmart.marketplace.commands;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class MarketPlaceDeliveredOrderCommand {

  private MarketPlaceDeliveredOrderCommand.MarketPlaceDeliveredOrderCommandData data;

  public String getSourceOrderId() {
    return this.getData().getSourceOrderId();
  }

  @Getter
  @Builder
  @AllArgsConstructor
  public static class MarketPlaceDeliveredOrderCommandData {

    private String sourceOrderId;
  }
}
