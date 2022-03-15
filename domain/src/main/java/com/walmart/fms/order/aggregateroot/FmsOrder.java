package com.walmart.fms.order.aggregateroot;

import com.walmart.common.domain.BaseEntity;
import com.walmart.common.domain.type.CancellationSource;
import com.walmart.common.domain.type.Tenant;
import com.walmart.common.domain.type.Vertical;
import com.walmart.fms.domain.error.exception.FMSBadRequestException;
import com.walmart.fms.order.domain.entity.FmsAddressInfo;
import com.walmart.fms.order.domain.entity.FmsCustomerContactInfo;
import com.walmart.fms.order.domain.entity.FmsOrderItem;
import com.walmart.fms.order.domain.entity.FmsOrderTimestamps;
import com.walmart.fms.order.domain.entity.FmsPickedItem;
import com.walmart.fms.order.domain.entity.FmsSchedulingInfo;
import com.walmart.fms.order.valueobject.CancelDetails;
import com.walmart.fms.order.valueobject.MarketPlaceInfo;
import com.walmart.fms.order.valueobject.OrderPriceInfo;
import com.walmart.marketplace.order.domain.entity.type.Vendor;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;

@Entity
@Slf4j
@Table(name = "OMSCORE.FULFILLMENT_ORDER")
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class FmsOrder extends BaseEntity {

  private static final String ORDER_CANCEL_ERROR = "Order is already cancelled";
  @Transient private boolean transientState;

  @Column(name = "TENANT_ID")
  @Enumerated(EnumType.STRING)
  private Tenant tenant;

  @Column(name = "VERTICAL_ID")
  @Enumerated(EnumType.STRING)
  private Vertical vertical;

  @Column(name = "SOURCE_ORDER_ID")
  private String sourceOrderId;

  @Column(name = "STORE_ORDER_ID")
  private String storeOrderId;

  @Column(name = "STORE_ID")
  private String storeId;

  @Column(name = "PICKUP_LOCATION_ID")
  private String pickupLocationId;

  @Column(name = "DELIVERY_DATE")
  private Date deliveryDate;

  @Column(name = "FULFILLMENT_ORDER_STATUS")
  private String fulfillmentOrderStatus;

  @Column(name = "ORDER_STATUS")
  private String orderState;

  @Embedded private OrderPriceInfo priceInfo;
  @Embedded private MarketPlaceInfo marketPlaceInfo;

  @Column(name = "DELIVERY_INSTRUCTION")
  private String deliveryInstruction;

  @Embedded private CancelDetails cancelDetails;

  @OneToMany(
      fetch = FetchType.LAZY,
      mappedBy = "fmsOrder",
      cascade = CascadeType.ALL,
      orphanRemoval = true)
  private List<FmsOrderItem> fmsOrderItems;

  @Column(name = "AUTH_STATUS")
  private String authStatus;

  @OneToOne(fetch = FetchType.LAZY, mappedBy = "order", cascade = CascadeType.ALL)
  private FmsCustomerContactInfo contactInfo;

  @OneToOne(fetch = FetchType.LAZY, mappedBy = "order", cascade = CascadeType.ALL)
  private FmsSchedulingInfo schedulingInfo;

  @OneToOne(fetch = FetchType.LAZY, mappedBy = "order", cascade = CascadeType.ALL)
  private FmsOrderTimestamps orderTimestamps;

  @OneToOne(fetch = FetchType.LAZY, mappedBy = "order", cascade = CascadeType.ALL)
  private FmsAddressInfo addressInfo;

  @Builder
  public FmsOrder(
      String id,
      Tenant tenant,
      Vertical vertical,
      String sourceOrderId,
      String storeOrderId,
      String storeId,
      String pickupLocationId,
      Date deliveryDate,
      String fulfillmentOrderStatus,
      String orderState,
      String deliveryInstruction,
      OrderPriceInfo priceInfo,
      CancelDetails cancelDetails,
      MarketPlaceInfo marketPlaceInfo) {
    super(id);

    this.assertArgumentNotEmpty(storeId, "StoreId cannot be empty");

    this.assertArgumentNotNull(deliveryDate, "Delivery date is required");

    this.assertArgumentNotNull(sourceOrderId, "Source order id cannot be null");

    this.vertical = vertical;
    this.tenant = tenant;
    this.sourceOrderId = sourceOrderId;
    this.storeOrderId = storeOrderId;
    this.storeId = storeId;
    this.pickupLocationId = pickupLocationId;
    this.deliveryDate = deliveryDate;
    this.fulfillmentOrderStatus = fulfillmentOrderStatus;
    this.orderState = orderState;
    this.priceInfo = priceInfo;
    this.deliveryInstruction = deliveryInstruction;
    this.cancelDetails = cancelDetails;
    this.marketPlaceInfo = marketPlaceInfo;

    this.fmsOrderItems = new ArrayList<>();
  }

  public FmsOrder(String orderState) {
    this.orderState = orderState;

    if (orderState.equalsIgnoreCase("INITIAL")) {
      this.transientState = true;
    }
  }

  public FmsOrder(String orderState, Tenant tenant, Vertical vertical) {
    this.orderState = orderState;
    this.tenant = tenant;
    this.vertical = vertical;

    if (orderState.equalsIgnoreCase("INITIAL")) {
      this.transientState = true;
    }
  }

  public boolean isOrderOpen() {
    return transientState;
  }

  public List<FmsOrderItem> getFmsOrderItems() {
    return CollectionUtils.isEmpty(fmsOrderItems)
        ? Collections.emptyList()
        : Collections.unmodifiableList(fmsOrderItems);
  }

  public String getVendorId() {
    return getMarketPlaceInfo().getVendorId();
  }

  public Vendor getVendor() {
    return getMarketPlaceInfo().getVendor();
  }

  public void addItem(FmsOrderItem fmsOrderItem) {
    this.fmsOrderItems.add(fmsOrderItem);
  }

  public void markAsReadyForStore() {
    this.orderState = OrderStatus.READY_FOR_STORE.getName();
  }

  public boolean isValid() {
    return sourceOrderId != null && !CollectionUtils.isEmpty(fmsOrderItems);
  }

  public boolean isMarketPlaceOrder() {
    return Vertical.MARKETPLACE.equals(this.vertical);
  }

  public void addContactInfo(FmsCustomerContactInfo contactInfo) {
    this.contactInfo = contactInfo;
  }

  public void addSchedulingInfo(FmsSchedulingInfo schedulingInfo) {
    this.schedulingInfo = schedulingInfo;
  }

  public void addOrderTimestamps(FmsOrderTimestamps orderTimestamps) {
    this.orderTimestamps = orderTimestamps;
  }

  public void addAddressInfo(FmsAddressInfo addressInfo) {
    this.addressInfo = addressInfo;
  }

  public void addMarketPlaceInfo(MarketPlaceInfo marketPlaceInfo) {
    this.marketPlaceInfo = marketPlaceInfo;
  }

  public void addPriceInfo(OrderPriceInfo priceInfo) {
    this.priceInfo = priceInfo;
  }

  public void updatePickedItems(Map<String, FmsPickedItem> pickedItemMapFromStore) {
    if (pickedItemMapFromStore != null) {
      this.getFmsOrderItems()
          .forEach(
              fmsOrderItem ->
                  addPickedItemToOrderedItem(
                      pickedItemMapFromStore.get(fmsOrderItem.getConsumerItemNumber()),
                      fmsOrderItem));
    } else {
      throw new FMSBadRequestException("Picked Iem List is Empty");
    }
  }

  private void addPickedItemToOrderedItem(FmsPickedItem pickedItem, FmsOrderItem fmsOrderitem) {
    fmsOrderitem.enrichPickedInfoWithPickedItem(pickedItem);
    pickedItem.updateOrderItem(fmsOrderitem);
  }

  public void markOrderAsReceivedAtStore() {
    this.orderState = OrderStatus.RECEIVED_AT_STORE.getName();
  }

  public void markOrderAsDelivered() {
    this.orderState = OrderStatus.DELIVERED.getName();
    this.updateDeliveredTime(new Date());
  }

  public void markOrderAsPickComplete() {
    this.orderState = OrderStatus.PICK_COMPLETE.getName();
    this.updatePickCompleteTime(new Date());
  }

  public void cancelOrder(
      String cancelledReasonCode,
      CancellationSource cancelledBy,
      String cancelledReasonDescription) {

    if (this.orderState != null
        && !this.orderState.equalsIgnoreCase(OrderStatus.CANCELLED.getName())
        && !this.orderState.equalsIgnoreCase(OrderStatus.DELIVERED.getName())) {
      this.orderState = OrderStatus.CANCELLED.getName();
      addCancelDetails(cancelledReasonCode, cancelledBy, cancelledReasonDescription);
      updateCancelledTime(new Date());
    } else {
      throw new FMSBadRequestException("Order cannot be cancelled");
    }
  }

  public void markOrderAsPickStarted() {
    if (isNotCancelled()) {
      this.orderState = OrderStatus.PICKING_STARTED.getName();
      updatePickStartedTime(new Date());
    } else {
      throw new FMSBadRequestException(ORDER_CANCEL_ERROR);
    }
  }

  public boolean isValidOrderStatusSequence(String status) {
    if (status != null && FmsOrder.OrderStatus.getOrderSequenceByName(status) != null) {
      return this.orderState == null
          || OrderStatus.getOrderSequenceByName(this.orderState) == null
          || OrderStatus.getOrderSequenceByName(this.orderState)
              < OrderStatus.getOrderSequenceByName(status);
    } else {
      return false;
    }
  }

  private void updatePickCompleteTime(Date pickCompleteTime) {
    if (this.orderTimestamps == null) {
      createOrderTimestamps();
    }
    this.orderTimestamps.updatePickCompleteTime(pickCompleteTime);
  }

  private void updateDeliveredTime(Date deliveredTime) {
    if (this.orderTimestamps == null) {
      createOrderTimestamps();
    }
    this.orderTimestamps.updateDeliveredTime(deliveredTime);
  }

  private void updateCancelledTime(Date cancelledTime) {
    if (this.orderTimestamps == null) {
      createOrderTimestamps();
    }
    this.orderTimestamps.updateCancelledTime(cancelledTime);
  }

  private void updatePickStartedTime(Date pickingStartTime) {
    if (this.orderTimestamps == null) {
      createOrderTimestamps();
    }
    this.orderTimestamps.updatePickStartedTime(pickingStartTime);
  }

  private void createOrderTimestamps() {
    this.orderTimestamps =
        FmsOrderTimestamps.builder().id(UUID.randomUUID().toString()).order(this).build();
  }

  private boolean isNotCancelled() {
    return this.orderState != null
        && !this.orderState.equalsIgnoreCase(OrderStatus.CANCELLED.getName());
  }

  private void addCancelDetails(
      String cancelledReasonCode,
      CancellationSource cancelledBy,
      String cancelledReasonDescription) {
    this.cancelDetails =
        CancelDetails.builder()
            .cancelledBy(cancelledBy)
            .cancelledReasonCode(cancelledReasonCode)
            .cancelledReasonDescription(cancelledReasonDescription)
            .build();
  }

  public boolean canBeCancelled() {
    return !isOrderOpen() && isNotCancelled() && isNotDelivered();
  }

  private boolean isNotDelivered() {
    return this.orderState != null && !this.orderState.equals(OrderStatus.DELIVERED.getName());
  }

  public boolean hasCarrierBag() {
    return false;
  }

  public boolean isEntireOrderCancellationValid() {
    return this.getFmsOrderItems().stream().anyMatch(FmsOrderItem::isCancellationValid);
  }

  public String getVendorOrderId() {
    return marketPlaceInfo.getVendorOrderId();
  }

  public enum OrderStatus {
    READY_FOR_STORE("READY_FOR_STORE", 1),
    RECEIVED_AT_STORE("RECD_AT_STORE", 2),
    PICKING_STARTED("PICK_STARTED", 3),
    PICK_COMPLETE("PICK_COMPLETE", 4),
    LOADED_TO_VAN("LOADED_TO_VAN", 5),
    DELIVERED("DELIVERED", 6),
    CANCELLED("CANCELLED", 6);

    private final String name;
    private final int sequence;

    OrderStatus(String name, int sequence) {
      this.name = name;
      this.sequence = sequence;
    }

    public static Integer getOrderSequenceByName(String name) {
      for (FmsOrder.OrderStatus orderStatus : FmsOrder.OrderStatus.values()) {
        if (name.equalsIgnoreCase(orderStatus.getName())) {
          return orderStatus.getSequence();
        }
      }
      log.warn("Unrecognizable Order status name found !!! name:{}", name);

      return null;
    }

    public int getSequence() {
      return sequence;
    }

    public String getName() {
      return name;
    }
  }

  public enum FulfillmentApp {
    GIF_MAAS
  }
}
