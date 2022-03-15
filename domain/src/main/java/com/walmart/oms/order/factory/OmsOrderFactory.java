package com.walmart.oms.order.factory;

import com.walmart.common.domain.type.Currency;
import com.walmart.common.domain.type.SubstitutionOption;
import com.walmart.common.domain.type.Tenant;
import com.walmart.common.domain.type.Vertical;
import com.walmart.marketplace.order.domain.entity.type.Vendor;
import com.walmart.oms.order.aggregateroot.OmsOrder;
import com.walmart.oms.order.domain.entity.AddressInfo;
import com.walmart.oms.order.domain.entity.CustomerContactInfo;
import com.walmart.oms.order.domain.entity.OmsOrderBundledItem;
import com.walmart.oms.order.domain.entity.OmsOrderItem;
import com.walmart.oms.order.domain.entity.PickedItem;
import com.walmart.oms.order.domain.entity.PickedItemUpc;
import com.walmart.oms.order.domain.entity.SchedulingInfo;
import com.walmart.oms.order.domain.entity.SubstitutedItem;
import com.walmart.oms.order.domain.entity.SubstitutedItemUpc;
import com.walmart.oms.order.repository.IOmsOrderRepository;
import com.walmart.oms.order.valueobject.FullName;
import com.walmart.oms.order.valueobject.ItemPriceInfo;
import com.walmart.oms.order.valueobject.MarketPlaceInfo;
import com.walmart.oms.order.valueobject.MobilePhone;
import com.walmart.oms.order.valueobject.Money;
import com.walmart.oms.order.valueobject.OrderPriceInfo;
import com.walmart.oms.order.valueobject.Picker;
import com.walmart.oms.order.valueobject.SubstitutedItemPriceInfo;
import com.walmart.oms.order.valueobject.TelePhone;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class OmsOrderFactory {

  @Autowired
  private IOmsOrderRepository omsOrderRepository;

  public OmsOrder getOmsOrder(String vendorOrderId, Tenant tenant, Vertical vertical) {
    OmsOrder omsOrder = omsOrderRepository.getOrderByMarketPlaceId(vendorOrderId, tenant, vertical);
    return omsOrder != null ? omsOrder : new OmsOrder("INITIAL", tenant, vertical);
  }

  public OmsOrder createOmsOrder(
      String sourceOrderId,
      String storeOrderId,
      String storeId,
      String pickUpLocationId,
      String spokeStoreId,
      Date deliveryDate,
      Tenant tenant,
      Vertical vertical) {

    return OmsOrder.builder()
        .id(omsOrderRepository.getNextIdentity())
        .sourceOrderId(sourceOrderId)
        .storeOrderId(storeOrderId)
        .pickupLocationId(pickUpLocationId)
        .spokeStoreId(spokeStoreId)
        .storeId(storeId)
        .deliveryDate(deliveryDate)
        .tenant(tenant)
        .vertical(vertical)
        .build();
  }

  public CustomerContactInfo createContactInfo(
      OmsOrder omsOrder,
      FullName fullName,
      MobilePhone mobileNumber,
      TelePhone phoneNumberOne,
      TelePhone phoneNumberTwo) {

    return CustomerContactInfo.builder()
        .id(omsOrderRepository.getNextIdentity())
        .order(omsOrder)
        .fullName(fullName)
        .phoneNumberOne(phoneNumberOne)
        .phoneNumberTwo(phoneNumberTwo)
        .mobileNumber(mobileNumber)
        .build();
  }

  public AddressInfo createAddressInfo(
      OmsOrder omsOrder,
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
    return AddressInfo.builder()
        .omsOrder(omsOrder)
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

  public MarketPlaceInfo createMarketPlaceInfo(Vendor vendor, String vendorOrderId) {

    return MarketPlaceInfo.builder().vendor(vendor).vendorOrderId(vendorOrderId).build();
  }

  public OmsOrderItem createOrderedItem(
      OmsOrder omsOrder,
      String cin,
      String itemDescription,
      long quantity,
      Money vendorUnitPrice,
      Money vendorTotalPrice,
      SubstitutionOption substitutionOption) {

    return OmsOrderItem.builder()
        .id(omsOrderRepository.getNextIdentity())
        .omsOrder(omsOrder)
        .cin(cin)
        .itemDescription(itemDescription)
        .quantity(quantity)
        .itemPriceInfo(new ItemPriceInfo(vendorUnitPrice, vendorTotalPrice))
        .substitutionOption(substitutionOption)
        .build();
  }

  public OmsOrder getOmsOrderBySourceOrder(String sourceOrderId, Tenant tenant, Vertical vertical) {

    OmsOrder omsOrder = omsOrderRepository.getOrder(sourceOrderId, tenant, vertical);
    return omsOrder != null ? omsOrder : new OmsOrder("INITIAL");
  }

  public PickedItem createPickedItem(
      String description,
      String departmentId,
      String cin,
      String pickedBy,
      List<PickedItemUpc> pickedItemUpcList) {

    return PickedItem.builder()
        .id(omsOrderRepository.getNextIdentity())
        .departmentID(departmentId)
        .orderedCin(cin)
        .pickedItemDescription(description)
        .picker(new Picker(pickedBy))
        .pickedItemUpcList(pickedItemUpcList)
        .build();
  }

  public PickedItemUpc createPickedItemUpc(
      long pickedQuantity, BigDecimal unitPrice, String uom, String win, String upc) {

    return PickedItemUpc.builder()
        .id(omsOrderRepository.getNextIdentity())
        .quantity(pickedQuantity)
        .upc(upc)
        .uom(uom)
        .storeUnitPrice(new Money(unitPrice, Currency.GBP))
        .win(win)
        .build();
  }

  public OmsOrderBundledItem createOmsOrderBundleItem(
      OmsOrderItem omsOrderItem,
      long bundleQuantity,
      long itemQuantity,
      String bundleSkuId,
      String bundleInstanceId,
      String description) {

    return OmsOrderBundledItem.builder()
        .id(omsOrderRepository.getNextIdentity())
        .bundleInstanceId(bundleInstanceId)
        .bundleQuantity(bundleQuantity)
        .itemQuantity(itemQuantity)
        .bundleSkuId(bundleSkuId)
        .omsOrderItem(omsOrderItem)
        .bundleDescription(description)
        .build();
  }

  public SchedulingInfo createSchedulingInfo(
      OmsOrder omsOrder,
      String tripId,
      int doorStepTime,
      Date plannedDueTime,
      String vanId,
      String scheduleNumber,
      String loadNumber) {

    return SchedulingInfo.builder()
        .id(omsOrderRepository.getNextIdentity())
        .doorStepTime(doorStepTime)
        .order(omsOrder)
        .plannedDueTime(plannedDueTime)
        .loadNumber(loadNumber)
        .vanId(vanId)
        .scheduleNumber(scheduleNumber)
        .tripId(tripId)
        .build();
  }

  public OrderPriceInfo createPriceInfo(double orderTotal, double carrierBagCharge) {
    return OrderPriceInfo.builder()
        .orderTotal(orderTotal)
        .carrierBagCharge(carrierBagCharge)
        .build();
  }

  public SubstitutedItemUpc buildSubstitutedItemUpcs(String upc, String uom) {
    return SubstitutedItemUpc.builder()
        .id(omsOrderRepository.getNextIdentity())
        .upc(upc)
        .uom(uom)
        .build();
  }

  public SubstitutedItem createSubstitutedItem(String consumerItemNumber,
                                               String walmartItemNumber,
                                               String department,
                                               String description,
                                               BigDecimal unitPrice,
                                               BigDecimal totalPrice,
                                               Long quantity,
                                               List<SubstitutedItemUpc> substitutedItemUpcList,
                                               Double weight) {
    return SubstitutedItem.builder()
        .id(omsOrderRepository.getNextIdentity())
        .consumerItemNumber(consumerItemNumber)
        .walmartItemNumber(walmartItemNumber)
        .department(department)
        .quantity(quantity)
        .weight(weight)
        .substitutedItemPriceInfo(
            buildSubstitutedItemPrice(unitPrice, totalPrice))
        .description(description)
        .upcs(substitutedItemUpcList)
        .build();

  }

  public SubstitutedItemPriceInfo buildSubstitutedItemPrice(BigDecimal unitPrice, BigDecimal totalPrice) {
    return SubstitutedItemPriceInfo.builder().unitPrice(unitPrice)
        .totalPrice(totalPrice).build();
  }

}
