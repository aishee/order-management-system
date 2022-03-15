package com.walmart.oms.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.walmart.common.domain.valueobject.CancellationDetails;
import com.walmart.marketplace.order.domain.entity.type.Vendor;
import java.io.Serializable;
import java.util.Date;
import java.util.List;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class OmsOrderDto implements Serializable {

  @JsonProperty("external_order_id")
  @Valid
  @NotNull
  private String externalOrderId;

  @JsonProperty("store_id")
  @Valid
  @NotNull
  private String storeId;

  @JsonProperty("spoke_store_id")
  @Valid
  @NotNull
  private String spokeStoreId;

  @JsonProperty("pickup_location_id")
  @Valid
  @NotNull
  private String pickupLocationId;

  @JsonProperty("date_of_delivery")
  @Valid
  @NotNull
  private Date dateOfDelivery;

  @JsonProperty("order_status")
  @Valid
  @NotNull
  private String orderStatus;

  @JsonProperty("delivery_instruction")
  @Valid
  private String deliveryInstruction;

  @JsonProperty("vendor")
  private Vendor vendor;

  @JsonProperty("vendor_order_id")
  private String vendorOrderId;

  @JsonProperty("order_price_info")
  @Valid
  @NotNull
  private OrderPriceInfoDto priceInfo;

  @JsonProperty("order_addresses")
  @Valid
  @NotNull
  private List<OrderAddressInfoDto> addressInfos;

  @JsonProperty("order_contact_info")
  @Valid
  @NotNull
  private OrderContactInfoDto contactInfo;

  @JsonProperty("order_scheduling_info")
  @Valid
  private OrderSchedulingInfoDto orderSchedulingInfo;

  @JsonProperty("order_items")
  @Valid
  @NotNull
  private List<OmsOrderItemDTO> orderItems;

  @JsonProperty("cancel_details")
  private CancellationDetails cancelDetails;

  @JsonProperty("store_order_id")
  @NotNull
  private String storeOrderId;

  @Data
  @Builder
  public static class OrderPriceInfoDto implements Serializable {

    @JsonProperty("order_sub_total")
    @Valid
    @NotNull
    private double orderSubTotal;

    @JsonProperty("pos_total")
    @Valid
    private double posTotal;

    @JsonProperty("delivery_charge")
    @Valid
    private double deliveryCharge;

    @JsonProperty("carrier_bag_charge")
    @Valid
    private double carrierBagCharge;

    @JsonProperty("order_total")
    @Valid
    private double orderTotal;
  }

  @Data
  @Builder
  public static class OrderSchedulingInfoDto implements Serializable {

    @JsonProperty("trip_id")
    @Valid
    @NotNull
    private String tripId;

    @JsonProperty("door_step_time")
    @Valid
    private int doorStepTime;

    @JsonProperty("van_id")
    private String vanId;

    @JsonProperty("schedule_number")
    private String scheduleNumber;

    @JsonProperty("planned_delivery_time")
    private Date plannedDeliveryTime;

    @JsonProperty("load_number")
    private String loadNumber;
  }

  @Data
  @Builder
  public static class OrderContactInfoDto implements Serializable {

    @JsonProperty("customer_id")
    @NotNull
    private String customerId;

    @JsonProperty("first_name")
    @NotNull
    private String firstName;

    @JsonProperty("last_name")
    @NotNull
    private String lastName;

    @JsonProperty("middle_name")
    private String middleName;

    @JsonProperty("phone_number_one")
    private String phoneNumberOne;

    @JsonProperty("phone_number_two")
    private String phoneNumberTwo;

    @JsonProperty("email")
    @NotNull
    private String email;

    @JsonProperty("mobile_number")
    private String mobileNumber;

    @JsonProperty("title")
    private String title;
  }

  @Data
  @Builder
  public static class OrderAddressInfoDto implements Serializable {

    @JsonProperty("address_type")
    @NotNull
    private String addressType;

    @JsonProperty("address_line_one")
    @NotNull
    private String addressOne;

    @JsonProperty("address_line_two")
    @NotNull
    private String addressTwo;

    @JsonProperty("address_line_three")
    @NotNull
    private String addressThree;

    @JsonProperty("city")
    @NotNull
    private String city;

    @JsonProperty("county")
    @NotNull
    private String county;

    @JsonProperty("state")
    @NotNull
    private String state;

    @JsonProperty("postal_code")
    @NotNull
    private String postalCode;

    @JsonProperty("country")
    @NotNull
    private String country;

    @JsonProperty("latitude")
    @NotNull
    private String latitude;

    @JsonProperty("longitude")
    @NotNull
    private String longitude;
  }
}
