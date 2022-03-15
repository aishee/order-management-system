package com.walmart.marketplace.domain.event.messages;

import com.walmart.common.domain.event.processing.Message;
import com.walmart.common.domain.type.CancellationSource;
import com.walmart.marketplace.order.domain.entity.type.Vendor;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class MarketPlaceOrderCancelMessage implements Message {
  private static final String CANCEL_REASON_CODE_NIL_PICKED = "9300";

  private String vendorOrderId;
  private CancellationSource cancellationSource;
  private String cancelledReasonCode;
  private boolean isTestOrder;
  private Vendor vendor;
  private String storeId;
  private String vendorStoreId;
  private List<String> externalItemIds;

  public boolean isCancelledDueToNilPick() {
    return CANCEL_REASON_CODE_NIL_PICKED.equalsIgnoreCase(cancelledReasonCode);
  }

  public boolean isCancelledByStore() {
    return CancellationSource.STORE.equals(cancellationSource);
  }

  public boolean isCancelledByVendor() {
    return CancellationSource.VENDOR.equals(cancellationSource);
  }
}
