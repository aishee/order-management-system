package com.walmart.marketplace.order.domain.entity;

import com.walmart.marketplace.order.domain.entity.type.Vendor;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Slf4j
public class UpdateItemInfo {

  private String vendorOrderId;
  private Vendor vendorId;
  private int suspendUntil;
  private String reason;
  private List<String> outOfStockItemIds;
  private String vendorStoreId;
  private String storeId;

  public List<String> getOutOfStockItemIdsList() {
    return Optional.ofNullable(this.outOfStockItemIds).orElse(Collections.emptyList());
  }

  public boolean containsOutOfStockItems() {
    return !this.getOutOfStockItemIdsList().isEmpty();
  }

  public int getOutOfStockItemsCount() {
    return this.getOutOfStockItemIdsList().size();
  }

  public boolean isValidVendor() {
    if (Vendor.TESTVENDOR.equals(vendorId)) {
      log.info("Value of Vendor is TESTVENDOR in update Item call for order {}", vendorOrderId);
      return false;
    }
    return true;
  }
}
