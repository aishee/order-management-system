package com.walmart.marketplace.infrastructure.gateway.util;

import com.walmart.marketplace.order.domain.entity.type.Vendor;
import com.walmart.marketplace.order.gateway.IMarketPlaceGatewayFinder;
import com.walmart.marketplace.order.repository.IMarketPlaceGateWay;
import groovy.util.logging.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class MarketPlaceGatewayFinder implements IMarketPlaceGatewayFinder {

  @Autowired private ServiceFinder serviceFinder;

  @Override
  public IMarketPlaceGateWay getMarketPlaceGateway(Vendor vendor) {
    return serviceFinder.getService(IMarketPlaceGateWay.class, vendor);
  }
}
