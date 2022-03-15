package com.walmart.oms.infrastructure.gateway.price.dto;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.ToString;

@XmlRootElement(name = "UPCDetails")
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UPCDetail {
  private String department = null;
  private long qtySold;
  private String upc = null;
  private String weight = null;
  private String wmItemNum = null;

  @XmlElement(name = "Department")
  public String getDepartment() {
    return department;
  }

  public void setDepartment(String department) {
    this.department = department;
  }

  @XmlElement(name = "QtySold")
  public long getQtySold() {
    return qtySold;
  }

  public void setQtySold(long qtySold) {
    this.qtySold = qtySold;
  }

  @XmlElement(name = "UPC")
  public String getUpc() {
    return upc;
  }

  public void setUpc(String upc) {
    this.upc = upc;
  }

  @XmlElement(name = "Weight")
  public String getWeight() {
    return weight;
  }

  public void setWeight(String weight) {
    this.weight = weight;
  }

  @XmlElement(name = "WmItemNum")
  public String getWmItemNum() {
    return wmItemNum;
  }

  public void setWmItemNum(String wmItemNum) {
    this.wmItemNum = wmItemNum;
  }
}
