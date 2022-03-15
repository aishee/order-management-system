package com.walmart.marketplace.eventprocessors;

import com.walmart.common.constants.CommonConstants;
import com.walmart.common.metrics.MetricConstants.MetricCounters;
import com.walmart.common.metrics.MetricService;
import com.walmart.fms.domain.error.exception.FMSBadRequestException;
import com.walmart.marketplace.MarketPlaceApplicationService;
import com.walmart.marketplace.commands.CancelMarketPlaceOrderCommand;
import com.walmart.marketplace.order.aggregateroot.MarketPlaceOrder;
import com.walmart.oms.domain.error.exception.OMSBadRequestException;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Slf4j
public class MarketPlaceStoreCancelCommandService {

  @Autowired private MetricService metricService;

  @Autowired private MarketPlaceApplicationService marketPlaceApplicationService;

  @Transactional
  public MarketPlaceOrder cancelOrder(CancelMarketPlaceOrderCommand cancelMarketPlaceOrderCommand) {
    initMDC("MARKETPLACE_CANCEL_ORDER", CommonConstants.MARKETPLACE);
    long startTime = System.currentTimeMillis();
    HttpStatus status = HttpStatus.OK;
    try {
      return marketPlaceApplicationService.cancelOrder(cancelMarketPlaceOrderCommand);
    } catch (OMSBadRequestException | FMSBadRequestException exception) {
      log.error("Exception occurred while cancelling the Marketplace order : ", exception);
      status = HttpStatus.BAD_REQUEST;
      incrementMetricCounter(exception);
      throw exception;
    } catch (Exception exception) {
      log.error("Exception occurred while cancelling the Marketplace order : ", exception);
      status = HttpStatus.INTERNAL_SERVER_ERROR;
      incrementMetricCounter(exception);
      throw exception;
    } finally {
      log.info(
          "action=MarketPlaceOrderCancel Completed, "
              + metricService.recordPrimaryPortMetrics(
                  System.currentTimeMillis() - startTime, String.valueOf(status.value())));
      MDC.clear();
    }
  }

  private void incrementMetricCounter(Exception exception) {
    String exceptionType = exception.getClass().getSimpleName();
    metricService.incrementCounterByType(
        MetricCounters.PRIMARY_PORT_EXCEPTION_COUNTER.getCounter(), exceptionType);
  }

  void initMDC(String api, String domain) {
    MDC.put("api", api);
    MDC.put("domain", domain);
  }
}
