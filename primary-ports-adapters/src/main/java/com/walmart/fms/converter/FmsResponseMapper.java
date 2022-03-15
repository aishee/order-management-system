package com.walmart.fms.converter;

import static java.util.Objects.isNull;
import static org.apache.commons.collections.CollectionUtils.isEmpty;

import com.walmart.fms.dto.FmsOrderDto;
import com.walmart.fms.dto.FmsOrderItemDTO;
import com.walmart.fms.dto.FmsOrderResponse;
import com.walmart.fms.order.aggregateroot.FmsOrder;
import com.walmart.fms.order.domain.entity.FmsAddressInfo;
import com.walmart.fms.order.domain.entity.FmsCustomerContactInfo;
import com.walmart.fms.order.domain.entity.FmsOrderItem;
import com.walmart.fms.order.domain.entity.FmsOrderTimestamps;
import com.walmart.fms.order.domain.entity.FmsPickedItem;
import com.walmart.fms.order.domain.entity.FmsPickedItemUpc;
import com.walmart.fms.order.domain.entity.FmsSchedulingInfo;
import com.walmart.fms.order.valueobject.Money;
import com.walmart.fms.order.valueobject.PickedItemPriceInfo;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class FmsResponseMapper {

  public FmsOrderResponse convertToOrderResponse(FmsOrder fmsOrder) {

    return FmsOrderResponse.builder()
        .data(
            FmsOrderResponse.FmsOrderResponseData.builder()
                .order(
                    FmsOrderDto.builder()
                        .fulfillmentItems(mapItems(fmsOrder))
                        .contactInfo(mapContactInfo(fmsOrder.getContactInfo()))
                        .orderSchedulingInfo(mapSchedulingInfo(fmsOrder.getSchedulingInfo()))
                        .addressInfo(mapAddressInfo(fmsOrder.getAddressInfo()))
                        .orderTimestamps(mapOrderTimestamps(fmsOrder.getOrderTimestamps()))
                        .externalOrderId(fmsOrder.getSourceOrderId())
                        .vendorOrderId(fmsOrder.getMarketPlaceInfo().getVendorOrderId())
                        .storeOrderId(fmsOrder.getStoreOrderId())
                        .vendorOrderId(fmsOrder.getMarketPlaceInfo().getVendorOrderId())
                        .vendor(fmsOrder.getMarketPlaceInfo().getVendor())
                        .pickupLocationId(fmsOrder.getPickupLocationId())
                        .storeId(fmsOrder.getStoreId())
                        .deliveryDate(fmsOrder.getDeliveryDate())
                        .orderStatus(fmsOrder.getOrderState())
                        .cancelDetails(CancellationDetailsMapper.INSTANCE
                            .convertToDomainObject(fmsOrder.getCancelDetails()))
                        .build())
                .build())
        .build();
  }

  private List<FmsOrderItemDTO> mapItems(FmsOrder fmsOrder) {

    return fmsOrder.getFmsOrderItems().stream().map(this::mapItem).collect(Collectors.toList());
  }

  private FmsOrderItemDTO mapItem(FmsOrderItem fmsOrderItem) {
    if (isNull(fmsOrderItem)) {
      return null;
    } else {
      return FmsOrderItemDTO.builder()
          .consumerItemNumber(fmsOrderItem.getConsumerItemNumber())
          .itemId(fmsOrderItem.getItemId())
          .nilPickQty(fmsOrderItem.getNilPickQuantity())
          .quantity(fmsOrderItem.getQuantity())
          .weight(fmsOrderItem.getWeight())
          .imageURL(fmsOrderItem.getImageUrl())
          .pickerItemDescription(fmsOrderItem.getItemDescription())
          .quantity(fmsOrderItem.getQuantity())
          .salesUnit(fmsOrderItem.getSalesUnit())
          .itemId(fmsOrderItem.getItemId())
          .unitPrice(fmsOrderItem.getUnitPrice())
          .unitOfMeasurement(fmsOrderItem.getUnitOfMeasurement())
          .maxIdealDayValue(fmsOrderItem.getMaxIdealDayValue())
          .minIdealDayValue(fmsOrderItem.getMinIdealDayValue())
          .temperatureZone(fmsOrderItem.getTemparatureZone())
          .isSellbyDateRequired(fmsOrderItem.isSellbyDateRequired())
          .pickedItem(mapPickedItem(fmsOrderItem.getPickedItem()))
          .orderItemUpcs(mapUpcs(fmsOrderItem))
          .substitutionOption(fmsOrderItem.getSubstitutionOption())
          .build();
    }
  }

  private List<FmsOrderItemDTO.FmsOrderItemUpc> mapUpcs(FmsOrderItem fmsOrderItem) {
    return fmsOrderItem.getOrderItemUpcs().stream().map(this::mapUpc).collect(Collectors.toList());
  }

  private FmsOrderItemDTO.FmsOrderItemUpc mapUpc(String upc) {
    return FmsOrderItemDTO.FmsOrderItemUpc.builder().upc(upc).build();
  }

  private FmsOrderDto.OrderContactInfoDto mapContactInfo(FmsCustomerContactInfo contactInfo) {

    if (isNull(contactInfo)) {
      return null;
    } else {
      return FmsOrderDto.OrderContactInfoDto.builder()
          .customerId(contactInfo.getCustomerId())
          .email(contactInfo.getEmailAddr())
          .firstName(contactInfo.getFirstlNameVal())
          .middleName(contactInfo.getMiddleNameVal())
          .lastName(contactInfo.getLastNameVal())
          .phoneNumberOne(contactInfo.getPhoneNoOne())
          .phoneNumberTwo(contactInfo.getPhoneNoTwo())
          .mobileNumber(contactInfo.getMobileNo())
          .title(contactInfo.getCustomerTitle())
          .build();
    }
  }

  private FmsOrderItemDTO.FmsPickedItemDTO mapPickedItem(FmsPickedItem pickedItem) {

    if (isNull(pickedItem)) {
      return null;
    } else {
      Optional<PickedItemPriceInfo> priceInfoOpt =
          Optional.ofNullable(pickedItem.getPickedItemPriceInfo());
      return FmsOrderItemDTO.FmsPickedItemDTO.builder()
          .consumerItemNumber(pickedItem.getCin())
          .departmentID(pickedItem.getDepartmentID())
          .pickedBy(
              !isNull(pickedItem.getPicker()) ? pickedItem.getPicker().getPickerUserName() : null)
          .pickedItemDescription(pickedItem.getPickedItemDescription())
          .quantity(pickedItem.getQuantity())
          .weight(pickedItem.getWeight())
          .sellByDate(pickedItem.getSellByDate())
          .walmartItemNumber(pickedItem.getWalmartItemNumber())
          .unitPrice(
              priceInfoOpt
                  .map(PickedItemPriceInfo::getUnitPrice)
                  .map(Money::getAmount)
                  .orElse(BigDecimal.ZERO))
          .pickedItemUpcs(mapPickedItemUpcs(pickedItem))
          .build();
    }
  }

  private List<FmsOrderItemDTO.FmsPickedItemUpcDTO> mapPickedItemUpcs(FmsPickedItem pickedItem) {
    if (isEmpty(pickedItem.getPickedItemUpcList())) {
      return new ArrayList<>();
    } else {
      return pickedItem.getPickedItemUpcList().stream()
          .map(this::mapPickedItemUpc)
          .collect(Collectors.toList());
    }
  }

  private FmsOrderItemDTO.FmsPickedItemUpcDTO mapPickedItemUpc(FmsPickedItemUpc fmsPickedItemUpc) {
    return FmsOrderItemDTO.FmsPickedItemUpcDTO.builder()
        .quantity(fmsPickedItemUpc.getQuantity())
        .storeUnitPrice(
            !isNull(fmsPickedItemUpc.getStoreUnitPrice())
                ? fmsPickedItemUpc.getStoreUnitPrice().getAmount()
                : BigDecimal.ZERO)
        .unitOfMeasurement(fmsPickedItemUpc.getUom())
        .upc(fmsPickedItemUpc.getUpc())
        .walmartItemNumber(fmsPickedItemUpc.getWin())
        .weight(fmsPickedItemUpc.getWeight())
        .build();
  }

  private FmsOrderDto.OrderSchedulingInfoDto mapSchedulingInfo(FmsSchedulingInfo schedulingInfo) {

    if (isNull(schedulingInfo)) {
      return null;
    } else {
      return FmsOrderDto.OrderSchedulingInfoDto.builder()
          .tripId(schedulingInfo.getTripId())
          .doorStepTime(schedulingInfo.getDoorStepTime())
          .loadNumber(schedulingInfo.getLoadNumber())
          .scheduleNumber(schedulingInfo.getScheduleNumber())
          .vanId(schedulingInfo.getVanId())
          .orderDueTime(schedulingInfo.getOrderDueTime())
          .slotEndTime(schedulingInfo.getSlotEndTime())
          .slotStartTime(schedulingInfo.getSlotStartTime())
          .build();
    }
  }

  private FmsOrderDto.OrderAddressInfoDto mapAddressInfo(FmsAddressInfo addressInfo) {

    if (isNull(addressInfo)) {
      return null;
    } else {

      return FmsOrderDto.OrderAddressInfoDto.builder()
          .addressOne(addressInfo.getAddressOne())
          .addressTwo(addressInfo.getAddressTwo())
          .addressThree(addressInfo.getAddressThree())
          .addressType(addressInfo.getAddressType())
          .city(addressInfo.getCity())
          .country(addressInfo.getCountry())
          .state(addressInfo.getState())
          .latitude(addressInfo.getLatitude())
          .longitude(addressInfo.getLongitude())
          .postalCode(addressInfo.getPostalCode())
          .build();
    }
  }

  private FmsOrderDto.OrderTimestampsDto mapOrderTimestamps(FmsOrderTimestamps orderTimestamps) {

    if (isNull(orderTimestamps)) {
      return null;
    } else {

      return FmsOrderDto.OrderTimestampsDto.builder()
          .cancelledTime(orderTimestamps.getCancelledTime())
          .orderDeliveredTime(orderTimestamps.getOrderDeliveredTime())
          .pickCompleteTime(orderTimestamps.getPickCompleteTime())
          .pickingStartTime(orderTimestamps.getPickingStartTime())
          .pickupReadyTime(orderTimestamps.getPickupReadyTime())
          .pickupTime(orderTimestamps.getPickupTime())
          .shipConfirmTime(orderTimestamps.getShipConfirmTime())
          .build();
    }
  }
}
