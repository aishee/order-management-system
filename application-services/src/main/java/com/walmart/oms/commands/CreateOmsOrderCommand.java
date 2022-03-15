package com.walmart.oms.commands;

import com.walmart.common.domain.type.SubstitutionOption;
import com.walmart.common.domain.type.Tenant;
import com.walmart.common.domain.type.Vertical;
import com.walmart.marketplace.order.domain.entity.type.Vendor;
import com.walmart.oms.commands.extensions.OrderInfo;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.util.StringUtils;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CreateOmsOrderCommand {

  private OmsOrderData data;

  public List<CreateOmsOrderCommand.AddressInfo> getAddressInfoList() {
    return Optional.ofNullable(this.data)
        .map(OmsOrderData::getAddressInfos)
        .orElse(Collections.emptyList());
  }

  public List<OrderItemInfo> getOrderItemInfoList() {
    return Optional.ofNullable(this.data)
        .map(OmsOrderData::getItems)
        .orElse(Collections.emptyList());
  }

  public MarketPlaceInfo getMarketPlaceInfo() {
    return Optional.ofNullable(this.data).map(OmsOrderData::getMarketPlaceInfo).orElse(null);
  }

  public String getVendorOrderId() {
    return Optional.ofNullable(this.data).flatMap(OmsOrderData::getVendorOrderId).orElse(null);
  }

  public Tenant getTenant() {
    return Optional.ofNullable(this.data).flatMap(OmsOrderData::getTenant).orElse(null);
  }

  public Vertical getVertical() {
    return Optional.ofNullable(this.data).flatMap(OmsOrderData::getVertical).orElse(null);
  }

  public ContactInfo getContactInfo() {
    return Optional.ofNullable(this.data).map(OmsOrderData::getContactinfo).orElse(null);
  }

  public SchedulingInfo getSchedulingInfo() {
    return Optional.ofNullable(this.data).map(OmsOrderData::getSchedulingInfo).orElse(null);
  }

  public PriceInfo getPriceInfo() {
    return Optional.ofNullable(this.data).map(OmsOrderData::getPriceInfo).orElse(null);
  }

  public OrderInfo getOrderInfo() {
    return Optional.ofNullable(this.data).map(OmsOrderData::getOrderInfo).orElse(null);
  }

  @Getter
  @Setter
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class OmsOrderData {

    private OrderInfo orderInfo;

    private MarketPlaceInfo marketPlaceInfo;

    private SchedulingInfo schedulingInfo;

    private ContactInfo contactinfo;

    private List<AddressInfo> addressInfos;

    private PriceInfo priceInfo;

    private List<OrderItemInfo> items;

    public Optional<String> getVendorOrderId() {
      return Optional.ofNullable(this.getMarketPlaceInfo()).map(MarketPlaceInfo::getVendorOrderId);
    }

    public Optional<Tenant> getTenant() {
      return Optional.ofNullable(this.getOrderInfo()).map(OrderInfo::getTenant);
    }

    public Optional<Vertical> getVertical() {
      return Optional.ofNullable(this.getOrderInfo()).map(OrderInfo::getVertical);
    }

    public String getStoreOrderId() {
      return StringUtils.isEmpty(orderInfo.getStoreOrderId())
          ? String.valueOf(
              com.walmart.oms.order.domain.entity.type.Vendor.SequenceGenerator.INSTANCE.nextId())
          : orderInfo.getStoreOrderId();
    }
  }

  @Getter
  @Setter
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class MarketPlaceInfo {

    private String vendorOrderId;

    private Vendor vendor;
  }

  @Getter
  @Setter
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class SchedulingInfo {

    private int doorStepTime;

    private String loadNumber;

    private String scheduleNumber;

    private Date plannedDueTime;

    private String vanId;

    private String tripId;
  }

  @Getter
  @Setter
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class ContactInfo {

    private String email;

    private String firstName;

    private String middleName;

    private String mobileNumber;

    private String lastName;

    private String phoneNumberOne;

    private String phoneNumberTwo;

    private String title;
  }

  @Getter
  @Setter
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class AddressInfo {

    private String addressType;

    private String addressOne;

    private String addressTwo;

    private String addressThree;

    private String city;

    private String county;

    private String country;

    private String latitude;

    private String longitude;

    private String postalCode;

    private String state;
  }

  @Getter
  @Setter
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class PriceInfo {

    private BigDecimal orderSubTotal;

    private BigDecimal deliveryCharge;

    private BigDecimal carrierBagCharge;

    private BigDecimal orderTotal;

    public double getOrderTotalValue() {
      return NumberUtils.toDouble(this.orderTotal);
    }

    public double getCarrierBagChargeValue() {
      return NumberUtils.toDouble(this.carrierBagCharge);
    }
  }

  @Getter
  @Setter
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class OrderItemInfo {

    private String cin;

    private BigDecimal unitPrice;

    private BigDecimal vendorUnitPrice;

    private long quantity;

    private BigDecimal weight;

    private BigDecimal vendorTotalPrice;

    private String itemDescription;

    private String skuId;

    private String imageUrl;

    private String salesUnit;

    private String uom;

    private List<BundleItem> bundledItemList;

    private SubstitutionOption substitutionOption;

  }

  @Getter
  @Setter
  @NoArgsConstructor
  public static class BundleItem {
    private String bundleInstanceId;
    private long bundleQuantity;
    private long itemQuantity;
    private String bundleSkuId;
    private String bundleDescription;
  }
}
