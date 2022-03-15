package com.walmart.marketplace.infrastructure.gateway.uber.dto.request;

/** Enum for Uber Cancel Reasons */
public enum UberCancelReason {
  OUT_OF_ITEMS,
  KITCHEN_CLOSED,
  CUSTOMER_CALLED_TO_CANCEL,
  RESTAURANT_TOO_BUSY,
  CANNOT_COMPLETE_CUSTOMER_NOTE,
  OTHER;

  /**
   * Returns an enum from the reasonStr
   *
   * @param reasonStr
   * @return
   */
  public static UberCancelReason getEnumFromStr(String reasonStr) {
    for (UberCancelReason reason : values()) {
      if (reason.name().equalsIgnoreCase(reasonStr)) {
        return reason;
      }
    }
    return OTHER;
  }
}
