package com.walmart.fms.commands;

import com.walmart.common.domain.type.SubstitutionOption;
import com.walmart.fms.commands.extensions.OrderInfo;
import com.walmart.marketplace.order.domain.entity.type.Vendor;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@EqualsAndHashCode
public class CreateFmsOrderCommand {

  private FulfillmentOrderData data;

  @Getter
  @Setter
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  @EqualsAndHashCode
  public static class FulfillmentOrderData {

    private OrderInfo orderInfo;

    private MarketPlaceInfo marketPlaceInfo;

    private SchedulingInfo schedulingInfo;

    private OrderTimestamps orderTimestamps;

    private ContactInfo contactinfo;

    private AddressInfo addressInfo;

    private PriceInfo priceInfo;

    private List<FmsItemInfo> items;
  }

  @Getter
  @Setter
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  @EqualsAndHashCode
  public static class MarketPlaceInfo {

    private String vendorOrderId;

    private Vendor vendor;
  }

  @Getter
  @Setter
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  @EqualsAndHashCode
  public static class PriceInfo {

    private double webOrderTotal;

    private double posTotal;

    private double orderVATAmount;

    private double carrierBagCharge;
  }

  @Getter
  @Setter
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  @EqualsAndHashCode
  public static class SchedulingInfo {

    private int doorStepTime;

    private String loadNumber;

    private Date orderDueTime;

    private String vanId;

    private String scheduleNumber;

    private Date slotStartTime;

    private Date slotEndTime;

    private String tripId;
  }

  @Getter
  @Setter
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  @EqualsAndHashCode
  public static class OrderTimestamps {

    private Date pickingStartTime;

    private Date cancelledTime;

    private Date pickCompleteTime;

    private Date shipConfirmTime;

    private Date orderDeliveredTime;

    private Date pickupReadyTime;

    private Date pickupTime;
  }

  @Getter
  @Setter
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  @EqualsAndHashCode
  public static class ContactInfo {

    private String customerId;

    private String email;

    private String firstName;

    private String lastName;

    private String middleName;

    private String mobileNumber;

    private String phoneNumberOne;

    private String phoneNumberTwo;

    private String title;
  }

  @Getter
  @Setter
  @NoArgsConstructor
  @AllArgsConstructor
  @Builder
  @EqualsAndHashCode
  public static class AddressInfo {

    private String addressType;

    private String addressOne;

    private String addressTwo;

    private String addressThree;

    private String city;

    private String county;

    private String country;

    private String latitude;

    private String longitude;

    private String postalCode;

    private String state;
  }

  @Getter
  @Setter
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  @EqualsAndHashCode
  public static class FmsItemInfo {

    private String itemId;

    private String consumerItemNumber;

    private String unitOfMeasurement;

    private String salesUnit;

    private long quantity;

    private long nilPickQty;

    private Integer minIdealDayValue;

    private Integer maxIdealDayValue;

    private boolean isSellbyDateRequired;

    private double weight;

    private BigDecimal unitPrice;

    private String imageURL;

    private String pickerItemDescription;

    private String temperatureZone;

    private List<String> upcs;

    private SubstitutionOption substitutionOption;

    public SubstitutionOption getSubstitutionOption() {
      return Optional.ofNullable(substitutionOption).orElse(SubstitutionOption.DO_NOT_SUBSTITUTE);
    }
  }
}
