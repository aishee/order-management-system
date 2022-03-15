package com.walmart.oms.commands;

import com.walmart.common.domain.type.Tenant;
import com.walmart.common.domain.type.Vertical;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class DeliveredOrderCommand {

  private DeliveredOrderCommandData data;

  public String getSourceOrderId() {
    return this.getData().getSourceOrderId();
  }

  public Vertical getVertical() {
    return this.getData().getVertical();
  }

  public Tenant getTenant() {
    return this.getData().getTenant();
  }

  @Getter
  @Builder
  @AllArgsConstructor
  public static class DeliveredOrderCommandData {

    private String sourceOrderId;

    private String storeId;

    private Tenant tenant;

    private Vertical vertical;
  }
}
