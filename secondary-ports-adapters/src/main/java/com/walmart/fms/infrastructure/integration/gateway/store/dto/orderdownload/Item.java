//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference
// Implementation, vhudson-jaxb-ri-2.1-792
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a>
// Any modifications to this file will be lost upon recompilation of the source schema.
// Generated on: 2020.08.30 at 08:54:50 PM PDT
//

package com.walmart.fms.infrastructure.integration.gateway.store.dto.orderdownload;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;

/**
 * Java class for Item complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="Item">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="number" type="{http://www.w3.org/2001/XMLSchema}unsignedInt" minOccurs="0"/>
 *         &lt;element name="cost" type="{http://www.xmlns.walmartstores.com/SupplyChain/FulfillmentManagement/datatypes/PlaceFulfillmentOrder/}Cost" minOccurs="0"/>
 *         &lt;element name="sKU" type="{http://www.xmlns.walmartstores.com/SupplyChain/FulfillmentManagement/datatypes/PlaceFulfillmentOrder/}SKU" minOccurs="0"/>
 *         &lt;element name="purchasedBy" type="{http://www.xmlns.walmartstores.com/SupplyChain/FulfillmentManagement/datatypes/PlaceFulfillmentOrder/}Company" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(
    name = "Item",
    propOrder = {"number", "cost", "sku", "purchasedBy"})
public class Item {

  @XmlSchemaType(name = "unsignedInt")
  protected Long number;

  protected Cost cost;

  @XmlElement(name = "sKU")
  protected SKU sku;

  protected Company purchasedBy;

  /**
   * Gets the value of the number property.
   *
   * @return possible object is {@link Long }
   */
  public Long getNumber() {
    return number;
  }

  /**
   * Sets the value of the number property.
   *
   * @param value allowed object is {@link Long }
   */
  public void setNumber(Long value) {
    this.number = value;
  }

  /**
   * Gets the value of the cost property.
   *
   * @return possible object is {@link Cost }
   */
  public Cost getCost() {
    return cost;
  }

  /**
   * Sets the value of the cost property.
   *
   * @param value allowed object is {@link Cost }
   */
  public void setCost(Cost value) {
    this.cost = value;
  }

  /**
   * Gets the value of the sku property.
   *
   * @return possible object is {@link SKU }
   */
  public SKU getSKU() {
    return sku;
  }

  /**
   * Sets the value of the sku property.
   *
   * @param value allowed object is {@link SKU }
   */
  public void setSKU(SKU value) {
    this.sku = value;
  }

  /**
   * Gets the value of the purchasedBy property.
   *
   * @return possible object is {@link Company }
   */
  public Company getPurchasedBy() {
    return purchasedBy;
  }

  /**
   * Sets the value of the purchasedBy property.
   *
   * @param value allowed object is {@link Company }
   */
  public void setPurchasedBy(Company value) {
    this.purchasedBy = value;
  }
}
