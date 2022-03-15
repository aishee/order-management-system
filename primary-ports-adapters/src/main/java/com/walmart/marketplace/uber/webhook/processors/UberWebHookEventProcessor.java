package com.walmart.marketplace.uber.webhook.processors;

import com.walmart.common.metrics.MetricConstants.MetricCounters;
import com.walmart.common.metrics.MetricService;
import com.walmart.marketplace.MarketPlaceApplicationService;
import com.walmart.marketplace.commands.WebHookEventCommand;
import com.walmart.marketplace.converter.RequestToCommandMapper;
import com.walmart.marketplace.order.domain.entity.MarketPlaceEvent;
import com.walmart.marketplace.uber.dto.UberWebHookRequest;
import com.walmart.oms.domain.error.exception.OMSBadRequestException;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;

@Slf4j
public abstract class UberWebHookEventProcessor {

  @Autowired MetricService metricService;
  @Autowired RequestToCommandMapper mapper;
  @Autowired MarketPlaceApplicationService marketPlaceApplicationService;

  public boolean processWebhookRequest(UberWebHookRequest webHookRequest) {
    long startTime = System.currentTimeMillis();
    HttpStatus status = HttpStatus.OK;
    try {
      return process(webHookRequest);
    } catch (OMSBadRequestException omsBadRequestException) {
      log.error(
          "Exception occurred while processing Uber Webhook event : ", omsBadRequestException);
      status = HttpStatus.BAD_REQUEST;
      incrementMetricCounter(omsBadRequestException);
      throw omsBadRequestException;
    } catch (Exception exception) {
      log.error("Exception occurred while processing Uber Webhook event : ", exception);
      status = HttpStatus.INTERNAL_SERVER_ERROR;
      incrementMetricCounter(exception);
      throw exception;
    } finally {
      log.info(
          "action=UBER_WEBHOOK Completed, {}",
          metricService.recordPrimaryPortMetrics(
              System.currentTimeMillis() - startTime, String.valueOf(status.value())));
      MDC.clear();
    }
  }

  public abstract boolean process(UberWebHookRequest webHookRequest);

  private void incrementMetricCounter(Exception exception) {
    String exceptionType = exception.getClass().getSimpleName();
    metricService.incrementCounterByType(
        MetricCounters.UBER_WEB_HOOK_EXCEPTION.getCounter(), exceptionType);
  }

  protected MarketPlaceEvent captureMarketPlaceEvent(UberWebHookRequest webHookRequest) {
    WebHookEventCommand webHookCommand = mapper.createWebHookCommand(webHookRequest);
    return marketPlaceApplicationService.captureWebHookEvent(webHookCommand);
  }
}
