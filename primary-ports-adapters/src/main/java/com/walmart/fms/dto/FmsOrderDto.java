package com.walmart.fms.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
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
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class FmsOrderDto implements Serializable {

  @Valid @NotNull private String retailCategory;

  @Valid @NotNull private Vendor vendor;

  @Valid @NotNull private String externalOrderId;

  @Valid @NotNull private String vendorOrderId;

  @Valid @NotNull private String storeOrderId;

  @Valid @NotNull private String storeId;

  @Valid @NotNull private String pickupLocationId;

  @Valid private Date deliveryDate;

  @Valid private String fulfillmentOrderStatus;

  @Valid private String orderStatus;

  @Valid private Double webOrderTotal;

  @Valid private Double posTotal;

  @Valid private Double orderVATAmount;

  @Valid private String deliveryInstruction;

  @Valid private String authStatus;

  @Valid @NotNull private OrderPriceInfoDto priceInfo;

  @Valid @NotNull private OrderAddressInfoDto addressInfo;

  @Valid @NotNull private OrderContactInfoDto contactInfo;

  @Valid private OrderSchedulingInfoDto orderSchedulingInfo;

  @Valid private OrderTimestampsDto orderTimestamps;

  @Valid private List<FmsOrderItemDTO> fulfillmentItems;

  private CancellationDetails cancelDetails;

  @Data
  @Builder
  @JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
  public static class OrderPriceInfoDto implements Serializable {

    @NotNull private double webOrderTotal;

    private double posTotal;

    private double orderVATAmount;
  }

  @Data
  @Builder
  @JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
  public static class OrderSchedulingInfoDto implements Serializable {

    @NotNull private String tripId;

    private int doorStepTime;

    private String vanId;

    private String scheduleNumber;

    private Date orderDueTime;

    private Date slotStartTime;

    private Date slotEndTime;

    private String loadNumber;
  }

  @Data
  @Builder
  @JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
  public static class OrderAddressInfoDto implements Serializable {

    @NotNull private String addressType;

    @JsonProperty("address_line_one")
    @NotNull
    private String addressOne;

    @NotNull private String addressTwo;

    @NotNull private String addressThree;

    @NotNull private String city;

    @NotNull private String county;

    @NotNull private String state;

    @NotNull private String postalCode;

    @NotNull private String country;

    @NotNull private String latitude;

    @NotNull private String longitude;
  }

  @Data
  @Builder
  @JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
  public static class OrderContactInfoDto implements Serializable {

    @NotNull private String customerId;

    private String title;

    @NotNull private String firstName;

    @NotNull private String lastName;

    private String middleName;

    private String phoneNumberOne;

    private String phoneNumberTwo;

    @NotNull private String email;

    private String mobileNumber;
  }

  @Data
  @Builder
  @JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
  public static class OrderTimestampsDto implements Serializable {

    private Date pickingStartTime;

    private Date cancelledTime;

    private Date pickCompleteTime;

    private Date shipConfirmTime;

    private Date orderDeliveredTime;

    private Date pickupReadyTime;

    private Date pickupTime;
  }
}
