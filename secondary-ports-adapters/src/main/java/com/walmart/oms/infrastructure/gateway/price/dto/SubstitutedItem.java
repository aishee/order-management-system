package com.walmart.oms.infrastructure.gateway.price.dto;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.ToString;

@XmlRootElement(name = "SubstitutedItem")
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SubstitutedItem {

  private String wmItemNum;
  private String isPriceOverride;
  private String code;
  private String department;
  private String description;
  private boolean isOverridden;
  private double originalStoreTotalPrice;
  private double storeTotalPrice;
  private double storeUnitPrice;
  private double taxAmount;
  private String pickedBy;
  private String posDesc;
  private long quantity;
  private Double weight;
  private String adjustedPrice;
  private String adjustedPriceExTax;
  private String webAdjustedPrice;
  private String displayPrice;
  private String colleagueDiscountedPrice;
  private String uom;
  private String upc;
  private String taxRate;
  private String taxType;
  private String taxRecord;

  @XmlElement(name = "WmItemNum")
  public String getWmItemNum() {
    return wmItemNum;
  }

  public void setWmItemNum(String wmItemNum) {
    this.wmItemNum = wmItemNum;
  }

  @XmlElement(name = "IsPriceOverride")
  public String getIsPriceOverride() {
    return isPriceOverride;
  }

  public void setIsPriceOverride(String isPriceOverride) {
    this.isPriceOverride = isPriceOverride;
  }

  @XmlElement(name = "code")
  public String getCode() {
    return code;
  }

  public void setCode(String code) {
    this.code = code;
  }

  @XmlElement(name = "Department")
  public String getDepartment() {
    return department;
  }

  public void setDepartment(String department) {
    this.department = department;
  }

  @XmlElement(name = "Desc")
  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  @XmlElement(name = "isOverridden")
  public boolean isOverridden() {
    return isOverridden;
  }

  public void setOverridden(boolean overridden) {
    isOverridden = overridden;
  }

  @XmlElement(name = "OriginalStoreTotalPrice")
  public double getOriginalStoreTotalPrice() {
    return originalStoreTotalPrice;
  }

  public void setOriginalStoreTotalPrice(double originalStoreTotalPrice) {
    this.originalStoreTotalPrice = originalStoreTotalPrice;
  }

  @XmlElement(name = "StoreTotalPrice")
  public double getStoreTotalPrice() {
    return storeTotalPrice;
  }

  public void setStoreTotalPrice(double storeTotalPrice) {
    this.storeTotalPrice = storeTotalPrice;
  }

  @XmlElement(name = "StoreUnitPrice")
  public double getStoreUnitPrice() {
    return storeUnitPrice;
  }

  public void setStoreUnitPrice(double storeUnitPrice) {
    this.storeUnitPrice = storeUnitPrice;
  }

  @XmlElement(name = "pickedBy")
  public String getPickedBy() {
    return pickedBy;
  }

  public void setPickedBy(String pickedBy) {
    this.pickedBy = pickedBy;
  }

  @XmlElement(name = "PosDesc")
  public String getPosDesc() {
    return posDesc;
  }

  public void setPosDesc(String posDesc) {
    this.posDesc = posDesc;
  }

  @XmlElement(name = "Quantity")
  public long getQuantity() {
    return quantity;
  }

  public void setQuantity(long quantity) {
    this.quantity = quantity;
  }

  @XmlElement(name = "weight")
  public Double getWeight() {
    return weight;
  }

  public void setWeight(Double weight) {
    this.weight = weight;
  }

  @XmlElement(name = "AdjustedPrice")
  public String getAdjustedPrice() {
    return adjustedPrice;
  }

  public void setAdjustedPrice(String adjustedPrice) {
    this.adjustedPrice = adjustedPrice;
  }

  @XmlElement(name = "AdjustedPriceExTax")
  public String getAdjustedPriceExTax() {
    return adjustedPriceExTax;
  }

  public void setAdjustedPriceExTax(String adjustedPriceExTax) {
    this.adjustedPriceExTax = adjustedPriceExTax;
  }

  @XmlElement(name = "WebAdjustedPrice")
  public String getWebAdjustedPrice() {
    return webAdjustedPrice;
  }

  public void setWebAdjustedPrice(String webAdjustedPrice) {
    this.webAdjustedPrice = webAdjustedPrice;
  }

  @XmlElement(name = "DisplayPrice")
  public String getDisplayPrice() {
    return displayPrice;
  }

  public void setDisplayPrice(String displayPrice) {
    this.displayPrice = displayPrice;
  }

  @XmlElement(name = "ColleagueDiscountedPrice")
  public String getColleagueDiscountedPrice() {
    return colleagueDiscountedPrice;
  }

  public void setColleagueDiscountedPrice(String colleagueDiscountedPrice) {
    this.colleagueDiscountedPrice = colleagueDiscountedPrice;
  }

  @XmlElement(name = "UOM")
  public String getUom() {
    return uom;
  }

  public void setUom(String uom) {
    this.uom = uom;
  }

  @XmlElement(name = "UPC")
  public String getUpc() {
    return upc;
  }

  public void setUpc(String upc) {
    this.upc = upc;
  }

  @XmlElement(name = "TaxRate")
  public String getTaxRate() {
    return taxRate;
  }

  public void setTaxRate(String taxRate) {
    this.taxRate = taxRate;
  }

  @XmlElement(name = "TaxType")
  public String getTaxType() {
    return taxType;
  }

  public void setTaxType(String taxType) {
    this.taxType = taxType;
  }

  @XmlElement(name = "TaxRecord")
  public String getTaxRecord() {
    return taxRecord;
  }

  public void setTaxRecord(String taxRecord) {
    this.taxRecord = taxRecord;
  }

  @XmlElement(name = "TaxAmount")
  public double getTaxAmount() {
    return taxAmount;
  }

  public void setTaxAmount(double taxAmount) {
    this.taxAmount = taxAmount;
  }
}
