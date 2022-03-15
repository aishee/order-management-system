package com.walmart.marketplace.order.aggregateroot;

import com.walmart.common.domain.BaseEntity;
import com.walmart.common.domain.type.SubstitutionOption;
import com.walmart.marketplace.order.domain.entity.MarketPlaceItem;
import com.walmart.marketplace.order.domain.entity.type.Vendor;
import com.walmart.marketplace.order.domain.valueobject.ItemIdentifier;
import com.walmart.marketplace.order.domain.valueobject.MarketPlaceItemPriceInfo;
import com.walmart.marketplace.order.domain.valueobject.MarketPlaceOrderContactInfo;
import com.walmart.marketplace.order.domain.valueobject.MarketPlaceOrderPaymentInfo;
import com.walmart.oms.domain.error.exception.OMSBadRequestException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "OMSCORE.MARKET_PLACE_ORDER")
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class MarketPlaceOrder extends BaseEntity {

  private static final String ACCEPTED = "ACCEPTED";
  private static final String REJECTED = "REJECTED";
  private static final String CANCELLED = "CANCELLED";
  private static final String CREATED = "CREATED";

  private static final String ORDER_CANCEL_ERROR = "Order is already cancelled";

  @Column(name = "ORDER_STATUS")
  private String orderState;

  @Column(name = "VENDOR_ORDER_ID")
  private String vendorOrderId;

  @Column(name = "VENDOR_NATIVE_ORDER_ID")
  private String vendorNativeOrderId;

  @Column(name = "VENDOR_ID")
  @Enumerated(EnumType.STRING)
  private Vendor vendorId;

  @Column(name = "STORE_ID")
  private String storeId;

  @Column(name = "VENDOR_STORE_ID")
  private String vendorStoreId;

  @Column(name = "ORDER_DUE_TIME")
  private Date orderDueTime;

  @Column(name = "SOURCE_MODIFIED_DATE")
  private Date sourceModifiedDate;

  @Embedded private MarketPlaceOrderContactInfo marketPlaceOrderContactInfo;
  @Embedded private MarketPlaceOrderPaymentInfo marketPlaceOrderPaymentInfo;

  @OneToMany(fetch = FetchType.LAZY, mappedBy = "marketPlaceOrder", cascade = CascadeType.ALL)
  private List<MarketPlaceItem> marketPlaceItems;

  @Builder
  public MarketPlaceOrder(
      String id,
      String orderState,
      String vendorOrderId,
      String vendorNativeOrderId,
      Vendor vendorId,
      String storeId,
      String vendorStoreId,
      Date orderDueTime,
      Date sourceModifiedDate,
      MarketPlaceOrderContactInfo marketPlaceOrderContactInfo,
      List<MarketPlaceItem> marketPlaceItems,
      MarketPlaceOrderPaymentInfo paymentInfo) {
    super(id);
    this.orderState = orderState;
    this.vendorOrderId = vendorOrderId;
    this.vendorNativeOrderId = vendorNativeOrderId;
    this.vendorId = vendorId;
    this.storeId = storeId;
    this.vendorStoreId = vendorStoreId;
    this.orderDueTime = orderDueTime;
    this.sourceModifiedDate = sourceModifiedDate;
    this.marketPlaceOrderContactInfo = marketPlaceOrderContactInfo;
    this.marketPlaceItems = marketPlaceItems;
    this.marketPlaceOrderPaymentInfo = paymentInfo;
  }

  public void accept() {
    this.orderState = ACCEPTED;
    updateLastModifiedTime();
  }

  public void reject() {
    this.orderState = REJECTED;
    updateLastModifiedTime();
  }

  public void create() {
    this.orderState = CREATED;
    updateLastModifiedTime();
  }

  public boolean isOrderAccepted() {
    return this.orderState.equals(ACCEPTED);
  }

  private void updateLastModifiedTime() {
    this.modifiedDate = Date.from(Instant.now());
  }

  public void addMarketPlaceItem(
      String id,
      String externalItemId,
      String itemDescription,
      long quantity,
      String vendorInstanceId,
      ItemIdentifier itemIdentifier,
      MarketPlaceItemPriceInfo marketPlaceItemPriceInfo,
      SubstitutionOption substitutionOption) {

    MarketPlaceItem item =
        MarketPlaceItem.builder()
            .externalItemId(externalItemId)
            .itemDescription(itemDescription)
            .itemIdentifier(itemIdentifier)
            .vendorInstanceId(vendorInstanceId)
            .marketPlacePriceInfo(marketPlaceItemPriceInfo)
            .quantity(quantity)
            .id(id)
            .marketPlaceOrder(this)
            .substitutionOption(substitutionOption)
            .build();

    if (marketPlaceItems == null) {
      marketPlaceItems = new ArrayList<>();
    }

    if (getMarketPlaceItemById(itemIdentifier.getItemId(), itemIdentifier.getItemType())
        .isPresent()) {
      throw new OMSBadRequestException(
          "Item already exists for id "
              + itemIdentifier.getItemId()
              + " in order :"
              + this.vendorOrderId);
    }

    marketPlaceItems.add(item);
  }

  private Optional<MarketPlaceItem> getMarketPlaceItemById(String itemId, String type) {

    return getMarketPlaceItems().stream()
        .filter(
            item ->
                Objects.equals(itemId, item.getItemIdentifier().getItemId())
                    && Objects.equals(type, item.getItemIdentifier().getItemType()))
        .findFirst();
  }

  public void cancel() {
    if (this.isCancellable()) {
      this.orderState = CANCELLED;
      updateLastModifiedTime();
    } else {
      throw new OMSBadRequestException("Order cannot be cancelled");
    }
  }

  private boolean isCancellable() {
    return (this.orderState != null && !this.orderState.equalsIgnoreCase("DELIVERED"));
  }

  public void addMarketPlaceItems(List<MarketPlaceItem> items) {
    this.setMarketPlaceItems(items);
  }

  public void markOrderAsDelivered() {

    if (isNotCancelled()) {
      this.orderState = "DELIVERED";
    } else {
      throw new OMSBadRequestException(ORDER_CANCEL_ERROR);
    }
  }

  public void pickCompleteOrder() {

    if (isNotCancelled()) {
      this.orderState = "PICK_COMPLETE";
    } else {
      throw new OMSBadRequestException(ORDER_CANCEL_ERROR);
    }
  }

  public void orderConfirmedAtStore() {

    if (isNotCancelled()) {
      this.orderState = "RECD_AT_STORE";
    } else {
      throw new OMSBadRequestException(ORDER_CANCEL_ERROR);
    }
  }

  private boolean isNotCancelled() {
    return this.orderState != null && !this.orderState.equals(CANCELLED);
  }

  public List<MarketPlaceItem> getMarketPlaceItems() {
    return Optional.ofNullable(this.marketPlaceItems).orElse(Collections.emptyList());
  }

  private void setMarketPlaceItems(List<MarketPlaceItem> items) {
    this.marketPlaceItems = items;
  }

  public boolean isTestOrder() {
    return Vendor.TESTVENDOR.equals(this.vendorId)
        // For automation test cases to by-pass Vendor API Calls.
        || this.getMarketPlaceOrderContactInfo().isTestVendor();
  }

  public List<String> getExternalItemIds() {
    return this.getMarketPlaceItems().stream()
        .map(MarketPlaceItem::getExternalItemId)
        .collect(Collectors.toList());
  }
}
