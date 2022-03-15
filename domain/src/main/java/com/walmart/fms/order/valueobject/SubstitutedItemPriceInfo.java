package com.walmart.fms.order.valueobject;

import java.io.Serializable;
import java.math.BigDecimal;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Embeddable
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class SubstitutedItemPriceInfo implements Serializable {

  @Column(name = "STORE_UNIT_PRICE")
  private BigDecimal unitPrice;

  @Column(name = "STORE_TOTAL_PRICE")
  private BigDecimal totalPrice;

}
