package com.walmart.marketplace.domain.event.messages;

import com.walmart.common.domain.event.processing.Message;
import com.walmart.marketplace.order.domain.entity.type.Vendor;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MarketPlacePickCompleteMessage implements Message {

  private String sourceOrderId;
  private String vendorOrderId;
  private Vendor vendorId;
  private String storeId;
  private String vendorStoreId;
  private List<MarketPlaceItemAttributes> marketPlaceItemAttributes;

  /**
   * This method checks the item picked status.
   *
   * @return {@link Boolean}
   */
  public boolean containsNilOrPartialPick() {
    return !this.getMarketPlaceItemAttributes().isEmpty()
        && this.getMarketPlaceItemAttributes().stream()
        .anyMatch(MarketPlaceItemAttributes::isNilOrPartialPicked);
  }

  /**
   * This method checks if order has any non-bundled item
   * which is not fulfilled.
   */
  public boolean hasAnyUnfulfilledNonBundledItem() {
    return !this.getMarketPlaceItemAttributes().isEmpty()
        && this.getMarketPlaceItemAttributes().stream()
        .anyMatch(MarketPlaceItemAttributes::isNonBundledItemNotFulfilled);
  }

  public boolean invokePatchCart() {
    return hasAnyUnfulfilledNonBundledItem();
  }
}