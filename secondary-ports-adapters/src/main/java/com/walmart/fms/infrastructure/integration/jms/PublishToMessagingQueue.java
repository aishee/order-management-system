package com.walmart.fms.infrastructure.integration.jms;

import lombok.extern.slf4j.Slf4j;
import org.apache.camel.CamelContext;
import org.apache.camel.ProducerTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class PublishToMessagingQueue {

  @Autowired CamelContext camelContext;

  /**
   * This is an helper method to post events to the JMS queue based on the camelRoute.
   *
   * @param endpointUri
   * @param message
   */
  public void postEventToByEndpointUri(String endpointUri, String message) {
    ProducerTemplate template = camelContext.createProducerTemplate();
    template.sendBody(endpointUri, message);
    log.info("Message sent to next Queue: endpoint-uri={}, message={}", endpointUri, message);
  }
}
