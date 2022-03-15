package com.walmart.fms.order.domain.entity;

import com.walmart.common.domain.BaseEntity;
import com.walmart.fms.order.valueobject.SubstitutedItemPriceInfo;
import java.util.List;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Getter
@ToString(exclude = "pickedItem")
@NoArgsConstructor
@EqualsAndHashCode(exclude = "pickedItem")
@Table(name = "OMSCORE.FULLFILLMENT_ORDER_SUBSTITUTED_ITEM")
public class FmsSubstitutedItem extends BaseEntity {
  @Column(name = "CONSUMER_ITEM_NUM")
  private String consumerItemNumber;

  @Column(name = "DEPARTMENT_ID")
  private String department;

  @Column(name = "ITEM_DESCRIPTION")
  private String description;

  @Column(name = "QUANTITY")
  private Long quantity;

  @Column(name = "WALMART_ITEM_NUM")
  private String walmartItemNumber;

  @Column(name = "WEIGHT")
  private Double weight;


  @OneToMany(
      fetch = FetchType.LAZY,
      cascade = CascadeType.ALL,
      mappedBy = "substitutedItem",
      orphanRemoval = true)
  private List<FmsSubstitutedItemUpc> upcs;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "PICKED_ITEM_RECORD_ID")
  private FmsPickedItem pickedItem;

  @Embedded
  private SubstitutedItemPriceInfo substitutedItemPriceInfo;

  @Builder
  public FmsSubstitutedItem(
      String id,
      String description,
      String department,
      String consumerItemNumber,
      String walmartItemNumber,
      Long quantity,
      Double weight,
      SubstitutedItemPriceInfo substitutedItemPriceInfo,
      List<FmsSubstitutedItemUpc> upcs) {
    super(id);
    this.assertArgumentNotEmpty(description, "Description cannot be empty or null");
    this.assertArgumentNotEmpty(department, "department cannot be null");
    this.assertArgumentNotNull(consumerItemNumber, "cin cannot be empty");
    this.assertArgumentNotNull(upcs, "upcs cannot be empty");

    this.description = description;
    this.department = department;
    this.walmartItemNumber = walmartItemNumber;
    this.consumerItemNumber = consumerItemNumber;
    this.quantity = quantity;
    this.weight = weight;
    this.substitutedItemPriceInfo = substitutedItemPriceInfo;
    this.upcs = upcs;
  }

  public void addPickedItem(FmsPickedItem fmsPickedItem) {
    this.pickedItem = fmsPickedItem;
  }
}