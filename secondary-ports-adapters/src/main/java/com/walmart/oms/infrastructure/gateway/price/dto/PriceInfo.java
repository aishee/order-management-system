package com.walmart.oms.infrastructure.gateway.price.dto;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

@XmlRootElement(name = "PriceInfo")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PriceInfo {

  private Double listPrice;

  private Double rawTotalPrice;

  private Double finalAmount;

  private Double minimumUnitPrice;

  private Integer quantityDiscounted;

  private Long quantityAsQualifier;

  private Double upliftedListPrice;

  @XmlElement(name = "UpliftedListPrice")
  public Double getUpliftedListPrice() {
    return upliftedListPrice;
  }

  public void setUpliftedListPrice(Double upliftedListPrice) {
    this.upliftedListPrice = upliftedListPrice;
  }

  @XmlElement(name = "ListPrice")
  public Double getListPrice() {
    return listPrice;
  }

  public void setListPrice(Double listPrice) {
    this.listPrice = listPrice;
  }

  @XmlElement(name = "RawTotalPrice")
  public Double getRawTotalPrice() {
    return rawTotalPrice;
  }

  public void setRawTotalPrice(Double rawTotalPrice) {
    this.rawTotalPrice = rawTotalPrice;
  }

  @XmlElement(name = "FinalAmount")
  public Double getFinalAmount() {
    return finalAmount;
  }

  public void setFinalAmount(Double finalAmount) {
    this.finalAmount = finalAmount;
  }

  @XmlElement(name = "MinimumUnitPrice")
  public Double getMinimumUnitPrice() {
    return minimumUnitPrice;
  }

  public void setMinimumUnitPrice(Double minimumUnitPrice) {
    this.minimumUnitPrice = minimumUnitPrice;
  }

  @XmlElement(name = "QuantityDiscounted")
  public Integer getQuantityDiscounted() {
    return quantityDiscounted;
  }

  public void setQuantityDiscounted(Integer quantityDiscounted) {
    this.quantityDiscounted = quantityDiscounted;
  }

  @XmlElement(name = "QuantityAsQualifier")
  public Long getQuantityAsQualifier() {
    return quantityAsQualifier;
  }

  public void setQuantityAsQualifier(Long quantityAsQualifier) {
    this.quantityAsQualifier = quantityAsQualifier;
  }
}
