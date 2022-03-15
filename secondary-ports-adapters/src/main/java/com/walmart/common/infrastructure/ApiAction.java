package com.walmart.common.infrastructure;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import lombok.Getter;

@Getter
public enum ApiAction {
  PYSIPYP(CircuitBreaker.PYSIPYP.name()),
  IRO(CircuitBreaker.IRO.name()),
  TAX(CircuitBreaker.TAX.name()),
  PATCH_CART(CircuitBreaker.UBER_CART_CIRCUIT_BREAKER.name()),
  UPDATE_ITEM(CircuitBreaker.UBER_CART_CIRCUIT_BREAKER.name()),
  GET_ORDER(CircuitBreaker.UBER_ORDER_UPDATE_CIRCUIT_BREAKER.name()),
  ACCEPT_ORDER(CircuitBreaker.UBER_ORDER_UPDATE_CIRCUIT_BREAKER.name()),
  DENY_ORDER(CircuitBreaker.UBER_ORDER_UPDATE_CIRCUIT_BREAKER.name()),
  CANCEL_ORDER(CircuitBreaker.UBER_BATCH_CIRCUIT_BREAKER.name()),
  STORE_API(CircuitBreaker.UBER_BATCH_CIRCUIT_BREAKER.name()),
  REPORT_API(CircuitBreaker.UBER_BATCH_CIRCUIT_BREAKER.name()),
  JUST_EATS_API(CircuitBreaker.JUST_EATS_CIRCUIT_BREAKER.name());

  private final String circuitBreakerName;

  ApiAction(String circuitBreakerName) {
    this.circuitBreakerName = circuitBreakerName;
  }

  public static List<CircuitBreaker> getAllCircuitBreakers() {
    return Arrays.stream(CircuitBreaker.values()).collect(Collectors.toList());
  }

  public enum CircuitBreaker {
    PYSIPYP,
    IRO,
    TAX,
    UBER_CART_CIRCUIT_BREAKER,
    UBER_ORDER_UPDATE_CIRCUIT_BREAKER,
    UBER_BATCH_CIRCUIT_BREAKER,
    JUST_EATS_CIRCUIT_BREAKER
  }
}
