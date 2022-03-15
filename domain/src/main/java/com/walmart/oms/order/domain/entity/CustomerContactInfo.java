package com.walmart.oms.order.domain.entity;

import static java.util.Objects.isNull;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.walmart.common.domain.BaseEntity;
import com.walmart.oms.order.aggregateroot.OmsOrder;
import com.walmart.oms.order.valueobject.EmailAddress;
import com.walmart.oms.order.valueobject.FullName;
import com.walmart.oms.order.valueobject.MobilePhone;
import com.walmart.oms.order.valueobject.TelePhone;
import java.util.Objects;
import javax.persistence.AttributeOverride;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "OMSCORE.OMS_CUSTOMER_CONTACT_INFO")
@Getter
@NoArgsConstructor
public class CustomerContactInfo extends BaseEntity {

  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "ORDER_RECORD_ID")
  private OmsOrder order;

  @Builder
  public CustomerContactInfo(
      String id,
      OmsOrder order,
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
    CustomerContactInfo that = (CustomerContactInfo) o;
    return fullName.equals(that.fullName)
        && Objects.equals(phoneNumberOne, that.phoneNumberOne)
        && Objects.equals(phoneNumberTwo, that.phoneNumberTwo)
        && email.equals(that.email)
        && Objects.equals(mobileNumber, that.mobileNumber);
  }

  @Override
  public int hashCode() {
    return Objects.hash(fullName, phoneNumberOne, phoneNumberTwo, email, mobileNumber);
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
  public String getFirstName() {
    return !isNull(this.getFullName()) ? this.getFullName().getFirstName() : null;
  }

  @JsonIgnore
  public String getMiddleName() {
    return !isNull(this.getFullName()) ? this.getFullName().getMiddleName() : null;
  }

  @JsonIgnore
  public String getLastName() {
    return !isNull(this.getFullName()) ? this.getFullName().getLastName() : null;
  }

  @JsonIgnore
  public String getRefPhoneNumberOne() {
    return !isNull(this.getPhoneNumberOne()) ? this.getPhoneNumberOne().getNumber() : null;
  }

  @JsonIgnore
  public String getRefPhoneNumberTwo() {
    return !isNull(this.getPhoneNumberTwo()) ? this.getPhoneNumberTwo().getNumber() : null;
  }

  @JsonIgnore
  public String getRefMobileNumber() {
    return !isNull(this.getMobileNumber()) ? this.getMobileNumber().getNumber() : null;
  }
}
