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
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;

/**
 * Java class for FulfillmentOrder complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="FulfillmentOrder">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="requestNumber" type="{http://www.w3.org/2001/XMLSchema}unsignedLong" minOccurs="0"/>
 *         &lt;element name="orderHeader" type="{http://www.xmlns.walmartstores.com/SupplyChain/FulfillmentManagement/datatypes/UpdateFulfillmentOrder/}OrderHeader" minOccurs="0"/>
 *         &lt;element name="orderType" type="{http://www.xmlns.walmartstores.com/SupplyChain/FulfillmentManagement/datatypes/UpdateFulfillmentOrder/}alpha20" minOccurs="0"/>
 *         &lt;element name="status" type="{http://www.xmlns.walmartstores.com/SupplyChain/FulfillmentManagement/datatypes/UpdateFulfillmentOrder/}Status" minOccurs="0"/>
 *         &lt;element name="node" type="{http://www.xmlns.walmartstores.com/SupplyChain/FulfillmentManagement/datatypes/UpdateFulfillmentOrder/}Node"/>
 *         &lt;element name="pickupDetails" type="{http://www.xmlns.walmartstores.com/SupplyChain/FulfillmentManagement/datatypes/UpdateFulfillmentOrder/}PickupDetails" minOccurs="0"/>
 *         &lt;element name="deliveryDetails" type="{http://www.xmlns.walmartstores.com/SupplyChain/FulfillmentManagement/datatypes/UpdateFulfillmentOrder/}DeliveryDetails" minOccurs="0"/>
 *         &lt;element name="totalTaxAmount" type="{http://www.xmlns.walmartstores.com/SupplyChain/FulfillmentManagement/datatypes/UpdateFulfillmentOrder/}Amount" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(
    name = "FulfillmentOrder",
    propOrder = {
      "requestNumber",
      "orderHeader",
      "orderType",
      "status",
      "node",
      "pickupDetails",
      "deliveryDetails",
      "totalTaxAmount"
    })
@XmlSeeAlso({
  com.walmart.fms.infrastructure.integration.gateway.store.dto.forceordercancellation
      .UpdateFulfillmentOrderRequest.MessageBody.CustomerOrder.FulfillmentOrder.class,
  com.walmart.fms.infrastructure.integration.gateway.store.dto.forceordercancellation
      .UpdateFulfillmentOrderResponse.MessageBody.CustomerOrder.FulfillmentOrder.class
})
public class FulfillmentOrder {

  @XmlSchemaType(name = "unsignedLong")
  protected BigInteger requestNumber;

  protected OrderHeader orderHeader;
  protected String orderType;
  protected Status status;

  @XmlElement(required = true)
  protected Node node;

  protected PickupDetails pickupDetails;
  protected DeliveryDetails deliveryDetails;
  protected Amount totalTaxAmount;

  /**
   * Gets the value of the requestNumber property.
   *
   * @return possible object is {@link BigInteger }
   */
  public BigInteger getRequestNumber() {
    return requestNumber;
  }

  /**
   * Sets the value of the requestNumber property.
   *
   * @param value allowed object is {@link BigInteger }
   */
  public void setRequestNumber(BigInteger value) {
    this.requestNumber = value;
  }

  /**
   * Gets the value of the orderHeader property.
   *
   * @return possible object is {@link OrderHeader }
   */
  public OrderHeader getOrderHeader() {
    return orderHeader;
  }

  /**
   * Sets the value of the orderHeader property.
   *
   * @param value allowed object is {@link OrderHeader }
   */
  public void setOrderHeader(OrderHeader value) {
    this.orderHeader = value;
  }

  /**
   * Gets the value of the orderType property.
   *
   * @return possible object is {@link String }
   */
  public String getOrderType() {
    return orderType;
  }

  /**
   * Sets the value of the orderType property.
   *
   * @param value allowed object is {@link String }
   */
  public void setOrderType(String value) {
    this.orderType = value;
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
   * Gets the value of the node property.
   *
   * @return possible object is {@link Node }
   */
  public Node getNode() {
    return node;
  }

  /**
   * Sets the value of the node property.
   *
   * @param value allowed object is {@link Node }
   */
  public void setNode(Node value) {
    this.node = value;
  }

  /**
   * Gets the value of the pickupDetails property.
   *
   * @return possible object is {@link PickupDetails }
   */
  public PickupDetails getPickupDetails() {
    return pickupDetails;
  }

  /**
   * Sets the value of the pickupDetails property.
   *
   * @param value allowed object is {@link PickupDetails }
   */
  public void setPickupDetails(PickupDetails value) {
    this.pickupDetails = value;
  }

  /**
   * Gets the value of the deliveryDetails property.
   *
   * @return possible object is {@link DeliveryDetails }
   */
  public DeliveryDetails getDeliveryDetails() {
    return deliveryDetails;
  }

  /**
   * Sets the value of the deliveryDetails property.
   *
   * @param value allowed object is {@link DeliveryDetails }
   */
  public void setDeliveryDetails(DeliveryDetails value) {
    this.deliveryDetails = value;
  }

  /**
   * Gets the value of the totalTaxAmount property.
   *
   * @return possible object is {@link Amount }
   */
  public Amount getTotalTaxAmount() {
    return totalTaxAmount;
  }

  /**
   * Sets the value of the totalTaxAmount property.
   *
   * @param value allowed object is {@link Amount }
   */
  public void setTotalTaxAmount(Amount value) {
    this.totalTaxAmount = value;
  }
}
