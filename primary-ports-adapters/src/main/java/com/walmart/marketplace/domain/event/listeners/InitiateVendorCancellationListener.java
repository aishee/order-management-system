package com.walmart.marketplace.domain.event.listeners;

import com.walmart.common.domain.event.processing.MessageListener;
import com.walmart.marketplace.domain.event.messages.MarketPlaceOrderCancelMessage;
import com.walmart.marketplace.order.gateway.IMarketPlaceGatewayFinder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class InitiateVendorCancellationListener
    implements MessageListener<MarketPlaceOrderCancelMessage> {

  private final IMarketPlaceGatewayFinder marketPlaceGatewayFinder;

  private static final String STORE_CANCELLED = "STORE_CANCELLED";
  private static final String SUCCESS_MESSAGE =
      "MarketPlaceOrder cancel message was successfully sent to Vendor";
  private static final String FAILURE_MESSAGE =
      "MarketPlaceOrder cancel message was not sent to Vendor";

  @Override
  @EventListener
  @Async
  public void listen(MarketPlaceOrderCancelMessage marketPlaceOrderCancelMessage) {
    log.info("MarketPlaceOrderCancelMessage received : {}", marketPlaceOrderCancelMessage);
    processMarketPlaceOrderCancelEvent(marketPlaceOrderCancelMessage);
  }

  private void processMarketPlaceOrderCancelEvent(
      MarketPlaceOrderCancelMessage marketPlaceOrderCancelMessage) {
    if (isEligibleForUpdatingCancelEventToVendor(marketPlaceOrderCancelMessage)) {
      boolean isSuccessful = publishCancelEventToVendor(marketPlaceOrderCancelMessage);
      logResponse(isSuccessful, marketPlaceOrderCancelMessage);
    } else {
      log.warn(
          "MarketPlace Order is not eligible for updating Cancel Event to vendor : {} ",
          marketPlaceOrderCancelMessage);
    }
  }

  private boolean isEligibleForUpdatingCancelEventToVendor(
      MarketPlaceOrderCancelMessage marketPlaceOrderCancelMessage) {
    return !marketPlaceOrderCancelMessage.isCancelledByVendor()
        && !marketPlaceOrderCancelMessage.isTestOrder();
  }

  private boolean publishCancelEventToVendor(
      MarketPlaceOrderCancelMessage marketPlaceOrderCancelMessage) {
    return marketPlaceGatewayFinder
        .getMarketPlaceGateway(marketPlaceOrderCancelMessage.getVendor())
        .cancelOrder(marketPlaceOrderCancelMessage.getVendorOrderId(), STORE_CANCELLED);
  }

  private void logResponse(
      boolean isSuccessful, MarketPlaceOrderCancelMessage marketPlaceOrderCancelMessage) {
    String message = isSuccessful ? SUCCESS_MESSAGE : FAILURE_MESSAGE;

    log.info(
        String.format(
            "%s for MarketPlaceOrderCancelMessage : %s ", message, marketPlaceOrderCancelMessage));
  }
}
