package com.walmart.fms.order.gateway;

import com.walmart.fms.order.aggregateroot.FmsOrder;
import java.util.concurrent.Future;

public interface IStoreGateway {

  Future<Boolean> sendMarketPlaceOrderDownloadAsync(FmsOrder fmsOrder);

  Boolean sendMarketPlaceOrderDownload(FmsOrder fmsOrder);

  Boolean sendMarketPlaceForceOrderCancellation(FmsOrder fmsOrder);
}
