package com.walmart.marketplace.justeats.factory;

import com.walmart.marketplace.justeats.processors.JustEatsOrderNotifyEventProcessor;
import com.walmart.marketplace.justeats.processors.JustEatsWebHookEventProcessor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class JustEatsEventProcessorFactory {

  private final JustEatsOrderNotifyEventProcessor justEatsOrderNotifyEventProcessor;

  @Autowired
  public JustEatsEventProcessorFactory(
      JustEatsOrderNotifyEventProcessor justEatsOrderNotifyEventProcessor) {
    this.justEatsOrderNotifyEventProcessor = justEatsOrderNotifyEventProcessor;
  }

  public JustEatsWebHookEventProcessor getJustEatsWebHookEventProcessor() {
    return justEatsOrderNotifyEventProcessor;
  }
}
