package com.walmart.marketplace.converter;

import com.walmart.common.domain.type.CancellationSource;
import com.walmart.common.domain.type.Currency;
import com.walmart.common.domain.valueobject.CancellationDetails;
import com.walmart.marketplace.commands.CancelMarketPlaceOrderCommand;
import com.walmart.marketplace.commands.CreateMarketPlaceOrderFromAdapterCommand;
import com.walmart.marketplace.commands.DownloadReportEventCommand;
import com.walmart.marketplace.commands.MarketPlaceCreateOrderCommand;
import com.walmart.marketplace.commands.MarketPlaceReportCommand;
import com.walmart.marketplace.commands.WebHookEventCommand;
import com.walmart.marketplace.commands.extensions.ExternalMarketPlaceItem;
import com.walmart.marketplace.commands.extensions.ExternalMarketPlaceItem.ExternalMarketPlaceBundledItem;
import com.walmart.marketplace.commands.extensions.MarketPlacePayment;
import com.walmart.marketplace.dto.request.CreateMarketPlaceOrderRequest;
import com.walmart.marketplace.dto.request.CreateMarketPlaceOrderRequest.MarketPlaceRequestBundledItemData;
import com.walmart.marketplace.dto.request.MarketPlaceReportRequest;
import com.walmart.marketplace.order.domain.entity.type.Vendor;
import com.walmart.marketplace.order.domain.uber.ReportType;
import com.walmart.marketplace.uber.dto.UberWebHookRequest;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

@Slf4j
@Component
public class RequestToCommandMapper {

  public WebHookEventCommand createWebHookCommand(UberWebHookRequest request) {

    return WebHookEventCommand.builder()
        .eventType(request.getEventType())
        .externalOrderId(request.getMeta() != null ? request.getMeta().getResourceId() : null)
        .resourceURL(request.getResourceHref())
        .sourceEventId(request.getEventId())
        .requestTime(Instant.ofEpochMilli(request.getEventTime()))
        .vendor(Vendor.UBEREATS)
        .build();
  }

  public DownloadReportEventCommand createReportWebHookCommand(UberWebHookRequest request) {

    return DownloadReportEventCommand.builder()
        .reportUrls(request.getDownloadUrlList())
        .reportType(ReportType.valueOf(request.getReportType()))
        .build();
  }

  public CreateMarketPlaceOrderFromAdapterCommand createMarketPlaceOrderCmd(
      String externalOrderId, String resourceUrl, Vendor vendor) {

    return CreateMarketPlaceOrderFromAdapterCommand.builder()
        .externalOrderId(externalOrderId)
        .resourceUrl(resourceUrl)
        .vendor(vendor)
        .build();
  }

  public MarketPlaceCreateOrderCommand createMarketPlaceOrderFromRequest(
      CreateMarketPlaceOrderRequest createMarketPlaceOrderRequest) {

    List<ExternalMarketPlaceItem> items =
        createMarketPlaceOrderRequest.getData().getMarketPlaceItems().stream()
            .map(this::buildItem)
            .collect(Collectors.toList());

    return MarketPlaceCreateOrderCommand.builder()
        .externalOrderId(createMarketPlaceOrderRequest.getData().getExternalOrderId())
        .externalNativeOrderId(createMarketPlaceOrderRequest.getData().getExternalNativeOrderId())
        .estimatedArrivalTime(createMarketPlaceOrderRequest.getData().getEstimatedArrivalTime())
        .firstName(createMarketPlaceOrderRequest.getData().getFirstName())
        .lastName(createMarketPlaceOrderRequest.getData().getStoreId())
        .storeId(createMarketPlaceOrderRequest.getData().getStoreId())
        .sourceOrderCreationTime(
            createMarketPlaceOrderRequest.getData().getSourceOrderCreationTime())
        .vendor(createMarketPlaceOrderRequest.getData().getVendor())
        .payment(
            MarketPlacePayment.builder()
                .total(createMarketPlaceOrderRequest.getTotal(Currency.GBP))
                .bagFee(createMarketPlaceOrderRequest.getBagFee(Currency.GBP))
                .totalFee(createMarketPlaceOrderRequest.getTotalFee(Currency.GBP))
                .tax(createMarketPlaceOrderRequest.getTax(Currency.GBP))
                .totalFeeTax(createMarketPlaceOrderRequest.getTotalFeeTax(Currency.GBP))
                .subTotal(createMarketPlaceOrderRequest.getSubTotal(Currency.GBP))
                .build())
        .marketPlaceItems(items)
        .build();
  }

  private ExternalMarketPlaceItem buildItem(
      CreateMarketPlaceOrderRequest.MarketPlaceRequestItemData marketPlaceRequestItemData) {
    return ExternalMarketPlaceItem.builder()
        .externalItemId(marketPlaceRequestItemData.getExternalItemId())
        .itemDescription(marketPlaceRequestItemData.getItemDescription())
        .itemId(marketPlaceRequestItemData.getItemId())
        .itemType(marketPlaceRequestItemData.getItemType())
        .vendorInstanceId(marketPlaceRequestItemData.getVendorInstanceId())
        .baseTotalPrice(marketPlaceRequestItemData.getBaseTotalPrice())
        .baseUnitPrice(marketPlaceRequestItemData.getBaseUnitPrice())
        .unitPrice(marketPlaceRequestItemData.getUnitPrice())
        .quantity(marketPlaceRequestItemData.getQuantity())
        .substitutionOption(marketPlaceRequestItemData.getSubstitutionOption())
        .bundledItems(buildBundledItems(marketPlaceRequestItemData.getMarketPlaceBundledItems()))
        .build();
  }

  private List<ExternalMarketPlaceBundledItem> buildBundledItems(
      List<MarketPlaceRequestBundledItemData> marketPlaceBundledItems) {
    if (CollectionUtils.isEmpty(marketPlaceBundledItems)) {
      return Collections.emptyList();
    }
    return marketPlaceBundledItems.stream()
        .map(this::buildBundledItem)
        .collect(Collectors.toList());
  }

  private ExternalMarketPlaceBundledItem buildBundledItem(
      MarketPlaceRequestBundledItemData bundledItem) {
    return ExternalMarketPlaceBundledItem.builder()
        .bundleDescription(bundledItem.getBundleDescription())
        .bundleQuantity(bundledItem.getBundleQuantity())
        .bundleSkuId(bundledItem.getBundleSkuId())
        .itemQuantity(bundledItem.getItemQuantity())
        .bundleInstanceId(bundledItem.getBundleInstanceId())
        .build();
  }

  public CancelMarketPlaceOrderCommand createMarketPlaceCancelCommand(
      String id,
      String cancelledReasonCode,
      String cancelReasonDescription,
      String resourceUrl,
      Vendor vendor) {
    return CancelMarketPlaceOrderCommand.builder()
        .sourceOrderId(id)
        .cancellationDetails(
            buildMarketPlaceCancellationDetails(cancelledReasonCode, cancelReasonDescription))
        .vendor(vendor)
        .resourceUrl(resourceUrl)
        .build();
  }

  private CancellationDetails buildMarketPlaceCancellationDetails(
      String cancelledReasonCode, String cancelReasonDescription) {
    return CancellationDetails.builder()
        .cancelledBy(CancellationSource.VENDOR)
        .cancelledReasonCode(cancelledReasonCode)
        .cancelledReasonDescription(cancelReasonDescription)
        .build();
  }

  /**
   * @param marketPlaceReportRequest {@code The request payload basis on which report will be
   *     generated.}
   * @return {@link MarketPlaceReportCommand}
   */
  public MarketPlaceReportCommand buildMarketPlaceReportCommand(
      MarketPlaceReportRequest marketPlaceReportRequest) {
    marketPlaceReportRequest.validateInput();
    LocalDate currentDate = LocalDate.now();
    return MarketPlaceReportCommand.builder()
        .vendor(marketPlaceReportRequest.getVendor())
        .reportType(marketPlaceReportRequest.getReportType())
        .startDate(currentDate.minusDays(marketPlaceReportRequest.getDayToStart()))
        .endDate(currentDate.minusDays(marketPlaceReportRequest.getDayToEnd()))
        .build();
  }
}
