package com.walmart.marketplace.controller;

import static com.walmart.common.constants.CommonConstants.MARKETPLACE;

import com.walmart.common.metrics.MetricConstants.MetricCounters;
import com.walmart.common.metrics.MetricService;
import com.walmart.marketplace.MarketPlaceApplicationService;
import com.walmart.marketplace.commands.MarketPlaceReportCommand;
import com.walmart.marketplace.converter.RequestToCommandMapper;
import com.walmart.marketplace.dto.request.MarketPlaceReportRequest;
import com.walmart.marketplace.order.domain.entity.type.Vendor;
import com.walmart.marketplace.order.domain.uber.ReportType;
import com.walmart.oms.domain.error.exception.OMSBadRequestException;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/** This controller is generating Marketplace Reports. UKGRFF-520 */
@Slf4j
@RestController
@RequestMapping("/marketplace_reports")
public class MarketPlaceReportController {

  @Autowired private RequestToCommandMapper mapper;
  @Autowired private MetricService metricService;
  @Autowired private MarketPlaceApplicationService marketPlaceApplicationService;

  @PostMapping(
      value = "/{vendor_id}/{report_type}/{day_to_start}/{day_to_end}",
      produces = MediaType.APPLICATION_JSON_VALUE)
  @ResponseStatus(HttpStatus.ACCEPTED)
  public String invokeMarketPlaceReport(
      @PathVariable("vendor_id") Vendor vendor,
      @PathVariable("report_type") ReportType reportType,
      @PathVariable("day_to_start") Integer dayToStart,
      @PathVariable("day_to_end") Integer dayToEnd) {
    initMDC("MARKETPLACE_REPORT", MARKETPLACE);
    long startTime = System.currentTimeMillis();
    HttpStatus status = HttpStatus.ACCEPTED;
    log.info(
        "MarketPlaceReport reportType:{}, dayToStart:{}, dayToEnd:{}",
        reportType,
        dayToStart,
        dayToEnd);
    try {
      MarketPlaceReportCommand reportCommand =
          mapper.buildMarketPlaceReportCommand(
              MarketPlaceReportRequest.builder()
                  .vendor(vendor)
                  .reportType(reportType)
                  .dayToStart(dayToStart)
                  .dayToEnd(dayToEnd)
                  .build());
      return marketPlaceApplicationService.invokeMarketPlaceReport(reportCommand);
    } catch (OMSBadRequestException exception) {
      status = HttpStatus.BAD_REQUEST;
      incrementMetricCounter(exception);
      log.error(
          "Exception occurred while MarketPlace report generation, reportType:{}, dayToStart:{}, dayToEnd:{}, status{}, exception {}",
          reportType,
          dayToStart,
          dayToEnd,
          status,
          exception.getMessage());
      throw exception;
    } catch (Exception exception) {
      status = HttpStatus.INTERNAL_SERVER_ERROR;
      incrementMetricCounter(exception);
      log.error(
          "Exception occurred while MarketPlace report generation, reportType:{}, dayToStart:{}, dayToEnd:{},status {}, exception {}",
          reportType,
          dayToStart,
          dayToEnd,
          status,
          exception.getMessage());
      throw exception;
    } finally {
      log.info(
          "action=MarketPlaceReportGeneration Accepted, {} ",
          metricService.recordPrimaryPortMetrics(
              System.currentTimeMillis() - startTime, String.valueOf(status.value())));
      MDC.clear();
    }
  }

  void initMDC(String api, String domain) {
    MDC.put("api", api);
    MDC.put("domain", domain);
  }

  private void incrementMetricCounter(Exception exception) {
    String exceptionType = exception.getClass().getSimpleName();
    metricService.incrementCounterByType(
        MetricCounters.PRIMARY_PORT_EXCEPTION_COUNTER.getCounter(), exceptionType);
  }
}
