package com.walmart.marketplace.order.domain.uber;

import com.walmart.marketplace.order.domain.entity.type.Vendor;
import java.time.LocalDate;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Getter
@Builder
@ToString
public class MarketPlaceReportRequest {
  private final ReportType reportType;
  private final LocalDate startDate;
  private final LocalDate endDate;
  private final Vendor vendor;
}
