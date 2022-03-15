package com.walmart.oms.infrastructure.gateway.orderservice;

import java.util.HashMap;
import java.util.Map;

public enum OrderLineQuantityInfoStatus {
  CREATED("CREATED", "1000", OrderLineQuantityInfoStatus.PROCESSING),
  READY_FOR_STORE(
      "READY_FOR_STORE",
      OrderLineQuantityInfoStatus.STATUS_CODE,
      OrderLineQuantityInfoStatus.PROCESSING),
  RECD_AT_STORE("RECD_AT_STORE", "2100.300", OrderLineQuantityInfoStatus.PROCESSING),
  PICKING_STARTED("PICKING_STARTED", "2100.500", OrderLineQuantityInfoStatus.PROCESSING),
  PICK_COMPLETE(
      "PICK_COMPLETE",
      OrderLineQuantityInfoStatus.STATUS_CODE,
      OrderLineQuantityInfoStatus.PROCESSING),
  EPOS_COMPLETE(
      "EPOS_COMPLETE",
      OrderLineQuantityInfoStatus.STATUS_CODE,
      OrderLineQuantityInfoStatus.PROCESSING),
  DELIVERED("DELIVERED", "2100.690", "PICKEDUP"),
  CANCELLED("CANCELLED", "9000", "CANCELLED"),
  NO_PENDING_ACTION("NO_PENDING_ACTION", "3700.81", "PICKEDUP");

  private static final Map<String, String> statusCodeData = new HashMap<>();
  private static final Map<String, String> statusDescriptionData = new HashMap<>();
  private static final String PROCESSING = "PROCESSING";
  private static final String STATUS_CODE = "2100.650";

  static {
    for (OrderLineQuantityInfoStatus e : values()) {
      statusCodeData.put(e.status, e.statusCode);
      statusDescriptionData.put(e.status, e.statusDesc);
    }
  }

  private final String status;
  private final String statusCode;
  private final String statusDesc;

  OrderLineQuantityInfoStatus(String status, String statusCode, String statusDesc) {
    this.status = status;
    this.statusCode = statusCode;
    this.statusDesc = statusDesc;
  }

  public static String getStatusCode(String status) {
    return statusCodeData.get(status);
  }

  public static String getStatusDescription(String status) {
    return statusDescriptionData.get(status);
  }
}
