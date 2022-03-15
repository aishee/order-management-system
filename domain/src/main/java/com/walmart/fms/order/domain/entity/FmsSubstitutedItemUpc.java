package com.walmart.fms.order.domain.entity;

import com.walmart.common.domain.BaseEntity;
import java.util.Objects;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Getter
@Table(name = "OMSCORE.FULFILLMENT_ORDER_SUBSTITUTED_ITEM_UPC")
@NoArgsConstructor
@ToString(exclude = "substitutedItem")
@EqualsAndHashCode(exclude = "substitutedItem")
public class FmsSubstitutedItemUpc extends BaseEntity {

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "SUBSTITUTED_ITEM_RECORD_ID")
  private FmsSubstitutedItem substitutedItem;

  @Column(name = "UPC")
  private String upc;

  @Column(name = "UOM")
  private String uom;

  @Builder
  public FmsSubstitutedItemUpc(String id,
                               String upc,
                               String uom) {
    super(id);
    this.upc = Objects.requireNonNull(upc);
    this.uom = Objects.requireNonNull(uom);
  }

  public void updateSubstitutedItem(FmsSubstitutedItem substitutedItem) {
    this.substitutedItem = substitutedItem;
  }
}
