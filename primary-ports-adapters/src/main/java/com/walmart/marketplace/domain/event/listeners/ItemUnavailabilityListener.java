package com.walmart.marketplace.domain.event.listeners;

import com.walmart.common.domain.event.processing.MessageListener;
import com.walmart.fms.domain.event.message.ItemUnavailabilityMessage;
import com.walmart.marketplace.MarketPlaceApplicationService;
import com.walmart.marketplace.order.aggregateroot.MarketPlaceOrder;
import com.walmart.marketplace.order.domain.entity.UpdateItemInfo;
import com.walmart.marketplace.order.domain.uber.mapper.UpdateItemInfoMapper;
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
public class ItemUnavailabilityListener implements MessageListener<ItemUnavailabilityMessage> {

  private final IMarketPlaceGatewayFinder marketPlaceGatewayFinder;
  private final MarketPlaceApplicationService marketPlaceApplicationService;

  @Override
  @EventListener
  @Async
  public void listen(ItemUnavailabilityMessage itemUnavailabilityMessage) {
    log.info(
        "Message received in item unavailability listener, vendorOrderId : {}",
        itemUnavailabilityMessage.getVendorOrderId());

    UpdateItemInfo updateItemInfo =
        UpdateItemInfoMapper.INSTANCE.convertToUpdateItemInfo(itemUnavailabilityMessage);

    // For Automation suite, Vendor Id is Test Vendor and we won't be invoking the API.
    if (updateItemInfo.isValidVendor()) {
      // Fetch Marketplace order record for fetching Vendor store id.
      MarketPlaceOrder marketPlaceOrder =
          marketPlaceApplicationService.getOrder(itemUnavailabilityMessage.getVendorOrderId());
      updateItemInfo.setVendorStoreId(marketPlaceOrder.getVendorStoreId());
      log.info(
          "Update Item for vendorOrderId : {} with outOfStockItemIds : {}",
          updateItemInfo.getVendorOrderId(),
          updateItemInfo.getOutOfStockItemIds());
      marketPlaceGatewayFinder
          .getMarketPlaceGateway(updateItemInfo.getVendorId())
          .updateItem(updateItemInfo);
    }
  }
}
