package com.walmart.fms.order.valueobject.events;

import com.walmart.fms.order.valueobject.EmailAddress;
import com.walmart.fms.order.valueobject.MobilePhone;
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
