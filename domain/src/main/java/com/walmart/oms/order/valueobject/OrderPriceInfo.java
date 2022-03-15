package com.walmart.oms.order.valueobject;

import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Embeddable
@NoArgsConstructor
public class OrderPriceInfo implements Serializable {

  @Builder
  public OrderPriceInfo(
      double orderSubTotal,
      double posTotal,
      double deliveryCharge,
      double carrierBagCharge,
      double orderTotal) {
    this.orderSubTotal = orderSubTotal;
    this.posTotal = posTotal;
    this.deliveryCharge = deliveryCharge;
    this.carrierBagCharge = carrierBagCharge;
    this.orderTotal = orderTotal;
  }

  @Column(name = "ORDER_SUB_TOTAL")
  private double orderSubTotal;

  @Column(name = "POS_TOTAL")
  private double posTotal;

  @Column(name = "DELIVERY_CHARGE")
  private double deliveryCharge;

  @Column(name = "CARRIER_BAG_CHARGE")
  private double carrierBagCharge;

  @Column(name = "ORDER_TOTAL")
  private double orderTotal;
}
