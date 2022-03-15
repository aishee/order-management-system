package com.walmart.oms.order.aggregateroot;

import com.walmart.common.domain.BaseEntity;
import com.walmart.common.domain.type.CancellationSource;
import com.walmart.common.domain.type.Currency;
import com.walmart.common.domain.type.FulfillmentType;
import com.walmart.common.domain.type.Tenant;
import com.walmart.common.domain.type.Vertical;
import com.walmart.fms.order.aggregateroot.FmsOrder;
import com.walmart.marketplace.order.domain.entity.type.Vendor;
import com.walmart.oms.domain.error.exception.OMSBadRequestException;
import com.walmart.oms.order.domain.entity.AddressInfo;
import com.walmart.oms.order.domain.entity.CustomerContactInfo;
import com.walmart.oms.order.domain.entity.OmsOrderBundledItem;
import com.walmart.oms.order.domain.entity.OmsOrderItem;
import com.walmart.oms.order.domain.entity.PickedItem;
import com.walmart.oms.order.domain.entity.SchedulingInfo;
import com.walmart.oms.order.valueobject.CancelDetails;
import com.walmart.oms.order.valueobject.CatalogItem;
import com.walmart.oms.order.valueobject.MarketPlaceInfo;
import com.walmart.oms.order.valueobject.Money;
import com.walmart.oms.order.valueobject.OrderPriceInfo;
import com.walmart.oms.order.valueobject.PricingResponse;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
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
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

@Slf4j
@Entity
@Table(name = "OMSCORE.OMS_ORDER")
@Getter
@NoArgsConstructor
public class OmsOrder extends BaseEntity {

  private static final String ORDER_CANCEL_ERROR = "Order is already cancelled";

  @Column(name = "TENANT_ID")
  @Enumerated(EnumType.STRING)
  private Tenant tenant;

  @Column(name = "VERTICAL_ID")
  @Enumerated(EnumType.STRING)
  private Vertical vertical;

  @Column(name = "SOURCE_ORDER_ID")
  private String sourceOrderId;

  @Column(name = "STORE_ID")
  private String storeId;

  @Column(name = "SPOKE_STORE_ID")
  private String spokeStoreId;

  @Column(name = "PICKUP_LOCATION_ID")
  private String pickupLocationId;

  @Column(name = "FULFILLMENT_TYPE")
  @Enumerated(EnumType.STRING)
  private FulfillmentType fulfillmentType;

  @Column(name = "DELIVERY_DATE")
  private Date deliveryDate;

  @Column(name = "AUTH_STATUS")
  private String authStatus;

  @Column(name = "ORDER_STATUS")
  private String orderState;

  @Column(name = "STORE_ORDER_ID")
  private String storeOrderId;

  @OneToOne(fetch = FetchType.LAZY, mappedBy = "order", cascade = CascadeType.ALL)
  private SchedulingInfo schedulingInfo;

  @OneToOne(fetch = FetchType.LAZY, mappedBy = "order", cascade = CascadeType.ALL)
  private CustomerContactInfo contactInfo;

  @OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
  private List<AddressInfo> addresses;

  @OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
  private List<OmsOrderItem> orderItemList;

  @Embedded private OrderPriceInfo priceInfo;
  @Embedded private MarketPlaceInfo marketPlaceInfo;
  @Transient private boolean transientState;
  @Embedded private CancelDetails cancelDetails;

  public OmsOrder(String orderState) {
    this.orderState = orderState;

    if (orderState.equalsIgnoreCase("INITIAL")) {
      this.transientState = true;
    }
  }

  public OmsOrder(String orderState, Tenant tenant, Vertical vertical) {
    this.orderState = orderState;
    this.tenant = tenant;
    this.vertical = vertical;

    if (orderState.equalsIgnoreCase("INITIAL")) {
      this.transientState = true;
    }
  }

  @Builder
  public OmsOrder(
      String id,
      Tenant tenant,
      Vertical vertical,
      String sourceOrderId,
      String storeOrderId,
      String storeId,
      String spokeStoreId,
      String pickupLocationId,
      FulfillmentType fulfillmentType,
      Date deliveryDate,
      String authStatus,
      String orderState,
      OrderPriceInfo priceInfo,
      MarketPlaceInfo marketPlaceInfo,
      CancelDetails cancelDetails) {
    super(id);

    this.assertArgumentNotEmpty(storeId, "StoreId cannot be empty");

    this.assertArgumentNotNull(deliveryDate, "Delivery date is required");

    this.assertArgumentNotNull(sourceOrderId, "Source order id cannot be null");

    this.assertArgumentNotNull(storeOrderId, "Store order id cannot be null");

    this.tenant = tenant;
    this.vertical = vertical;
    this.sourceOrderId = sourceOrderId;
    this.storeOrderId = storeOrderId;
    this.storeId = storeId;
    this.spokeStoreId = spokeStoreId;
    this.pickupLocationId = pickupLocationId;
    this.deliveryDate = deliveryDate;
    this.authStatus = authStatus;
    this.orderState = orderState;
    this.priceInfo = priceInfo;
    this.marketPlaceInfo = marketPlaceInfo;

    if (this.isMarketPlaceOrder()) {
      this.fulfillmentType = FulfillmentType.INSTORE_PICKUP;
    } else {
      this.fulfillmentType = fulfillmentType;
    }

    this.priceInfo = new OrderPriceInfo();
    this.orderItemList = new ArrayList<>();
    this.addresses = new ArrayList<>();
    this.cancelDetails = cancelDetails;
  }

  public boolean isOrderOpen() {
    return transientState;
  }

  public void enrichItemsWithCatalogData(Map<String, CatalogItem> catalogItems) {

    if (isOrderStatusEligibleForCatalogItemData()) {
      orderItemList.forEach(
          omsOrderItem ->
              omsOrderItem.enrichItemWithCatalogItemData(catalogItems.get(omsOrderItem.getCin())));
    } else {
      throw new OMSBadRequestException(
          String.format(
              "Order is in a post pick complete state for Order id: %s", this.getStoreOrderId()));
    }
  }

  private boolean isOrderStatusEligibleForCatalogItemData() {
    return this.orderState != null
        && (this.orderState.equalsIgnoreCase(OrderStatus.CREATED.getName())
            || this.orderState.equalsIgnoreCase(OrderStatus.READY_FOR_STORE.getName()));
  }

  public void addContactInfo(
      CustomerContactInfo
          contactInfo) { // rename addCustomerInfo//rename contact info customer info.
    this.contactInfo = contactInfo;
  }

  public void addAddress(AddressInfo addressInfo) {
    this.addresses.add(addressInfo);
  }

  public void addItem(OmsOrderItem omsOrderItem) {
    this.orderItemList.add(omsOrderItem);
  }

  public void addSchedulingInfo(SchedulingInfo schedulingInfo) {
    this.schedulingInfo = schedulingInfo;
  }

  public void addPriceInfo(OrderPriceInfo priceInfo) {
    this.priceInfo = priceInfo;
  }

  public void addMarketPlaceInfo(MarketPlaceInfo marketPlaceInfo) {
    this.marketPlaceInfo = marketPlaceInfo;
  }

  public void created() {
    this.orderState = OmsOrder.OrderStatus.CREATED.getName();
  }

  public List<String> getItemIds() {

    if (!CollectionUtils.isEmpty(orderItemList)) {
      return this.orderItemList.stream().map(OmsOrderItem::getCin).collect(Collectors.toList());
    }
    return Collections.emptyList();
  }

  public boolean isValid() {

    return storeId != null
        && deliveryDate != null
        && sourceOrderId != null
        && !CollectionUtils.isEmpty(orderItemList);
  }

  public boolean isCancelValid() {
    return this.getOrderItemList().stream().anyMatch(OmsOrderItem::isCancellationValid);
  }

  public boolean isMarketPlaceOrder() {
    return Vertical.MARKETPLACE.equals(this.vertical);
  }

  public void markOrderAsPickComplete() {
    this.orderState = OmsOrder.OrderStatus.PICK_COMPLETE.getName();
  }

  public void markOrderAsReadyForStore() {

    if (isNotCancelled()) {
      this.orderState = OmsOrder.OrderStatus.READY_FOR_STORE.getName();
    }
  }

  public void pickStarted() {

    if (isNotCancelled()) {
      this.orderState = OrderStatus.PICKING_STARTED.getName();
    }
  }

  public Optional<CancellationSource> getCancellationSource() {
    return Optional.ofNullable(cancelDetails).map(CancelDetails::getCancelledBy);
  }

  public void enrichPickedItemsAfterPricing(OmsOrder omsOrder, PricingResponse pricingResponse) {

    if (null != pricingResponse.getPosOrderTotalPrice()) {
      OrderPriceInfo omsOrderPriceInfo = omsOrder.getPriceInfo();
      if (null != omsOrderPriceInfo) {
        omsOrderPriceInfo.setPosTotal(pricingResponse.getPosOrderTotalPrice());
      } else {
        omsOrderPriceInfo = new OrderPriceInfo();
        omsOrderPriceInfo.setPosTotal(pricingResponse.getPosOrderTotalPrice());
        this.priceInfo = omsOrderPriceInfo;
      }
    }
    this.orderItemList.stream()
        .filter(omsOrderItem -> pricingResponse.hasItemPricingDetails(omsOrderItem.getCin()))
        .forEach(
            omsOrderItem -> {
              PricingResponse.ItemPriceService itemPriceService =
                  pricingResponse.getItemPricingDetails(omsOrderItem.getCin());
              if (null != itemPriceService) {
                omsOrderItem.enrichPickedItemWithPostPricingData(
                    getMoneyFromPrice(itemPriceService.getAdjustedPriceExVat()),
                    getMoneyFromPrice(itemPriceService.getAdjustedPrice()),
                    getMoneyFromPrice(itemPriceService.getWebAdjustedPrice()),
                    getMoneyFromPrice(itemPriceService.getDisplayPrice()),
                    getMoneyFromPrice(itemPriceService.getVatAmount()));
                omsOrderItem.getSubstitutedItems().stream()
                    .filter(
                        substitutedItem ->
                            itemPriceService.hasSubstitutedItemPriceDetails(
                                substitutedItem.getWalmartItemNumber()))
                    .forEach(
                        substitutedItem -> {
                          PricingResponse.SubstitutedItemPriceResponse
                              substitutedItemPriceResponse =
                                  itemPriceService.getSubstitutedItemPriceDetails(
                                      substitutedItem.getWalmartItemNumber());
                          substitutedItem.addPostPricingPrices(
                              substitutedItemPriceResponse.getAdjustedPriceExVat(),
                              substitutedItemPriceResponse.getAdjustedPrice(),
                              substitutedItemPriceResponse.getWebAdjustedPrice(),
                              substitutedItemPriceResponse.getVatAmount());
                        });
              }
            });
  }

  private Money getMoneyFromPrice(double price) {
    return new Money(BigDecimal.valueOf(price), Currency.GBP);
  }

  public void updatePickedItemsFromStore(Map<String, PickedItem> pickedItemMapFromStore) {

    if (!this.orderState.equals(OmsOrder.OrderStatus.CANCELLED.getName())) {
      this.getOrderItemList()
          .forEach(
              omsOrderItem ->
                  addPickedItemToOrderedItem(
                      pickedItemMapFromStore.get(omsOrderItem.getCin()), omsOrderItem));
    } else {
      throw new OMSBadRequestException(ORDER_CANCEL_ERROR);
    }
  }

  private void addPickedItemToOrderedItem(PickedItem pickedItem, OmsOrderItem omsOrderitem) {
    omsOrderitem.enrichPickedInfoWithPickedItem(pickedItem);
    pickedItem.updateOrderItem(omsOrderitem);
  }

  public void completePricing() {

    if (isNotCancelled()) {
      this.orderState = OmsOrder.OrderStatus.EPOS_COMPLETE.getName();
    } else {
      throw new OMSBadRequestException(ORDER_CANCEL_ERROR);
    }
  }

  public void markOrderAsDelivered() {

    if (isNotCancelled()) {
      if (isMarketPlaceOrder()) {
        this.orderState = OmsOrder.OrderStatus.NO_PENDING_ACTION.getName();
      } else {
        this.orderState = OmsOrder.OrderStatus.DELIVERED.getName();
      }
    } else {
      throw new OMSBadRequestException(ORDER_CANCEL_ERROR);
    }
  }

  public void markOrderAsReceivedAtStore() {

    if (isNotCancelled()) {
      this.orderState = OmsOrder.OrderStatus.RECEIVED_AT_STORE.getName();
    } else {
      throw new OMSBadRequestException(ORDER_CANCEL_ERROR);
    }
  }

  private boolean isNotCancelled() {
    return this.orderState != null
        && !this.orderState.equals(OmsOrder.OrderStatus.CANCELLED.getName());
  }

  public void cancelOrder(CancelDetails cancellationDetails) {

    if (isCancellable()) {
      this.orderState = "CANCELLED";
      this.cancelDetails = cancellationDetails;
    } else {
      throw new OMSBadRequestException("Order cannot be cancelled");
    }
  }

  private boolean isCancellable() {
    return this.orderState != null
        && !this.orderState.equalsIgnoreCase(OmsOrder.OrderStatus.DELIVERED.getName())
        && !this.orderState.equals(OmsOrder.OrderStatus.NO_PENDING_ACTION.getName());
  }

  public boolean isOrderStatusUpdatable(String status) {
    boolean returnValue = true;
    if (isTransientState()) {
      returnValue = false;
    } else if (status != null && OmsOrder.OrderStatus.getOrderSequenceByName(status) != null) {
      if (isOrderSequenceInvalid(status)) {
        returnValue = false;
      }
    } else {
      returnValue = false;
    }
    return returnValue;
  }

  private boolean isOrderSequenceInvalid(String status) {
    return this.orderState != null
        && OmsOrder.OrderStatus.getOrderSequenceByName(this.orderState) != null
        && OmsOrder.OrderStatus.getOrderSequenceByName(this.orderState)
            >= OmsOrder.OrderStatus.getOrderSequenceByName(status);
  }

  public boolean isReverseSaleApplicable() {
    return this.orderState != null
        && OrderStatus.getOrderSequenceByName(this.orderState) != null
        && OrderStatus.getOrderSequenceByName(this.orderState)
            >= OrderStatus.EPOS_COMPLETE.getSequence();
  }

  /**
   * Retrieve the list of orderedCIN corresponding to nil picked items
   *
   * @return
   */
  public List<String> getNilPicks() {
    return Optional.ofNullable(this.getOrderItemList())
        .map(this::getNilPickList)
        .orElse(Collections.emptyList());
  }

  /**
   * @param list
   * @return
   */
  private List<String> getNilPickList(List<OmsOrderItem> list) {
    return list.stream()
        .filter(OmsOrderItem::isNilPicked)
        .map(OmsOrderItem::getOrderedCin)
        .filter(Objects::nonNull)
        .collect(Collectors.toList());
  }

  /**
   * Retrieves orderedCIN and picked quantity of partially picked items
   *
   * @return
   */
  public Map<String, Integer> getPartialPicks() {
    return Optional.ofNullable(this.getOrderItemList())
        .map(
            list ->
                list.stream()
                    .filter(OmsOrderItem::isPartialPicked)
                    .filter(item -> item.getOrderedCin() != null)
                    .collect(
                        Collectors.toMap(
                            OmsOrderItem::getOrderedCin, OmsOrderItem::getPickedItemQuantity)))
        .orElse(Collections.emptyMap());
  }

  /**
   * For Async Methods, Inner lazy loaded objects are not fetched and sometimes new thread is not
   * part of a transaction. We can call this method to eagerly fetch all the inner items.
   */
  public void initializeInnerEntitiesEagerly() {

    this.getOrderItemList().forEach(OmsOrderItem::initializeInnerEntitiesEagerly);
    log.debug("Scheduling Info: {}", this.getSchedulingInfo());
    log.debug("Contact Info: {}", this.getContactInfo());
    log.debug("Address: {}", this.getAddresses());
  }

  public boolean hasCarrierBag() {
    return this.getPriceInfo() != null && this.getPriceInfo().getCarrierBagCharge() > 0;
  }

  public String getVendorOrderId() {
    return this.getMarketPlaceInfo().getVendorOrderId();
  }

  public Vendor getVendor() {
    return this.getMarketPlaceInfo().getVendor();
  }

  public String getVendorId() {
    return this.getMarketPlaceInfo().getVendorId();
  }

  public String getVendorName() {
    return this.getMarketPlaceInfo().getVendorName();
  }

  public double getOrderTotal() {
    return this.getPriceInfo().getOrderTotal();
  }

  public double getDeliveryCharge() {
    return this.getPriceInfo().getDeliveryCharge();
  }

  public double getCarrierBagCharge() {
    return this.getPriceInfo().getCarrierBagCharge();
  }

  public double getPosTotal() {
    return this.getPriceInfo().getPosTotal();
  }

  public double getOrderSubTotal() {
    return this.getPriceInfo().getOrderSubTotal();
  }

  public List<OmsOrderItem> getOrderItemsContainingBundles() {
    return this.getOrderItemList().stream()
        .filter(OmsOrderItem::hasBundle)
        .collect(Collectors.toList());
  }

  public Map<String, List<OmsOrderBundledItem>> getBundleItemToOrderItemMap() {
    return this.getOrderItemsContainingBundles().stream()
        .flatMap(omsOrderItem -> omsOrderItem.getBundledItemList().stream())
        .collect(Collectors.groupingBy(OmsOrderBundledItem::getBundleInstanceId));
  }

  public boolean hasBundles() {
    return this.getOrderItemList().stream().anyMatch(OmsOrderItem::hasBundle);
  }

  public Optional<String> getCancelledBySourceName() {
    return Optional.ofNullable(cancelDetails).map(CancelDetails::getCancelledBySourceName);
  }

  public Optional<String> getCancelledReasonCode() {
    return Optional.ofNullable(cancelDetails).map(CancelDetails::getCancelledReasonCode);
  }

  public boolean isValidOrder() {
    return !StringUtils.isEmpty(getStoreOrderId()) && !StringUtils.isEmpty(getStoreId());
  }

  public enum OrderStatus {
    CREATED("CREATED", 1),
    READY_FOR_STORE("READY_FOR_STORE", 2),
    RECEIVED_AT_STORE("RECD_AT_STORE", 3),
    PICKING_STARTED("PICK_STARTED", 4),
    PICK_COMPLETE("PICK_COMPLETE", 5),
    EPOS_COMPLETE("EPOS_COMPLETE", 6),
    DELIVERED("DELIVERED", 7),
    CANCELLED("CANCELLED", 7),
    NO_PENDING_ACTION("NO_PENDING_ACTION", 7);

    private String name;

    private int sequence;

    OrderStatus(String name, int sequence) {
      this.name = name;
      this.sequence = sequence;
    }

    public static String getOrderStatusByCode(int code) {
      for (FmsOrder.OrderStatus orderStatus : FmsOrder.OrderStatus.values()) {
        if (code == orderStatus.getSequence()) {
          return orderStatus.getName();
        }
      }
      log.warn("Unrecognizable order Status sequence found !!! code:{}", code);
      return null;
    }

    public static Integer getOrderSequenceByName(String name) {
      for (OmsOrder.OrderStatus orderStatus : OmsOrder.OrderStatus.values()) {
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
}
