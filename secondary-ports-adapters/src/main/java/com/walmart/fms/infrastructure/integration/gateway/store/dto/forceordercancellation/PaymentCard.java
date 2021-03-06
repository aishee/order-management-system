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
 * Java class for PaymentCard complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="PaymentCard">
 *   &lt;complexContent>
 *     &lt;extension base="{http://www.xmlns.walmartstores.com/SupplyChain/FulfillmentManagement/datatypes/UpdateFulfillmentOrder/}PaymentInstrument">
 *       &lt;sequence>
 *         &lt;element name="type" type="{http://www.xmlns.walmartstores.com/SupplyChain/FulfillmentManagement/datatypes/UpdateFulfillmentOrder/}CardType" minOccurs="0"/>
 *         &lt;element name="billingAddress" type="{http://www.xmlns.walmartstores.com/SupplyChain/FulfillmentManagement/datatypes/UpdateFulfillmentOrder/}Location" minOccurs="0"/>
 *         &lt;element name="lastFourDigits" minOccurs="0">
 *           &lt;simpleType>
 *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *               &lt;pattern value="[0-9][0-9][0-9][0-9]"/>
 *             &lt;/restriction>
 *           &lt;/simpleType>
 *         &lt;/element>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(
    name = "PaymentCard",
    propOrder = {"type", "billingAddress", "lastFourDigits"})
public class PaymentCard extends PaymentInstrument {

  protected CardType type;
  protected Location billingAddress;
  protected String lastFourDigits;

  /**
   * Gets the value of the type property.
   *
   * @return possible object is {@link CardType }
   */
  public CardType getType() {
    return type;
  }

  /**
   * Sets the value of the type property.
   *
   * @param value allowed object is {@link CardType }
   */
  public void setType(CardType value) {
    this.type = value;
  }

  /**
   * Gets the value of the billingAddress property.
   *
   * @return possible object is {@link Location }
   */
  public Location getBillingAddress() {
    return billingAddress;
  }

  /**
   * Sets the value of the billingAddress property.
   *
   * @param value allowed object is {@link Location }
   */
  public void setBillingAddress(Location value) {
    this.billingAddress = value;
  }

  /**
   * Gets the value of the lastFourDigits property.
   *
   * @return possible object is {@link String }
   */
  public String getLastFourDigits() {
    return lastFourDigits;
  }

  /**
   * Sets the value of the lastFourDigits property.
   *
   * @param value allowed object is {@link String }
   */
  public void setLastFourDigits(String value) {
    this.lastFourDigits = value;
  }
}
