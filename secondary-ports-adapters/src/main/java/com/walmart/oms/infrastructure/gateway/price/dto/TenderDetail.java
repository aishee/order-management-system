package com.walmart.oms.infrastructure.gateway.price.dto;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import lombok.ToString;

@XmlRootElement(name = "TenderDetail")
@ToString
public class TenderDetail {
  private String amount;
  private String cardType;
  private String piHash;

  @XmlElement(name = "Amount")
  public String getAmount() {
    return amount;
  }

  public void setAmount(String amount) {
    this.amount = amount;
  }

  @XmlElement(name = "CardType")
  public String getCardType() {
    return cardType;
  }

  public void setCardType(String cardType) {
    this.cardType = cardType;
  }

  @XmlElement(name = "PiHash")
  public String getPiHash() {
    return piHash;
  }

  public void setPiHash(String piHash) {
    this.piHash = piHash;
  }
}
