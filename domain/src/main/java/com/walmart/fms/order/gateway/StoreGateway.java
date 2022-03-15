package com.walmart.fms.order.gateway;

import com.walmart.fms.order.aggregateroot.FmsOrder;

/** An abstraction representing the gateway to the downstream fulfillment system. */
public interface StoreGateway extends ActionMapper {
  /**
   * Sends the order confirmation message to store.
   *
   * @param fmsOrder An FmsOrder for which order confirmation must be send to store.
   * @see FmsOrder
   */
  void sendOrderConfirmation(FmsOrder fmsOrder);

  /**
   * Sends the order cancellation message to store.
   *
   * @param fmsOrder An FmsOrder for which order cancellation message must be send to store.
   * @see FmsOrder
   */
  void sendOrderCancellation(FmsOrder fmsOrder);
}
