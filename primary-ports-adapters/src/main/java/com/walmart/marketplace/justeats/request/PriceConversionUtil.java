package com.walmart.marketplace.justeats.request;

import java.math.BigDecimal;
import java.math.RoundingMode;
import lombok.experimental.UtilityClass;

@UtilityClass
public class PriceConversionUtil {

  private static final int SCALE = 2;
  private static final RoundingMode DEFAULT_ROUNDING = RoundingMode.HALF_UP;
  private static final int DIVIDER = 100;

  public static BigDecimal convertPriceToAmount(int price) {
    return BigDecimal.valueOf(price).divide(BigDecimal.valueOf(DIVIDER), SCALE, DEFAULT_ROUNDING);
  }
}
