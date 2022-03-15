package com.walmart.common.domain.type;

import static java.util.stream.Collectors.toMap;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import lombok.Getter;

@Getter
public enum Currency {
  GBP("GBP"),
  DEFAULT("USD");

  private static final Map<String, Currency> currencyCache =
      Arrays.stream(Currency.values())
          .collect(toMap(Currency::getCurrencyUnit, Function.identity()));

  private final String currencyUnit;

  Currency(String currencyUnit) {
    this.currencyUnit = currencyUnit;
  }

  public static Currency get(String currency) {
    return currencyCache.getOrDefault(currency, DEFAULT);
  }
}
