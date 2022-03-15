package com.walmart.marketplace.order.domain.valueobject;

import java.io.Serializable;
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
public class MarketPlaceItemPriceInfo implements Serializable {

  @Column(name = "UNIT_PRICE")
  private double unitPrice;

  @Column(name = "BASE_UNIT_PRICE")
  private double baseUnitPrice;

  @Column(name = "TOTAL_PRICE")
  private double totalPrice;

  @Column(name = "BASE_TOTAL_PRICE")
  private double baseTotalPrice;
}
