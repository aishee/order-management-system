//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference
// Implementation, vhudson-jaxb-ri-2.1-792
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a>
// Any modifications to this file will be lost upon recompilation of the source schema.
// Generated on: 2020.08.31 at 01:59:04 PM PDT
//

package com.walmart.fms.infrastructure.integration.gateway.store.dto.forceordercancellation;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

/**
 * Java class for TaxAdjustment complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="TaxAdjustment">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="amount" type="{http://www.xmlns.walmartstores.com/SupplyChain/FulfillmentManagement/datatypes/UpdateFulfillmentOrder/}Amount" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(
    name = "TaxAdjustment",
    propOrder = {"amount"})
public class TaxAdjustment {

  protected Amount amount;

  /**
   * Gets the value of the amount property.
   *
   * @return possible object is {@link Amount }
   */
  public Amount getAmount() {
    return amount;
  }

  /**
   * Sets the value of the amount property.
   *
   * @param value allowed object is {@link Amount }
   */
  public void setAmount(Amount value) {
    this.amount = value;
  }
}
