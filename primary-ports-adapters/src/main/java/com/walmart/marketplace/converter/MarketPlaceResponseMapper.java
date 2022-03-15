package com.walmart.marketplace.converter;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

import com.walmart.marketplace.dto.response.MarketPlaceOrderResponse;
import com.walmart.marketplace.dto.response.MarketPlaceOrderResponse.MarketPlaceResponseBundledItemData;
import com.walmart.marketplace.order.aggregateroot.MarketPlaceOrder;
import com.walmart.marketplace.order.domain.entity.MarketPlaceBundledItem;
import com.walmart.marketplace.order.domain.entity.MarketPlaceItem;
import com.walmart.marketplace.order.domain.valueobject.Money;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

@Component
public class MarketPlaceResponseMapper {

  public MarketPlaceOrderResponse mapToDto(MarketPlaceOrder marketPlaceOrder) {

    if (nonNull(marketPlaceOrder)) {

      return MarketPlaceOrderResponse.builder()
          .data(
              MarketPlaceOrderResponse.MarketPlaceOrderResponseData.builder()
                  .orderStatus(marketPlaceOrder.getOrderState())
                  .id(marketPlaceOrder.getId())
                  .externalOrderId(marketPlaceOrder.getVendorOrderId())
                  .firstName(marketPlaceOrder.getMarketPlaceOrderContactInfo().getFirstName())
                  .lastName(marketPlaceOrder.getMarketPlaceOrderContactInfo().getLastName())
                  .storeId(marketPlaceOrder.getStoreId())
                  .vendorStoreId(marketPlaceOrder.getVendorStoreId())
                  .vendor(marketPlaceOrder.getVendorId())
                  .estimatedDueTime(marketPlaceOrder.getOrderDueTime())
                  .payment(mapPayment(marketPlaceOrder))
                  .marketPlaceItems(mapItems(marketPlaceOrder))
                  .build())
          .build();
    }
    return null;
  }

  private List<MarketPlaceOrderResponse.MarketPlaceResponseItemData> mapItems(
      MarketPlaceOrder marketPlaceOrder) {
    return marketPlaceOrder.getMarketPlaceItems().stream()
        .map(this::mapItem)
        .filter(Objects::nonNull)
        .collect(Collectors.toList());
  }

  private MarketPlaceOrderResponse.MarketPlaceResponseItemData mapItem(
      MarketPlaceItem marketPlaceItem) {
    if (isNull(marketPlaceItem)) {
      return null;
    } else {
      return MarketPlaceOrderResponse.MarketPlaceResponseItemData.builder()
          .baseTotalPrice(marketPlaceItem.getMarketPlacePriceInfo().getBaseTotalPrice())
          .baseUnitPrice(marketPlaceItem.getMarketPlacePriceInfo().getBaseUnitPrice())
          .externalItemId(marketPlaceItem.getExternalItemId())
          .itemDescription(marketPlaceItem.getItemDescription())
          .itemId(marketPlaceItem.getItemIdentifier().getItemId())
          .itemType(marketPlaceItem.getItemIdentifier().getItemType())
          .vendorInstanceId(marketPlaceItem.getVendorInstanceId())
          .quantity(marketPlaceItem.getQuantity())
          .totalPrice(marketPlaceItem.getMarketPlacePriceInfo().getTotalPrice())
          .unitPrice(marketPlaceItem.getMarketPlacePriceInfo().getUnitPrice())
          .substitutionOption(marketPlaceItem.getSubstitutionOption())
          .bundledItems(mapBundledItems(marketPlaceItem.getBundledItemList()))
          .build();
    }
  }

  private List<MarketPlaceResponseBundledItemData> mapBundledItems(
      List<MarketPlaceBundledItem> bundledItemList) {
    if (CollectionUtils.isEmpty(bundledItemList)) {
      return Collections.emptyList();
    }
    return bundledItemList.stream()
        .map(this::mapBundledItem)
        .filter(Objects::nonNull)
        .collect(Collectors.toList());
  }

  private MarketPlaceResponseBundledItemData mapBundledItem(
      MarketPlaceBundledItem marketPlaceBundledItem) {
    if (isNull(marketPlaceBundledItem)) {
      return null;
    } else {
      return MarketPlaceResponseBundledItemData.builder()
          .bundleSkuId(marketPlaceBundledItem.getBundleSkuId())
          .bundleQuantity(marketPlaceBundledItem.getBundleQuantity())
          .bundleInstanceId(marketPlaceBundledItem.getBundleInstanceId())
          .bundleDescription(marketPlaceBundledItem.getBundleDescription())
          .build();
    }
  }

  private MarketPlaceOrderResponse.PaymentInfo mapPayment(MarketPlaceOrder marketPlaceOrder) {

    if (isNull(marketPlaceOrder.getMarketPlaceOrderPaymentInfo())) {
      return null;
    } else {
      return MarketPlaceOrderResponse.PaymentInfo.builder()
          .total(getAmountFromMoney(marketPlaceOrder.getMarketPlaceOrderPaymentInfo().getTotal()))
          .bagFee(getAmountFromMoney(marketPlaceOrder.getMarketPlaceOrderPaymentInfo().getBagFee()))
          .subTotal(
              getAmountFromMoney(marketPlaceOrder.getMarketPlaceOrderPaymentInfo().getSubTotal()))
          .totalFee(
              getAmountFromMoney(marketPlaceOrder.getMarketPlaceOrderPaymentInfo().getTotalFee()))
          .totalFeeTax(
              getAmountFromMoney(
                  marketPlaceOrder.getMarketPlaceOrderPaymentInfo().getTotalFeeTax()))
          .tax(getAmountFromMoney(marketPlaceOrder.getMarketPlaceOrderPaymentInfo().getTax()))
          .build();
    }
  }

  private BigDecimal getAmountFromMoney(Money uberMoney) {

    if (uberMoney != null) {
      return uberMoney.getAmount();
    }
    return null;
  }
}
