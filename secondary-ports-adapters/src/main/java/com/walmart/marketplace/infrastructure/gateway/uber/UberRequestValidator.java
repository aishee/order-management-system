package com.walmart.marketplace.infrastructure.gateway.uber;

import static org.apache.commons.lang.StringUtils.isNotEmpty;

import lombok.experimental.UtilityClass;

/** Util class for the Uber Api integration */
@UtilityClass
public class UberRequestValidator {

  /**
   * @param uberOrderId
   * @return if operation succeeded
   */
  public static boolean isValidGetOrderRequest(String uberOrderId) {
    return isNotEmpty(uberOrderId);
  }

  /**
   * @param uberOrderId
   * @param reason
   * @return
   */
  public static boolean isValidCancelOrderRequest(String uberOrderId, String reason) {
    return isNotEmpty(uberOrderId) && isNotEmpty(reason);
  }

  /**
   * @param uberOrderId
   * @param denyExplanation
   * @return
   */
  public static boolean isValidDenyOrderRequest(String uberOrderId, String denyExplanation) {
    return isNotEmpty(uberOrderId) && isNotEmpty(denyExplanation);
  }

  /**
   * @param uberOrderId
   * @param reason
   * @return
   */
  public static boolean isValidAcceptOrderRequest(String uberOrderId, String reason) {
    return isNotEmpty(uberOrderId) && isNotEmpty(reason);
  }

  /**
   * @param uberOrderId
   * @return
   */
  public static boolean isValidPatchCartRequest(String uberOrderId) {
    return isNotEmpty(uberOrderId);
  }

  /**
   * @param vendorStoreId
   * @return
   */
  public static boolean isValidUpdateItemRequest(String vendorStoreId) {
    return isNotEmpty(vendorStoreId);
  }
}
