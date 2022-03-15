package com.walmart.marketplace.uber.webhook.processors;

import com.walmart.common.metrics.MetricConstants;
import com.walmart.marketplace.commands.CreateMarketPlaceOrderFromAdapterCommand;
import com.walmart.marketplace.order.domain.entity.MarketPlaceEvent;
import com.walmart.marketplace.uber.dto.UberWebHookRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class OrderNotifyEventProcessor extends UberWebHookEventProcessor {

  @Override
  public boolean process(UberWebHookRequest webHookRequest) {
    metricService.incrementCounterByType(
        MetricConstants.MetricCounters.WEB_HOOK_COUNTER.getCounter(),
        MetricConstants.MetricTypes.UBER_ORDER_CREATE.getType());
    MarketPlaceEvent marketPlaceEvent = captureMarketPlaceEvent(webHookRequest);
    CreateMarketPlaceOrderFromAdapterCommand createMarketPlaceOrderFromAdapterCommand =
        mapper.createMarketPlaceOrderCmd(
            marketPlaceEvent.getExternalOrderId(),
            marketPlaceEvent.getResourceURL(),
            marketPlaceEvent.getVendor());
    marketPlaceApplicationService.createAndProcessMarketPlaceOrder(
        createMarketPlaceOrderFromAdapterCommand);
    return true;
  }
}
