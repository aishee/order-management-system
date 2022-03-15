package com.walmart.marketplace.converter;

import com.walmart.common.constants.CommonConstants;
import com.walmart.common.domain.type.SubstitutionOption;
import com.walmart.marketplace.commands.MarketPlaceCreateOrderCommand;
import com.walmart.marketplace.commands.WebHookEventCommand;
import com.walmart.marketplace.commands.extensions.ExternalMarketPlaceItem;
import com.walmart.marketplace.commands.extensions.MarketPlacePayment;
import com.walmart.marketplace.justeats.request.Item;
import com.walmart.marketplace.justeats.request.JustEatsWebHookRequest;
import com.walmart.marketplace.order.domain.entity.type.Vendor;
import com.walmart.oms.domain.error.exception.OMSBadRequestException;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class JustEatsRequestToCommandMapper {

  private static final String CIN = "CIN";

  /**
   * Conversion of Web hook request into Web hook event command.
   *
   * @param request JustEats request payload.
   * @return Command object.
   */
  public WebHookEventCommand createWebHookCommand(JustEatsWebHookRequest request) {

    return WebHookEventCommand.builder()
        .eventType(request.getType())
        .externalOrderId(request.getThirdPartyOrderReference())
        .resourceURL(StringUtils.EMPTY)
        .sourceEventId(request.getId())
        .requestTime(Instant.ofEpochSecond(Long.parseLong(request.getCreatedAt())))
        .vendor(Vendor.JUSTEAT)
        .build();
  }

  /**
   * Conversion of Web hook request to Marketplace order command.
   *
   * @param webHookRequest JustEats request payload.
   * @return Command Object.
   */
  public MarketPlaceCreateOrderCommand createMarketPlaceOrderCmd(
      JustEatsWebHookRequest webHookRequest) {
    // JustEats doesn't provide item quantity,rather it contains multiple item objects.
    // We are grouping the items based on CIN and creating 1 marketplace item for 1 CIN number.
    List<ExternalMarketPlaceItem> items =
        webHookRequest.getItems().stream()
            .collect(Collectors.groupingBy(Item::getPlu))
            .values()
            .stream()
            .map(this::buildItem)
            .collect(Collectors.toList());

    return MarketPlaceCreateOrderCommand.builder()
        .externalOrderId(webHookRequest.getThirdPartyOrderReference())
        .externalNativeOrderId(webHookRequest.getId())
        .estimatedArrivalTime(webHookRequest.getArrivalTime())
        .firstName(webHookRequest.getCustomerFirstName())
        .lastName(webHookRequest.getCustomerLastName())
        .storeId(webHookRequest.getPosLocationId())
        .vendorStoreId(webHookRequest.getPosLocationId())
        .sourceOrderCreationTime(webHookRequest.getOrderCreationTime())
        .vendor(Vendor.JUSTEAT)
        .payment(
            MarketPlacePayment.builder()
                .total(webHookRequest.getTotalPayment())
                .bagFee(webHookRequest.getBagFee())
                .totalFee(CommonConstants.ZERO_MONEY)
                .tax(webHookRequest.getTaxAmount())
                .totalFeeTax(CommonConstants.ZERO_MONEY)
                .subTotal(webHookRequest.getSubTotal())
                .build())
        .marketPlaceItems(items)
        .build();
  }

  private ExternalMarketPlaceItem buildItem(List<Item> itemList) {
    int itemQuantity = itemList.size();
    return itemList.stream()
        .findFirst()
        .map(
            item ->
                ExternalMarketPlaceItem.builder()
                    .externalItemId(item.getPlu())
                    .itemId(item.getPlu())
                    .itemDescription(item.getDescription())
                    .itemType(CIN)
                    .vendorInstanceId(item.getPlu())
                    .baseTotalPrice(getTotalItemPrice(itemQuantity, item))
                    .baseUnitPrice(item.getItemPrice().doubleValue())
                    .unitPrice(item.getItemPrice().doubleValue())
                    .totalPrice(getTotalItemPrice(itemQuantity, item))
                    .quantity(itemQuantity)
                    .substitutionOption(SubstitutionOption.DO_NOT_SUBSTITUTE)
                    .build())
        .orElseThrow(() -> new OMSBadRequestException("Item not found"));
  }

  private double getTotalItemPrice(int itemQuantity, Item item) {
    return item.getItemPrice()
        .multiply(BigDecimal.valueOf(itemQuantity))
        .setScale(CommonConstants.SCALE, CommonConstants.ROUNDING_MODE)
        .doubleValue();
  }
}
