package com.walmart.marketplace.order.domain.entity.type;

import lombok.Getter;
import org.apache.commons.lang3.RandomUtils;

@Getter
public enum Vendor {
  UBEREATS("UBEREATS", VendorType.UBEREATS, "U"),
  JUSTEAT("JUSTEAT", VendorType.JUSTEAT, "J"),
  TESTVENDOR("TESTVENDOR", VendorType.TESTVENDOR, "T");

  private static final long OSN_LOWER_BOUND = 100;
  private static final long OSN_UPPER_BOUND = 9999;
  private final String vendorId;
  private final VendorType vendorType;
  private final String code;

  Vendor(String vendorId, VendorType vendorType, String code) {
    this.vendorId = vendorId;
    this.vendorType = vendorType;
    this.code = code;
  }

  /**
   * Scheduling number generator.
   *
   * @return OSN.
   */
  public String nextOSN() {

    if (this.vendorType == VendorType.TESTVENDOR) {
      return null;
    }
    return this.code + RandomUtils.nextLong(OSN_LOWER_BOUND,OSN_UPPER_BOUND);
  }

  public String getVendorName() {
    return this.getVendorType().name();
  }

  public enum VendorType {
    UBEREATS,
    JUSTEAT,
    TESTVENDOR
  }
}
