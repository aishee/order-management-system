//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference
// Implementation, vhudson-jaxb-ri-2.1-792
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a>
// Any modifications to this file will be lost upon recompilation of the source schema.
// Generated on: 2020.08.31 at 01:59:04 PM PDT
//

package com.walmart.fms.infrastructure.integration.gateway.store.dto.forceordercancellation;

import java.math.BigDecimal;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.datatype.XMLGregorianCalendar;

/**
 * Java class for Promotion complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="Promotion">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="id" minOccurs="0">
 *           &lt;simpleType>
 *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *               &lt;maxLength value="10"/>
 *             &lt;/restriction>
 *           &lt;/simpleType>
 *         &lt;/element>
 *         &lt;element name="description" minOccurs="0">
 *           &lt;simpleType>
 *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *               &lt;maxLength value="512"/>
 *             &lt;/restriction>
 *           &lt;/simpleType>
 *         &lt;/element>
 *         &lt;element name="beginDate" type="{http://www.w3.org/2001/XMLSchema}date" minOccurs="0"/>
 *         &lt;element name="endDate" type="{http://www.w3.org/2001/XMLSchema}date" minOccurs="0"/>
 *         &lt;element name="linkSaveId" type="{http://www.w3.org/2001/XMLSchema}unsignedShort" minOccurs="0"/>
 *         &lt;element name="minSpendAmount" minOccurs="0">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;sequence>
 *                     &lt;element name="value">
 *                       &lt;simpleType>
 *                         &lt;restriction base="{http://www.w3.org/2001/XMLSchema}decimal">
 *                           &lt;fractionDigits value="4"/>
 *                         &lt;/restriction>
 *                       &lt;/simpleType>
 *                     &lt;/element>
 *                     &lt;element name="currency" minOccurs="0">
 *                       &lt;complexType>
 *                         &lt;complexContent>
 *                           &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                             &lt;sequence>
 *                               &lt;sequence>
 *                                 &lt;element name="code" minOccurs="0">
 *                                   &lt;simpleType>
 *                                     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *                                       &lt;minLength value="3"/>
 *                                       &lt;pattern value="[A-Z][A-Z][A-Z]"/>
 *                                     &lt;/restriction>
 *                                   &lt;/simpleType>
 *                                 &lt;/element>
 *                                 &lt;element name="description" minOccurs="0">
 *                                   &lt;simpleType>
 *                                     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *                                       &lt;maxLength value="80"/>
 *                                     &lt;/restriction>
 *                                   &lt;/simpleType>
 *                                 &lt;/element>
 *                                 &lt;element name="numericCode" minOccurs="0">
 *                                   &lt;simpleType>
 *                                     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *                                       &lt;minLength value="3"/>
 *                                       &lt;pattern value="[0-9][0-9][0-9]"/>
 *                                     &lt;/restriction>
 *                                   &lt;/simpleType>
 *                                 &lt;/element>
 *                               &lt;/sequence>
 *                             &lt;/sequence>
 *                           &lt;/restriction>
 *                         &lt;/complexContent>
 *                       &lt;/complexType>
 *                     &lt;/element>
 *                     &lt;element name="type" minOccurs="0">
 *                       &lt;simpleType>
 *                         &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *                           &lt;maxLength value="10"/>
 *                         &lt;/restriction>
 *                       &lt;/simpleType>
 *                     &lt;/element>
 *                   &lt;/sequence>
 *                 &lt;/sequence>
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *         &lt;element name="maxRepeats" type="{http://www.w3.org/2001/XMLSchema}unsignedShort" minOccurs="0"/>
 *         &lt;element name="type" minOccurs="0">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;sequence>
 *                     &lt;element name="code" type="{http://www.w3.org/2001/XMLSchema}unsignedShort" minOccurs="0"/>
 *                     &lt;element name="description" minOccurs="0">
 *                       &lt;simpleType>
 *                         &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *                           &lt;maxLength value="80"/>
 *                         &lt;/restriction>
 *                       &lt;/simpleType>
 *                     &lt;/element>
 *                   &lt;/sequence>
 *                 &lt;/sequence>
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *         &lt;element name="amount" type="{http://www.w3.org/2001/XMLSchema}decimal" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(
    name = "Promotion",
    propOrder = {
      "id",
      "description",
      "beginDate",
      "endDate",
      "linkSaveId",
      "minSpendAmount",
      "maxRepeats",
      "type",
      "amount"
    })
public class Promotion {

  protected String id;
  protected String description;

  @XmlSchemaType(name = "date")
  protected XMLGregorianCalendar beginDate;

  @XmlSchemaType(name = "date")
  protected XMLGregorianCalendar endDate;

  @XmlSchemaType(name = "unsignedShort")
  protected Integer linkSaveId;

  protected Promotion.MinSpendAmount minSpendAmount;

  @XmlSchemaType(name = "unsignedShort")
  protected Integer maxRepeats;

  protected Promotion.Type type;
  protected BigDecimal amount;

  /**
   * Gets the value of the id property.
   *
   * @return possible object is {@link String }
   */
  public String getId() {
    return id;
  }

  /**
   * Sets the value of the id property.
   *
   * @param value allowed object is {@link String }
   */
  public void setId(String value) {
    this.id = value;
  }

  /**
   * Gets the value of the description property.
   *
   * @return possible object is {@link String }
   */
  public String getDescription() {
    return description;
  }

  /**
   * Sets the value of the description property.
   *
   * @param value allowed object is {@link String }
   */
  public void setDescription(String value) {
    this.description = value;
  }

  /**
   * Gets the value of the beginDate property.
   *
   * @return possible object is {@link XMLGregorianCalendar }
   */
  public XMLGregorianCalendar getBeginDate() {
    return beginDate;
  }

  /**
   * Sets the value of the beginDate property.
   *
   * @param value allowed object is {@link XMLGregorianCalendar }
   */
  public void setBeginDate(XMLGregorianCalendar value) {
    this.beginDate = value;
  }

  /**
   * Gets the value of the endDate property.
   *
   * @return possible object is {@link XMLGregorianCalendar }
   */
  public XMLGregorianCalendar getEndDate() {
    return endDate;
  }

  /**
   * Sets the value of the endDate property.
   *
   * @param value allowed object is {@link XMLGregorianCalendar }
   */
  public void setEndDate(XMLGregorianCalendar value) {
    this.endDate = value;
  }

  /**
   * Gets the value of the linkSaveId property.
   *
   * @return possible object is {@link Integer }
   */
  public Integer getLinkSaveId() {
    return linkSaveId;
  }

  /**
   * Sets the value of the linkSaveId property.
   *
   * @param value allowed object is {@link Integer }
   */
  public void setLinkSaveId(Integer value) {
    this.linkSaveId = value;
  }

  /**
   * Gets the value of the minSpendAmount property.
   *
   * @return possible object is {@link Promotion.MinSpendAmount }
   */
  public Promotion.MinSpendAmount getMinSpendAmount() {
    return minSpendAmount;
  }

  /**
   * Sets the value of the minSpendAmount property.
   *
   * @param value allowed object is {@link Promotion.MinSpendAmount }
   */
  public void setMinSpendAmount(Promotion.MinSpendAmount value) {
    this.minSpendAmount = value;
  }

  /**
   * Gets the value of the maxRepeats property.
   *
   * @return possible object is {@link Integer }
   */
  public Integer getMaxRepeats() {
    return maxRepeats;
  }

  /**
   * Sets the value of the maxRepeats property.
   *
   * @param value allowed object is {@link Integer }
   */
  public void setMaxRepeats(Integer value) {
    this.maxRepeats = value;
  }

  /**
   * Gets the value of the type property.
   *
   * @return possible object is {@link Promotion.Type }
   */
  public Promotion.Type getType() {
    return type;
  }

  /**
   * Sets the value of the type property.
   *
   * @param value allowed object is {@link Promotion.Type }
   */
  public void setType(Promotion.Type value) {
    this.type = value;
  }

  /**
   * Gets the value of the amount property.
   *
   * @return possible object is {@link BigDecimal }
   */
  public BigDecimal getAmount() {
    return amount;
  }

  /**
   * Sets the value of the amount property.
   *
   * @param value allowed object is {@link BigDecimal }
   */
  public void setAmount(BigDecimal value) {
    this.amount = value;
  }

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
   *         &lt;sequence>
   *           &lt;element name="value">
   *             &lt;simpleType>
   *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}decimal">
   *                 &lt;fractionDigits value="4"/>
   *               &lt;/restriction>
   *             &lt;/simpleType>
   *           &lt;/element>
   *           &lt;element name="currency" minOccurs="0">
   *             &lt;complexType>
   *               &lt;complexContent>
   *                 &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
   *                   &lt;sequence>
   *                     &lt;sequence>
   *                       &lt;element name="code" minOccurs="0">
   *                         &lt;simpleType>
   *                           &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
   *                             &lt;minLength value="3"/>
   *                             &lt;pattern value="[A-Z][A-Z][A-Z]"/>
   *                           &lt;/restriction>
   *                         &lt;/simpleType>
   *                       &lt;/element>
   *                       &lt;element name="description" minOccurs="0">
   *                         &lt;simpleType>
   *                           &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
   *                             &lt;maxLength value="80"/>
   *                           &lt;/restriction>
   *                         &lt;/simpleType>
   *                       &lt;/element>
   *                       &lt;element name="numericCode" minOccurs="0">
   *                         &lt;simpleType>
   *                           &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
   *                             &lt;minLength value="3"/>
   *                             &lt;pattern value="[0-9][0-9][0-9]"/>
   *                           &lt;/restriction>
   *                         &lt;/simpleType>
   *                       &lt;/element>
   *                     &lt;/sequence>
   *                   &lt;/sequence>
   *                 &lt;/restriction>
   *               &lt;/complexContent>
   *             &lt;/complexType>
   *           &lt;/element>
   *           &lt;element name="type" minOccurs="0">
   *             &lt;simpleType>
   *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
   *                 &lt;maxLength value="10"/>
   *               &lt;/restriction>
   *             &lt;/simpleType>
   *           &lt;/element>
   *         &lt;/sequence>
   *       &lt;/sequence>
   *     &lt;/restriction>
   *   &lt;/complexContent>
   * &lt;/complexType>
   * </pre>
   */
  @XmlAccessorType(XmlAccessType.FIELD)
  @XmlType(
      name = "",
      propOrder = {"value", "currency", "type"})
  public static class MinSpendAmount {

    @XmlElement(required = true)
    protected BigDecimal value;

    protected Promotion.MinSpendAmount.Currency currency;
    protected String type;

    /**
     * Gets the value of the value property.
     *
     * @return possible object is {@link BigDecimal }
     */
    public BigDecimal getValue() {
      return value;
    }

    /**
     * Sets the value of the value property.
     *
     * @param value allowed object is {@link BigDecimal }
     */
    public void setValue(BigDecimal value) {
      this.value = value;
    }

    /**
     * Gets the value of the currency property.
     *
     * @return possible object is {@link Promotion.MinSpendAmount.Currency }
     */
    public Promotion.MinSpendAmount.Currency getCurrency() {
      return currency;
    }

    /**
     * Sets the value of the currency property.
     *
     * @param value allowed object is {@link Promotion.MinSpendAmount.Currency }
     */
    public void setCurrency(Promotion.MinSpendAmount.Currency value) {
      this.currency = value;
    }

    /**
     * Gets the value of the type property.
     *
     * @return possible object is {@link String }
     */
    public String getType() {
      return type;
    }

    /**
     * Sets the value of the type property.
     *
     * @param value allowed object is {@link String }
     */
    public void setType(String value) {
      this.type = value;
    }

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
     *         &lt;sequence>
     *           &lt;element name="code" minOccurs="0">
     *             &lt;simpleType>
     *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
     *                 &lt;minLength value="3"/>
     *                 &lt;pattern value="[A-Z][A-Z][A-Z]"/>
     *               &lt;/restriction>
     *             &lt;/simpleType>
     *           &lt;/element>
     *           &lt;element name="description" minOccurs="0">
     *             &lt;simpleType>
     *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
     *                 &lt;maxLength value="80"/>
     *               &lt;/restriction>
     *             &lt;/simpleType>
     *           &lt;/element>
     *           &lt;element name="numericCode" minOccurs="0">
     *             &lt;simpleType>
     *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
     *                 &lt;minLength value="3"/>
     *                 &lt;pattern value="[0-9][0-9][0-9]"/>
     *               &lt;/restriction>
     *             &lt;/simpleType>
     *           &lt;/element>
     *         &lt;/sequence>
     *       &lt;/sequence>
     *     &lt;/restriction>
     *   &lt;/complexContent>
     * &lt;/complexType>
     * </pre>
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(
        name = "",
        propOrder = {"code", "description", "numericCode"})
    public static class Currency {

      protected String code;
      protected String description;
      protected String numericCode;

      /**
       * Gets the value of the code property.
       *
       * @return possible object is {@link String }
       */
      public String getCode() {
        return code;
      }

      /**
       * Sets the value of the code property.
       *
       * @param value allowed object is {@link String }
       */
      public void setCode(String value) {
        this.code = value;
      }

      /**
       * Gets the value of the description property.
       *
       * @return possible object is {@link String }
       */
      public String getDescription() {
        return description;
      }

      /**
       * Sets the value of the description property.
       *
       * @param value allowed object is {@link String }
       */
      public void setDescription(String value) {
        this.description = value;
      }

      /**
       * Gets the value of the numericCode property.
       *
       * @return possible object is {@link String }
       */
      public String getNumericCode() {
        return numericCode;
      }

      /**
       * Sets the value of the numericCode property.
       *
       * @param value allowed object is {@link String }
       */
      public void setNumericCode(String value) {
        this.numericCode = value;
      }
    }
  }

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
   *         &lt;sequence>
   *           &lt;element name="code" type="{http://www.w3.org/2001/XMLSchema}unsignedShort" minOccurs="0"/>
   *           &lt;element name="description" minOccurs="0">
   *             &lt;simpleType>
   *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
   *                 &lt;maxLength value="80"/>
   *               &lt;/restriction>
   *             &lt;/simpleType>
   *           &lt;/element>
   *         &lt;/sequence>
   *       &lt;/sequence>
   *     &lt;/restriction>
   *   &lt;/complexContent>
   * &lt;/complexType>
   * </pre>
   */
  @XmlAccessorType(XmlAccessType.FIELD)
  @XmlType(
      name = "",
      propOrder = {"code", "description"})
  public static class Type {

    @XmlSchemaType(name = "unsignedShort")
    protected Integer code;

    protected String description;

    /**
     * Gets the value of the code property.
     *
     * @return possible object is {@link Integer }
     */
    public Integer getCode() {
      return code;
    }

    /**
     * Sets the value of the code property.
     *
     * @param value allowed object is {@link Integer }
     */
    public void setCode(Integer value) {
      this.code = value;
    }

    /**
     * Gets the value of the description property.
     *
     * @return possible object is {@link String }
     */
    public String getDescription() {
      return description;
    }

    /**
     * Sets the value of the description property.
     *
     * @param value allowed object is {@link String }
     */
    public void setDescription(String value) {
      this.description = value;
    }
  }
}
