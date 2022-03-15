package com.walmart.oms.infrastructure.gateway.price.dto;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

@XmlRootElement(name = "OrderedItem")
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrderedItem {

  private String skuId;

  private String cin;

  private Long quantity;

  private PriceInfo priceInfo;

  @XmlElement(name = "Sku")
  public String getSkuId() {
    return skuId;
  }

  public void setSkuId(String skuId) {
    this.skuId = skuId;
  }

  @XmlElement(name = "CIN")
  public String getCin() {
    return cin;
  }

  public void setCin(String cin) {
    this.cin = cin;
  }

  @XmlElement(name = "Quantity")
  public Long getQuantity() {
    return quantity;
  }

  public void setQuantity(Long quantity) {
    this.quantity = quantity;
  }

  @XmlElement(name = "PriceInfo")
  public PriceInfo getPriceInfo() {
    return priceInfo;
  }

  public void setPriceInfo(PriceInfo priceInfo) {
    this.priceInfo = priceInfo;
  }
}
