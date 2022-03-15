package com.walmart.fms.order.valueobject;

import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Embeddable
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderPriceInfo implements Serializable {

  @Column(name = "WEB_ORDER_TOTAL")
  private double webOrderTotal;

  @Column(name = "POS_TOTAL")
  private double posTotal;

  @Column(name = "ORDER_VAT_AMOUNT")
  private double orderVATAmount;

  @Column(name = "CARRIER_BAG_CHARGE")
  private double carrierBagCharge;
}
