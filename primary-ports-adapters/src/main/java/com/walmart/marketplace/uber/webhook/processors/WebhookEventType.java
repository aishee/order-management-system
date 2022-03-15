package com.walmart.marketplace.uber.webhook.processors;

import lombok.Getter;

@Getter
public enum WebhookEventType {
  ORDER_NOTIFY_EVENT_TYPE("orders.notification"),
  ORDER_CANCEL_EVENT_TYPE("orders.cancel"),
  REPORT_SUCCESS_EVENT_TYPE("eats.report.success");

  private final String eventType;

  WebhookEventType(String eventType) {
    this.eventType = eventType;
  }
}
