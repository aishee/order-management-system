package com.walmart.fms.order.gateway;

import com.walmart.fms.order.aggregateroot.FmsOrder;

/**
 * A factory component which will give different implementation of {@link StoreGateway} based on
 * what is requested.
 */
public interface StoreGatewayFactory {
  /**
   * Retrieves an StoreGateway which makes produces integration messages asynchronously.
   *
   * @param type An instance of {@link FmsOrder.FulfillmentApp} indicating the type of store
   *     gateway.
   * @return An instance of StoreGateway. If no concrete implementation is found then a default
   *     gateway implementation is returned.
   * @throws IllegalArgumentException If the <code>type</code> is null.
   * @see StoreGateway
   */
  StoreGateway getGatewayFor(FmsOrder.FulfillmentApp type);
}
