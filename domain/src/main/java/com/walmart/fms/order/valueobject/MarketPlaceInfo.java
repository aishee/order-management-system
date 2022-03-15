package com.walmart.fms.order.valueobject;

import com.walmart.common.domain.AssertionConcern;
import com.walmart.marketplace.order.domain.entity.type.Vendor;
import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@Getter
@NoArgsConstructor
public class MarketPlaceInfo extends AssertionConcern implements Serializable {

  @Column(name = "VENDOR")
  @Enumerated(EnumType.STRING)
  private Vendor vendor;

  @Column(name = "VENDOR_ORDER_ID")
  private String vendorOrderId;

  @Builder
  public MarketPlaceInfo(Vendor vendor, String vendorOrderId) {
    this.assertArgumentNotNull(vendor, "Vendor cannot be null");
    this.assertArgumentNotNull(vendorOrderId, "vendor Order id cannot be null");
    this.vendor = vendor;
    this.vendorOrderId = vendorOrderId;
  }

  public String getVendorId() {
    return vendor.getVendorId();
  }
}
