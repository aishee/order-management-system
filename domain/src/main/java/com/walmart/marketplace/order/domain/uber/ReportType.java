package com.walmart.marketplace.order.domain.uber;

/** @<code> This enum type is used to prepare request for market reports.</code> */
public enum ReportType {
  PAYMENT_DETAILS_REPORT,
  ORDER_ERRORS_MENU_ITEM_REPORT,
  ORDER_ERRORS_TRANSACTION_REPORT,
  ORDER_HISTORY_REPORT,
  DOWNTIME_REPORT,
  CUSTOMER_AND_DELIVERY_FEEDBACK_REPORT,
  MENU_ITEM_FEEDBACK_REPORT,
  ORDERS_AND_ITEMS_REPORT,
  FINANCE_SUMMARY_REPORT;

  public boolean isDownTimeReport() {
    return this == DOWNTIME_REPORT;
  }
}
