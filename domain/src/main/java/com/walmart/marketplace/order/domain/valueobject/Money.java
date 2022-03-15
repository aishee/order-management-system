package com.walmart.marketplace.order.domain.valueobject;

import com.walmart.common.domain.AssertionConcern;
import com.walmart.common.domain.type.Currency;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Objects;
import javax.persistence.Embeddable;
import javax.persistence.Transient;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@Getter
@NoArgsConstructor
public class Money extends AssertionConcern implements Serializable, Comparable<Money> {

  public Money(BigDecimal amount, Currency currency) {

    this.amount = Objects.requireNonNull(amount);
    this.assertArgumentTrue(amount.compareTo(BigDecimal.ZERO) >= 0, "Amount cannot be less than 0");
    this.currency = Objects.requireNonNull(currency);
  }

  @Override
  public int compareTo(Money o) {

    if (this.currency == o.getCurrency()) {

      return this.getAmount().compareTo(o.getAmount());
    } else {
      throw new IllegalArgumentException(
          "Currencies are different.A valid comaprison cannot be made");
    }
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Money money = (Money) o;
    return amount.equals(money.amount) && currency == money.currency;
  }

  @Override
  public int hashCode() {
    return Objects.hash(amount, currency);
  }

  private BigDecimal amount;

  @Transient private Currency currency;
}
