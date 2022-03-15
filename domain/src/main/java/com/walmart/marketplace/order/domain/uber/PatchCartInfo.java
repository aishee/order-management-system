package com.walmart.marketplace.order.domain.uber;

import com.walmart.marketplace.order.domain.entity.type.Vendor;
import java.util.Collections;
import java.util.List;
import java.util.Map;
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
@NoArgsConstructor
@AllArgsConstructor
@Slf4j
public class PatchCartInfo {

  private String vendorOrderId;
  private Vendor vendorId;
  private String storeId;
  private List<String> nilPickInstanceIds;
  private Map<String, Integer> partialPickInstanceIds;

  public List<String> getNilPickInstanceIdsList() {
    return Optional.ofNullable(this.nilPickInstanceIds).orElse(Collections.emptyList());
  }

  public Map<String, Integer> getPartialPickInstanceIdsMap() {
    return Optional.ofNullable(this.partialPickInstanceIds).orElse(Collections.emptyMap());
  }

  public boolean containsNilPicks() {
    return !this.getNilPickInstanceIdsList().isEmpty();
  }

  public boolean containsPartialPicks() {
    return !this.getPartialPickInstanceIdsMap().isEmpty();
  }

  public boolean containsNilOrPartialPicks() {
    return this.containsNilPicks() || this.containsPartialPicks();
  }

  public int getNilPicksCount() {
    return this.getNilPickInstanceIdsList().size();
  }

  public int getPartialPicksCount() {
    return this.getPartialPickInstanceIdsMap().size();
  }

  public boolean isValidVendor() {
    if (Vendor.TESTVENDOR.equals(vendorId)) {
      log.info("Value of Vendor is TESTVENDOR in update Item call for order {}", vendorOrderId);
      return false;
    }
    return true;
  }
}
