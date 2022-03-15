package com.walmart.marketplace.order.domain.valueobject;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.walmart.common.domain.type.Currency;
import com.walmart.common.domain.type.SubstitutionOption;
import com.walmart.marketplace.order.domain.entity.type.Vendor;
import com.walmart.oms.order.valueobject.events.CancellationDetailsValueObject;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class MarketPlaceOrderValueObject {

  private String orderState;
  private String vendorOrderId;
  private Vendor vendorId;
  private String storeId;
  private Date orderDueTime;
  private Date sourceModifiedDate;
  private ContactInfo contactInfo;
  private PaymentInfo marketPlaceOrderPaymentInfo;
  private List<Item> items;
  private String sourceOrderId;
  private CancellationDetailsValueObject cancellationDetails;
  private List<String> nilPicks;
  private Map<String, Integer> partialPicks;

  public static boolean isValid(MarketPlaceOrderValueObject valueObject) {
    return valueObject.storeId != null
        && valueObject.vendorId != null
        && valueObject.sourceOrderId != null
        && valueObject.vendorOrderId != null;
  }

  @Getter
  @Setter
  @NoArgsConstructor
  public static class Item {

    private String externalItemId;
    private String itemDescription;
    private long quantity;
    private ItemIdentifier itemIdentifier;
    private ItemPriceInfo itemPriceInfo;
    private List<BundleItem> bundledItemList;
    private PickedItem pickedItem;
    private SubstitutionOption substitutionOption;

    @JsonIgnore
    public String getPickedItemId() {
      return pickedItem.getItemId();
    }

    @JsonIgnore
    public long getPickedItemQuantity() {
      return pickedItem.getPickedQuantity();
    }

    @JsonIgnore
    public List<SubstitutedItem> getSubstitutedItems() {
      return Optional.ofNullable(pickedItem)
          .map(PickedItem::getSubstitutedItems)
          .orElse(Collections.emptyList());
    }

    public SubstitutionOption getSubstitutionOption() {
      return Optional.ofNullable(substitutionOption).orElse(SubstitutionOption.DO_NOT_SUBSTITUTE);
    }
  }

  @Getter
  @Setter
  @NoArgsConstructor
  public static class PickedItem {
    private String itemId;
    private long pickedQuantity;
    private List<SubstitutedItem> substitutedItems;
  }

  @Getter
  @Setter
  @NoArgsConstructor
  public static class SubstitutedItem {
    private Long quantity;
    private String description;
    private String externalItemId;
    private BigDecimal totalPrice;
    private BigDecimal unitPrice;
  }

  @Getter
  @Setter
  @NoArgsConstructor
  public static class ItemPriceInfo {
    private double unitPrice;
    private double baseTotalPrice;
    private double baseUnitPrice;
    private double totalPrice;
  }

  @Getter
  @Setter
  @NoArgsConstructor
  public static class ItemIdentifier {
    private String itemId;
    private String itemType;
  }

  @Getter
  @Setter
  @NoArgsConstructor
  public static class PaymentInfo {
    private Money total;
    private Money subTotal;
    private Money tax;
    private Money totalFee;
    private Money totalFeeTax;
    private Money bagFee;
  }

  @Getter
  @Setter
  @NoArgsConstructor
  public static class Money {
    private BigDecimal amount;
    private Currency currency;
  }

  @Getter
  @Setter
  @NoArgsConstructor
  public static class ContactInfo {
    private String firstName;
    private String lastName;
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
