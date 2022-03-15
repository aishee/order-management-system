package com.walmart.marketplace.infrastructure.gateway.uber.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.walmart.common.domain.type.SubstitutionOption;
import java.io.Serializable;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.Data;
import org.apache.commons.lang.StringUtils;
import org.springframework.util.CollectionUtils;

/**
 * This class represents Uber Order
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class UberOrder implements Serializable {

  Store store;
  Eater eater;
  Cart cart;
  Payment payment;
  Packaging packaging;
  private String id;
  private String displayId;
  private String externalReferenceId;
  private CurrentState currentState;
  private Date placedAt;
  private Date estimatedReadyForPickupAt;
  private Type type;

  public List<Item> getItems() {
    return this.getCart().getItems();
  }

  public void setItems(List<Item> items) {
    this.getCart().setItems(items);
  }

  /*
   * Returns a list of Items from all non-bundled and bundled items from cart.items
   * In case of bundled items, it extracts all items from selectedModifierGroups upto 1 level.
   * */
  public List<Item> getAllItems() {
    return this.getItems().stream()
        .flatMap(item -> item.getAllEmbeddedItems().stream())
        .collect(Collectors.toList());
  }

  public List<UberOrderItemCommand> getUberOrderItemCommandList() {
    return this.getAllItems().stream()
        .collect(Collectors.groupingBy(Item::getExternalData))
        .entrySet()
        .stream()
        .map(entry -> new UberOrderItemCommand(entry.getKey(), entry.getValue()))
        .collect(Collectors.toList());
  }

  public String getExternalReferenceId() {
    return this.getStore().getExternalReferenceId();
  }

  public String getFirstName() {
    return this.getEater().getFirstName();
  }

  public String getLastName() {
    return this.getEater().getFirstName();
  }

  public Money getBagFee() {
    return this.getPayment().getBagFee();
  }

  public Money getTotalFeeTax() {
    return this.getPayment().getTotalFeeTax();
  }

  public Money getTotalFee() {
    return this.getPayment().getTotalFee();
  }

  public Money getTotal() {
    return this.getPayment().getTotal();
  }

  public Money getSubTotal() {
    return this.getPayment().getSubTotal();
  }

  public Money getTax() {
    return this.getPayment().getTax();
  }

  public UberOrder.Charges getCharges() {
    return this.getPayment().getCharges();
  }

  public String getStoreId() {
    return this.getStore().getId();
  }

  public enum CurrentState {
    CREATED,
    ACCEPTED,
    DENIED,
    FINISHED,
    CANCELLED, // per doc but seems invalid
    CANCELED
  }

  public enum Type {
    PICK_UP,
    DINE_IN,
    DELIVERY_BY_UBER,
    DELIVERY_BY_RESTAURANT
  }

  public enum AllergenEnum {
    DAIRY,
    EGGS,
    FISH,
    SHELLFISH,
    TREENUTS,
    PEANUTS,
    GLUTEN,
    SOY,
    OTHER
  }

  public enum FulfillmentActionType {
    REPLACE_FOR_ME,
    SUBSTITUTE_ME,
    CANCEL,
    REMOVE_ITEM;

    public SubstitutionOption getSubstitutionOption() {

      if (this == REPLACE_FOR_ME) {
        return SubstitutionOption.SUBSTITUTE;
      } else if (this == CANCEL) {
        return SubstitutionOption.CANCEL_ENTIRE_ORDER;
      } else {
        return SubstitutionOption.DO_NOT_SUBSTITUTE;
      }
    }
  }

  @Data
  @JsonIgnoreProperties(ignoreUnknown = true)
  @JsonNaming()
  public static class Packaging implements Serializable {

    boolean shouldInclude;
  }

  @Data
  @JsonIgnoreProperties(ignoreUnknown = true)
  @JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
  public static class Payment implements Serializable {
    Charges charges;
    Accounting accounting;

    public Money getBagFee() {
      return this.getCharges().getBagFee();
    }

    public Money getTotal() {
      return this.getCharges().getTotal();
    }

    public Money getSubTotal() {
      return this.getCharges().getSubTotal();
    }

    public Money getTax() {
      return this.getCharges().getTax();
    }

    public Money getTotalFeeTax() {
      return this.getCharges().getTotalFeeTax();
    }

    public Money getTotalFee() {
      return this.getCharges().getTotalFee();
    }
  }

  @Data
  @JsonIgnoreProperties(ignoreUnknown = true)
  @JsonNaming()
  public static class Accounting implements Serializable {
    TaxRemittance taxRemittance;
  }

  @Data
  @JsonIgnoreProperties(ignoreUnknown = true)
  @JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
  public static class TaxRemittance implements Serializable {
    private RemittanceInfo tax;
    private RemittanceInfo totalFeeTax;
    private RemittanceInfo deliveryFeeTax;
    private RemittanceInfo smallOrderFeeTax;
  }

  @Data
  @JsonIgnoreProperties(ignoreUnknown = true)
  @JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
  public static class RemittanceInfo implements Serializable {

    private List<PayeeDetail> uber;
    private List<PayeeDetail> restaurant;
    private List<PayeeDetail> courier;
    private List<PayeeDetail> eater;
  }

  @Data
  @JsonIgnoreProperties(ignoreUnknown = true)
  @JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
  public static class PayeeDetail implements Serializable {
    Money value;
  }

  @Data
  @JsonIgnoreProperties(ignoreUnknown = true)
  @JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
  public static class Charges implements Serializable {

    private Money total;
    private Money subTotal;
    private Money tax;
    private Money totalFee;
    private Money totalFeeTax;
    private Money bagFee;
  }

  @Data
  @JsonIgnoreProperties(ignoreUnknown = true)
  @JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
  public static class Money implements Serializable {
    private int amount;
    private String currencyCode;
    private String formattedAmount;
  }

  @Data
  @JsonIgnoreProperties(ignoreUnknown = true)
  @JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
  public static class Cart implements Serializable {
    private List<Item> items;
    private String specialInstructions;
  }

  @Data
  @JsonIgnoreProperties(ignoreUnknown = true)
  @JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
  public static class Eater implements Serializable {
    private String firstName;
    private String lastName;
  }

  @Data
  @JsonIgnoreProperties(ignoreUnknown = true)
  @JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
  public static class Store implements Serializable {
    private String id;
    private String name;
    private String externalReferenceId;
  }

  @Data
  @JsonIgnoreProperties(ignoreUnknown = true)
  @JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
  public static class Item implements Serializable {
    ItemPrice price;
    private String id;
    private String title;
    private String externalData;
    private long quantity;
    private List<ModifierGroup> selectedModifierGroups;
    private List<SpecialRequests> specialRequests;
    private FulfillmentAction fulfillmentAction;
    private Integer defaultQuantity;
    private String specialInstructions;
    private String instanceId;
    private boolean isBundledItem;
    private String bundleDescription;
    private long bundleQuantity;
    private String bundleExternalData;

    public int getTotalPriceAmount() {
      return this.getPrice().getTotalPriceAmount();
    }

    public void setTotalPriceAmount(int amount) {
      this.getTotalPrice().setAmount(amount);
    }

    public int getUnitPriceAmount() {
      return this.getPrice().getUnitPriceAmount();
    }

    public Money getTotalPrice() {
      return this.getPrice().getTotalPrice();
    }

    public Money getUnitPrice() {
      return this.getPrice().getUnitPrice();
    }

    public long getTotalBundledQuantity() {
      return bundleQuantity * quantity;
    }

    public long getTotalItemQuantity() {
      return isBundledItem() ? getTotalBundledQuantity() : getQuantity();
    }

    private boolean containsBundledItems() {
      return !CollectionUtils.isEmpty(this.getSelectedModifierGroups());
    }

    /*
     * if an item is non-bundled item, it is returned as it is
     * else if it is bundledItem, returns a list of all bundledItems within this item with adjusted
     * price, instanceId and quantity
     * */
    private List<Item> getAllEmbeddedItems() {
      if (this.containsBundledItems()) {
        return this.getSelectedModifierGroups().stream()
            .flatMap(modifierGroup -> modifierGroup.getSelectedItems().stream())
            .peek(
                item -> {
                  item.setBundledItem(true);
                  item.setBundleExternalData(getExternalData());
                  item.setBundleDescription(getTitle());
                  item.setBundleQuantity(getQuantity());
                  item.setInstanceId(getInstanceId());
                  item.setTotalPriceAmount(
                      (int) (item.getTotalBundledQuantity() * item.getUnitPriceAmount()));
                })
            .collect(Collectors.toList());
      } else {
        return Collections.singletonList(this);
      }
    }

    public SubstitutionOption getSubstitutionOption() {
      return Optional.ofNullable(fulfillmentAction)
          .map(FulfillmentAction::getSubstitutionOption)
          .orElse(SubstitutionOption.DO_NOT_SUBSTITUTE);
    }
  }

  @Data
  @JsonIgnoreProperties(ignoreUnknown = true)
  @JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
  public static class SpecialRequests implements Serializable {
    Allergy allergy;
  }

  @Data
  @JsonIgnoreProperties(ignoreUnknown = true)
  @JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
  public static class FulfillmentAction implements Serializable {
    FulfillmentActionType fulfillmentActionType;

    public SubstitutionOption getSubstitutionOption() {
      return Optional.ofNullable(fulfillmentActionType)
          .map(FulfillmentActionType::getSubstitutionOption)
          .orElse(SubstitutionOption.DO_NOT_SUBSTITUTE);
    }
  }

  @Data
  @JsonIgnoreProperties(ignoreUnknown = true)
  @JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
  public static class Allergy implements Serializable {
    String allergyInstructions;
    private List<Allergen> allergensToExclude;
  }

  @Data
  @JsonIgnoreProperties(ignoreUnknown = true)
  @JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
  public static class Allergen implements Serializable {
    AllergenEnum type;
    String freeformText;
  }

  @Data
  @JsonIgnoreProperties(ignoreUnknown = true)
  @JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
  public static class ItemPrice implements Serializable {
    Money unitPrice;
    Money totalPrice;
    Money baseUnitPrice;
    Money baseTotalPrice;

    public int getTotalPriceAmount() {
      return this.getTotalPrice().getAmount();
    }

    public int getUnitPriceAmount() {
      return this.getUnitPrice().getAmount();
    }
  }

  @Data
  @JsonIgnoreProperties(ignoreUnknown = true)
  @JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
  public static class ModifierGroup implements Serializable {
    private String id;
    private String title;
    private String externalData;
    private List<Item> selectedItems;
    private List<Item> removedItems;
  }

  @Data
  @JsonIgnoreProperties(ignoreUnknown = true)
  @JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
  public static class BaseTotalPrice {
    private float amount;
    private String currencyCode;
    private String formattedAmount;
  }

  @Data
  @JsonIgnoreProperties(ignoreUnknown = true)
  @JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
  public static class BaseUnitPrice {
    private float amount;
    private String currencyCode;
    private String formattedAmount;
  }

  @Data
  @JsonIgnoreProperties(ignoreUnknown = true)
  @JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
  public static class TotalPrice {
    private float amount;
    private String currencyCode;
    private String formattedAmount;
  }

  @Data
  @JsonIgnoreProperties(ignoreUnknown = true)
  @JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
  public static class UnitPrice {
    private float amount;
    private String currencyCode;
    private String formattedAmount;
  }

  /*
   * A utility class to convert a list of items with same cin to MarketPlaceItem
   */
  @Data
  public static class UberOrderItemCommand {

    private String cin;
    private List<Item> itemList;

    public UberOrderItemCommand(String cin, List<Item> itemList) {
      this.cin = cin;
      this.itemList = itemList;
    }

    public long getQty() {
      return itemList.stream().map(Item::getTotalItemQuantity).reduce(Long::sum).orElse(0L);
    }

    public int getTotalPriceAmount() {
      return itemList.stream().mapToInt(Item::getTotalPriceAmount).sum();
    }

    public int getUnitPriceAmount() {
      return itemList.stream().mapToInt(Item::getUnitPriceAmount).findFirst().orElse(0);
    }

    public String getInstanceId() {
      return itemList.stream()
          .filter(item -> !item.isBundledItem())
          .map(Item::getInstanceId)
          .filter(Objects::nonNull)
          .findFirst()
          .orElse(StringUtils.EMPTY);
    }

    public List<Item> getBundledItems() {
      return itemList.stream().filter(Item::isBundledItem).collect(Collectors.toList());
    }

    public SubstitutionOption getSubstitutionOption() {
      return itemList.stream()
          .findFirst()
          .map(Item::getSubstitutionOption)
          .orElse(SubstitutionOption.DO_NOT_SUBSTITUTE);
    }
  }
}
