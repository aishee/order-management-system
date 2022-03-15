package com.walmart.oms.infrastructure.gateway.price.dto;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.ToString;

@XmlRootElement(name = "Header")
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PYSIPYPHeader {

  private String accessCode;
  private String countryCode;
  private String messageType;
  private String orderNumber;
  private String storeNumber;
  private String saleType = null;

  @XmlElement(name = "AccessCode")
  public String getAccessCode() {
    return accessCode;
  }

  public void setAccessCode(String accessCode) {
    this.accessCode = accessCode;
  }

  @XmlElement(name = "CountryCode")
  public String getCountryCode() {
    return countryCode;
  }

  public void setCountryCode(String countryCode) {
    this.countryCode = countryCode;
  }

  @XmlElement(name = "MessageType")
  public String getMessageType() {
    return messageType;
  }

  public void setMessageType(String messageType) {
    this.messageType = messageType;
  }

  @XmlElement(name = "OrderNumber")
  public String getOrderNumber() {
    return orderNumber;
  }

  public void setOrderNumber(String orderNumber) {
    this.orderNumber = orderNumber;
  }

  @XmlElement(name = "StoreNumber")
  public String getStoreNumber() {
    return storeNumber;
  }

  public void setStoreNumber(String storeNumber) {
    this.storeNumber = storeNumber;
  }

  @XmlElement(name = "SaleType")
  public String getSaleType() {
    return saleType;
  }

  public void setSaleType(String saleType) {
    this.saleType = saleType;
  }
}
