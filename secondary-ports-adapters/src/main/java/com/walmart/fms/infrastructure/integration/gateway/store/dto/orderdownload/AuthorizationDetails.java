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
import javax.xml.bind.annotation.XmlType;

/**
 * Java class for AuthorizationDetails complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="AuthorizationDetails">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="approvalNumber" type="{http://www.xmlns.walmartstores.com/SupplyChain/FulfillmentManagement/datatypes/PlaceFulfillmentOrder/}alpha6" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(
    name = "AuthorizationDetails",
    propOrder = {"approvalNumber"})
public class AuthorizationDetails {

  protected String approvalNumber;

  /**
   * Gets the value of the approvalNumber property.
   *
   * @return possible object is {@link String }
   */
  public String getApprovalNumber() {
    return approvalNumber;
  }

  /**
   * Sets the value of the approvalNumber property.
   *
   * @param value allowed object is {@link String }
   */
  public void setApprovalNumber(String value) {
    this.approvalNumber = value;
  }
}
