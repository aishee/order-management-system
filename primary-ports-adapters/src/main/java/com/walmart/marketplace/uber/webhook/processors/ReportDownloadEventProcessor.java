package com.walmart.marketplace.uber.webhook.processors;

import com.walmart.common.metrics.MetricConstants;
import com.walmart.marketplace.commands.DownloadReportEventCommand;
import com.walmart.marketplace.dwh.ReportService;
import com.walmart.marketplace.uber.dto.UberWebHookRequest;
import com.walmart.oms.domain.error.exception.OMSBadRequestException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ReportDownloadEventProcessor extends UberWebHookEventProcessor {

  @Autowired private ReportService reportService;

  @Override
  public boolean process(UberWebHookRequest webHookRequest) {
    metricService.incrementCounterByType(
        MetricConstants.MetricCounters.WEB_HOOK_COUNTER.getCounter(),
        MetricConstants.MetricTypes.UBER_ORDER_REPORT.getType());
    if (webHookRequest.hasDownloadUrl()) {
      DownloadReportEventCommand downloadReportEventCommand =
          mapper.createReportWebHookCommand(webHookRequest);
      reportService.uploadReport(downloadReportEventCommand);
      return true;
    } else {
      String errorMessage =
          String.format("Missing Report Download URL for event %s ", webHookRequest.getEventId());
      log.error(errorMessage);
      throw new OMSBadRequestException(errorMessage);
    }
  }
}
