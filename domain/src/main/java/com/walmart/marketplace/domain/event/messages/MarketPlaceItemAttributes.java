package com.walmart.marketplace.domain.event.messages;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.apache.commons.lang.StringUtils;

@Getter
@Builder
@AllArgsConstructor
public class MarketPlaceItemAttributes {

  private String itemId;
  private String vendorInstanceId;
  private List<MarketplaceBundledItemAttributes> marketplaceBundledItemAttributesList;
  private String externalItemId;
  private int pickedQuantity;
  private int orderedQuantity;

  public boolean isNilPicked() {
    return pickedQuantity == 0;
  }

  public boolean isPartialPicked() {
    return pickedQuantity != 0 && pickedQuantity < orderedQuantity;
  }

  public boolean isNilOrPartialPicked() {
    return this.isNilPicked() || this.isPartialPicked();
  }

  public boolean isValidItem() {
    return !StringUtils.isEmpty(this.getItemId()) && this.isNilOrPartialPicked();
  }

  public boolean isValidNilPick() {
    return !StringUtils.isEmpty(this.getVendorInstanceId())
        && (getPickedNonBundledQuantity() == 0);
  }

  public List<MarketplaceBundledItemAttributes> getMarketplaceBundledItemAttributesList() {
    return Optional.ofNullable(marketplaceBundledItemAttributesList)
        .orElse(Collections.emptyList());
  }

  /**
   * This method will return Bundled Item count for an Item
   */
  private int getBundledQuantity() {
    return getMarketplaceBundledItemAttributesList().stream()
        .mapToInt(MarketplaceBundledItemAttributes::getBundledQuantity)
        .sum();
  }

  /**
   * This method will check if the item is partially picked
   */
  public boolean isValidPartialPick() {
    return !StringUtils.isEmpty(this.getItemId())
        && !StringUtils.isEmpty(this.getVendorInstanceId())
        && isPartialPicked()
        && (getPickedNonBundledQuantity() > 0);
  }

  /**
   * This method will check if any non-bundled item is not fulfilled
   */
  public boolean isNonBundledItemNotFulfilled() {
    return (getOrderedNonBundledQuantity() > 0) && isNilOrPartialPicked();
  }

  /**
   * This method will return nonBundled Item count for an Item
   */
  public int getOrderedNonBundledQuantity() {
    return this.orderedQuantity - getBundledQuantity();
  }

  /**
   * This  method will return us picked quantity for non-bundled items.
   */
  public int getPickedNonBundledQuantity() {
    return Math.toIntExact(Math.max(this.pickedQuantity - getBundledQuantity(), 0));
  }
}