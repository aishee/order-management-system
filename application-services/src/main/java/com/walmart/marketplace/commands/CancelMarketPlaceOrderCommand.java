package com.walmart.marketplace.commands;

import com.walmart.common.domain.valueobject.CancellationDetails;
import com.walmart.marketplace.order.domain.entity.type.Vendor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CancelMarketPlaceOrderCommand {

  private String sourceOrderId;

  private String vendorOrderId;

  private CancellationDetails cancellationDetails;

  private Vendor vendor;

  private String resourceUrl;

}