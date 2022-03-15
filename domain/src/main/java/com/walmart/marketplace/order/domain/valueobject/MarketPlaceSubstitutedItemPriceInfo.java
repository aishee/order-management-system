package com.walmart.marketplace.order.domain.valueobject;

import java.io.Serializable;
import java.math.BigDecimal;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MarketPlaceSubstitutedItemPriceInfo implements Serializable {
  @Column(name = "UNIT_PRICE")
  private BigDecimal unitPrice;

  @Column(name = "TOTAL_PRICE")
  private BigDecimal totalPrice;

}
