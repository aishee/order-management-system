package com.walmart.marketplace.uber.webhook;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.walmart.config.WebHandlerConfiguration;
import com.walmart.marketplace.uber.dto.UberWebHookRequest;
import com.walmart.marketplace.uber.webhook.factory.UberWebHookHandlerFactory;
import com.walmart.marketplace.uber.webhook.processors.UberWebHookEventProcessor;
import io.strati.configuration.annotation.ManagedConfiguration;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/marketplace/uber")
@Slf4j
public class UberWebHookHandler {

  private ExecutorService uberWebHookHandlerService;

  @ManagedConfiguration private WebHandlerConfiguration uberWebHandlerConfiguration;
  @Autowired private UberWebHookHandlerFactory uberWebHookHandlerFactory;

  @PostConstruct
  public void setup() {
    uberWebHookHandlerService =
        Executors.newFixedThreadPool(
            uberWebHandlerConfiguration.getThreadPoolSize(),
            new ThreadFactoryBuilder().setNameFormat("Uber-Webhook-Thread-%d").build());
  }

  @PostMapping(path = "/webhook", produces = MediaType.APPLICATION_JSON_VALUE)
  public void uberWebHook(@RequestBody UberWebHookRequest webHookRequest) {
    initMDC("UBER_WEBHOOK_API", "MarketPlace");
    log.info("Received Uber webhook Request: {}", webHookRequest.toString());
    UberWebHookEventProcessor uberWebHookEventProcessor =
        uberWebHookHandlerFactory.getUberWebHookEventProcessor(webHookRequest.getEventType());
    Callable<Boolean> getWebHookTask =
        () -> uberWebHookEventProcessor.processWebhookRequest(webHookRequest);
    uberWebHookHandlerService.submit(getWebHookTask);
  }

  void initMDC(String api, String domain) {
    MDC.put("api", api);
    MDC.put("domain", domain);
  }
}
