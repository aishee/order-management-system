package com.walmart.marketplace.justeats.processors;

import com.walmart.common.metrics.MetricConstants;
import com.walmart.marketplace.commands.MarketPlaceCreateOrderCommand;
import com.walmart.marketplace.justeats.request.JustEatsWebHookRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class JustEatsOrderNotifyEventProcessor extends JustEatsWebHookEventProcessor {

  @Override
  public boolean process(JustEatsWebHookRequest webHookRequest) {
    metricService.incrementCounterByType(
        MetricConstants.MetricCounters.WEB_HOOK_COUNTER.getCounter(),
        MetricConstants.MetricTypes.JUST_EAT_ORDER_CREATE.getType());
    captureMarketPlaceEvent(webHookRequest);
    MarketPlaceCreateOrderCommand marketPlaceCreateOrderCommand =
        mapper.createMarketPlaceOrderCmd(webHookRequest);
    marketPlaceApplicationService.createAndProcessMarketPlaceOrder(marketPlaceCreateOrderCommand);
    return true;
  }
}
