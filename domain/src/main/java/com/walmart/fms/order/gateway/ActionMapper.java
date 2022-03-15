package com.walmart.fms.order.gateway;

import com.walmart.fms.order.aggregateroot.FmsOrder;
import java.util.Optional;
import java.util.function.Consumer;

public interface ActionMapper<T> {
  /**
   * Returns a handle to a method which consumes domain model and performs some action. This can be
   * used keep a map of actions to methods in a component with a code; this will come in handy when
   * there is a need to retry certain actions of a component.
   *
   * @param eventName The name of the event.
   * @return A consumer which consumers {@link FmsOrder}
   */
  Optional<Consumer<T>> getActionByCode(String eventName);
}
