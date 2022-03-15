package com.walmart.oms.order.domain.entity;

import com.walmart.common.domain.BaseEntity;
import com.walmart.oms.order.aggregateroot.OmsOrder;
import java.util.Objects;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import lombok.Builder;
import lombok.Getter;

@Entity
@Table(name = "OMSCORE.OMS_ORDER_ADDRESS_INFO")
@Getter
public class AddressInfo extends BaseEntity {

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "ORDER_RECORD_ID")
  private OmsOrder order;

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
  public AddressInfo(
      OmsOrder omsOrder,
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
    this.order = Objects.requireNonNull(omsOrder);
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
