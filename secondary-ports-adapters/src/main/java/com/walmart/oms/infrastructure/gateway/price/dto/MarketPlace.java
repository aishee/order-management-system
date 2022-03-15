package com.walmart.oms.infrastructure.gateway.price.dto;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import lombok.ToString;

@XmlRootElement(name = "MarketPlace")
@ToString
public class MarketPlace {
  private String vendorName;

  @XmlElement(name = "VendorName")
  public String getVendorName() {
    return vendorName;
  }

  public void setVendorName(String vendorName) {
    this.vendorName = vendorName;
  }
}
