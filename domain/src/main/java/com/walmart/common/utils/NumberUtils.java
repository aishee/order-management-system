package com.walmart.common.utils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class NumberUtils {

  public static final int DEFAULT_DECIMAL_PRECISION_POINT = 2;

  public static BigDecimal getRoundedBigDecimal(BigDecimal value) {
    return value == null
        ? BigDecimal.ZERO
        : value.setScale(DEFAULT_DECIMAL_PRECISION_POINT, RoundingMode.HALF_UP);
  }

  public static double getRoundedDouble(BigDecimal value) {
    return org.apache.commons.lang3.math.NumberUtils.toDouble(getRoundedBigDecimal(value));
  }
}
