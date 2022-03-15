package com.walmart.fms.order.domain.entity;

import static java.util.Objects.isNull;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.walmart.common.domain.BaseEntity;
import com.walmart.fms.order.aggregateroot.FmsOrder;
import com.walmart.fms.order.valueobject.EmailAddress;
import com.walmart.fms.order.valueobject.FullName;
import com.walmart.fms.order.valueobject.MobilePhone;
import com.walmart.fms.order.valueobject.TelePhone;
import java.util.Objects;
import javax.persistence.AttributeOverride;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "OMSCORE.FULFILLMENT_ORDER_CONTACT_INFO")
@Getter
@NoArgsConstructor
public class FmsCustomerContactInfo extends BaseEntity {

  @Builder
  public FmsCustomerContactInfo(
      String id,
      FmsOrder order,
      String customerId,
      FullName fullName,
      TelePhone phoneNumberOne,
      TelePhone phoneNumberTwo,
      EmailAddress email,
      MobilePhone mobileNumber) {

    super(id);

    this.order = Objects.requireNonNull(order);

    if (phoneNumberOne == null && phoneNumberTwo == null && mobileNumber == null) {
      throw new IllegalArgumentException("Please provide atleast one phone number");
    }

    this.assertArgumentNotNull(fullName, "Full name cannot be null");

    this.fullName = fullName;
    this.phoneNumberOne = phoneNumberOne;
    this.phoneNumberTwo = phoneNumberTwo;
    this.email = email;
    this.mobileNumber = mobileNumber;
  }

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "ORDER_RECORD_ID")
  private FmsOrder order;

  @Column(name = "CUSTOMER_ID")
  private String customerId;

  @Embedded private FullName fullName;

  @Embedded
  @AttributeOverride(name = "number", column = @Column(name = "phone_number_one"))
  private TelePhone phoneNumberOne;

  @Embedded
  @AttributeOverride(name = "number", column = @Column(name = "phone_number_two"))
  private TelePhone phoneNumberTwo;

  @Embedded private EmailAddress email;

  @Embedded
  @AttributeOverride(name = "number", column = @Column(name = "mobile_number"))
  private MobilePhone mobileNumber;

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    FmsCustomerContactInfo that = (FmsCustomerContactInfo) o;
    return order.equals(that.order)
        && fullName.equals(that.fullName)
        && Objects.equals(phoneNumberOne, that.phoneNumberOne)
        && Objects.equals(phoneNumberTwo, that.phoneNumberTwo)
        && email.equals(that.email)
        && Objects.equals(mobileNumber, that.mobileNumber);
  }

  @Override
  public int hashCode() {
    return Objects.hash(order, fullName, phoneNumberOne, phoneNumberTwo, email, mobileNumber);
  }

  @Override
  public String toString() {
    return "CustomerContactInfo{"
        + "fullName="
        + fullName
        + ", phoneNumberOne="
        + phoneNumberOne
        + ", phoneNumberTwo="
        + phoneNumberTwo
        + ", email="
        + email
        + ", mobileNumber="
        + mobileNumber
        + '}';
  }

  @JsonIgnore
  public String getPhoneNoOne() {
    return !isNull(this.getPhoneNumberOne()) ? this.getPhoneNumberOne().getNumber() : null;
  }

  @JsonIgnore
  public String getPhoneNoTwo() {
    return !isNull(this.getPhoneNumberTwo()) ? this.getPhoneNumberTwo().getNumber() : null;
  }

  @JsonIgnore
  public String getMobileNo() {
    return !isNull(this.getMobileNumber()) ? this.getMobileNumber().getNumber() : null;
  }

  @JsonIgnore
  public String getCustomerTitle() {
    return !isNull(this.getFullName()) ? this.getFullName().getTitle() : null;
  }

  @JsonIgnore
  public String getEmailAddr() {
    return !isNull(this.getEmail()) ? this.getEmail().getAddress() : null;
  }

  @JsonIgnore
  public String getFirstlNameVal() {
    return !isNull(this.getFullName()) ? this.getFullName().getFirstName() : null;
  }

  @JsonIgnore
  public String getMiddleNameVal() {
    return !isNull(this.getFullName()) ? this.getFullName().getMiddleName() : null;
  }

  @JsonIgnore
  public String getLastNameVal() {
    return !isNull(this.getFullName()) ? this.getFullName().getLastName() : null;
  }
}
