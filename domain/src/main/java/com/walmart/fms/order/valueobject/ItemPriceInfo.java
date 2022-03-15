package com.walmart.fms.order.valueobject;

import static java.util.Objects.isNull;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.walmart.common.domain.AssertionConcern;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Objects;
import javax.persistence.AttributeOverride;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@Getter
@NoArgsConstructor
public class ItemPriceInfo extends AssertionConcern implements Serializable {

  public ItemPriceInfo(Money unitPrice) {
    this.unitPrice = Objects.requireNonNull(unitPrice, "Unit price cannot be null");
  }

  @AttributeOverride(name = "amount", column = @Column(name = "UNIT_PRICE"))
  private Money unitPrice;

  @JsonIgnore
  public BigDecimal getUnitPriceAmount() {
    return !isNull(this.unitPrice) ? this.unitPrice.getAmount() : BigDecimal.ZERO;
  }
}
