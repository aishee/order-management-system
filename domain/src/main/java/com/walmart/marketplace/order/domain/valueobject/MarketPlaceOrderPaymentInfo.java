package com.walmart.marketplace.order.domain.valueobject;

import java.io.Serializable;
import javax.persistence.AttributeOverride;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Embedded;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MarketPlaceOrderPaymentInfo implements Serializable {

  @Embedded
  @AttributeOverride(name = "amount", column = @Column(name = "ORDER_TOTAL"))
  private Money total;

  @Embedded
  @AttributeOverride(name = "amount", column = @Column(name = "SUB_TOTAL"))
  private Money subTotal;

  @Embedded
  @AttributeOverride(name = "amount", column = @Column(name = "TAX"))
  private Money tax;

  @Embedded
  @AttributeOverride(name = "amount", column = @Column(name = "TOTAL_FEE"))
  private Money totalFee;

  @Embedded
  @AttributeOverride(name = "amount", column = @Column(name = "TOTAL_FEE_TAX"))
  private Money totalFeeTax;

  @Embedded
  @AttributeOverride(name = "amount", column = @Column(name = "BAG_FEE"))
  private Money bagFee;
}
