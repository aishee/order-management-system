package com.walmart.marketplace.eventprocessors;

import com.walmart.common.metrics.MetricService;
import com.walmart.marketplace.commands.MarketPlaceOrderConfirmationCommand;
import com.walmart.marketplace.order.aggregateroot.MarketPlaceOrder;
import com.walmart.marketplace.order.factory.MarketPlaceOrderFactory;
import com.walmart.marketplace.order.repository.IMarketPlaceRepository;
import com.walmart.oms.domain.error.exception.OMSBadRequestException;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Slf4j
public class MarketPlaceOrderConfirmationService {

  @Autowired private MarketPlaceOrderFactory marketPlaceOrderFactory;

  @Autowired private IMarketPlaceRepository marketPlaceRepository;

  @Autowired private MetricService metricService;

  @Transactional
  public MarketPlaceOrder orderConfirmedAtStore(
      MarketPlaceOrderConfirmationCommand marketPlaceOrderConfirmationCommand) {
    try {
      return marketPlaceOrderFactory
          .getOrder(marketPlaceOrderConfirmationCommand.getSourceOrderId())
          .map(this::confirmOrder)
          .orElseThrow(() -> handleException(marketPlaceOrderConfirmationCommand));
    } catch (Exception exception) {
      log.error("Exception occurred in order confirmation : ", exception);
      String exceptionType = exception.getClass().getSimpleName();
      metricService.incrementCounterByType("oms_inbound_exception_counter", exceptionType);
      throw exception;
    } finally {
      metricService.incrementCounterByType("inbound_order_count", "OMS");
      MDC.clear();
    }
  }

  private OMSBadRequestException handleException(
      MarketPlaceOrderConfirmationCommand marketPlaceOrderConfirmationCommand) {
    log.error(
        "Order doesn't exist with source order id :{}",
        marketPlaceOrderConfirmationCommand.getSourceOrderId());
    return new OMSBadRequestException(
        "Order doesn't exist with order id :"
            + marketPlaceOrderConfirmationCommand.getSourceOrderId());
  }

  private MarketPlaceOrder confirmOrder(MarketPlaceOrder marketPlaceOrder) {
    marketPlaceOrder.orderConfirmedAtStore();
    marketPlaceRepository.save(marketPlaceOrder);
    return marketPlaceOrder;
  }
}
