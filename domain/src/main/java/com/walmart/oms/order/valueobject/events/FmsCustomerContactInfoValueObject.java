package com.walmart.oms.order.valueobject.events;

import com.walmart.oms.order.valueobject.EmailAddress;
import com.walmart.oms.order.valueobject.MobilePhone;
import lombok.Data;

@Data
public class FmsCustomerContactInfoValueObject {

  private String firstName;

  private String lastName;

  private String phoneNumberTwo;

  private String phoneNumberOne;

  private String title;

  private EmailAddress email;

  private MobilePhone mobileNumber;
}
