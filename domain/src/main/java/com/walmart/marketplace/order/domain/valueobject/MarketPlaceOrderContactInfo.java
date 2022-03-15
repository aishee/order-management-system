package com.walmart.marketplace.order.domain.valueobject;

import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MarketPlaceOrderContactInfo implements Serializable {

  @Column(name = "FIRST_NAME")
  private String firstName;

  @Column(name = "LAST_NAME")
  private String lastName;

  // this method is used to by-pass any Vendor API calls while keeping other Vendor specific code
  // intact.
  public boolean isTestVendor() {
    return "test_automation".equalsIgnoreCase(firstName);
  }
}
