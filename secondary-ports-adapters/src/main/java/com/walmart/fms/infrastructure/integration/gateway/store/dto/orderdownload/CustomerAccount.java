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
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;

/**
 * Java class for CustomerAccount complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="CustomerAccount">
 *   &lt;complexContent>
 *     &lt;extension base="{http://www.xmlns.walmartstores.com/SupplyChain/FulfillmentManagement/datatypes/PlaceFulfillmentOrder/}Account">
 *       &lt;sequence>
 *         &lt;element name="isBadAccount" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *         &lt;element name="isPINRequired" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(
    name = "CustomerAccount",
    propOrder = {"isBadAccount", "isPINRequired"})
@XmlSeeAlso({MembershipAccount.class})
public class CustomerAccount extends Account {

  protected Boolean isBadAccount;
  protected Boolean isPINRequired;

  /**
   * Gets the value of the isBadAccount property.
   *
   * @return possible object is {@link Boolean }
   */
  public Boolean isIsBadAccount() {
    return isBadAccount;
  }

  /**
   * Sets the value of the isBadAccount property.
   *
   * @param value allowed object is {@link Boolean }
   */
  public void setIsBadAccount(Boolean value) {
    this.isBadAccount = value;
  }

  /**
   * Gets the value of the isPINRequired property.
   *
   * @return possible object is {@link Boolean }
   */
  public Boolean isIsPINRequired() {
    return isPINRequired;
  }

  /**
   * Sets the value of the isPINRequired property.
   *
   * @param value allowed object is {@link Boolean }
   */
  public void setIsPINRequired(Boolean value) {
    this.isPINRequired = value;
  }
}
