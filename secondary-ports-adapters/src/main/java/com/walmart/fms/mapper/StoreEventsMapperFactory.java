package com.walmart.fms.mapper;

import com.walmart.common.domain.event.processing.EgressEvent;
import com.walmart.fms.order.gateway.StoreEvents;
import java.util.function.Function;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.util.Assert;

/**
 * A factory component which will give a {@link MappingFunction} instance for {@link EgressEvent}
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class StoreEventsMapperFactory {

  public static MappingFunction getMapper(StoreEvents code) {
    Assert.notNull(code, "Mapping code cannot be null !!!");
    switch (code) {
      case PFO:
        return new MappingFunction<>(FmsOrderToPFORequestMapper::map, StoreEvents.PFO);
      case UFO:
        return new MappingFunction<>(FmsOrderToUFORequestMapper::map, StoreEvents.UFO);
      default:
        return null;
    }
  }

  /**
   * A container class to keep the mapping function and the mapping code.
   *
   * @param <T>
   * @param <R>
   */
  @Getter
  public static class MappingFunction<T, R> {
    private final Function<T, R> function;
    private final StoreEvents code;

    public MappingFunction(Function<T, R> function, StoreEvents code) {
      this.function = function;
      this.code = code;
    }
  }
}
