package com.walmart.fms.commands.extensions;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.walmart.common.domain.type.Tenant;
import com.walmart.common.domain.type.Vertical;
import java.util.Date;
import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Builder
@AllArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
public class OrderInfo {

  private Tenant tenant;

  private Vertical vertical;

  private String vendor;

  private String sourceOrderId;

  private String storeOrderId;

  private String storeId;

  private String pickupLocationId;

  private Date deliveryDate;

  private String fulfillmentOrderStatus;

  private String orderStatus;

  private Double webOrderTotal;

  private Double posTotal;

  private Double orderVATAmount;

  private String deliveryInstruction;

  private String cancelledReasonCode;

  private String cancelledReasonDescription;

  private String cancelledBy;

  private String authStatus;

  @JsonIgnore
  public Optional<String> getOptionalCancelledReasonCode() {
    return Optional.ofNullable(cancelledReasonCode);
  }
}
