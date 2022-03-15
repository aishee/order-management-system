package com.walmart.marketplace.order.domain.mappers;

import com.walmart.common.domain.valueobject.CancellationDetails;
import com.walmart.marketplace.domain.event.messages.MarketPlaceOrderCancelMessage;
import com.walmart.marketplace.order.aggregateroot.MarketPlaceOrder;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class MarketPlaceDomainToEventMessageMapper {

  public static MarketPlaceOrderCancelMessage mapToMarketPlaceOrderCancelMessage(
      MarketPlaceOrder marketPlaceOrder, CancellationDetails cancellationDetails) {
    return MarketPlaceOrderCancelMessage.builder()
        .vendorOrderId(marketPlaceOrder.getVendorOrderId())
        .isTestOrder(marketPlaceOrder.isTestOrder())
        .vendor(marketPlaceOrder.getVendorId())
        .cancellationSource(cancellationDetails.getCancelledBy())
        .cancelledReasonCode(cancellationDetails.getCancelledReasonCode())
        .vendorStoreId(marketPlaceOrder.getVendorStoreId())
        .storeId(marketPlaceOrder.getStoreId())
        .externalItemIds(marketPlaceOrder.getExternalItemIds())
        .build();
  }
}