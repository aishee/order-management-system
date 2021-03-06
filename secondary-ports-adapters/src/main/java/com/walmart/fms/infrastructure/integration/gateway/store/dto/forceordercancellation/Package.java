//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference
// Implementation, vhudson-jaxb-ri-2.1-792
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a>
// Any modifications to this file will be lost upon recompilation of the source schema.
// Generated on: 2020.08.31 at 01:59:04 PM PDT
//

package com.walmart.fms.infrastructure.integration.gateway.store.dto.forceordercancellation;

import java.math.BigInteger;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;

/**
 * Java class for Package complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="Package">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="sequenceNumber" type="{http://www.w3.org/2001/XMLSchema}unsignedShort" minOccurs="0"/>
 *         &lt;element name="assignmentNumber" type="{http://www.w3.org/2001/XMLSchema}unsignedLong" minOccurs="0"/>
 *         &lt;element name="packageID" type="{http://www.xmlns.walmartstores.com/SupplyChain/FulfillmentManagement/datatypes/UpdateFulfillmentOrder/}alpha30" minOccurs="0"/>
 *         &lt;element name="trackingNumber" type="{http://www.xmlns.walmartstores.com/SupplyChain/FulfillmentManagement/datatypes/UpdateFulfillmentOrder/}alpha50" minOccurs="0"/>
 *         &lt;element name="status" type="{http://www.xmlns.walmartstores.com/SupplyChain/FulfillmentManagement/datatypes/UpdateFulfillmentOrder/}Status" minOccurs="0"/>
 *         &lt;element name="asnNumber" type="{http://www.xmlns.walmartstores.com/SupplyChain/FulfillmentManagement/datatypes/UpdateFulfillmentOrder/}alpha25" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(
    name = "Package",
    propOrder = {
      "sequenceNumber",
      "assignmentNumber",
      "packageID",
      "trackingNumber",
      "status",
      "asnNumber"
    })
@XmlSeeAlso({
  com.walmart.fms.infrastructure.integration.gateway.store.dto.forceordercancellation
      .UpdateFulfillmentOrderRequest.MessageBody.CustomerOrder.FulfillmentOrder.Packages.class,
  com.walmart.fms.infrastructure.integration.gateway.store.dto.forceordercancellation
      .UpdateFulfillmentOrderResponse.MessageBody.CustomerOrder.FulfillmentOrder.Packages.class
})
public class Package {

  @XmlSchemaType(name = "unsignedShort")
  protected Integer sequenceNumber;

  @XmlSchemaType(name = "unsignedLong")
  protected BigInteger assignmentNumber;

  protected String packageID;
  protected String trackingNumber;
  protected Status status;
  protected String asnNumber;

  /**
   * Gets the value of the sequenceNumber property.
   *
   * @return possible object is {@link Integer }
   */
  public Integer getSequenceNumber() {
    return sequenceNumber;
  }

  /**
   * Sets the value of the sequenceNumber property.
   *
   * @param value allowed object is {@link Integer }
   */
  public void setSequenceNumber(Integer value) {
    this.sequenceNumber = value;
  }

  /**
   * Gets the value of the assignmentNumber property.
   *
   * @return possible object is {@link BigInteger }
   */
  public BigInteger getAssignmentNumber() {
    return assignmentNumber;
  }

  /**
   * Sets the value of the assignmentNumber property.
   *
   * @param value allowed object is {@link BigInteger }
   */
  public void setAssignmentNumber(BigInteger value) {
    this.assignmentNumber = value;
  }

  /**
   * Gets the value of the packageID property.
   *
   * @return possible object is {@link String }
   */
  public String getPackageID() {
    return packageID;
  }

  /**
   * Sets the value of the packageID property.
   *
   * @param value allowed object is {@link String }
   */
  public void setPackageID(String value) {
    this.packageID = value;
  }

  /**
   * Gets the value of the trackingNumber property.
   *
   * @return possible object is {@link String }
   */
  public String getTrackingNumber() {
    return trackingNumber;
  }

  /**
   * Sets the value of the trackingNumber property.
   *
   * @param value allowed object is {@link String }
   */
  public void setTrackingNumber(String value) {
    this.trackingNumber = value;
  }

  /**
   * Gets the value of the status property.
   *
   * @return possible object is {@link Status }
   */
  public Status getStatus() {
    return status;
  }

  /**
   * Sets the value of the status property.
   *
   * @param value allowed object is {@link Status }
   */
  public void setStatus(Status value) {
    this.status = value;
  }

  /**
   * Gets the value of the asnNumber property.
   *
   * @return possible object is {@link String }
   */
  public String getAsnNumber() {
    return asnNumber;
  }

  /**
   * Sets the value of the asnNumber property.
   *
   * @param value allowed object is {@link String }
   */
  public void setAsnNumber(String value) {
    this.asnNumber = value;
  }
}
