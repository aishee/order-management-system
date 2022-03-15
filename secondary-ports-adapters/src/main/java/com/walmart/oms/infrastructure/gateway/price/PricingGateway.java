package com.walmart.oms.infrastructure.gateway.price;

import com.walmart.oms.domain.event.messages.OrderCancelledDomainEventMessage;
import com.walmart.oms.infrastructure.gateway.price.dto.DetailLine;
import com.walmart.oms.infrastructure.gateway.price.dto.OrderInformation;
import com.walmart.oms.infrastructure.gateway.price.dto.SubstitutedItem;
import com.walmart.oms.infrastructure.gateway.price.validators.PYSIPYPRequestValidator;
import com.walmart.oms.order.aggregateroot.OmsOrder;
import com.walmart.oms.order.gateway.IPricingGateway;
import com.walmart.oms.order.valueobject.PricingResponse;
import com.walmart.tax.calculator.dto.Tax;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class PricingGateway implements IPricingGateway {

  private static final String PRICING_SUCCESSFUL_RESPONSE_CODE = "SUCCESS";
  @Autowired private PYSIPYPWebClient pysipypWebClient;

  @Autowired private PYSIPYPRequestValidator pysipypRequestValidator;

  @Override
  public Optional<PricingResponse> priceOrder(OmsOrder omsOrder, Map<String, Tax> taxInfoMap) {
    if (pysipypRequestValidator.isValidRecordSaleRequest(omsOrder, taxInfoMap)) {
      OrderInformation pysipypResponse = pysipypWebClient.getPriceData(omsOrder, taxInfoMap);
      return mapToPricingResponse(pysipypResponse);
    }
    return Optional.empty();
  }

  private Optional<PricingResponse> mapToPricingResponse(OrderInformation pysipypResponse) {
    PricingResponse pricingResponse = new PricingResponse();
    pricingResponse.setPosOrderTotalPrice(
        Double.parseDouble(pysipypResponse.getPosOrderTotalPrice()));
    pricingResponse.setItemPriceServiceMap(
        getItemLevelPriceServiceMap(pysipypResponse.getDetailLine()));
    return Optional.of(pricingResponse);
  }

  private Map<String, PricingResponse.ItemPriceService> getItemLevelPriceServiceMap(
      List<DetailLine> detailLines) {

    return detailLines.stream()
        .collect(Collectors.toMap(DetailLine::getProductID, this::getItemServiceDetail));
  }

  private PricingResponse.ItemPriceService getItemServiceDetail(DetailLine detailLine) {
    PricingResponse.ItemPriceService itemPriceService = new PricingResponse.ItemPriceService();
    if (detailLine != null) {
      itemPriceService.setAdjustedPrice(
          detailLine.getAdjustedPrice() != null
              ? Double.parseDouble(detailLine.getAdjustedPrice())
              : 0.0);
      itemPriceService.setAdjustedPriceExVat(
          detailLine.getAdjPriceExVAT() != null
              ? Double.parseDouble(detailLine.getAdjPriceExVAT())
              : 0.0);
      itemPriceService.setWebAdjustedPrice(
          detailLine.getWebAdjustedPrice() != null
              ? Double.parseDouble(detailLine.getWebAdjustedPrice())
              : 0.0);
      itemPriceService.setDisplayPrice(
          detailLine.getDisplayPrice() != null
              ? Double.parseDouble(detailLine.getDisplayPrice())
              : 0.0);
      itemPriceService.setVatAmount(
          detailLine.getVatAmount() != null ? Double.parseDouble(detailLine.getVatAmount()) : 0.0);

      if (detailLine.isSubstituted()) {
        itemPriceService.setSubstitutedItemPriceResponseMap(
            detailLine.getSubstitutedItems().stream()
                .collect(
                    Collectors.toMap(
                        SubstitutedItem::getWmItemNum, this::getSubstitutedItemServiceDetail)));
      }
    }
    return itemPriceService;
  }

  private PricingResponse.SubstitutedItemPriceResponse getSubstitutedItemServiceDetail(
      SubstitutedItem substitutedItem) {

    PricingResponse.SubstitutedItemPriceResponse substitutedItemPriceResponse =
        new PricingResponse.SubstitutedItemPriceResponse();
    substitutedItemPriceResponse.setAdjustedPrice(
        Double.valueOf(substitutedItem.getAdjustedPrice()));
    substitutedItemPriceResponse.setWebAdjustedPrice(
        Double.valueOf(substitutedItem.getWebAdjustedPrice()));
    substitutedItemPriceResponse.setAdjustedPriceExVat(
        Double.valueOf(substitutedItem.getAdjustedPriceExTax()));
    return substitutedItemPriceResponse;
  }

  @Override
  public boolean reverseSale(OrderCancelledDomainEventMessage orderCancelledDomainEventMessage) {
    if (!pysipypRequestValidator.isValidReverseSaleRequest(orderCancelledDomainEventMessage)) {
      return false;
    }
    OrderInformation pricingResponse =
        pysipypWebClient.reverseSale(orderCancelledDomainEventMessage);
    return PRICING_SUCCESSFUL_RESPONSE_CODE.equalsIgnoreCase(pricingResponse.getTransactionCode());
  }
}
