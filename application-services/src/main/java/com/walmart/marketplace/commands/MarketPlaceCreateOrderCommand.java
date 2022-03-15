package com.walmart.marketplace.commands;

import com.walmart.marketplace.commands.extensions.ExternalMarketPlaceItem;
import com.walmart.marketplace.commands.extensions.MarketPlacePayment;
import com.walmart.marketplace.order.domain.entity.type.Vendor;
import com.walmart.marketplace.order.domain.valueobject.Money;
import java.util.Date;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MarketPlaceCreateOrderCommand {

  private String externalOrderId;

  private String externalNativeOrderId;

  private String vendorStoreId;

  private String storeId;

  private String firstName;

  private String lastName;

  private List<ExternalMarketPlaceItem> marketPlaceItems;

  private MarketPlacePayment payment;

  private Date sourceOrderCreationTime;

  private Date estimatedArrivalTime;

  private Vendor vendor;

  public Money getSubTotal() {
    return this.getPayment().getSubTotal();
  }

  public Money getTotal() {
    return this.getPayment().getTotal();
  }

  public Money getTax() {
    return this.getPayment().getTax();
  }

  public Money getTotalFee() {
    return this.getPayment().getTotalFee();
  }

  public Money getTotalFeeTax() {
    return this.getPayment().getTotalFeeTax();
  }

  public Money getBagFee() {
    return this.getPayment().getBagFee();
  }
}
