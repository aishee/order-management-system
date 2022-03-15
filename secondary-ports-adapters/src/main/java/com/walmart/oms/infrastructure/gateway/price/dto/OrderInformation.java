package com.walmart.oms.infrastructure.gateway.price.dto;

import java.util.List;
import java.util.Optional;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.ToString;

@XmlType(propOrder = {})
@XmlRootElement(name = "OrderInformation")
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderInformation {

  private String consumerId;
  private PYSIPYPHeader header;
  private String cardTypeUsed;
  private String colleagueDiscount;
  private List<DetailLine> detailLine;
  private boolean minBasketChargeAppliedOnPricing;
  private String minOrderAmount;
  private String posOrderTotalPrice;
  private List<TenderDetail> tenderDetail;
  private ErrorDetail error;
  private MarketPlace marketPlace;
  private List<OrderedItem> orderedItems;

  @XmlElement(name = "TransactionCode")
  public String getTransactionCode() {
    return transactionCode;
  }

  public void setTransactionCode(String transactionCode) {
    this.transactionCode = transactionCode;
  }

  private String transactionCode;

  @XmlElement(name = "CardTypeUsed")
  public String getCardTypeUsed() {
    return cardTypeUsed;
  }

  public void setCardTypeUsed(String cardTypeUsed) {
    this.cardTypeUsed = cardTypeUsed;
  }

  @XmlElement(name = "ColleagueDiscount")
  public String getColleagueDiscount() {
    return colleagueDiscount;
  }

  public void setColleagueDiscount(String colleagueDiscount) {
    this.colleagueDiscount = colleagueDiscount;
  }

  @XmlElement(name = "consumerId")
  public String getConsumerId() {
    return consumerId;
  }

  public void setConsumerId(String consumerId) {
    this.consumerId = consumerId;
  }

  @XmlElement(name = "Header")
  public PYSIPYPHeader getHeader() {
    return header;
  }

  public void setHeader(PYSIPYPHeader header) {
    this.header = header;
  }

  @XmlElementWrapper(name = "ItemInformation")
  @XmlElement(name = "DetailLine")
  public List<DetailLine> getDetailLine() {
    return detailLine;
  }

  public void setDetailLine(List<DetailLine> detailLine) {
    this.detailLine = detailLine;
  }

  @XmlElement(name = "MinBasketChargeAppliedOnPricing")
  public boolean isMinBasketChargeAppliedOnPricing() {
    return minBasketChargeAppliedOnPricing;
  }

  public void setMinBasketChargeAppliedOnPricing(boolean minBasketChargeAppliedOnPricing) {
    this.minBasketChargeAppliedOnPricing = minBasketChargeAppliedOnPricing;
  }

  @XmlElement(name = "MinOrderAmount")
  public String getMinOrderAmount() {
    return minOrderAmount;
  }

  public void setMinOrderAmount(String minOrderAmount) {
    this.minOrderAmount = minOrderAmount;
  }

  @XmlElement(name = "POSOrderTotalPrice")
  public String getPosOrderTotalPrice() {
    return posOrderTotalPrice;
  }

  public void setPosOrderTotalPrice(String posOrderTotalPrice) {
    this.posOrderTotalPrice = posOrderTotalPrice;
  }

  @XmlElementWrapper(name = "TenderInformation")
  @XmlElement(name = "TenderDetail")
  public List<TenderDetail> getTenderDetail() {
    return tenderDetail;
  }

  public void setTenderDetail(List<TenderDetail> tenderDetail) {
    this.tenderDetail = tenderDetail;
  }

  @XmlElement(name = "Error")
  public ErrorDetail getError() {
    return error;
  }

  public void setError(ErrorDetail error) {
    this.error = error;
  }

  @XmlElement(name = "MarketPlace")
  public MarketPlace getMarketPlace() {
    return marketPlace;
  }

  public void setMarketPlace(MarketPlace marketPlace) {
    this.marketPlace = marketPlace;
  }

  @XmlElementWrapper(name = "OrderedItemList")
  @XmlElement(name = "OrderedItem")
  public List<OrderedItem> getOrderedItems() {
    return orderedItems;
  }

  public void setOrderedItems(List<OrderedItem> orderedItems) {
    this.orderedItems = orderedItems;
  }

  public String getOrderNumber() {
    return Optional.ofNullable(this.getHeader()).map(PYSIPYPHeader::getOrderNumber).orElse(null);
  }
}
