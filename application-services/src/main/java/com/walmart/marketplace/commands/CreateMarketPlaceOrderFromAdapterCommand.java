package com.walmart.marketplace.commands;

import com.walmart.marketplace.order.domain.entity.type.Vendor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CreateMarketPlaceOrderFromAdapterCommand {

  private String externalOrderId;

  private String resourceUrl;

  private Vendor vendor;
}
