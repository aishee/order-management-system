package com.walmart.oms.order.domain.entity;

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
@Table(name = "OMSCORE.OMS_SUBSTITUTED_ITEM_UPC")
@NoArgsConstructor
@ToString(exclude = "substitutedItem")
@EqualsAndHashCode(exclude = "substitutedItem")
public class SubstitutedItemUpc extends BaseEntity {
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "SUB_ITEM_RECORD_ID")
  private SubstitutedItem substitutedItem;

  @Column(name = "UPC")
  private String upc;

  @Column(name = "UOM")
  private String uom;

  @Builder
  public SubstitutedItemUpc(String id,
                            String upc,
                            String uom) {
    super(id);
    this.upc = Objects.requireNonNull(upc);
    this.uom = Objects.requireNonNull(uom);
  }

  public void updateSubstitutedItem(SubstitutedItem substitutedItem) {
    this.substitutedItem = substitutedItem;
  }

}
