package com.walmart.marketplace.commands;

import com.walmart.marketplace.order.domain.entity.type.Vendor;
import com.walmart.marketplace.order.domain.uber.ReportType;
import java.time.LocalDate;
import lombok.Builder;
import lombok.Getter;

/** This POJO is used to pass the command to Uber Report Api's to generate reports. */
@Getter
@Builder
public class MarketPlaceReportCommand {
  private final Vendor vendor;
  private final ReportType reportType;
  private final LocalDate startDate;
  private final LocalDate endDate;
}
