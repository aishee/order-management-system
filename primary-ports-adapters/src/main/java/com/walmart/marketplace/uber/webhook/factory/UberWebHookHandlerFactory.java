package com.walmart.marketplace.uber.webhook.factory;

import com.walmart.marketplace.uber.webhook.processors.OrderCancelEventProcessor;
import com.walmart.marketplace.uber.webhook.processors.OrderNotifyEventProcessor;
import com.walmart.marketplace.uber.webhook.processors.ReportDownloadEventProcessor;
import com.walmart.marketplace.uber.webhook.processors.UberWebHookEventProcessor;
import com.walmart.marketplace.uber.webhook.processors.WebhookEventType;
import com.walmart.oms.domain.error.exception.OMSBadRequestException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class UberWebHookHandlerFactory {

  @Autowired private OrderCancelEventProcessor orderCancelEventProcessor;
  @Autowired private OrderNotifyEventProcessor orderNotifyEventProcessor;
  @Autowired private ReportDownloadEventProcessor reportDownloadEventProcessor;

  public UberWebHookEventProcessor getUberWebHookEventProcessor(String eventType) {
    log.info("Uber Webhook eventType: {} ", eventType);
    if (WebhookEventType.ORDER_NOTIFY_EVENT_TYPE.getEventType().equalsIgnoreCase(eventType)) {
      return orderNotifyEventProcessor;
    } else if (WebhookEventType.ORDER_CANCEL_EVENT_TYPE
        .getEventType()
        .equalsIgnoreCase(eventType)) {
      return orderCancelEventProcessor;
    } else if (WebhookEventType.REPORT_SUCCESS_EVENT_TYPE
        .getEventType()
        .equalsIgnoreCase(eventType)) {
      return reportDownloadEventProcessor;
    } else {
      String message = String.format("Unknown event type %s", eventType);
      log.error(message);
      throw new OMSBadRequestException(message);
    }
  }
}
