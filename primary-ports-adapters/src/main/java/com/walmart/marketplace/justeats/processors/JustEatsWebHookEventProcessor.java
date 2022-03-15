package com.walmart.marketplace.justeats.processors;

import com.walmart.common.metrics.MetricConstants.MetricCounters;
import com.walmart.common.metrics.MetricService;
import com.walmart.marketplace.MarketPlaceApplicationService;
import com.walmart.marketplace.commands.WebHookEventCommand;
import com.walmart.marketplace.converter.JustEatsRequestToCommandMapper;
import com.walmart.marketplace.justeats.request.JustEatsWebHookRequest;
import com.walmart.oms.domain.error.exception.OMSBadRequestException;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;

@Slf4j
public abstract class JustEatsWebHookEventProcessor {

  @Autowired MetricService metricService;
  @Autowired JustEatsRequestToCommandMapper mapper;
  @Autowired MarketPlaceApplicationService marketPlaceApplicationService;

  /**
   * Processing JustEats web hook request.
   *
   * @param webHookRequest Web hook request payload.
   * @return Processing status.
   */
  public boolean processWebhookRequest(JustEatsWebHookRequest webHookRequest) {
    long startTime = System.currentTimeMillis();
    HttpStatus status = HttpStatus.OK;
    try {
      return process(webHookRequest);
    } catch (OMSBadRequestException omsBadRequestException) {
      log.error(
          "Exception occurred while processing Just Eats Webhook event : ", omsBadRequestException);
      status = HttpStatus.BAD_REQUEST;
      incrementMetricCounter(omsBadRequestException);
      throw omsBadRequestException;
    } catch (Exception exception) {
      incrementMetricCounter(exception);
      log.error("Exception occurred while processing Just Eats Webhook event : ", exception);
      status = HttpStatus.INTERNAL_SERVER_ERROR;
      throw exception;
    } finally {
      log.info(
          "action=JUST_EATS_WEBHOOK Completed, {}",
          metricService.recordPrimaryPortMetrics(
              System.currentTimeMillis() - startTime, String.valueOf(status.value())));
      MDC.clear();
    }
  }

  public abstract boolean process(JustEatsWebHookRequest webHookRequest);

  private void incrementMetricCounter(Exception exception) {
    String exceptionType = exception.getClass().getSimpleName();
    metricService.incrementCounterByType(
        MetricCounters.JUST_EAT_WEB_HOOK_EXCEPTION.getCounter(), exceptionType);
  }

  protected void captureMarketPlaceEvent(JustEatsWebHookRequest webHookRequest) {
    WebHookEventCommand webHookCommand = mapper.createWebHookCommand(webHookRequest);
    marketPlaceApplicationService.captureWebHookEvent(webHookCommand);
  }
}
