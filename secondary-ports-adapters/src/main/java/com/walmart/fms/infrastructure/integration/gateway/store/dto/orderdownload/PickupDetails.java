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
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.datatype.XMLGregorianCalendar;

/**
 * Java class for PickupDetails complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="PickupDetails">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="promisedTimeForPickup" type="{http://www.w3.org/2001/XMLSchema}dateTime" minOccurs="0"/>
 *         &lt;element name="expirationTime" type="{http://www.w3.org/2001/XMLSchema}dateTime" minOccurs="0"/>
 *         &lt;element name="plannedPickupDate" type="{http://www.w3.org/2001/XMLSchema}date" minOccurs="0"/>
 *         &lt;element name="plannedPickupTimeSlot" type="{http://www.xmlns.walmartstores.com/SupplyChain/FulfillmentManagement/datatypes/PlaceFulfillmentOrder/}TimeRange" minOccurs="0"/>
 *         &lt;element name="alternatePickupContact" type="{http://www.xmlns.walmartstores.com/SupplyChain/FulfillmentManagement/datatypes/PlaceFulfillmentOrder/}AlternatePickupContact" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(
    name = "PickupDetails",
    propOrder = {
      "promisedTimeForPickup",
      "expirationTime",
      "plannedPickupDate",
      "plannedPickupTimeSlot",
      "alternatePickupContact"
    })
public class PickupDetails {

  @XmlSchemaType(name = "dateTime")
  protected XMLGregorianCalendar promisedTimeForPickup;

  @XmlSchemaType(name = "dateTime")
  protected XMLGregorianCalendar expirationTime;

  @XmlSchemaType(name = "date")
  protected XMLGregorianCalendar plannedPickupDate;

  protected TimeRange plannedPickupTimeSlot;
  protected AlternatePickupContact alternatePickupContact;

  /**
   * Gets the value of the promisedTimeForPickup property.
   *
   * @return possible object is {@link XMLGregorianCalendar }
   */
  public XMLGregorianCalendar getPromisedTimeForPickup() {
    return promisedTimeForPickup;
  }

  /**
   * Sets the value of the promisedTimeForPickup property.
   *
   * @param value allowed object is {@link XMLGregorianCalendar }
   */
  public void setPromisedTimeForPickup(XMLGregorianCalendar value) {
    this.promisedTimeForPickup = value;
  }

  /**
   * Gets the value of the expirationTime property.
   *
   * @return possible object is {@link XMLGregorianCalendar }
   */
  public XMLGregorianCalendar getExpirationTime() {
    return expirationTime;
  }

  /**
   * Sets the value of the expirationTime property.
   *
   * @param value allowed object is {@link XMLGregorianCalendar }
   */
  public void setExpirationTime(XMLGregorianCalendar value) {
    this.expirationTime = value;
  }

  /**
   * Gets the value of the plannedPickupDate property.
   *
   * @return possible object is {@link XMLGregorianCalendar }
   */
  public XMLGregorianCalendar getPlannedPickupDate() {
    return plannedPickupDate;
  }

  /**
   * Sets the value of the plannedPickupDate property.
   *
   * @param value allowed object is {@link XMLGregorianCalendar }
   */
  public void setPlannedPickupDate(XMLGregorianCalendar value) {
    this.plannedPickupDate = value;
  }

  /**
   * Gets the value of the plannedPickupTimeSlot property.
   *
   * @return possible object is {@link TimeRange }
   */
  public TimeRange getPlannedPickupTimeSlot() {
    return plannedPickupTimeSlot;
  }

  /**
   * Sets the value of the plannedPickupTimeSlot property.
   *
   * @param value allowed object is {@link TimeRange }
   */
  public void setPlannedPickupTimeSlot(TimeRange value) {
    this.plannedPickupTimeSlot = value;
  }

  /**
   * Gets the value of the alternatePickupContact property.
   *
   * @return possible object is {@link AlternatePickupContact }
   */
  public AlternatePickupContact getAlternatePickupContact() {
    return alternatePickupContact;
  }

  /**
   * Sets the value of the alternatePickupContact property.
   *
   * @param value allowed object is {@link AlternatePickupContact }
   */
  public void setAlternatePickupContact(AlternatePickupContact value) {
    this.alternatePickupContact = value;
  }
}
