package com.walmart.marketplace.infrastructure.gateway.uber;

import static java.util.Objects.isNull;

import com.walmart.common.constants.CommonConstants;
import com.walmart.common.domain.type.Currency;
import com.walmart.marketplace.infrastructure.gateway.uber.dto.response.UberOrder;
import com.walmart.marketplace.infrastructure.gateway.uber.dto.response.UberOrder.UberOrderItemCommand;
import com.walmart.marketplace.infrastructure.gateway.uber.report.dto.UberReportReq;
import com.walmart.marketplace.infrastructure.gateway.util.ServiceFinder;
import com.walmart.marketplace.order.aggregateroot.MarketPlaceOrder;
import com.walmart.marketplace.order.domain.entity.MarketPlaceBundledItem;
import com.walmart.marketplace.order.domain.entity.MarketPlaceItem;
import com.walmart.marketplace.order.domain.entity.UpdateItemInfo;
import com.walmart.marketplace.order.domain.entity.type.Vendor;
import com.walmart.marketplace.order.domain.uber.MarketPlaceReportRequest;
import com.walmart.marketplace.order.domain.uber.PatchCartInfo;
import com.walmart.marketplace.order.domain.valueobject.ItemIdentifier;
import com.walmart.marketplace.order.domain.valueobject.MarketPlaceItemPriceInfo;
import com.walmart.marketplace.order.domain.valueobject.MarketPlaceOrderContactInfo;
import com.walmart.marketplace.order.domain.valueobject.MarketPlaceOrderPaymentInfo;
import com.walmart.marketplace.order.domain.valueobject.Money;
import com.walmart.marketplace.order.repository.IMarketPlaceGateWay;
import com.walmart.marketplace.repository.MarketPlaceRepository;
import com.walmart.util.CompletableFutureUtil;
import io.strati.configuration.annotation.ManagedConfiguration;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.validation.constraints.NotEmpty;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/** Uber Order API Gateway */
@Slf4j
@Component
@ServiceFinder.UBEREATS
public class UberOrderGateway implements IMarketPlaceGateWay {

  private static final String CIN = "CIN";
  private static final String ACCEPTED = "accepted";
  private static final BigDecimal HUNDRED = new BigDecimal(100);
  private static final ThreadLocal<SimpleDateFormat> SIMPLE_DATE_FORMAT =
      ThreadLocal.withInitial(() -> new SimpleDateFormat("yyyy/MM/dd HH:mm:ss"));
  @Autowired private UberWebClient uberWebClient;
  @Autowired private MarketPlaceRepository marketPlaceRepository;
  @ManagedConfiguration private UberServiceConfiguration uberServiceConfiguration;

  /**
   * Get the Uber Order
   *
   * @param uberOrderId
   * @return
   */
  @Override
  public MarketPlaceOrder getOrder(@NotEmpty String uberOrderId, String resourceUrl) {

    MarketPlaceOrder marketPlaceOrder = null;
    UberOrder uberOrder = null;
    if (UberRequestValidator.isValidGetOrderRequest(uberOrderId)) {
      uberOrder = uberWebClient.getUberOrder(uberOrderId);
    }

    if (uberOrder != null && CollectionUtils.isNotEmpty(uberOrder.getItems())) {
      Map<String, UberOrder.Item> mergedItemMap =
          uberOrder.getItems().stream()
              .collect(
                  Collectors.toMap(
                      UberOrder.Item::getExternalData,
                      Function.identity(),
                      mergeFunctionForDupItems()));

      if (!mergedItemMap.isEmpty()) {
        uberOrder.setItems(new ArrayList<>(mergedItemMap.values()));
      }
    }

    if (uberOrder != null) {
      marketPlaceOrder = convertToMarketPlaceOrder(uberOrder);
    }

    return marketPlaceOrder;
  }

  private BinaryOperator<UberOrder.Item> mergeFunctionForDupItems() {
    return (item1, item2) -> {
      log.warn("Duplicate lineItem found in the payload, ignoring one ItemId :: {}", item1.getId());
      item1.setQuantity(item1.getQuantity() + item2.getQuantity());
      if (item1.getPrice() != null && item2.getPrice() != null) {
        item1.setTotalPriceAmount(item1.getTotalPriceAmount() + item2.getTotalPriceAmount());
      }
      return item1;
    };
  }

  private MarketPlaceOrder convertToMarketPlaceOrder(UberOrder uberOrder) {

    // calculate default delivery date if Uber GET ORDER API returns null.
    Date estimatedDeliveryDate = getEstimatedDeliveryDate(uberOrder);

    MarketPlaceOrder marketPlaceOrder = buildMarketplaceOrder(uberOrder, estimatedDeliveryDate);
    List<MarketPlaceItem> items =
        uberOrder.getUberOrderItemCommandList().stream()
            .map(
                uberOrderItemCommand ->
                    buildMarketPlaceItem(marketPlaceOrder, uberOrderItemCommand))
            .collect(Collectors.toList());

    marketPlaceOrder.addMarketPlaceItems(items);
    return marketPlaceOrder;
  }

  private MarketPlaceItem buildMarketPlaceItem(
      MarketPlaceOrder marketPlaceOrder, UberOrderItemCommand uberOrderItemCommand) {
    MarketPlaceItem marketPlaceItem =
        MarketPlaceItem.builder()
            .id(marketPlaceRepository.getNextIdentity())
            .itemIdentifier(getItemIdentifier(uberOrderItemCommand.getCin()))
            .externalItemId(uberOrderItemCommand.getCin())
            .vendorInstanceId(uberOrderItemCommand.getInstanceId())
            .marketPlaceOrder(marketPlaceOrder)
            .quantity(uberOrderItemCommand.getQty())
            .marketPlacePriceInfo(buildMarketplaceItemPriceInfo(uberOrderItemCommand))
            .substitutionOption(uberOrderItemCommand.getSubstitutionOption())
            .build();

    marketPlaceItem.setBundledItemList(getBundledItemList(uberOrderItemCommand, marketPlaceItem));
    return marketPlaceItem;
  }

  private MarketPlaceOrder buildMarketplaceOrder(UberOrder uberOrder, Date estimatedDeliveryDate) {
    return MarketPlaceOrder.builder()
        .id(marketPlaceRepository.getNextIdentity())
        .vendorOrderId(uberOrder.getId())
        .vendorNativeOrderId(uberOrder.getId())
        .vendorId(Vendor.UBEREATS)
        .orderDueTime(estimatedDeliveryDate)
        .storeId(uberOrder.getExternalReferenceId())
        .vendorStoreId(uberOrder.getStoreId())
        .marketPlaceOrderContactInfo(buildMarketplaceOrderContactInfo(uberOrder))
        .sourceModifiedDate(uberOrder.getPlacedAt())
        .paymentInfo(mapPayment(uberOrder))
        .build();
  }

  private List<MarketPlaceBundledItem> getBundledItemList(
      UberOrderItemCommand uberOrderItemCommand, MarketPlaceItem marketPlaceItem) {
    return uberOrderItemCommand.getBundledItems().stream()
        .map(item -> buildMarketPlaceBundleItem(item, marketPlaceItem))
        .collect(Collectors.toList());
  }

  private MarketPlaceBundledItem buildMarketPlaceBundleItem(
      UberOrder.Item item, MarketPlaceItem marketPlaceItem) {
    return MarketPlaceBundledItem.builder()
        .bundleInstanceId(item.getInstanceId())
        .bundleQuantity(item.getBundleQuantity())
        .itemQuantity(item.getQuantity())
        .marketPlaceItem(marketPlaceItem)
        .bundleSkuId(item.getBundleExternalData())
        .bundleDescription(item.getBundleDescription())
        .id(marketPlaceRepository.getNextIdentity())
        .build();
  }

  private MarketPlaceOrderContactInfo buildMarketplaceOrderContactInfo(UberOrder uberOrder) {
    return MarketPlaceOrderContactInfo.builder()
        .firstName(uberOrder.getFirstName())
        .lastName(uberOrder.getLastName())
        .build();
  }

  private ItemIdentifier getItemIdentifier(String cin) {
    return ItemIdentifier.builder().itemType(CIN).itemId(cin).build();
  }

  private MarketPlaceItemPriceInfo buildMarketplaceItemPriceInfo(
      UberOrderItemCommand uberOrderItemCommand) {
    return MarketPlaceItemPriceInfo.builder()
        .unitPrice(getMarketplaceAdjustedAmount(uberOrderItemCommand.getUnitPriceAmount()))
        .totalPrice(getMarketplaceAdjustedAmount(uberOrderItemCommand.getTotalPriceAmount()))
        .build();
  }

  private Date getEstimatedDeliveryDate(UberOrder uberOrder) {
    // if estimated ready for pickup comes as Null, adding configurable time to PlacedAt date and
    // using it as delivery date.
    return Optional.ofNullable(uberOrder.getEstimatedReadyForPickupAt())
        .orElseGet(
            () ->
                // if placed at date is also null, keeping the delivery date as null.
                Optional.ofNullable(uberOrder.getPlacedAt())
                    .map(placedAt -> calculateDeliveryDateBasedOnPlacedAtDate(uberOrder, placedAt))
                    .orElseGet(
                        () -> {
                          log.error(
                              "Both Estimated Ready for PickUp At and Placed At dates are null. Can not calculate Delivery date for Order={}",
                              uberOrder.getId());
                          return null;
                        }));
  }

  private Date calculateDeliveryDateBasedOnPlacedAtDate(UberOrder uberOrder, Date placedAt) {

    LocalDateTime calculatedDeliveryDateTime =
        placedAt
            .toInstant()
            .atZone(ZoneId.systemDefault())
            .toLocalDateTime()
            .plusMinutes(uberServiceConfiguration.getDefaultDeliveryTimeInMinutes());

    Date calculatedDeliveryDate =
        Date.from(calculatedDeliveryDateTime.atZone(ZoneId.systemDefault()).toInstant());

    log.info(
        "Estimated Ready For Pickup At field is Null from Uber. Calculated delivery date={} based on PlacedAt time for Order={}",
        SIMPLE_DATE_FORMAT.get().format(calculatedDeliveryDate),
        uberOrder.getId());
    return calculatedDeliveryDate;
  }

  private MarketPlaceOrderPaymentInfo mapPayment(UberOrder uberOrder) {

    if (isNull(uberOrder.getPayment()) || isNull(uberOrder.getCharges())) {
      return null;
    } else {
      return MarketPlaceOrderPaymentInfo.builder()
          .bagFee(getMarketplaceMoneyFromUberMoney(uberOrder.getBagFee()))
          .total(getMarketplaceMoneyFromUberMoney(uberOrder.getTotal()))
          .subTotal(getMarketplaceMoneyFromUberMoney(uberOrder.getSubTotal()))
          .totalFeeTax(getMarketplaceMoneyFromUberMoney(uberOrder.getTotalFeeTax()))
          .totalFee(getMarketplaceMoneyFromUberMoney(uberOrder.getTotalFee()))
          .tax(getMarketplaceMoneyFromUberMoney(uberOrder.getTax()))
          .build();
    }
  }

  /**
   * Deny an uber order
   *
   * @param uberOrderId
   * @param denialExplanation
   * @param invalidItems
   * @param outOfStockItems
   * @return
   */
  public boolean denyOrder(
      @NotEmpty String uberOrderId,
      @NotEmpty String denialExplanation,
      List<String> invalidItems,
      List<String> outOfStockItems) {

    if (UberRequestValidator.isValidDenyOrderRequest(uberOrderId, denialExplanation)) {
      return uberWebClient.denyUberOrder(
          uberOrderId, denialExplanation, invalidItems, outOfStockItems);
    }
    return false;
  }

  /**
   * Cancel an uber Order
   *
   * @param uberOrderId
   * @param reason
   * @param reasonDetails
   * @return
   */
  public boolean cancelUberOrder(
      @NotEmpty String uberOrderId, @NotEmpty String reason, String reasonDetails) {

    if (UberRequestValidator.isValidCancelOrderRequest(uberOrderId, reason)) {
      return uberWebClient.cancelUberOrder(uberOrderId, reason, reasonDetails);
    }
    return false;
  }

  /**
   * Invoke uber patch cart endpoint
   *
   * @param patchCartInfo
   * @return
   */
  public CompletableFuture<Boolean> uberPatchCart(PatchCartInfo patchCartInfo) {
    String message;
    if (UberRequestValidator.isValidPatchCartRequest(patchCartInfo.getVendorOrderId())) {
      message =
          String.format(
              "Patch Cart request will be invoked for vendorOrderId : %s with nilPickInstanceIds : %s and partialPickInstanceIds : %s",
              patchCartInfo.getVendorOrderId(),
              patchCartInfo.getNilPickInstanceIds(),
              patchCartInfo.getPartialPickInstanceIds());
      log.info(message);
      return uberWebClient.patchCart(patchCartInfo);
    }
    message =
        String.format(
            "Patch Cart request is invalid for vendorOrderId : %s with nilPickInstanceIds : %s and partialPickInstanceIds : %s",
            patchCartInfo.getVendorOrderId(),
            patchCartInfo.getNilPickInstanceIds(),
            patchCartInfo.getPartialPickInstanceIds());
    log.error(message);
    return CompletableFuture.completedFuture(Boolean.FALSE);
  }

  /**
   * Invoke uber update item endpoint
   *
   * @param updateItemInfo
   * @return
   */
  private CompletableFuture<List<Boolean>> uberUpdateItem(UpdateItemInfo updateItemInfo) {
    if (UberRequestValidator.isValidUpdateItemRequest(updateItemInfo.getVendorStoreId())) {
      return CompletableFutureUtil.sequence(
          updateItemInfo.getOutOfStockItemIds().stream()
              .map(externalItemId -> getUpdateItemCompletableFuture(updateItemInfo, externalItemId))
              .collect(Collectors.toList()));
    } else {
      log.error(
          "Update Item request is invalid for OrderId : {} storeId : {}",
          updateItemInfo.getVendorOrderId(),
          updateItemInfo.getVendorStoreId());
      return CompletableFuture.completedFuture(Collections.emptyList());
    }
  }

  private CompletableFuture<Boolean> getUpdateItemCompletableFuture(
      UpdateItemInfo updateItemInfo, String externalItemId) {
    return uberWebClient
        .updateItem(updateItemInfo, externalItemId)
        .whenComplete(
            (result, exception) -> {
              if (exception != null) {
                String errorMessage =
                    String.format(
                        "Exception occurred while marking item with externalItemId : %s as OUT_OF_STOCK, vendorOrderId : %s",
                        externalItemId, updateItemInfo.getVendorOrderId());
                log.error(errorMessage, exception);
              } else {
                log.info(
                    "Item with externalItemId : {} has been marked as OUT OF STOCK, vendorOrderId : {}",
                    externalItemId,
                    updateItemInfo.getVendorOrderId());
              }
            });
  }

  /**
   * Accept an uber order
   *
   * @param uberOrderId
   * @param reason
   * @return
   */
  public boolean acceptOrder(@NotEmpty String uberOrderId, String reason) {

    if (UberRequestValidator.isValidAcceptOrderRequest(uberOrderId, reason)) {
      return uberWebClient.acceptUberOrder(uberOrderId, reason);
    }
    return false;
  }

  @Override
  public boolean acceptOrder(MarketPlaceOrder marketPlaceOrder) {
    return acceptOrder(marketPlaceOrder.getVendorOrderId(), ACCEPTED);
  }

  @Override
  public boolean rejectOrder(MarketPlaceOrder marketPlaceOrder, String reason) {
    return denyOrder(marketPlaceOrder.getVendorOrderId(), reason, null, null);
  }

  @Override
  public boolean cancelOrder(String vendorOrderId, String reason) {
    return cancelUberOrder(vendorOrderId, reason, null);
  }

  @Override
  public CompletableFuture<Boolean> patchCart(PatchCartInfo patchCartInfo) {
    return uberPatchCart(patchCartInfo);
  }

  @Override
  public CompletableFuture<List<Boolean>> updateItem(UpdateItemInfo updateItemInfo) {
    return uberUpdateItem(updateItemInfo);
  }

  @Override
  public String invokeMarketPlaceReport(MarketPlaceReportRequest marketplaceReportRequest) {
    return uberWebClient.invokeUberReport(createUberReportReq(marketplaceReportRequest));
  }

  private Money getMarketplaceMoneyFromUberMoney(UberOrder.Money uberMoney) {

    if (uberMoney != null) {
      return new Money(
          BigDecimal.valueOf(uberMoney.getAmount())
              .divide(HUNDRED, CommonConstants.SCALE, CommonConstants.ROUNDING_MODE),
          Currency.GBP);
    }
    return null;
  }

  private double getMarketplaceAdjustedAmount(int amount) {
    return BigDecimal.valueOf(amount)
        .divide(HUNDRED, CommonConstants.SCALE, CommonConstants.ROUNDING_MODE)
        .doubleValue();
  }

  private List<String> getStoreIdList() {
    return uberWebClient.getUberStore().getStoreIds();
  }

  private UberReportReq createUberReportReq(MarketPlaceReportRequest uberReportRequest) {
    UberReportReq reportRequest =
        UberReportReq.builder()
            .endDate(uberReportRequest.getEndDate())
            .startDate(uberReportRequest.getStartDate())
            .storeUUIDs(getStoreIdList())
            .reportType(uberReportRequest.getReportType())
            .build();
    reportRequest.validateReportEndDate(uberServiceConfiguration.getDayToEnd());
    return reportRequest;
  }
}
