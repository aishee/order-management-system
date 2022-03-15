package com.walmart.fms.mapper;

import static com.walmart.util.DateTimeUtil.getTime;

import com.walmart.fms.infrastructure.integration.gateway.store.dto.orderdownload.Amount;
import com.walmart.fms.infrastructure.integration.gateway.store.dto.orderdownload.BaseDivision;
import com.walmart.fms.infrastructure.integration.gateway.store.dto.orderdownload.CarrierBag;
import com.walmart.fms.infrastructure.integration.gateway.store.dto.orderdownload.DeliveryDetails;
import com.walmart.fms.infrastructure.integration.gateway.store.dto.orderdownload.DispatchMethod;
import com.walmart.fms.infrastructure.integration.gateway.store.dto.orderdownload.GlobalTradeItem;
import com.walmart.fms.infrastructure.integration.gateway.store.dto.orderdownload.Location;
import com.walmart.fms.infrastructure.integration.gateway.store.dto.orderdownload.Node;
import com.walmart.fms.infrastructure.integration.gateway.store.dto.orderdownload.OrderHeader;
import com.walmart.fms.infrastructure.integration.gateway.store.dto.orderdownload.PaymentAuthorization;
import com.walmart.fms.infrastructure.integration.gateway.store.dto.orderdownload.PlaceFulfillmentOrderRequest;
import com.walmart.fms.infrastructure.integration.gateway.store.dto.orderdownload.Price;
import com.walmart.fms.infrastructure.integration.gateway.store.dto.orderdownload.Priority;
import com.walmart.fms.infrastructure.integration.gateway.store.dto.orderdownload.Product;
import com.walmart.fms.infrastructure.integration.gateway.store.dto.orderdownload.Quantity;
import com.walmart.fms.infrastructure.integration.gateway.store.dto.orderdownload.RichInformation;
import com.walmart.fms.infrastructure.integration.gateway.store.dto.orderdownload.Status;
import com.walmart.fms.infrastructure.integration.gateway.store.dto.orderdownload.TimeRange;
import com.walmart.fms.infrastructure.integration.mapper.OrderDownloadMapper;
import com.walmart.fms.order.aggregateroot.FmsOrder;
import com.walmart.fms.order.domain.entity.FmsOrderItem;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;

/** An object to object mapper between {@link FmsOrder} and {@link PlaceFulfillmentOrderRequest} */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class FmsOrderToPFORequestMapper {

  public static PlaceFulfillmentOrderRequest map(FmsOrder fmsOrder) {
    PlaceFulfillmentOrderRequest fulfillmentOrderRequest =
        OrderDownloadMapper.INSTANCE.toPlaceFulfillmentOrderRequest(fmsOrder);

    Optional.ofNullable(fulfillmentOrderRequest.getMessageHeader())
        .ifPresent(
            messageHeader -> {
              messageHeader.setSubId("SUB-ASDA-PFO-V1");
              messageHeader.setCnsmrId("CON-ASDA-HOS-V1");
              messageHeader.setSrvcNm("PlaceFulfillmentOrder.placeFulfillmentOrder");
              messageHeader.setVersion("2.6");
            });

    Optional.ofNullable(fulfillmentOrderRequest.getMessageBody())
        .ifPresent(
            messageBody -> {
              // orderLine
              List<
                      PlaceFulfillmentOrderRequest.MessageBody.CustomerOrderInfo.CustomerOrder
                          .FulfillmentOrders.OrderLines>
                  orderLines = new ArrayList<>();

              long lineNumber = 0;
              for (FmsOrderItem orderItem : fmsOrder.getFmsOrderItems()) {
                ++lineNumber;
                orderLines.add(buildPfoRequestOrderLine(orderItem, lineNumber));
              }

              PlaceFulfillmentOrderRequest.MessageBody.CustomerOrderInfo.CustomerOrder
                      .FulfillmentOrders
                  fulfillmentOrder =
                      new PlaceFulfillmentOrderRequest.MessageBody.CustomerOrderInfo.CustomerOrder
                          .FulfillmentOrders();
              fulfillmentOrder.setOrderType("GRP");
              fulfillmentOrder.setLoadGroupNumber(BigInteger.valueOf(996));
              fulfillmentOrder.setLoadSequenceNumber(
                  fmsOrder.getSchedulingInfo().getScheduleNumber());
              fulfillmentOrder.setIsPrepaid(true);

              CarrierBag carrierBag = getCarrierBag(fmsOrder);
              fulfillmentOrder.setCarrierBag(carrierBag);

              DispatchMethod dispatchMethod = getDispatchMethod();
              fulfillmentOrder.setDispatchMethod(dispatchMethod);

              DeliveryDetails deliveryDetails = getDeliveryDetails(fmsOrder);
              fulfillmentOrder.setDeliveryDetails(deliveryDetails);

              Status status = new Status();
              status.setCode("OR");
              fulfillmentOrder.setStatus(status);

              Location location = new Location();
              location.setCountryCode("GB");
              location.setName("ASDA.COM");
              Node node = new Node();
              node.setNodeID(
                  Long.parseLong(null != fmsOrder.getStoreId() ? fmsOrder.getStoreId() : "9999"));
              node.setLocation(location);
              fulfillmentOrder.setNode(node);
              // Marketplace order are set as "600/Express"
              Priority priority = new Priority();
              priority.setCode((long) 600);
              priority.setDescription("Express");
              fulfillmentOrder.setOrderPriority(priority);
              fulfillmentOrder.setOrderLines(orderLines);

              PlaceFulfillmentOrderRequest.MessageBody.CustomerOrderInfo.CustomerOrder
                  customerOrder =
                      new PlaceFulfillmentOrderRequest.MessageBody.CustomerOrderInfo
                          .CustomerOrder();
              customerOrder.setFulfillmentOrders(Collections.singletonList(fulfillmentOrder));
              fulfillmentOrderRequest
                  .getMessageBody()
                  .getCustomerOrderInfo()
                  .setCustomerOrder(customerOrder);

              PlaceFulfillmentOrderRequest.MessageBody.CustomerOrderInfo.CustomerOrder
                      .PaymentDetails
                  paymentDetail =
                      new PlaceFulfillmentOrderRequest.MessageBody.CustomerOrderInfo.CustomerOrder
                          .PaymentDetails();

              PaymentAuthorization paymentAuthorization = new PaymentAuthorization();
              paymentAuthorization.setAuthResultCode("1");
              paymentDetail.setPaymentAuthorization(paymentAuthorization);
              fulfillmentOrderRequest
                  .getMessageBody()
                  .getCustomerOrderInfo()
                  .getCustomerOrder()
                  .setPaymentDetails(Collections.singletonList(paymentDetail));

              Node originatingNode = new Node();
              originatingNode.setNodeID(4715);
              originatingNode.setLocation(location);
              BaseDivision baseDivision = new BaseDivision();
              baseDivision.setCode("1");
              originatingNode.setBaseDivision(baseDivision);
              fulfillmentOrderRequest
                  .getMessageBody()
                  .getCustomerOrderInfo()
                  .setOriginatingNode(originatingNode);

              OrderHeader orderHeader = new OrderHeader();
              orderHeader.setOrderNumber(
                  BigInteger.valueOf(Long.parseLong(fmsOrder.getStoreOrderId())));
              fulfillmentOrderRequest
                  .getMessageBody()
                  .getCustomerOrderInfo()
                  .getCustomerOrder()
                  .setOrderHeader(orderHeader);
            });

    return fulfillmentOrderRequest;
  }

  private static DeliveryDetails getDeliveryDetails(FmsOrder fmsOrder) {
    TimeRange timeRange = new TimeRange();
    timeRange.setStart(getTime(fmsOrder.getSchedulingInfo().getDeliverySlotEndTimeRange()));
    timeRange.setEnd(getTime(fmsOrder.getSchedulingInfo().getDeliverySlotEndTimeRange()));

    DeliveryDetails deliveryDetails = new DeliveryDetails();
    deliveryDetails.setDeliveryETA(getTime(fmsOrder.getSchedulingInfo().getOrderDueTime()));
    deliveryDetails.setDeliveryScheduleTimeSlot(timeRange);
    deliveryDetails.setShipETA(getTime(fmsOrder.getSchedulingInfo().getOrderDueTime()));
    return deliveryDetails;
  }

  private static DispatchMethod getDispatchMethod() {
    DispatchMethod dispatchMethod = new DispatchMethod();
    dispatchMethod.setCode("1");
    dispatchMethod.setName("Pickup in store");
    return dispatchMethod;
  }

  private static CarrierBag getCarrierBag(FmsOrder fmsOrder) {
    CarrierBag carrierBag = new CarrierBag();
    carrierBag.setIsCarrierBagRequested(fmsOrder.hasCarrierBag());
    carrierBag.setIsCarrierBagCountRequired(fmsOrder.hasCarrierBag());
    return carrierBag;
  }

  private static PlaceFulfillmentOrderRequest.MessageBody.CustomerOrderInfo.CustomerOrder
          .FulfillmentOrders.OrderLines
      buildPfoRequestOrderLine(FmsOrderItem orderItem, long lineNumber) {
    Amount amount = new Amount();
    amount.setValue(orderItem.getItemUnitPriceAmountValue());
    Price price = new Price();
    price.setAmount(amount);

    Quantity quantity = new Quantity();
    quantity.setUom("EACH");
    quantity.setAmount(BigDecimal.valueOf(orderItem.getQuantity()));
    PlaceFulfillmentOrderRequest.MessageBody.CustomerOrderInfo.CustomerOrder.FulfillmentOrders
            .OrderLines
        orderLine =
            new PlaceFulfillmentOrderRequest.MessageBody.CustomerOrderInfo.CustomerOrder
                .FulfillmentOrders.OrderLines();
    orderLine.setLineNumber(++lineNumber);
    orderLine.setIsSubstitutionAllowed(false);
    orderLine.setOrderLineQuantity(quantity);
    orderLine.setIsOrderByQuantity(true);

    Product product = getProduct(orderItem, price);
    orderLine.setProduct(Collections.singletonList(product));

    orderLine.setIsSellbyDateRequired(orderItem.getCatalogInfo().isSellbyDateRequired());
    orderLine.setMinIdealDayQuantity(
        orderItem.getMinIdealDayValue() != null
            ? Long.valueOf(orderItem.getMinIdealDayValue())
            : 0);
    orderLine.setMaxIdealDayQuantity(
        orderItem.getMaxIdealDayValue() != null
            ? Long.valueOf(orderItem.getMaxIdealDayValue())
            : 0);
    orderLine.setPrice(price);
    return orderLine;
  }

  private static Product getProduct(FmsOrderItem orderItem, Price price) {
    Product product = new Product();
    product.setId(Long.parseLong(orderItem.getConsumerItemNumber()));
    product.setDescription(orderItem.getCatalogInfo().getPickerItemDescription());
    RichInformation richInformation = new RichInformation();
    richInformation.setPictureURL(orderItem.getCatalogInfo().getImageUrl());
    product.setRichInformation(Collections.singletonList(richInformation));
    product.setPrice(price);

    List<GlobalTradeItem> globalTradeItems = getGlobalTradeItems(orderItem);
    if (!CollectionUtils.isEmpty(globalTradeItems)) {
      product.setGlobalTradeItem(globalTradeItems);
    }
    return product;
  }

  private static List<GlobalTradeItem> getGlobalTradeItems(FmsOrderItem orderItem) {
    return orderItem.getUpcNumbersList().stream()
        .map(
            upc -> {
              GlobalTradeItem globalTradeItem = new GlobalTradeItem();
              globalTradeItem.setGtin(upc);
              return globalTradeItem;
            })
        .collect(Collectors.toList());
  }
}
