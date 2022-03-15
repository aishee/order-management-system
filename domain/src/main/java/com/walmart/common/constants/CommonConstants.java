package com.walmart.common.constants;

import com.walmart.common.domain.type.Currency;
import com.walmart.marketplace.order.domain.valueobject.Money;
import java.math.BigDecimal;
import java.math.RoundingMode;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class CommonConstants {

  public static final int SCALE = 2;
  public static final RoundingMode ROUNDING_MODE = RoundingMode.HALF_UP;
  public static final String DOMAIN_KEY = "domain";
  public static final String MARKETPLACE = "MARKETPLACE";
  public static final String OMS = "OMS";
  public static final Money ZERO_MONEY = new Money(BigDecimal.ZERO, Currency.GBP);
}
