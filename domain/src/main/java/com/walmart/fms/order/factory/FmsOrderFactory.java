package com.walmart.fms.order.factory;

import com.walmart.common.domain.type.Currency;
import com.walmart.common.domain.type.SubstitutionOption;
import com.walmart.common.domain.type.Tenant;
import com.walmart.common.domain.type.Vertical;
import com.walmart.fms.order.aggregateroot.FmsOrder;
import com.walmart.fms.order.domain.entity.FmsAddressInfo;
import com.walmart.fms.order.domain.entity.FmsCustomerContactInfo;
import com.walmart.fms.order.domain.entity.FmsOrderItem;
import com.walmart.fms.order.domain.entity.FmsOrderTimestamps;
import com.walmart.fms.order.domain.entity.FmsPickedItem;
import com.walmart.fms.order.domain.entity.FmsPickedItemUpc;
import com.walmart.fms.order.domain.entity.FmsSchedulingInfo;
import com.walmart.fms.order.domain.entity.FmsSubstitutedItem;
import com.walmart.fms.order.domain.entity.FmsSubstitutedItemUpc;
import com.walmart.fms.order.repository.IFmsOrderRepository;
import com.walmart.fms.order.valueobject.FullName;
import com.walmart.fms.order.valueobject.ItemCatalogInfo;
import com.walmart.fms.order.valueobject.ItemPriceInfo;
import com.walmart.fms.order.valueobject.ItemUpcInfo;
import com.walmart.fms.order.valueobject.MarketPlaceInfo;
import com.walmart.fms.order.valueobject.MobilePhone;
import com.walmart.fms.order.valueobject.Money;
import com.walmart.fms.order.valueobject.OrderPriceInfo;
import com.walmart.fms.order.valueobject.Picker;
import com.walmart.fms.order.valueobject.SubstitutedItemPriceInfo;
import com.walmart.fms.order.valueobject.TelePhone;
import com.walmart.marketplace.order.domain.entity.type.Vendor;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

@Component
public class FmsOrderFactory {

  @Autowired
  private IFmsOrderRepository fmsOrderRepository;

  private static final String FMS_ORDER_STATE_INITIAL = "INITIAL";

  public FmsOrder getFmsOrder(String vendorOrderId, Tenant tenant, Vertical vertical) {
    FmsOrder fmsOrder = fmsOrderRepository.getOrderByMarketPlaceId(vendorOrderId, tenant, vertical);
    return fmsOrder != null ? fmsOrder : new FmsOrder(FMS_ORDER_STATE_INITIAL, tenant, vertical);
  }

  public FmsOrder getFmsOrderBySourceOrder(String sourceOrderId, Tenant tenant, Vertical vertical) {

    FmsOrder fmsOrder = fmsOrderRepository.getOrderByMarketPlaceId(sourceOrderId, tenant, vertical);
    return fmsOrder != null ? fmsOrder : new FmsOrder(FMS_ORDER_STATE_INITIAL);
  }

  public FmsOrder getFmsOrderByStoreOrder(String storeOrderId) {

    FmsOrder fmsOrder = fmsOrderRepository.getOrderByStoreOrderId(storeOrderId);
    return fmsOrder != null ? fmsOrder : new FmsOrder(FMS_ORDER_STATE_INITIAL);
  }

  public FmsOrder createFmsOrder(
      String sourceOrderId,
      String storeId,
      String storeOrderId,
      Date deliveryDate,
      Tenant tenant,
      Vertical vertical,
      String pickupLocationId) {

    return FmsOrder.builder()
        .id(fmsOrderRepository.getNextIdentity())
        .storeId(storeId)
        .storeOrderId(storeOrderId)
        .sourceOrderId(sourceOrderId)
        .deliveryDate(deliveryDate)
        .tenant(tenant)
        .vertical(vertical)
        .pickupLocationId(pickupLocationId)
        .build();
  }

  public MarketPlaceInfo createMarketPlaceInfo(Vendor vendor, String vendorOrderId) {

    return MarketPlaceInfo.builder().vendor(vendor).vendorOrderId(vendorOrderId).build();
  }

  public FmsOrderItem createOrderedItem(
      String itemId,
      String consumerItemNumber,
      long quantity,
      double weight,
      Money unitPrice,
      List<String> upcs,
      ItemCatalogInfo catalogInfo,
      FmsOrder fmsOrder,
      SubstitutionOption substitutionOption) {

    FmsOrderItem orderItem =
        FmsOrderItem.builder()
            .id(fmsOrderRepository.getNextIdentity())
            .fmsOrder(fmsOrder)
            .itemId(itemId)
            .consumerItemNumber(consumerItemNumber)
            .quantity(quantity)
            .weight(weight)
            .itemPriceInfo(new ItemPriceInfo(unitPrice))
            .substitutionOption(substitutionOption)
            .build();

    orderItem.addUpcInfo(createItemUpcInfo(upcs));
    orderItem.addCatalogInfo(catalogInfo);
    return orderItem;
  }

  public ItemUpcInfo createItemUpcInfo(List<String> upcs) {
    if (!CollectionUtils.isEmpty(upcs)) {
      return ItemUpcInfo.builder().upcNumbers(upcs).build();
    }

    return null;
  }

  public FmsAddressInfo createAddressInfo(
      FmsOrder fmsOrder,
      String addressOne,
      String addressTwo,
      String addressThree,
      String addressType,
      String city,
      String country,
      String latitude,
      String longitude,
      String postCode,
      String county,
      String state) {
    return FmsAddressInfo.builder()
        .id(fmsOrderRepository.getNextIdentity())
        .order(fmsOrder)
        .addressOne(addressOne)
        .addressTwo(addressTwo)
        .addressThree(addressThree)
        .addressType(addressType)
        .city(city)
        .country(country)
        .latitude(latitude)
        .longitude(longitude)
        .postalCode(postCode)
        .state(state)
        .county(county)
        .build();
  }

  public FmsCustomerContactInfo createContactInfo(
      FmsOrder fmsOrder,
      String customerId,
      FullName fullName,
      MobilePhone mobileNumber,
      TelePhone phoneNumberOne,
      TelePhone phoneNumberTwo) {

    return FmsCustomerContactInfo.builder()
        .id(fmsOrderRepository.getNextIdentity())
        .order(fmsOrder)
        .customerId(customerId)
        .fullName(fullName)
        .phoneNumberOne(phoneNumberOne)
        .phoneNumberTwo(phoneNumberTwo)
        .mobileNumber(mobileNumber)
        .build();
  }

  public FmsSchedulingInfo createSchedulingInfo(
      FmsOrder order,
      String tripId,
      int doorStepTime,
      Date slotStartTime,
      Date slotEndTime,
      Date orderDueTime,
      String vanId,
      String scheduleNumber,
      String loadNumber) {

    return FmsSchedulingInfo.builder()
        .id(fmsOrderRepository.getNextIdentity())
        .order(order)
        .tripId(tripId)
        .doorStepTime(doorStepTime)
        .slotStartTime(slotStartTime)
        .slotEndTime(slotEndTime)
        .orderDueTime(orderDueTime)
        .vanId(vanId)
        .scheduleNumber(scheduleNumber)
        .loadNumber(loadNumber)
        .build();
  }

  public FmsOrderTimestamps createOrderTimestamps(
      FmsOrder order,
      Date pickingStartTime,
      Date cancelledTime,
      Date pickCompleteTime,
      Date shipConfirmTime,
      Date orderDeliveredTime,
      Date pickupReadyTime,
      Date pickupTime) {

    return FmsOrderTimestamps.builder()
        .id(fmsOrderRepository.getNextIdentity())
        .order(order)
        .cancelledTime(cancelledTime)
        .orderDeliveredTime(orderDeliveredTime)
        .pickCompleteTime(pickCompleteTime)
        .pickingStartTime(pickingStartTime)
        .pickupReadyTime(pickupReadyTime)
        .pickupTime(pickupTime)
        .shipConfirmTime(shipConfirmTime)
        .build();
  }

  public FmsPickedItem createPickedItem(
      String description,
      String departmentId,
      String cin,
      String pickedBy,
      List<FmsPickedItemUpc> pickedItemUpcList) {

    FmsPickedItem pickedItem =
        FmsPickedItem.builder()
            .id(fmsOrderRepository.getNextIdentity())
            .departmentID(departmentId)
            .cin(cin)
            .pickedItemDescription(description)
            .picker(new Picker(pickedBy))
            .build();

    pickedItem.addPickedItemUpcList(pickedItemUpcList);

    return pickedItem;
  }

  public FmsPickedItemUpc createPickedItemUpc(
      long pickedQuantity, BigDecimal unitPrice, String uom, String win, String upc) {

    return FmsPickedItemUpc.builder()
        .id(fmsOrderRepository.getNextIdentity())
        .quantity(pickedQuantity)
        .upc(upc)
        .uom(uom)
        .storeUnitPrice(new Money(unitPrice, Currency.GBP))
        .win(win)
        .build();
  }

  public OrderPriceInfo createOrderPriceInfo(double webOrderTotal, double carrierBagCharge) {
    return OrderPriceInfo.builder()
        .webOrderTotal(webOrderTotal)
        .carrierBagCharge(carrierBagCharge)
        .build();
  }

  public FmsSubstitutedItem createSubstitutedItem(String consumerItemNumber,
                                                  String walmartItemNumber,
                                                  String department,
                                                  String description,
                                                  BigDecimal unitPrice,
                                                  Long quantity,
                                                  List<FmsSubstitutedItemUpc> upcs,
                                                  Double weight) {
    return FmsSubstitutedItem.builder()
        .id(fmsOrderRepository.getNextIdentity())
        .consumerItemNumber(consumerItemNumber)
        .walmartItemNumber(walmartItemNumber)
        .department(department)
        .quantity(quantity)
        .weight(weight)
        .substitutedItemPriceInfo(
            buildSubstitutedItemPrice(unitPrice, quantity))
        .description(description)
        .upcs(upcs)
        .build();
  }

  public SubstitutedItemPriceInfo buildSubstitutedItemPrice(BigDecimal unitPrice, Long quantity) {
    return SubstitutedItemPriceInfo.builder().unitPrice(unitPrice)
        .totalPrice(unitPrice.multiply(BigDecimal.valueOf(quantity))).build();
  }

  public FmsSubstitutedItemUpc buildSubstitutedItemUpcs(String upc, String uom) {
    return FmsSubstitutedItemUpc.builder()
        .id(fmsOrderRepository.getNextIdentity())
        .upc(upc)
        .uom(uom)
        .build();
  }
}
