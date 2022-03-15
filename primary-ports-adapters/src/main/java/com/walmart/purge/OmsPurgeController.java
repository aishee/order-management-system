package com.walmart.purge;

import static com.walmart.common.constants.CommonConstants.OMS;

import com.walmart.common.metrics.MetricConstants;
import com.walmart.common.metrics.MetricService;
import com.walmart.oms.domain.error.exception.OMSThirdPartyException;
import com.walmart.purge.response.OmsPurgeResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

/** This controller class offers API to purge historical data from database UKGRFF-669 */
@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/oms_purge")
public class OmsPurgeController {
  private final OmsPurgeService omsPurgeService;
  private final MetricService metricService;

  @PostMapping
  @ResponseStatus(HttpStatus.ACCEPTED)
  public Mono<OmsPurgeResponse> purgeHistoricalData() {
    initMDC("OMS_PURGE", OMS);
    long startTime = System.currentTimeMillis();
    log.info("Inside OMS purge.");
    HttpStatus status = HttpStatus.ACCEPTED;
    try {
      return omsPurgeService.purgeHistoricalData().map(OmsPurgeResponse::from);
    } catch (OMSThirdPartyException exception) {
      status = HttpStatus.INTERNAL_SERVER_ERROR;
      incrementMetricCounter(exception);
      log.error(
          "Exception occurred in historical data purge with, status:{}, exception:{}",
          status.value(),
          exception.getMessage());
      throw exception;
    } finally {
      log.info(
          "action=OMS Purge Accepted, {} ",
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
    metricService.incrementExceptionCounterByType(
        MetricConstants.MetricCounters.OMS_PURGE_EXCEPTION_COUNTER.getCounter(),
        MetricConstants.MetricTypes.OMS_PURGE.getType(),
        exceptionType);
  }
}
