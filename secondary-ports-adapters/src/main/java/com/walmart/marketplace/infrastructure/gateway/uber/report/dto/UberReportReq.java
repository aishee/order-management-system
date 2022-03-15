package com.walmart.marketplace.infrastructure.gateway.uber.report.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.walmart.marketplace.order.domain.uber.ReportType;
import com.walmart.oms.domain.error.exception.OMSBadRequestException;
import com.walmart.util.CustomLocalDateSerializer;
import java.time.LocalDate;
import java.util.List;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
@ToString
@Builder
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class UberReportReq {
  private final ReportType reportType;
  private final List<String> storeUUIDs;

  @JsonSerialize(using = CustomLocalDateSerializer.class)
  private final LocalDate startDate;

  @JsonSerialize(using = CustomLocalDateSerializer.class)
  private final LocalDate endDate;

  public void validateReportEndDate(int endDay) {
    LocalDate acceptableEndDate = LocalDate.now().minusDays(endDay);
    if (this.getEndDate().compareTo(acceptableEndDate) > 0) {
      String errorMsg =
          String.format(
              "End Date cannot be greater than acceptable end date, endDate:%s, acceptableEndDate:%s, reportType:%s",
              this.getEndDate(), acceptableEndDate, this.reportType);
      log.error(errorMsg);
      throw new OMSBadRequestException(errorMsg);
    }
  }
}
