package com.walmart.oms.infrastructure.gateway.price.dto;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import lombok.ToString;

@XmlRootElement(name = "Error")
@ToString
public class ErrorDetail {

  private String code;
  private String description;
  private String type;

  @XmlElement(name = "Code")
  public String getCode() {
    return code;
  }

  public void setCode(String code) {
    this.code = code;
  }

  @XmlElement(name = "Description")
  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  @XmlElement(name = "Type")
  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }
}
