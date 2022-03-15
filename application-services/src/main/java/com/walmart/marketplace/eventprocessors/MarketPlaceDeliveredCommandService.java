package com.walmart.marketplace.eventprocessors;

import com.walmart.marketplace.commands.MarketPlaceDeliveredOrderCommand;
import com.walmart.marketplace.order.aggregateroot.MarketPlaceOrder;
import com.walmart.marketplace.order.factory.MarketPlaceOrderFactory;
import com.walmart.marketplace.order.repository.IMarketPlaceRepository;
import com.walmart.oms.domain.error.exception.OMSBadRequestException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Slf4j
public class MarketPlaceDeliveredCommandService {

  @Autowired private MarketPlaceOrderFactory marketPlaceOrderFactory;

  @Autowired private IMarketPlaceRepository marketPlaceRepository;

  @Transactional
  public MarketPlaceOrder deliverOrder(
      MarketPlaceDeliveredOrderCommand marketPlaceDeliveredOrderCommand) {
    return marketPlaceOrderFactory
        .getOrder(marketPlaceDeliveredOrderCommand.getSourceOrderId())
        .map(this::deliverMarketPlaceOrder)
        .orElseThrow(() -> handleException(marketPlaceDeliveredOrderCommand));
  }

  private OMSBadRequestException handleException(
      MarketPlaceDeliveredOrderCommand marketPlaceDeliveredOrderCommand) {
    log.error(
        "Order doesn't exist with source order id :{}",
        marketPlaceDeliveredOrderCommand.getSourceOrderId());
    return new OMSBadRequestException(
        "Order doesn't exist with order id :"
            + marketPlaceDeliveredOrderCommand.getSourceOrderId());
  }

  private MarketPlaceOrder deliverMarketPlaceOrder(MarketPlaceOrder marketPlaceOrder) {
    marketPlaceOrder.markOrderAsDelivered();
    marketPlaceRepository.save(marketPlaceOrder);
    return marketPlaceOrder;
  }
}
