package com.walmart.fms.order.valueobject;

import java.io.Serializable;
import java.util.Objects;
import javax.persistence.AttributeOverride;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Embedded;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Embeddable
@NoArgsConstructor
public class PickedItemPriceInfo implements Serializable {

  @Embedded
  @AttributeOverride(name = "amount", column = @Column(name = "UNIT_PRICE"))
  private Money unitPrice;

  @Builder
  public PickedItemPriceInfo(Money unitPrice) {
    this.unitPrice = Objects.requireNonNull(unitPrice);
  }

}
