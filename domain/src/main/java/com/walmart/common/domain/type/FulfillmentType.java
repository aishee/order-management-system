package com.walmart.common.domain.type;

import static java.util.stream.Collectors.toMap;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import lombok.Getter;

@Getter
public enum FulfillmentType {
  HOME_DELIVERY("HOME_DELIVERY"),
  INSTORE_PICKUP("INSTORE_PICKUP"),
  REMOTE_POPUP("REMOTE_POPUP");

  private final String name;

  FulfillmentType(String name) {
    this.name = name;
  }

  private static final Map<String, FulfillmentType> fulfillmentCache =
      Arrays.stream(FulfillmentType.values())
          .collect(toMap(FulfillmentType::getName, Function.identity()));

  public static FulfillmentType get(String fulfillmentType) {
    return fulfillmentCache.getOrDefault(fulfillmentType, HOME_DELIVERY);
  }
}
