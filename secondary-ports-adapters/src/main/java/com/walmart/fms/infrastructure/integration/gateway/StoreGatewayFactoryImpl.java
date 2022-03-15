package com.walmart.fms.infrastructure.integration.gateway;

import com.walmart.fms.infrastructure.integration.gateway.store.GifStoreGateway;
import com.walmart.fms.order.aggregateroot.FmsOrder;
import com.walmart.fms.order.gateway.StoreGateway;
import com.walmart.fms.order.gateway.StoreGatewayFactory;
import java.util.Optional;
import java.util.function.Consumer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component("storeGatewayFactory")
public class StoreGatewayFactoryImpl implements StoreGatewayFactory {

  @Autowired private GifStoreGateway gifMaasStoreGateway;

  @Override
  public StoreGateway getGatewayFor(FmsOrder.FulfillmentApp type) {
    if (type == FmsOrder.FulfillmentApp.GIF_MAAS) {
      return gifMaasStoreGateway;
    }
    return new DefaultStoreGateway();
  }

  /**
   * A default {@link com.walmart.fms.infrastructure.integration.gateway.store.StoreGateway}
   * implementation which just logs the <code>sourceOrderId</code> of the order.
   */
  @Slf4j
  public static class DefaultStoreGateway implements StoreGateway {
    @Override
    public void sendOrderConfirmation(FmsOrder fmsOrder) {
      log.warn(
          "You are using default store gateway. This is a dummy gateway implementation!!! OrderID={}",
          fmsOrder.getSourceOrderId());
    }

    @Override
    public void sendOrderCancellation(FmsOrder fmsOrder) {
      log.warn(
          "You are using default store gateway. This is a dummy gateway implementation!!! OrderID={}",
          fmsOrder.getSourceOrderId());
    }

    @Override
    public Optional<Consumer<FmsOrder>> getActionByCode(String eventName) {
      return Optional.ofNullable(this::sendOrderCancellation);
    }
  }
}
