package com.walmart.marketplace.domain.event.messages;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class MarketplaceBundledItemAttributes {

  private String bundleInstanceId;
  private int bundledQuantity;

}