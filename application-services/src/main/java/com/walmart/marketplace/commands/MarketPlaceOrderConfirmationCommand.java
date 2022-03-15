package com.walmart.marketplace.commands;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class MarketPlaceOrderConfirmationCommand {

  private MarketPlaceOrderConfirmationCommand.MarketPlaceOrderConfirmationCommandData data;

  public String getSourceOrderId() {
    return this.getData().getSourceOrderId();
  }

  @Getter
  @Builder
  @AllArgsConstructor
  public static class MarketPlaceOrderConfirmationCommandData {

    private String sourceOrderId;
  }
}
