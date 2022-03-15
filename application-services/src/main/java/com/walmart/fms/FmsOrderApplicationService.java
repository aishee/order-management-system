package com.walmart.fms;

import static java.util.Objects.isNull;

import com.walmart.common.domain.type.Currency;
import com.walmart.common.domain.type.Tenant;
import com.walmart.common.domain.type.Vertical;
import com.walmart.fms.commands.CreateFmsOrderCommand;
import com.walmart.fms.commands.CreateFmsOrderCommand.FulfillmentOrderData;
import com.walmart.fms.commands.CreateFmsOrderCommand.PriceInfo;
import com.walmart.fms.commands.extensions.OrderInfo;
import com.walmart.fms.domain.error.exception.FMSBadRequestException;
import com.walmart.fms.order.aggregateroot.FmsOrder;
import com.walmart.fms.order.domain.FmsOrderDomainService;
import com.walmart.fms.order.domain.entity.FmsAddressInfo;
import com.walmart.fms.order.domain.entity.FmsCustomerContactInfo;
import com.walmart.fms.order.domain.entity.FmsOrderItem;
import com.walmart.fms.order.domain.entity.FmsOrderTimestamps;
import com.walmart.fms.order.domain.entity.FmsSchedulingInfo;
import com.walmart.fms.order.factory.FmsOrderFactory;
import com.walmart.fms.order.repository.IFmsOrderRepository;
import com.walmart.fms.order.valueobject.FullName;
import com.walmart.fms.order.valueobject.ItemCatalogInfo;
import com.walmart.fms.order.valueobject.MobilePhone;
import com.walmart.fms.order.valueobject.Money;
import com.walmart.fms.order.valueobject.OrderPriceInfo;
import com.walmart.fms.order.valueobject.TelePhone;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Slf4j
public class FmsOrderApplicationService {

  @Autowired private IFmsOrderRepository fulfillmentRepository;

  @Autowired private FmsOrderFactory foFactory;

  @Autowired private FmsOrderDomainService fulfillmentDomainService;

  @Transactional
  public FmsOrder createAndProcessFulfillmentOrder(CreateFmsOrderCommand createFmsOrderCommand) {

    if (!isNull(createFmsOrderCommand.getData())
        && !isNull(createFmsOrderCommand.getData().getMarketPlaceInfo())) {

      FmsOrder existingFmsOrder =
          foFactory.getFmsOrder(
              createFmsOrderCommand.getData().getMarketPlaceInfo().getVendorOrderId(),
              createFmsOrderCommand.getData().getOrderInfo().getTenant(),
              createFmsOrderCommand.getData().getOrderInfo().getVertical());

      if (!existingFmsOrder.isOrderOpen() && existingFmsOrder.isMarketPlaceOrder()) {
        throw new FMSBadRequestException("Order already exists cannot create order");
      }

      FmsOrder fmsOrder = createFmsOrder(createFmsOrderCommand);
      return fulfillmentDomainService.processFmsOrder(fmsOrder);
    }

    throw new FMSBadRequestException("Invalid Create Order Request market place data missing");
  }

  private FmsOrder createFmsOrder(CreateFmsOrderCommand createFmsOrderCommand) {
    OrderInfo orderInfo = createFmsOrderCommand.getData().getOrderInfo();
    FmsOrder fmsOrder =
        foFactory.createFmsOrder(
            orderInfo.getSourceOrderId(),
            orderInfo.getStoreId(),
            orderInfo.getStoreOrderId(),
            orderInfo.getDeliveryDate(),
            orderInfo.getTenant(),
            orderInfo.getVertical(),
            orderInfo.getPickupLocationId());

    FulfillmentOrderData fulfillmentOrderData = createFmsOrderCommand.getData();

    addContactInfoToOrder(fulfillmentOrderData, fmsOrder);
    addSchedulingInfoToOrder(fulfillmentOrderData, fmsOrder);
    addOrderTimestampsToOrder(fulfillmentOrderData, fmsOrder);
    addMarketPlaceInfoToOrder(fulfillmentOrderData, fmsOrder);
    addAddressToOrder(fulfillmentOrderData, fmsOrder);
    addOrderItemsToOrder(createFmsOrderCommand, fmsOrder);
    addPriceInfoToOrder(fulfillmentOrderData.getPriceInfo(), fmsOrder);
    return fmsOrder;
  }

  private void addSchedulingInfoToOrder(
      FulfillmentOrderData fulfillmentOrderData, FmsOrder fmsOrder) {
    CreateFmsOrderCommand.SchedulingInfo schedulingInfo = fulfillmentOrderData.getSchedulingInfo();
    if (!isNull(schedulingInfo)) {
      fmsOrder.addSchedulingInfo(getSchedulingInfo(fmsOrder, schedulingInfo));
    }
  }

  private FmsSchedulingInfo getSchedulingInfo(
      FmsOrder fmsOrder, CreateFmsOrderCommand.SchedulingInfo schedulingInfo) {
    return foFactory.createSchedulingInfo(
        fmsOrder,
        schedulingInfo.getTripId(),
        schedulingInfo.getDoorStepTime(),
        schedulingInfo.getSlotStartTime(),
        schedulingInfo.getSlotEndTime(),
        schedulingInfo.getOrderDueTime(),
        schedulingInfo.getVanId(),
        schedulingInfo.getScheduleNumber(),
        schedulingInfo.getLoadNumber());
  }

  private void addOrderTimestampsToOrder(
      FulfillmentOrderData fulfillmentOrderData, FmsOrder fmsOrder) {
    CreateFmsOrderCommand.OrderTimestamps orderTimestamps =
        fulfillmentOrderData.getOrderTimestamps();
    if (!isNull(orderTimestamps)) {
      fmsOrder.addOrderTimestamps(getOrderTimestamps(fmsOrder, orderTimestamps));
    }
  }

  private FmsOrderTimestamps getOrderTimestamps(
      FmsOrder fmsOrder, CreateFmsOrderCommand.OrderTimestamps orderTimestamps) {
    return foFactory.createOrderTimestamps(
        fmsOrder,
        orderTimestamps.getPickingStartTime(),
        orderTimestamps.getCancelledTime(),
        orderTimestamps.getPickCompleteTime(),
        orderTimestamps.getShipConfirmTime(),
        orderTimestamps.getOrderDeliveredTime(),
        orderTimestamps.getPickupReadyTime(),
        orderTimestamps.getPickupTime());
  }

  private void addOrderItemsToOrder(
      CreateFmsOrderCommand createFmsOrderCommand, FmsOrder fulfillmentOrder) {

    if (!isNull(createFmsOrderCommand.getData().getItems())) {
      createFmsOrderCommand
          .getData()
          .getItems()
          .forEach(
              itemInfo -> fulfillmentOrder.addItem(getOrderedItem(fulfillmentOrder, itemInfo)));
    }
  }

  private FmsOrderItem getOrderedItem(
      FmsOrder fulfillmentOrder, CreateFmsOrderCommand.FmsItemInfo itemInfo) {
    return foFactory.createOrderedItem(
        itemInfo.getItemId(),
        itemInfo.getConsumerItemNumber(),
        itemInfo.getQuantity(),
        itemInfo.getWeight(),
        getUnitPrice(itemInfo),
        itemInfo.getUpcs(),
        getCatalogInfo(itemInfo),
        fulfillmentOrder,
        itemInfo.getSubstitutionOption());
  }

  private Money getUnitPrice(CreateFmsOrderCommand.FmsItemInfo itemInfo) {
    return new Money(itemInfo.getUnitPrice(), Currency.GBP);
  }

  private ItemCatalogInfo getCatalogInfo(CreateFmsOrderCommand.FmsItemInfo itemInfo) {
    return new ItemCatalogInfo(
        itemInfo.getSalesUnit(),
        itemInfo.getUnitOfMeasurement(),
        itemInfo.getPickerItemDescription(),
        itemInfo.getImageURL(),
        itemInfo.getMinIdealDayValue(),
        itemInfo.getMaxIdealDayValue(),
        itemInfo.getTemperatureZone(),
        itemInfo.isSellbyDateRequired());
  }

  private void addMarketPlaceInfoToOrder(
      FulfillmentOrderData fulfillmentOrderData, FmsOrder fmsOrder) {
    CreateFmsOrderCommand.MarketPlaceInfo marketPlaceInfo =
        fulfillmentOrderData.getMarketPlaceInfo();
    fmsOrder.addMarketPlaceInfo(
        foFactory.createMarketPlaceInfo(
            marketPlaceInfo.getVendor(), marketPlaceInfo.getVendorOrderId()));
  }

  private void addAddressToOrder(FulfillmentOrderData fulfillmentOrderData, FmsOrder fmsOrder) {
    CreateFmsOrderCommand.AddressInfo addressInfo = fulfillmentOrderData.getAddressInfo();
    if (!isNull(addressInfo)) {
      fmsOrder.addAddressInfo(getAddressInfo(fmsOrder, addressInfo));
    }
  }

  private FmsAddressInfo getAddressInfo(
      FmsOrder fmsOrder, CreateFmsOrderCommand.AddressInfo addressInfo) {
    return foFactory.createAddressInfo(
        fmsOrder,
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
        addressInfo.getState());
  }

  private void addContactInfoToOrder(FulfillmentOrderData fulfillmentOrderData, FmsOrder fmsOrder) {
    CreateFmsOrderCommand.ContactInfo contactInfo = fulfillmentOrderData.getContactinfo();
    if (!isNull(contactInfo)) {
      fmsOrder.addContactInfo(getContactInfo(fmsOrder, contactInfo));
    }
  }

  private FmsCustomerContactInfo getContactInfo(
      FmsOrder fmsOrder, CreateFmsOrderCommand.ContactInfo contactInfo) {
    return foFactory.createContactInfo(
        fmsOrder,
        contactInfo.getCustomerId(),
        getFullName(contactInfo),
        getMobileNumber(contactInfo),
        getPhoneNumber(contactInfo.getPhoneNumberOne()),
        getPhoneNumber(contactInfo.getPhoneNumberTwo()));
  }

  private TelePhone getPhoneNumber(String phoneNumber) {
    return new TelePhone(phoneNumber);
  }

  private MobilePhone getMobileNumber(CreateFmsOrderCommand.ContactInfo contactInfo) {
    return new MobilePhone(contactInfo.getMobileNumber());
  }

  private FullName getFullName(CreateFmsOrderCommand.ContactInfo contactInfo) {
    return new FullName(
        contactInfo.getTitle(),
        contactInfo.getFirstName(),
        contactInfo.getMiddleName(),
        contactInfo.getLastName());
  }

  public FmsOrder getOrder(String sourceOrderId, Tenant tenant, Vertical vertical) {
    return fulfillmentRepository.getOrder(sourceOrderId, tenant, vertical);
  }

  private void addPriceInfoToOrder(PriceInfo priceInfo, FmsOrder fmsOrder) {
    if (!isNull(priceInfo)) {
      fmsOrder.addPriceInfo(getOrderPriceInfo(priceInfo));
    }
  }

  private OrderPriceInfo getOrderPriceInfo(PriceInfo priceInfo) {
    return foFactory.createOrderPriceInfo(
        priceInfo.getWebOrderTotal(), priceInfo.getCarrierBagCharge());
  }
}
