package com.walmart.oms.order.valueobject;

import com.walmart.common.domain.AssertionConcern;
import java.io.Serializable;
import java.util.Objects;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Embeddable
@NoArgsConstructor
public class EmailAddress extends AssertionConcern implements Serializable {

  @Column(name = "email")
  private String address;

  public EmailAddress(String address) {

    this.assertArgumentNotEmpty(address, "The email address is required.");
    this.assertArgumentLength(address, 1, 100, "Email address must be 100 characters or less.");
    this.address = address;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    EmailAddress that = (EmailAddress) o;
    return address.equals(that.address);
  }

  @Override
  public int hashCode() {
    return Objects.hash(address);
  }
}
