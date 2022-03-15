package com.walmart.marketplace.infrastructure.gateway.justeats;

import com.walmart.marketplace.infrastructure.gateway.justeats.dto.request.DenialErrorCode;
import com.walmart.marketplace.infrastructure.gateway.util.ServiceFinder;
import com.walmart.marketplace.order.aggregateroot.MarketPlaceOrder;
import com.walmart.marketplace.order.domain.entity.UpdateItemInfo;
import com.walmart.marketplace.order.domain.uber.MarketPlaceReportRequest;
import com.walmart.marketplace.order.domain.uber.PatchCartInfo;
import com.walmart.marketplace.order.repository.IMarketPlaceGateWay;
import com.walmart.marketplace.repository.MarketPlaceRepository;
import com.walmart.oms.domain.error.exception.OMSBadRequestException;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@ServiceFinder.JUSTEAT
public class JustEatsOrderGateway implements IMarketPlaceGateWay {

  @Autowired private JustEatsOrderStatusUpdateClient justEatsOrderStatusUpdateClient;
  @Autowired private JustEatsItemAvailabilityUpdateClient justEatsItemAvailabilityUpdateClient;
  @Autowired private MarketPlaceRepository marketPlaceRepository;

  @Override
  public boolean acceptOrder(MarketPlaceOrder marketPlaceOrder) {
    return justEatsOrderStatusUpdateClient.acceptOrder(marketPlaceOrder.getVendorNativeOrderId());
  }

  @Override
  public boolean rejectOrder(MarketPlaceOrder marketPlaceOrder, String reason) {
    return justEatsOrderStatusUpdateClient.rejectOrder(
        marketPlaceOrder.getVendorNativeOrderId(), DenialErrorCode.IN_USE);
  }

  @Override
  public MarketPlaceOrder getOrder(String vendorOrderId, String resourceUrl) {
    return marketPlaceRepository.get(vendorOrderId);
  }

  @Override
  public boolean cancelOrder(String vendorOrderId, String reason) {
    log.warn("JustEat doesn't provide Cancellation API. Order id:{}", vendorOrderId);
    return true;
  }

  @Override
  public CompletableFuture<Boolean> patchCart(PatchCartInfo patchCartInfo) {
    log.warn(
        "JustEat doesn't provide Patch cart API. Order Id:{}", patchCartInfo.getVendorOrderId());
    return CompletableFuture.completedFuture(true);
  }

  @Override
  public CompletableFuture<List<Boolean>> updateItem(UpdateItemInfo updateItemInfo) {
    return CompletableFuture.completedFuture(
        justEatsItemAvailabilityUpdateClient.updateItemInfo(updateItemInfo));
  }

  @Override
  public String invokeMarketPlaceReport(MarketPlaceReportRequest marketplaceReportRequest) {
    log.warn("JustEat doesn't provide Business Report APIs, Request:{}", marketplaceReportRequest);
    throw new OMSBadRequestException("JustEat doesn't provide Report API");
  }
}
