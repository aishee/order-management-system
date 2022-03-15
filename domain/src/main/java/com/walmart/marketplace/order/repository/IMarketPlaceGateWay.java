package com.walmart.marketplace.order.repository;

import com.walmart.marketplace.order.aggregateroot.MarketPlaceOrder;
import com.walmart.marketplace.order.domain.entity.UpdateItemInfo;
import com.walmart.marketplace.order.domain.uber.MarketPlaceReportRequest;
import com.walmart.marketplace.order.domain.uber.PatchCartInfo;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface IMarketPlaceGateWay {

  boolean acceptOrder(MarketPlaceOrder marketPlaceOrder);

  boolean rejectOrder(MarketPlaceOrder marketPlaceOrder, String reason);

  MarketPlaceOrder getOrder(String vendorOrderId, String resourceUrl);

  boolean cancelOrder(String vendorOrderId, String reason);

  CompletableFuture<Boolean> patchCart(PatchCartInfo patchCartInfo);

  CompletableFuture<List<Boolean>> updateItem(UpdateItemInfo updateItemInfo);

  String invokeMarketPlaceReport(MarketPlaceReportRequest marketplaceReportRequest);
}
