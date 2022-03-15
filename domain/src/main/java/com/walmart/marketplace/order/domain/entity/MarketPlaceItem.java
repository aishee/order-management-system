package com.walmart.marketplace.order.domain.entity;

import com.walmart.common.domain.BaseEntity;
import com.walmart.common.domain.type.SubstitutionOption;
import com.walmart.marketplace.order.aggregateroot.MarketPlaceOrder;
import com.walmart.marketplace.order.domain.valueobject.ItemIdentifier;
import com.walmart.marketplace.order.domain.valueobject.MarketPlaceItemPriceInfo;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "OMSCORE.MARKETPLACE_ITEM")
@NoArgsConstructor
@AllArgsConstructor
public class MarketPlaceItem extends BaseEntity {

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "ORDER_RECORD_ID")
  private MarketPlaceOrder marketPlaceOrder;

  @Column(name = "EXTERNAL_ITEM_ID")
  private String externalItemId;

  @Column(name = "ITEM_DESCRIPTION")
  private String itemDescription;

  @Embedded
  private ItemIdentifier itemIdentifier;

  @Column(name = "QUANTITY")
  private long quantity;

  @Column(name = "VENDOR_INSTANCE_ID")
  private String vendorInstanceId;

  @Column(name = "SUBSTITUTION_OPTION")
  @Enumerated(EnumType.STRING)
  private SubstitutionOption substitutionOption;

  @Embedded private MarketPlaceItemPriceInfo marketPlacePriceInfo;

  @OneToMany(fetch = FetchType.LAZY, mappedBy = "marketPlaceItem", cascade = CascadeType.ALL)
  private List<MarketPlaceBundledItem> bundledItemList;

  @OneToMany(fetch = FetchType.LAZY, mappedBy = "marketPlaceItem", cascade = CascadeType.ALL)
  private List<MarketPlaceSubstitutedItem> substitutedItemList;

  @Builder(toBuilder = true)
  public MarketPlaceItem(
      MarketPlaceOrder marketPlaceOrder,
      String id,
      String externalItemId,
      String itemDescription,
      String vendorInstanceId,
      ItemIdentifier itemIdentifier,
      long quantity,
      MarketPlaceItemPriceInfo marketPlacePriceInfo,
      List<MarketPlaceBundledItem> bundledItemList,
      SubstitutionOption substitutionOption) {
    super(id);
    this.marketPlaceOrder = Objects.requireNonNull(marketPlaceOrder);
    this.externalItemId = Objects.requireNonNull(externalItemId);
    this.itemDescription = itemDescription;
    this.vendorInstanceId = vendorInstanceId;
    this.itemIdentifier = Objects.requireNonNull(itemIdentifier);
    this.quantity = quantity;
    this.marketPlacePriceInfo = marketPlacePriceInfo;
    this.bundledItemList = bundledItemList;
    this.substitutionOption = substitutionOption;
  }

  /** @return */
  public String getItemId() {
    return Optional.ofNullable(this.getItemIdentifier())
        .map(ItemIdentifier::getItemId)
        .orElse(null);
  }

  public SubstitutionOption getSubstitutionOption() {
    return Optional.ofNullable(substitutionOption).orElse(SubstitutionOption.DO_NOT_SUBSTITUTE);
  }

  public void setBundledItemList(List<MarketPlaceBundledItem> bundledItemList) {
    this.bundledItemList = bundledItemList;
  }

  public void setSubstitutedItemList(List<MarketPlaceSubstitutedItem> substitutedItemList) {
    this.substitutedItemList = substitutedItemList;
  }

}
