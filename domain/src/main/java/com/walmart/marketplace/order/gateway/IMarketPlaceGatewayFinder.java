package com.walmart.marketplace.order.gateway;

import com.walmart.marketplace.order.domain.entity.type.Vendor;
import com.walmart.marketplace.order.repository.IMarketPlaceGateWay;

public interface IMarketPlaceGatewayFinder {

  IMarketPlaceGateWay getMarketPlaceGateway(Vendor vendor);
}
