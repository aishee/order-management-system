package com.walmart.marketplace.order.factory;

import com.walmart.common.domain.type.Currency;
import com.walmart.marketplace.order.aggregateroot.MarketPlaceOrder;
import com.walmart.marketplace.order.domain.entity.type.Vendor;
import com.walmart.marketplace.order.domain.valueobject.MarketPlaceOrderContactInfo;
import com.walmart.marketplace.order.domain.valueobject.MarketPlaceOrderPaymentInfo;
import com.walmart.marketplace.order.domain.valueobject.Money;
import com.walmart.marketplace.order.gateway.IMarketPlaceGatewayFinder;
import com.walmart.marketplace.order.repository.IMarketPlaceRepository;
import com.walmart.oms.domain.error.exception.OMSBadRequestException;
import java.math.BigDecimal;
import java.util.Date;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class MarketPlaceOrderFactory {

  @Autowired private IMarketPlaceRepository marketPlaceRepository;

  @Autowired private IMarketPlaceGatewayFinder marketPlaceGatewayFinder;

  public MarketPlaceOrder getMarketPlaceOrderFromGateway(
      String vendorOrderId, String resourceUrl, Vendor vendor) {

    MarketPlaceOrder order = marketPlaceRepository.get(vendorOrderId);
    if (order == null) {
      order =
          marketPlaceGatewayFinder
              .getMarketPlaceGateway(vendor)
              .getOrder(vendorOrderId, resourceUrl);
    }

    return order;
  }

  public MarketPlaceOrder getMarketPlaceOrderFromCommand(
      String externalOrderId,
      String externalNativeOrderId,
      String firstName,
      String lastName,
      String storeId,
      String vendorStoreId,
      Date sourceOrderCreationTime,
      Vendor vendor,
      Date estimatedPickuptime,
      MarketPlaceOrderPaymentInfo paymentInfo) {

    MarketPlaceOrder order = marketPlaceRepository.get(externalOrderId);

    if (order == null) {
      String nextId = marketPlaceRepository.getNextIdentity();
      MarketPlaceOrderContactInfo marketPlaceOrderContactInfo =
          MarketPlaceOrderContactInfo.builder().firstName(firstName).lastName(lastName).build();
      order =
          MarketPlaceOrder.builder()
              .id(nextId)
              .vendorOrderId(externalOrderId)
              .vendorNativeOrderId(externalNativeOrderId)
              .orderDueTime(estimatedPickuptime)
              .vendorId(vendor)
              .orderState("CREATED")
              .sourceModifiedDate(sourceOrderCreationTime)
              .marketPlaceOrderContactInfo(marketPlaceOrderContactInfo)
              .storeId(storeId)
              .vendorStoreId(vendorStoreId)
              .paymentInfo(paymentInfo)
              .build();
    } else {
      String errorMsg = String.format("Order is already Created orderInfo :: %s", order);
      log.error(errorMsg);
      throw new OMSBadRequestException(errorMsg);
    }

    return order;
  }

  public MarketPlaceOrderPaymentInfo getPaymentInfo(
      Money total, Money subTotal, Money tax, Money totalFee, Money totalFeeTax, Money bagFee) {
    return MarketPlaceOrderPaymentInfo.builder()
        .total(total != null ? total : new Money(BigDecimal.ZERO, Currency.GBP))
        .subTotal(subTotal != null ? subTotal : new Money(BigDecimal.ZERO, Currency.GBP))
        .tax(tax != null ? tax : new Money(BigDecimal.ZERO, Currency.GBP))
        .totalFee(totalFee != null ? totalFee : new Money(BigDecimal.ZERO, Currency.GBP))
        .totalFeeTax(totalFeeTax != null ? totalFeeTax : new Money(BigDecimal.ZERO, Currency.GBP))
        .bagFee(bagFee != null ? bagFee : new Money(BigDecimal.ZERO, Currency.GBP))
        .build();
  }

  public Optional<MarketPlaceOrder> getOrder(String id) {
    return marketPlaceRepository.getById(id);
  }
}
