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
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 * Java class for anonymous complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element ref="{http://www.xmlns.walmartstores.com/Header/datatypes/MessageHeader/1.4/}MessageHeader"/>
 *         &lt;element ref="{http://www.xmlns.walmartstores.com/Fault/datatypes/MessageFault/1.0/}MessageFault"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(
    name = "",
    propOrder = {"messageHeader", "messageFault"})
@XmlRootElement(name = "Fault")
public class Fault {

  @XmlElement(
      name = "MessageHeader",
      namespace = "http://www.xmlns.walmartstores.com/Header/datatypes/MessageHeader/1.4/",
      required = true)
  protected MessageHeader messageHeader;

  @XmlElement(
      name = "MessageFault",
      namespace = "http://www.xmlns.walmartstores.com/Fault/datatypes/MessageFault/1.0/",
      required = true)
  protected MessageFault messageFault;

  /**
   * Gets the value of the messageHeader property.
   *
   * @return possible object is {@link MessageHeader }
   */
  public MessageHeader getMessageHeader() {
    return messageHeader;
  }

  /**
   * Sets the value of the messageHeader property.
   *
   * @param value allowed object is {@link MessageHeader }
   */
  public void setMessageHeader(MessageHeader value) {
    this.messageHeader = value;
  }

  /**
   * Gets the value of the messageFault property.
   *
   * @return possible object is {@link MessageFault }
   */
  public MessageFault getMessageFault() {
    return messageFault;
  }

  /**
   * Sets the value of the messageFault property.
   *
   * @param value allowed object is {@link MessageFault }
   */
  public void setMessageFault(MessageFault value) {
    this.messageFault = value;
  }
}
