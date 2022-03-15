package com.walmart.marketplace;

import com.walmart.marketplace.commands.CancelMarketPlaceOrderCommand;
import com.walmart.marketplace.commands.CreateMarketPlaceOrderFromAdapterCommand;
import com.walmart.marketplace.commands.MarketPlaceCreateOrderCommand;
import com.walmart.marketplace.commands.MarketPlaceReportCommand;
import com.walmart.marketplace.commands.WebHookEventCommand;
import com.walmart.marketplace.commands.extensions.ExternalMarketPlaceItem;
import com.walmart.marketplace.commands.extensions.ExternalMarketPlaceItem.ExternalMarketPlaceBundledItem;
import com.walmart.marketplace.order.aggregateroot.MarketPlaceOrder;
import com.walmart.marketplace.order.domain.MarketPlaceDomainService;
import com.walmart.marketplace.order.domain.entity.MarketPlaceBundledItem;
import com.walmart.marketplace.order.domain.entity.MarketPlaceEvent;
import com.walmart.marketplace.order.domain.entity.MarketPlaceItem;
import com.walmart.marketplace.order.domain.uber.MarketPlaceReportRequest;
import com.walmart.marketplace.order.domain.valueobject.ItemIdentifier;
import com.walmart.marketplace.order.domain.valueobject.MarketPlaceItemPriceInfo;
import com.walmart.marketplace.order.domain.valueobject.MarketPlaceOrderPaymentInfo;
import com.walmart.marketplace.order.factory.MarketPlaceOrderFactory;
import com.walmart.marketplace.order.repository.IMarketPlaceEventRepository;
import com.walmart.marketplace.order.repository.IMarketPlaceRepository;
import com.walmart.oms.domain.error.exception.OMSBadRequestException;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

@Component
@Slf4j
public class MarketPlaceApplicationService {

  @Autowired private IMarketPlaceRepository marketPlaceRepository;

  @Autowired private MarketPlaceOrderFactory moFactory;

  @Autowired private MarketPlaceDomainService marketPlaceDomainService;

  @Autowired private IMarketPlaceEventRepository eventRepository;

  @Transactional
  public MarketPlaceEvent captureWebHookEvent(WebHookEventCommand webHookCommand) {

    MarketPlaceEvent event = eventRepository.get(webHookCommand.getSourceEventId());

    if (event != null
        && event.getEventType() != null
        && event.getEventType().equalsIgnoreCase(webHookCommand.getEventType())) {
      throw new OMSBadRequestException("Duplicate event .Event already exists");
    } else {
      String recordId = eventRepository.getNextIdentity();
      event = createMarketPlaceEvent(webHookCommand, recordId);
    }
    return eventRepository.save(event);
  }

  private MarketPlaceEvent createMarketPlaceEvent(
      WebHookEventCommand webHookCommand, String recordId) {
    return new MarketPlaceEvent(
        recordId,
        webHookCommand.getExternalOrderId(),
        webHookCommand.getResourceURL(),
        webHookCommand.getEventType(),
        webHookCommand.getSourceEventId(),
        webHookCommand.getVendor());
  }

  @Transactional
  public void createAndProcessMarketPlaceOrder(
      CreateMarketPlaceOrderFromAdapterCommand createCommand) {

    MarketPlaceOrder order = getMarketPlaceOrderFromGateway(createCommand);
    if (order != null) {
      marketPlaceDomainService.processMarketPlaceOrder(order);
    } else {
      log.error(
          "Could not create market place order from web hook event with external order id :{}",
          createCommand.getExternalOrderId());
    }
  }

  @Transactional
  public MarketPlaceOrder createAndProcessMarketPlaceOrder(
      MarketPlaceCreateOrderCommand createCommand) {
    MarketPlaceOrderPaymentInfo marketPlaceOrderPaymentInfo = getPaymentInfo(createCommand);
    MarketPlaceOrder order =
        getMarketPlaceOrderFromCommand(createCommand, marketPlaceOrderPaymentInfo);
    List<MarketPlaceItem> marketPlaceItemList =
        createCommand.getMarketPlaceItems().stream()
            .map(externalMarketPlaceItem -> addItem(order, externalMarketPlaceItem))
            .collect(Collectors.toList());
    order.addMarketPlaceItems(marketPlaceItemList);
    marketPlaceDomainService.processMarketPlaceOrder(order);
    return order;
  }

  private MarketPlaceOrder getMarketPlaceOrderFromGateway(
      CreateMarketPlaceOrderFromAdapterCommand createCommand) {
    return moFactory.getMarketPlaceOrderFromGateway(
        createCommand.getExternalOrderId(),
        createCommand.getResourceUrl(),
        createCommand.getVendor());
  }

  private MarketPlaceOrder getMarketPlaceOrderFromCommand(
      MarketPlaceCreateOrderCommand createCommand,
      MarketPlaceOrderPaymentInfo marketPlaceOrderPaymentInfo) {
    return moFactory.getMarketPlaceOrderFromCommand(
        createCommand.getExternalOrderId(),
        createCommand.getExternalNativeOrderId(),
        createCommand.getFirstName(),
        createCommand.getLastName(),
        createCommand.getStoreId(),
        createCommand.getVendorStoreId(),
        createCommand.getSourceOrderCreationTime(),
        createCommand.getVendor(),
        createCommand.getEstimatedArrivalTime(),
        marketPlaceOrderPaymentInfo);
  }

  private MarketPlaceOrderPaymentInfo getPaymentInfo(MarketPlaceCreateOrderCommand createCommand) {
    return moFactory.getPaymentInfo(
        createCommand.getTotal(),
        createCommand.getSubTotal(),
        createCommand.getTax(),
        createCommand.getTotalFee(),
        createCommand.getTotalFeeTax(),
        createCommand.getBagFee());
  }

  private MarketPlaceItem addItem(
      MarketPlaceOrder order, ExternalMarketPlaceItem externalMarketPlaceItem) {
    String nextId = marketPlaceRepository.getNextIdentity();

    MarketPlaceItem marketPlaceItem =
        MarketPlaceItem.builder()
            .marketPlaceOrder(order)
            .id(nextId)
            .externalItemId(externalMarketPlaceItem.getExternalItemId())
            .itemDescription(externalMarketPlaceItem.getItemDescription())
            .vendorInstanceId(externalMarketPlaceItem.getVendorInstanceId())
            .itemIdentifier(buildItemIdentifier(externalMarketPlaceItem))
            .quantity(externalMarketPlaceItem.getQuantity())
            .marketPlacePriceInfo(buildItemPriceInfo(externalMarketPlaceItem))
            .substitutionOption(externalMarketPlaceItem.getSubstitutionOption())
            .build();

    marketPlaceItem.setBundledItemList(buildBundledItems(externalMarketPlaceItem, marketPlaceItem));
    return marketPlaceItem;
  }

  private List<MarketPlaceBundledItem> buildBundledItems(
      ExternalMarketPlaceItem externalMarketPlaceItem, MarketPlaceItem marketPlaceItem) {
    if (CollectionUtils.isEmpty(externalMarketPlaceItem.getBundledItems())) {
      return Collections.emptyList();
    }
    return externalMarketPlaceItem.getBundledItems().stream()
        .map(bundledItem -> mapBundledItems(bundledItem, marketPlaceItem))
        .collect(Collectors.toList());
  }

  private MarketPlaceBundledItem mapBundledItems(
      ExternalMarketPlaceBundledItem bundledItem, MarketPlaceItem marketPlaceItem) {
    String nextId = marketPlaceRepository.getNextIdentity();
    return MarketPlaceBundledItem.builder()
        .id(nextId)
        .marketPlaceItem(marketPlaceItem)
        .bundleQuantity(bundledItem.getBundleQuantity())
        .itemQuantity(bundledItem.getItemQuantity())
        .bundleDescription(bundledItem.getBundleDescription())
        .bundleInstanceId(bundledItem.getBundleInstanceId())
        .bundleSkuId(bundledItem.getBundleSkuId())
        .build();
  }

  private MarketPlaceItemPriceInfo buildItemPriceInfo(
      ExternalMarketPlaceItem externalMarketPlaceItem) {
    return MarketPlaceItemPriceInfo.builder()
        .unitPrice(externalMarketPlaceItem.getUnitPrice())
        .baseUnitPrice(externalMarketPlaceItem.getBaseUnitPrice())
        .totalPrice(externalMarketPlaceItem.getTotalPrice())
        .baseTotalPrice(externalMarketPlaceItem.getBaseTotalPrice())
        .build();
  }

  private ItemIdentifier buildItemIdentifier(ExternalMarketPlaceItem externalMarketPlaceItem) {
    return ItemIdentifier.builder()
        .itemId(externalMarketPlaceItem.getItemId())
        .itemType(externalMarketPlaceItem.getItemType())
        .build();
  }

  @Transactional
  public MarketPlaceOrder cancelOrder(CancelMarketPlaceOrderCommand cancelCommand) {
    return moFactory
        .getOrder(cancelCommand.getSourceOrderId())
        .map(
            marketPlaceOrder ->
                marketPlaceDomainService.cancelOrder(
                    marketPlaceOrder, cancelCommand.getCancellationDetails()))
        .orElse(null);
  }

  @Transactional
  public MarketPlaceOrder getOrder(String vendorOrderId) {
    return marketPlaceRepository.get(vendorOrderId);
  }

  /**
   * @param reportCommand
   * @return
   */
  public String invokeMarketPlaceReport(MarketPlaceReportCommand reportCommand) {
    return marketPlaceDomainService.invokeMarketPlaceReport(
        getMarketPlaceReportRequest(reportCommand));
  }

  private MarketPlaceReportRequest getMarketPlaceReportRequest(
      MarketPlaceReportCommand reportCommand) {
    return MarketPlaceReportRequest.builder()
        .reportType(reportCommand.getReportType())
        .startDate(reportCommand.getStartDate())
        .endDate(reportCommand.getEndDate())
        .vendor(reportCommand.getVendor())
        .build();
  }
}
