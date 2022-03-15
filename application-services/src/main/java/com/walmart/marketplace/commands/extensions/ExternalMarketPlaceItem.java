package com.walmart.marketplace.commands.extensions;

import com.walmart.common.domain.type.SubstitutionOption;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ExternalMarketPlaceItem {

  private String externalItemId;

  private String itemDescription;

  private String itemId;

  private String itemType;

  private String vendorInstanceId;

  private long quantity;

  private double unitPrice;

  private double baseUnitPrice;

  private double totalPrice;

  private double baseTotalPrice;

  private SubstitutionOption substitutionOption;

  private List<ExternalMarketPlaceBundledItem> bundledItems;

  @Getter
  @Builder
  public static class ExternalMarketPlaceBundledItem {

    private long bundleQuantity;

    private long itemQuantity;

    private String bundleSkuId;

    private String bundleInstanceId;

    private String bundleDescription;
  }
}
