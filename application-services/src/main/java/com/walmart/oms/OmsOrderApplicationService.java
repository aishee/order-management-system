package com.walmart.oms;

import static java.util.Objects.isNull;

import com.walmart.common.domain.type.Currency;
import com.walmart.common.domain.type.Tenant;
import com.walmart.common.domain.type.Vertical;
import com.walmart.oms.commands.CreateOmsOrderCommand;
import com.walmart.oms.commands.CreateOmsOrderCommand.PriceInfo;
import com.walmart.oms.commands.extensions.OrderInfo;
import com.walmart.oms.domain.error.exception.OMSBadRequestException;
import com.walmart.oms.order.aggregateroot.OmsOrder;
import com.walmart.oms.order.domain.OmsOrderDomainService;
import com.walmart.oms.order.domain.entity.OmsOrderItem;
import com.walmart.oms.order.domain.entity.type.Vendor;
import com.walmart.oms.order.factory.OmsOrderFactory;
import com.walmart.oms.order.repository.IOmsOrderRepository;
import com.walmart.oms.order.valueobject.FullName;
import com.walmart.oms.order.valueobject.MobilePhone;
import com.walmart.oms.order.valueobject.Money;
import com.walmart.oms.order.valueobject.TelePhone;
import java.util.List;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

@Component
@Slf4j
public class OmsOrderApplicationService {

  @Autowired
  private OmsOrderFactory omsOrderFactory;

  @Autowired
  private OmsOrderDomainService omsOrderDomainService;

  @Autowired
  private IOmsOrderRepository omsOrderRepository;

  public OmsOrder createOmsOrderFromCommand(CreateOmsOrderCommand createOmsOrderCommand) {

    if (!isNull(createOmsOrderCommand.getMarketPlaceInfo())) {

      OmsOrder existingOmsOrder =
          omsOrderFactory.getOmsOrder(
              createOmsOrderCommand.getVendorOrderId(),
              createOmsOrderCommand.getTenant(),
              createOmsOrderCommand.getVertical());

      if (!existingOmsOrder.isOrderOpen() && existingOmsOrder.isMarketPlaceOrder()) {
        // send error event to market place domain that order cannot be accepted.
        throw new OMSBadRequestException("Order already exists cannot create order");
      }

      OrderInfo orderInfo = createOmsOrderCommand.getOrderInfo();
      String storeOrderId =
          StringUtils.isEmpty(orderInfo.getStoreOrderId())
              ? String.valueOf(Vendor.SequenceGenerator.INSTANCE.nextId())
              : orderInfo.getStoreOrderId();

      OmsOrder omsOrder =
          omsOrderFactory.createOmsOrder(
              orderInfo.getSourceOrderId(),
              storeOrderId,
              orderInfo.getStoreId(),
              orderInfo.getPickupLocationId(),
              orderInfo.getSpokeStoreId(),
              orderInfo.getDeliveryDate(),
              orderInfo.getTenant(),
              orderInfo.getVertical());

      CreateOmsOrderCommand.ContactInfo contactInfo = createOmsOrderCommand.getContactInfo();
      addContactInfoToOrder(contactInfo, omsOrder);
      CreateOmsOrderCommand.SchedulingInfo schedulingInfo =
          createOmsOrderCommand.getSchedulingInfo();
      addSchedulingInfo(schedulingInfo, omsOrder);
      addAddressesToOrder(createOmsOrderCommand, omsOrder);
      CreateOmsOrderCommand.MarketPlaceInfo marketPlaceInfo =
          createOmsOrderCommand.getMarketPlaceInfo();
      addMarketPlaceInfoToOrder(marketPlaceInfo, omsOrder);
      addOrderItemsToOrder(createOmsOrderCommand, omsOrder);
      CreateOmsOrderCommand.PriceInfo priceInfo = createOmsOrderCommand.getPriceInfo();
      addPriceInfoToOrder(priceInfo, omsOrder);

      omsOrderDomainService.processOmsOrder(omsOrder);
      if (omsOrder.isValid()) {
        // Spring application event generator for executing side effects in same domain.
        omsOrderDomainService.publishOrderCreatedDomainEvent(omsOrder);
      }
      return omsOrder;
    }

    throw new OMSBadRequestException("Invalid Create Order Request market place data missing");
  }

  private void addPriceInfoToOrder(PriceInfo priceInfo, OmsOrder omsOrder) {
    if (!isNull(priceInfo)) {
      omsOrder.addPriceInfo(
          omsOrderFactory.createPriceInfo(
              priceInfo.getOrderTotalValue(), priceInfo.getCarrierBagChargeValue()));
    }
  }

  private void addSchedulingInfo(
      CreateOmsOrderCommand.SchedulingInfo schedulingInfo, OmsOrder omsOrder) {

    if (!isNull(schedulingInfo)) {

      omsOrder.addSchedulingInfo(
          omsOrderFactory.createSchedulingInfo(
              omsOrder,
              schedulingInfo.getTripId(),
              schedulingInfo.getDoorStepTime(),
              schedulingInfo.getPlannedDueTime(),
              schedulingInfo.getVanId(),
              schedulingInfo.getScheduleNumber(),
              schedulingInfo.getLoadNumber()));
    }
  }

  public OmsOrder getOrder(String sourceOrderId, Tenant tenant, Vertical vertical) {
    return omsOrderRepository.getOrder(sourceOrderId, tenant, vertical);
  }

  private void addOrderItemsToOrder(
      CreateOmsOrderCommand createOmsOrderCommand, OmsOrder omsOrder) {

    if (!createOmsOrderCommand.getOrderItemInfoList().isEmpty()) {
      createOmsOrderCommand
          .getOrderItemInfoList()
          .forEach(orderItemInfo -> addOmsOrderItem(omsOrder, orderItemInfo));
    }
  }

  private void addOmsOrderItem(
      OmsOrder omsOrder, CreateOmsOrderCommand.OrderItemInfo orderItemInfo) {
    OmsOrderItem omsOrderItem = omsOrderFactory.createOrderedItem(
        omsOrder,
        orderItemInfo.getCin(),
        orderItemInfo.getItemDescription(),
        orderItemInfo.getQuantity(),
        new Money(orderItemInfo.getVendorUnitPrice(), Currency.GBP),
        new Money(orderItemInfo.getVendorTotalPrice(), Currency.GBP),
        orderItemInfo.getSubstitutionOption());

    addBundleItems(omsOrderItem, orderItemInfo.getBundledItemList());
    omsOrder.addItem(omsOrderItem);
  }

  private void addBundleItems(OmsOrderItem omsOrderItem,
                              List<CreateOmsOrderCommand.BundleItem> bundleItems) {
    if (!CollectionUtils.isEmpty(bundleItems)) {
      omsOrderItem.addBundleItems(
          bundleItems.stream().map(bundleItem ->
              omsOrderFactory.createOmsOrderBundleItem(omsOrderItem,
                  bundleItem.getBundleQuantity(),
                  bundleItem.getItemQuantity(),
                  bundleItem.getBundleSkuId(),
                  bundleItem.getBundleInstanceId(),
                  bundleItem.getBundleDescription())
          ).collect(Collectors.toList()));
    }
  }

  private void addMarketPlaceInfoToOrder(
      CreateOmsOrderCommand.MarketPlaceInfo marketPlaceInfo, OmsOrder omsOrder) {
    omsOrder.addMarketPlaceInfo(
        omsOrderFactory.createMarketPlaceInfo(
            marketPlaceInfo.getVendor(), marketPlaceInfo.getVendorOrderId()));
  }

  private void addAddressesToOrder(CreateOmsOrderCommand createOmsOrderCommand, OmsOrder omsOrder) {

    if (!createOmsOrderCommand.getAddressInfoList().isEmpty()) {
      createOmsOrderCommand
          .getAddressInfoList()
          .forEach(addressInfo -> addAddressToOrder(omsOrder, addressInfo));
    }
  }

  private void addContactInfoToOrder(
      CreateOmsOrderCommand.ContactInfo contactInfo, OmsOrder omsOrder) {
    if (!isNull(contactInfo)) {
      omsOrder.addContactInfo(
          omsOrderFactory.createContactInfo(
              omsOrder,
              getFullName(contactInfo),
              getMobileNumber(contactInfo),
              getPhoneNumber(contactInfo.getPhoneNumberOne()),
              getPhoneNumber(contactInfo.getPhoneNumberTwo())));
    }
  }

  private TelePhone getPhoneNumber(String phoneNumberOne) {
    return new TelePhone(phoneNumberOne);
  }

  private MobilePhone getMobileNumber(CreateOmsOrderCommand.ContactInfo contactInfo) {
    return new MobilePhone(contactInfo.getMobileNumber());
  }

  private FullName getFullName(CreateOmsOrderCommand.ContactInfo contactInfo) {
    return new FullName(
        contactInfo.getTitle(),
        contactInfo.getFirstName(),
        contactInfo.getMiddleName(),
        contactInfo.getLastName());
  }

  private void addAddressToOrder(OmsOrder omsOrder, CreateOmsOrderCommand.AddressInfo addressInfo) {

    if (!isNull(addressInfo)) {
      omsOrder.addAddress(
          omsOrderFactory.createAddressInfo(
              omsOrder,
              addressInfo.getAddressOne(),
              addressInfo.getAddressTwo(),
              addressInfo.getAddressThree(),
              addressInfo.getAddressType(),
              addressInfo.getCity(),
              addressInfo.getCountry(),
              addressInfo.getLatitude(),
              addressInfo.getLongitude(),
              addressInfo.getPostalCode(),
              addressInfo.getCounty(),
              addressInfo.getState()));
    }
  }
}
