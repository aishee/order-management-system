package com.walmart.oms.commands.extensions;

import com.walmart.common.domain.type.Tenant;
import com.walmart.common.domain.type.Vertical;
import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderInfo {

  private Tenant tenant;

  private Vertical vertical;

  private String sourceOrderId;

  private String storeOrderId;

  private String storeId;

  private String spokeStoreId;

  private String pickupLocationId;

  private Date deliveryDate;

  private String authStatus;

  public Tenant getTenant() {
    return tenant;
  }

  public void setTenant(Tenant tenant) {
    this.tenant = tenant;
  }

  public Vertical getVertical() {
    return vertical;
  }

  public void setVertical(Vertical vertical) {
    this.vertical = vertical;
  }

  public String getSourceOrderId() {
    return sourceOrderId;
  }

  public void setSourceOrderId(String sourceOrderId) {
    this.sourceOrderId = sourceOrderId;
  }

  public String getStoreOrderId() {
    return storeOrderId;
  }

  public void setStoreOrderId(String storeOrderId) {
    this.storeOrderId = storeOrderId;
  }

  public String getStoreId() {
    return storeId;
  }

  public void setStoreId(String storeId) {
    this.storeId = storeId;
  }

  public String getSpokeStoreId() {
    return spokeStoreId;
  }

  public void setSpokeStoreId(String spokeStoreId) {
    this.spokeStoreId = spokeStoreId;
  }

  public String getPickupLocationId() {
    return pickupLocationId;
  }

  public void setPickupLocationId(String pickupLocationId) {
    this.pickupLocationId = pickupLocationId;
  }

  public Date getDeliveryDate() {
    return deliveryDate;
  }

  public void setDeliveryDate(Date deliveryDate) {
    this.deliveryDate = deliveryDate;
  }

  public String getAuthStatus() {
    return authStatus;
  }

  public void setAuthStatus(String authStatus) {
    this.authStatus = authStatus;
  }
}
