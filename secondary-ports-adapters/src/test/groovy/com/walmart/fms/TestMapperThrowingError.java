package com.walmart.fms;

import com.walmart.fms.infrastructure.integration.gateway.store.dto.orderdownload.PlaceFulfillmentOrderRequest;
import com.walmart.fms.order.aggregateroot.FmsOrder;

/** A testing aid to simulate an error during object t object mapping. */
public class TestMapperThrowingError {
  private TestMapperThrowingError() {}

  public static PlaceFulfillmentOrderRequest map(FmsOrder fmsOrder) {
    throw new NullPointerException("Error during mapping");
  }
}
