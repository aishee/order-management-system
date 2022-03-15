package com.walmart.marketplace.domain.event.listeners;

import com.walmart.common.domain.event.processing.MessageListener;
import com.walmart.marketplace.domain.event.messages.MarketPlacePickCompleteMessage;
import com.walmart.marketplace.order.domain.uber.PatchCartInfo;
import com.walmart.marketplace.order.domain.uber.mapper.PatchCartInfoMapper;
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
public class PickCompletePatchCartListener
    implements MessageListener<MarketPlacePickCompleteMessage> {

  private final IMarketPlaceGatewayFinder marketPlaceGatewayFinder;

  @Override
  @EventListener
  @Async
  public void listen(MarketPlacePickCompleteMessage marketPlacePickCompleteMessage) {
    log.info(
        "Message received in patch cart listener, vendorOrderId : {}",
        marketPlacePickCompleteMessage.getVendorOrderId());
    if (marketPlacePickCompleteMessage.invokePatchCart()) {
      PatchCartInfo patchCartInfo =
          PatchCartInfoMapper.INSTANCE.convertToPatchCartInfo(marketPlacePickCompleteMessage);
      if (patchCartInfo.isValidVendor() && patchCartInfo.containsNilOrPartialPicks()) {
        log.info(
            "Nil pick count : {} and Partial pick count : {} for order {}",
            patchCartInfo.getNilPicksCount(),
            patchCartInfo.getPartialPicksCount(),
            patchCartInfo.getVendorOrderId());
        marketPlaceGatewayFinder
            .getMarketPlaceGateway(patchCartInfo.getVendorId())
            .patchCart(patchCartInfo);
      }
    }
  }
}
