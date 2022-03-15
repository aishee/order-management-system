package com.walmart.fms.order.valueobject.events;

import com.walmart.common.domain.type.FulfillmentType;
import com.walmart.common.domain.type.Tenant;
import com.walmart.common.domain.type.Vertical;
import com.walmart.marketplace.order.domain.entity.type.Vendor;
import java.util.Date;
import java.util.List;
import lombok.Data;

@Data
public class FmsOrderValueObject {

  private Tenant tenant;

  private Vertical vertical;

  private String sourceOrderId;

  private String storeId;

  private String spokeStoreId;

  private String pickupLocationId;

  private String storeOrderId;

  private FulfillmentType fulfillmentType;

  private Date deliveryDate;

  private String authStatus;

  private String orderState;

  private OrderPriceInfo priceInfo;

  private MarketPlaceInfo marketPlaceInfo;

  private FmsSchedulingInfoValueObject schedulingInfo;

  private FmsCustomerContactInfoValueObject contactInfo;

  private FmsAddressInfoValueObject addressInfo;

  private List<FmsOrderItemvalueObject> fmsOrderItemvalueObjectList;

  private CancellationDetailsValueObject cancellationDetails;

  @Data
  public static class OrderPriceInfo {

    private double orderSubTotal;

    private double deliveryCharge;

    private double carrierBagCharge;

    private double orderTotal;
  }

  @Data
  public static class MarketPlaceInfo {

    private Vendor vendor;

    private String vendorOrderId;
  }

  public static boolean isValid(FmsOrderValueObject valueObject) {
    return valueObject.storeId != null
        && valueObject.deliveryDate != null
        && valueObject.sourceOrderId != null;
  }

  public static boolean hasValidCancellationDetails(FmsOrderValueObject valueObject) {
    return valueObject.getCancellationDetails() != null
        && valueObject.getCancellationDetails().getCancelledBy() != null
        && valueObject.getCancellationDetails().getCancelledReasonCode() != null;
  }
}
