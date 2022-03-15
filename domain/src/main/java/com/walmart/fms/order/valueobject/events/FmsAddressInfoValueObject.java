package com.walmart.fms.order.valueobject.events;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FmsAddressInfoValueObject {

  private String addressType;

  private String addressOne;

  private String addressTwo;

  private String addressThree;

  private String city;

  private String county;

  private String state;

  private String postalCode;

  private String country;

  private String latitude;

  private String longitude;
}
