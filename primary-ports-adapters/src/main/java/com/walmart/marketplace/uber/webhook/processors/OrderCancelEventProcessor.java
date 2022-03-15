package com.walmart.marketplace.uber.webhook.processors;

import com.walmart.common.metrics.MetricConstants;
import com.walmart.marketplace.commands.CancelMarketPlaceOrderCommand;
import com.walmart.marketplace.order.aggregateroot.MarketPlaceOrder;
import com.walmart.marketplace.order.domain.entity.MarketPlaceEvent;
import com.walmart.marketplace.uber.dto.UberWebHookRequest;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class OrderCancelEventProcessor extends UberWebHookEventProcessor {

  @Override
  public boolean process(UberWebHookRequest webHookRequest) {
    metricService.incrementCounterByType(
        MetricConstants.MetricCounters.WEB_HOOK_COUNTER.getCounter(),
        MetricConstants.MetricTypes.UBER_ORDER_CANCEL.getType());
    MarketPlaceEvent marketPlaceEvent = captureMarketPlaceEvent(webHookRequest);
    MarketPlaceOrder marketPlaceOrder =
        marketPlaceApplicationService.getOrder(marketPlaceEvent.getExternalOrderId());
    return Optional.ofNullable(marketPlaceOrder)
        .map(order -> cancelMarketPlaceOrder(marketPlaceEvent, order))
        .orElseGet(() -> errorHandling(marketPlaceEvent));
  }

  private boolean errorHandling(MarketPlaceEvent marketPlaceEvent) {
    log.error("Order not found for vendor order id :{}", marketPlaceEvent.getExternalOrderId());
    return false;
  }

  private boolean cancelMarketPlaceOrder(
      MarketPlaceEvent marketPlaceEvent, MarketPlaceOrder marketPlaceOrder) {
    CancelMarketPlaceOrderCommand cancelMarketPlaceOrderCommand =
        createMarketPlaceCancelCommand(marketPlaceEvent, marketPlaceOrder);
    marketPlaceApplicationService.cancelOrder(cancelMarketPlaceOrderCommand);
    return true;
  }

  private CancelMarketPlaceOrderCommand createMarketPlaceCancelCommand(
      MarketPlaceEvent event, MarketPlaceOrder marketPlaceOrder) {
    return mapper.createMarketPlaceCancelCommand(
        marketPlaceOrder.getId(),
        "VENDOR",
        "cancelled by vendor",
        event.getResourceURL(),
        event.getVendor());
  }
}
