package com.walmart.fms.order.domain.entity;

import com.walmart.common.domain.BaseEntity;
import com.walmart.fms.order.aggregateroot.FmsOrder;
import java.util.Objects;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "OMSCORE.FULFILLMENT_ORDER_ADDRESS")
@NoArgsConstructor
public class FmsAddressInfo extends BaseEntity {

  // @ManyToOne(fetch = FetchType.LAZY)
  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "REF_RECORD_ID")
  private FmsOrder order;

  @Column(name = "ADDRESS_TYPE")
  private String addressType;

  @Column(name = "ADDRESS_ONE")
  private String addressOne;

  @Column(name = "ADDRESS_TWO")
  private String addressTwo;

  @Column(name = "ADDRESS_THREE")
  private String addressThree;

  @Column(name = "CITY")
  private String city;

  @Column(name = "COUNTY")
  private String county;

  @Column(name = "STATE")
  private String state;

  @Column(name = "POSTAL_CODE")
  private String postalCode;

  @Column(name = "COUNTRY")
  private String country;

  @Column(name = "LATITUDE")
  private String latitude;

  @Column(name = "LONGITUDE")
  private String longitude;

  @Builder
  public FmsAddressInfo(
      FmsOrder order,
      String id,
      String addressType,
      String addressOne,
      String addressTwo,
      String addressThree,
      String city,
      String county,
      String state,
      String postalCode,
      String country,
      String latitude,
      String longitude) {

    super(id);
    this.order = Objects.requireNonNull(order);
    this.addressType = addressType;
    this.addressOne = addressOne;
    this.addressTwo = addressTwo;
    this.addressThree = addressThree;
    this.city = city;
    this.county = county;
    this.state = state;
    this.postalCode = postalCode;
    this.country = country;
    this.latitude = latitude;
    this.longitude = longitude;
  }
}
