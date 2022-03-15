package com.walmart.marketplace.dto.request;

import com.walmart.marketplace.order.domain.entity.type.Vendor;
import com.walmart.marketplace.order.domain.uber.ReportType;
import com.walmart.oms.domain.error.exception.OMSBadRequestException;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

/** This request is used to generate Uber Reports. */
@Slf4j
@Getter
@ToString
@Builder
public class MarketPlaceReportRequest {
  /** This vendor is used to find API vendor. */
  private final Vendor vendor;
  /**
   * This report type is used to mentioned the type of report to be generated. Report Type must be
   * from below collection PAYMENT_DETAILS_REPORT, ORDER_ERRORS_MENU_ITEM_REPORT,
   * ORDER_ERRORS_TRANSACTION_REPORT, ORDER_HISTORY_REPORT, DOWNTIME_REPORT,
   * CUSTOMER_AND_DELIVERY_FEEDBACK_REPORT, MENU_ITEM_FEEDBACK_REPORT
   */
  private final ReportType reportType;
  /**
   * This dayToStart will be calculated as LocalDate.now().minusDays(dayToStart). And this must be
   * greater then dayToEnd e.r. 3>2
   */
  private final int dayToStart;
  /**
   * This dayToEnd will be calculated as LocalDate.now().minusDays(dayToEnd). This must not less
   * then 2.
   */
  private final int dayToEnd;

  public void validateInput() {
    if (this.dayToEnd > this.dayToStart) {
      String errorMsg =
          String.format(
              "dayToEnd cannot be greater then dayToStart, dayToStart:%s, dayToEnd:%s, reportType:%s",
              this.dayToStart, this.dayToEnd, this.reportType);
      log.error(errorMsg);
      throw new OMSBadRequestException(errorMsg);
    }
  }
}
