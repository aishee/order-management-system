package com.walmart.marketplace.justeats.webhook;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.walmart.config.WebHandlerConfiguration;
import com.walmart.marketplace.justeats.factory.JustEatsEventProcessorFactory;
import com.walmart.marketplace.justeats.processors.JustEatsWebHookEventProcessor;
import com.walmart.marketplace.justeats.request.JustEatsWebHookRequest;
import io.strati.configuration.annotation.ManagedConfiguration;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/marketplace/justeats")
@Slf4j
public class JustEatsWebHookHandler {

  private ExecutorService webHookHandlerService;

  @ManagedConfiguration private WebHandlerConfiguration webHandlerConfiguration;

  @Autowired private JustEatsEventProcessorFactory justEatsWebHookHandlerFactory;

  /** Initial setup for Executor in Post Construct. */
  @PostConstruct
  public void setup() {
    webHookHandlerService =
        Executors.newFixedThreadPool(
            webHandlerConfiguration.getThreadPoolSize(),
            new ThreadFactoryBuilder().setNameFormat("JustEats-Webhook-Thread-%d").build());
  }

  /**
   * Exposing Web hook event that will be invoked by JustEats.
   *
   * @param webHookRequest JustEats Web hook request payload.
   */
  @PostMapping(path = "/webhook", produces = MediaType.APPLICATION_JSON_VALUE)
  @ResponseStatus(HttpStatus.ACCEPTED)
  public void justEatsWebHook(@RequestBody JustEatsWebHookRequest webHookRequest) {
    initMDC();
    log.info("Received JustEats webhook Request: {}", webHookRequest.toString());
    JustEatsWebHookEventProcessor justEatsWebHookEventProcessor =
        justEatsWebHookHandlerFactory.getJustEatsWebHookEventProcessor();
    Callable<Boolean> getWebHookTask =
        () -> justEatsWebHookEventProcessor.processWebhookRequest(webHookRequest);
    webHookHandlerService.submit(getWebHookTask);
  }

  private void initMDC() {
    MDC.put("api", "JUST_EATS_WEBHOOK_API");
    MDC.put("domain", "MarketPlace");
  }
}
