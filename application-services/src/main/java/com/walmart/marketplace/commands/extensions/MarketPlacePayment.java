package com.walmart.marketplace.commands.extensions;

import com.walmart.marketplace.order.domain.valueobject.Money;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MarketPlacePayment {

  private Money total;

  private Money subTotal;

  private Money tax;

  private Money totalFee;

  private Money totalFeeTax;

  private Money bagFee;
}
