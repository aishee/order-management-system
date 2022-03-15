package com.walmart.oms.converter;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static org.apache.commons.collections.CollectionUtils.isEmpty;

import com.walmart.oms.commands.mappers.CancellationDetailsMapper;
import com.walmart.oms.dto.OmsOrderDto;
import com.walmart.oms.dto.OmsOrderItemDTO;
import com.walmart.oms.dto.OmsOrderResponse;
import com.walmart.oms.order.aggregateroot.OmsOrder;
import com.walmart.oms.order.domain.entity.AddressInfo;
import com.walmart.oms.order.domain.entity.CustomerContactInfo;
import com.walmart.oms.order.domain.entity.OmsOrderItem;
import com.walmart.oms.order.domain.entity.PickedItem;
import com.walmart.oms.order.domain.entity.PickedItemUpc;
import com.walmart.oms.order.domain.entity.SchedulingInfo;
import com.walmart.oms.order.valueobject.Money;
import com.walmart.oms.order.valueobject.OrderPriceInfo;
import com.walmart.oms.order.valueobject.PickedItemPriceInfo;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class OmsResponseMapper {

  public OmsOrderResponse convertToOrderResponse(OmsOrder omsOrder) {

    if (nonNull(omsOrder)) {

      return OmsOrderResponse.builder()
          .data(
              OmsOrderResponse.OmsOrderResponseData.builder()
                  .order(
                      OmsOrderDto.builder()
                          .contactInfo(mapContactInfo(omsOrder.getContactInfo()))
                          .orderSchedulingInfo(mapSchedulingInfo(omsOrder.getSchedulingInfo()))
                          .addressInfos(mapAddresses(omsOrder))
                          .orderItems(mapItems(omsOrder))
                          .externalOrderId(omsOrder.getSourceOrderId())
                          .storeOrderId(omsOrder.getStoreOrderId())
                          .vendorOrderId(omsOrder.getVendorOrderId())
                          .vendor(omsOrder.getVendor())
                          .pickupLocationId(omsOrder.getPickupLocationId())
                          .storeId(omsOrder.getStoreId())
                          .priceInfo(mapPriceInfo(omsOrder.getPriceInfo()))
                          .spokeStoreId(omsOrder.getSpokeStoreId())
                          .orderStatus(omsOrder.getOrderState())
                          .cancelDetails(CancellationDetailsMapper.INSTANCE
                              .convertToDomainObject(omsOrder.getCancelDetails()))
                          .build())
                  .build())
          .build();
    }
    return null;
  }

  private OmsOrderDto.OrderPriceInfoDto mapPriceInfo(OrderPriceInfo priceInfo) {

    if (isNull(priceInfo)) {
      return null;
    } else {
      return OmsOrderDto.OrderPriceInfoDto.builder()
          .orderSubTotal(priceInfo.getOrderSubTotal())
          .posTotal(priceInfo.getPosTotal())
          .carrierBagCharge(priceInfo.getCarrierBagCharge())
          .deliveryCharge(priceInfo.getDeliveryCharge())
          .orderTotal(priceInfo.getOrderTotal())
          .build();
    }
  }

  private List<OmsOrderItemDTO> mapItems(OmsOrder omsOrder) {

    return omsOrder.getOrderItemList().stream().map(this::mapItem).collect(Collectors.toList());
  }

  private OmsOrderItemDTO mapItem(OmsOrderItem omsOrderItem) {

    if (isNull(omsOrderItem)) {
      return null;
    } else {
      return OmsOrderItemDTO.builder()
          .consumerItemNumber(omsOrderItem.getCin())
          .imageURL(omsOrderItem.getImageUrl())
          .itemDescription(omsOrderItem.getItemDescription())
          .quantity(omsOrderItem.getQuantity())
          .salesUnit(omsOrderItem.getSalesUnit())
          .skuId(omsOrderItem.getSkuId())
          .unitPrice(omsOrderItem.getOrderedItemUnitPriceAmount())
          .vendorUnitPrice(omsOrderItem.getVendorUnitPriceAmount())
          .vendorTotalPrice(omsOrderItem.getVendorTotalPriceAmount())
          .weight(omsOrderItem.getWeight())
          .uom(omsOrderItem.getUom())
          .orderItemUpcs(mapUpcs(omsOrderItem))
          .pickedItem(mapPickedItem(omsOrderItem.getPickedItem()).orElse(null))
          .substitutionOption(omsOrderItem.getSubstitutionOption())
          .build();
    }
  }

  private Optional<OmsOrderItemDTO.PickedItemDto> mapPickedItem(PickedItem pickedItem) {

    if (isNull(pickedItem)) {
      return Optional.empty();
    } else {
      Optional<PickedItemPriceInfo> priceInfoOpt =
          Optional.ofNullable(pickedItem.getPickedItemPriceInfo());
      OmsOrderItemDTO.PickedItemDto omsOrderItemDTO =
          OmsOrderItemDTO.PickedItemDto.builder()
              .departmentId(pickedItem.getDepartmentID())
              .consumerItemNum(pickedItem.getOrderedCin())
              .quantity(pickedItem.getQuantity())
              .weight(pickedItem.getWeight())
              .pickedItemDescription(pickedItem.getPickedItemDescription())
              .pickedBy(pickedItem.getPickedByUser().orElse(null))
              .unitPrice(
                  priceInfoOpt
                      .map(PickedItemPriceInfo::getUnitPrice)
                      .map(Money::getAmount)
                      .orElse(BigDecimal.ZERO))
              .adjustedPrice(
                  priceInfoOpt
                      .map(PickedItemPriceInfo::getAdjustedPrice)
                      .map(Money::getAmount)
                      .orElse(BigDecimal.ZERO))
              .adjustedPriceExVat(
                  priceInfoOpt
                      .map(PickedItemPriceInfo::getAdjustedPriceExVat)
                      .map(Money::getAmount)
                      .orElse(BigDecimal.ZERO))
              .webAdjustedPrice(
                  priceInfoOpt
                      .map(PickedItemPriceInfo::getWebAdjustedPrice)
                      .map(Money::getAmount)
                      .orElse(BigDecimal.ZERO))
              .vatAmount(
                  priceInfoOpt
                      .map(PickedItemPriceInfo::getVatAmount)
                      .map(Money::getAmount)
                      .orElse(BigDecimal.ZERO))
              .pickedItemUpcs(mapPickedItemUpcs(pickedItem))
              .build();
      return Optional.of(omsOrderItemDTO);
    }
  }

  private List<OmsOrderItemDTO.PickedItemUpcDto> mapPickedItemUpcs(PickedItem pickedItem) {
    return pickedItem.getPickedItemUpcList().stream()
        .map(this::mapPickedItemUpc)
        .collect(Collectors.toList());
  }

  private OmsOrderItemDTO.PickedItemUpcDto mapPickedItemUpc(PickedItemUpc pickedItemUpc) {
    return OmsOrderItemDTO.PickedItemUpcDto.builder()
        .quantity(pickedItemUpc.getQuantity())
        .storeUnitPrice(pickedItemUpc.getUnitPriceAmount().orElse(BigDecimal.ZERO))
        .unitOfMeasurement(pickedItemUpc.getUom())
        .upc(pickedItemUpc.getUpc())
        .walmartItemNumber(pickedItemUpc.getWin())
        .weight(pickedItemUpc.getWeight())
        .build();
  }

  private List<OmsOrderItemDTO.OmsOrderItemUpc> mapUpcs(OmsOrderItem omsOrderItem) {

    if (isEmpty(omsOrderItem.getOrderItemUpcs())) {
      return new ArrayList<>();
    } else {
      return omsOrderItem.getOrderItemUpcs().stream()
          .map(this::mapUpc)
          .collect(Collectors.toList());
    }
  }

  private OmsOrderItemDTO.OmsOrderItemUpc mapUpc(String upc) {
    return OmsOrderItemDTO.OmsOrderItemUpc.builder().upc(upc).build();
  }

  private List<OmsOrderDto.OrderAddressInfoDto> mapAddresses(OmsOrder omsOrder) {

    if (isEmpty(omsOrder.getAddresses())) {
      return new ArrayList<>();
    } else {
      return omsOrder.getAddresses().stream()
          .map(this::mapAddressInfo)
          .collect(Collectors.toList());
    }
  }

  private OmsOrderDto.OrderContactInfoDto mapContactInfo(CustomerContactInfo contactInfo) {

    if (isNull(contactInfo)) {
      return null;
    } else {
      return OmsOrderDto.OrderContactInfoDto.builder()
          .customerId(contactInfo.getFullName().getTitle())
          .email(!isNull(contactInfo.getEmail()) ? contactInfo.getEmail().getAddress() : null)
          .firstName(contactInfo.getFirstName())
          .middleName(contactInfo.getMiddleName())
          .lastName(contactInfo.getLastName())
          .phoneNumberOne(contactInfo.getRefPhoneNumberOne())
          .phoneNumberTwo(contactInfo.getRefPhoneNumberTwo())
          .mobileNumber(contactInfo.getRefMobileNumber())
          .title(!isNull(contactInfo.getEmail()) ? contactInfo.getFullName().getTitle() : null)
          .build();
    }
  }

  private OmsOrderDto.OrderSchedulingInfoDto mapSchedulingInfo(SchedulingInfo schedulingInfo) {

    if (isNull(schedulingInfo)) {
      return null;
    } else {
      return OmsOrderDto.OrderSchedulingInfoDto.builder()
          .tripId(schedulingInfo.getTripId())
          .doorStepTime(schedulingInfo.getDoorStepTime())
          .loadNumber(schedulingInfo.getLoadNumber())
          .scheduleNumber(schedulingInfo.getScheduleNumber())
          .plannedDeliveryTime(schedulingInfo.getPlannedDueTime())
          .build();
    }
  }

  private OmsOrderDto.OrderAddressInfoDto mapAddressInfo(AddressInfo addressInfo) {

    if (isNull(addressInfo)) {
      return null;
    } else {

      return OmsOrderDto.OrderAddressInfoDto.builder()
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
}
