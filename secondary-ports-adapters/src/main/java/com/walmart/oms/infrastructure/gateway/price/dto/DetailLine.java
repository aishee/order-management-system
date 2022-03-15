package com.walmart.oms.infrastructure.gateway.price.dto;

import java.util.List;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.util.CollectionUtils;

@XmlRootElement(name = "DetailLine")
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DetailLine {

  private String productID;
  private long pickedQuantity;
  private String storeTotalPrice = null;
  private String hasColleagueDiscount;
  private String colleagueDiscountedPrice;
  private String replenishUnitIndicator;
  private int substitutionQty;
  private String taxType = null;
  private String taxRecord = null;
  private String taxRate = null;
  private String uom = null;
  private String adjustedPrice;
  private String adjPriceExVAT;
  private String vatAmount = null;
  private String webAdjustedPrice;
  private String displayPrice;
  private List<UPCDetail> upcDetails = null;
  private List<SubstitutedItem> substitutedItems = null;

  @XmlElement(name = "ProductID")
  public String getProductID() {
    return productID;
  }

  public void setProductID(String productID) {
    this.productID = productID;
  }

  @XmlElement(name = "PickedQuantity")
  public long getPickedQuantity() {
    return pickedQuantity;
  }

  public void setPickedQuantity(long pickedQuantity) {
    this.pickedQuantity = pickedQuantity;
  }

  @XmlElement(name = "StoreTotalPrice")
  public String getStoreTotalPrice() {
    return storeTotalPrice;
  }

  public void setStoreTotalPrice(String storeTotalPrice) {
    this.storeTotalPrice = storeTotalPrice;
  }

  @XmlElement(name = "HasColleagueDiscount")
  public String getHasColleagueDiscount() {
    return hasColleagueDiscount;
  }

  public void setHasColleagueDiscount(String hasColleagueDiscount) {
    this.hasColleagueDiscount = hasColleagueDiscount;
  }

  @XmlElement(name = "ColleagueDiscountedPrice")
  public String getColleagueDiscountedPrice() {
    return colleagueDiscountedPrice;
  }

  public void setColleagueDiscountedPrice(String colleagueDiscountedPrice) {
    this.colleagueDiscountedPrice = colleagueDiscountedPrice;
  }

  @XmlElement(name = "ReplenishUnitIndicator")
  public String getReplenishUnitIndicator() {
    return replenishUnitIndicator;
  }

  public void setReplenishUnitIndicator(String replenishUnitIndicator) {
    this.replenishUnitIndicator = replenishUnitIndicator;
  }

  @XmlElement(name = "SubstitutionQty")
  public int getSubstitutionQty() {
    return substitutionQty;
  }

  public void setSubstitutionQty(int substitutionQty) {
    this.substitutionQty = substitutionQty;
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

  @XmlElement(name = "TaxRate")
  public String getTaxRate() {
    return taxRate;
  }

  public void setTaxRate(String taxRate) {
    this.taxRate = taxRate;
  }

  @XmlElement(name = "UOM")
  public String getUom() {
    return uom;
  }

  public void setUom(String uom) {
    this.uom = uom;
  }

  @XmlElement(name = "AdjustedPrice")
  public String getAdjustedPrice() {
    return adjustedPrice;
  }

  public void setAdjustedPrice(String adjustedPrice) {
    this.adjustedPrice = adjustedPrice;
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

  @XmlElementWrapper(name = "UPCList")
  @XmlElement(name = "UPCDetails")
  public List<UPCDetail> getUpcDetails() {
    return upcDetails;
  }

  public void setUpcDetails(List<UPCDetail> upcDetails) {
    this.upcDetails = upcDetails;
  }

  @XmlElementWrapper(name = "SubstitutionInformation")
  @XmlElement(name = "SubstitutionLine")
  public List<SubstitutedItem> getSubstitutedItems() {
    return substitutedItems;
  }

  public void setSubstitutedItems(List<SubstitutedItem> substitutedItems) {
    this.substitutedItems = substitutedItems;
  }

  @XmlElement(name = "AdjustedPriceExTax")
  public String getAdjPriceExVAT() {
    return adjPriceExVAT;
  }

  public void setAdjPriceExVAT(String adjPriceExVAT) {
    this.adjPriceExVAT = adjPriceExVAT;
  }

  @XmlElement(name = "TaxAmount")
  public String getVatAmount() {
    return vatAmount;
  }

  public void setVatAmount(String vatAmount) {
    this.vatAmount = vatAmount;
  }

  public boolean isSubstituted() {
    return !CollectionUtils.isEmpty(getSubstitutedItems());
  }
}
