package com.walmart.fms.infrastructure.integration.gateway.store;

import static com.walmart.util.DateTimeUtil.getTime;

import com.walmart.fms.infrastructure.integration.gateway.store.dto.forceordercancellation.MessageHeader;
import com.walmart.fms.infrastructure.integration.gateway.store.dto.forceordercancellation.UpdateFulfillmentOrderRequest;
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
import com.walmart.fms.infrastructure.integration.jms.PublishToMessagingQueue;
import com.walmart.fms.infrastructure.integration.jms.config.JmsProducerEndpointConfig;
import com.walmart.fms.infrastructure.integration.mapper.OrderDownloadMapper;
import com.walmart.fms.order.aggregateroot.FmsOrder;
import com.walmart.fms.order.domain.entity.FmsOrderItem;
import com.walmart.fms.order.gateway.IStoreGateway;
import com.walmart.util.JAXBContextUtil;
import io.strati.configuration.annotation.ManagedConfiguration;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

@Component
@Slf4j
public class StoreGateway implements IStoreGateway {

  @Autowired OrderDownloadMapper orderDownloadMapper;

  @ManagedConfiguration JmsProducerEndpointConfig jmsProducerEndpointConfig;

  @Autowired PublishToMessagingQueue publishToMessagingQueue;

  @ManagedConfiguration StoreGatewayConfig gatewayConfig;
  private ExecutorService sendToStorePool;

  @Override
  public Future<Boolean> sendMarketPlaceOrderDownloadAsync(FmsOrder fmsOrder) {

    Assert.notNull(fmsOrder, "FmsOrder for download Cannot be empty");

    Callable<Boolean> sendMarketPlaceOrderDownloadTask =
        () -> this.sendMarketPlaceOrderDownload(fmsOrder);
    return sendToStorePool.submit(sendMarketPlaceOrderDownloadTask);
  }

  @Override
  public Boolean sendMarketPlaceOrderDownload(FmsOrder fmsOrder) {
    try {
      if (null != fmsOrder) {
        PlaceFulfillmentOrderRequest fulfillmentOrderRequest =
            constructMarketPlaceOrderDownloadRequestXML(fmsOrder);
        log.info(
            "MarketPlaceOrderDownload's PlaceFulfillmentOrderRequest :: {}",
            marshalXMLtoString(fulfillmentOrderRequest, PlaceFulfillmentOrderRequest.class));
        publishToMessagingQueue.postEventToByEndpointUri(
            jmsProducerEndpointConfig.getgifOrderDownloadConfig().getEndpointUrl(),
            marshalXMLtoString(fulfillmentOrderRequest, PlaceFulfillmentOrderRequest.class));
        return true;
      } else {
        log.error("FmsOrder is null");
        return false;
      }
    } catch (Exception e) {
      log.error("Exception in sending MarketPlaceOrderDownload : ,", e);
      return false;
    }
  }

  @Override
  public Boolean sendMarketPlaceForceOrderCancellation(FmsOrder fmsOrder) {
    try {
      if (null != fmsOrder) {
        UpdateFulfillmentOrderRequest updateFulfillmentOrderRequest =
            constructMarketPlaceForceOrderCancellationRequestXML(fmsOrder);
        if (null != updateFulfillmentOrderRequest) {
          publishToMessagingQueue.postEventToByEndpointUri(
              jmsProducerEndpointConfig.getGifForceOrderCancelConfig().getEndpointUrl(),
              marshalXMLtoString(
                  updateFulfillmentOrderRequest, UpdateFulfillmentOrderRequest.class));
          return true;
        } else {
          log.error(
              "MarketPlace Force OrderCancellation's UpdateFulfillmentOrderRequest is null/empty ");
          return false;
        }
      } else {
        log.error("FmsOrder is null");
        return false;
      }
    } catch (Exception e) {
      log.error("Exception in sending MarketPlace Force OrderCancellation : ", e);
      return false;
    }
  }

  private PlaceFulfillmentOrderRequest constructMarketPlaceOrderDownloadRequestXML(
      FmsOrder fmsOrder) {
    PlaceFulfillmentOrderRequest fulfillmentOrderRequest =
        orderDownloadMapper.toPlaceFulfillmentOrderRequest(fmsOrder);

    Optional.ofNullable(fulfillmentOrderRequest.getMessageHeader())
        .ifPresent(
            messageHeader -> {
              messageHeader.setSubId("SUB-ASDA-PFO-V1");
              messageHeader.setCnsmrId("CON-ASDA-HOS-V1");
              messageHeader.setSrvcNm("PlaceFulfillmentOrder.placeFulfillmentOrder");
              messageHeader.setVersion("2.6");
            });

    Optional.ofNullable(fulfillmentOrderRequest.getMessageBody())
        .ifPresent(messageBody -> buildPlaceFulfillmentOrderRequest(fmsOrder, messageBody));

    return fulfillmentOrderRequest;
  }

  private void buildPlaceFulfillmentOrderRequest(
      FmsOrder fmsOrder, PlaceFulfillmentOrderRequest.MessageBody messageBody) {

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

    PlaceFulfillmentOrderRequest.MessageBody.CustomerOrderInfo.CustomerOrder.FulfillmentOrders
        fulfillmentOrder =
            new PlaceFulfillmentOrderRequest.MessageBody.CustomerOrderInfo.CustomerOrder
                .FulfillmentOrders();
    fulfillmentOrder.setOrderLines(orderLines);
    fulfillmentOrder.setOrderType("GRP");
    fulfillmentOrder.setLoadGroupNumber(BigInteger.valueOf(996));
    fulfillmentOrder.setLoadSequenceNumber(fmsOrder.getSchedulingInfo().getScheduleNumber());
    fulfillmentOrder.setIsPrepaid(true);
    fulfillmentOrder.setCarrierBag(getCarrierBagFromFmsOrder(fmsOrder));

    setFulfilmentOrderDispatchMethod(fulfillmentOrder);
    setFulfilmentOrderDeliveryDetails(fmsOrder, fulfillmentOrder);
    setFulfilmentOrderStatus(fulfillmentOrder);

    Location location = getLocation();
    setFulfilmentOrderNodeId(fmsOrder, fulfillmentOrder, location);
    setFulfilmentOrderPriority(fulfillmentOrder);

    List<PlaceFulfillmentOrderRequest.MessageBody.CustomerOrderInfo.CustomerOrder.FulfillmentOrders>
        fulfillmentOrders = Collections.singletonList(fulfillmentOrder);

    PlaceFulfillmentOrderRequest.MessageBody.CustomerOrderInfo.CustomerOrder customerOrder =
        new PlaceFulfillmentOrderRequest.MessageBody.CustomerOrderInfo.CustomerOrder();
    customerOrder.setFulfillmentOrders(fulfillmentOrders);
    messageBody.getCustomerOrderInfo().setCustomerOrder(customerOrder);

    PlaceFulfillmentOrderRequest.MessageBody.CustomerOrderInfo.CustomerOrder.PaymentDetails
        paymentDetail =
            new PlaceFulfillmentOrderRequest.MessageBody.CustomerOrderInfo.CustomerOrder
                .PaymentDetails();
    PaymentAuthorization paymentAuthorization = new PaymentAuthorization();
    paymentAuthorization.setAuthResultCode("1");
    paymentDetail.setPaymentAuthorization(paymentAuthorization);
    List<PlaceFulfillmentOrderRequest.MessageBody.CustomerOrderInfo.CustomerOrder.PaymentDetails>
        paymentDetails = Collections.singletonList(paymentDetail);
    messageBody.getCustomerOrderInfo().getCustomerOrder().setPaymentDetails(paymentDetails);

    setOriginatingNodeInformation(messageBody, location);

    OrderHeader orderHeader = new OrderHeader();
    orderHeader.setOrderNumber(BigInteger.valueOf(Long.parseLong(fmsOrder.getStoreOrderId())));
    messageBody.getCustomerOrderInfo().getCustomerOrder().setOrderHeader(orderHeader);
  }

  private PlaceFulfillmentOrderRequest.MessageBody.CustomerOrderInfo.CustomerOrder.FulfillmentOrders
          .OrderLines
      buildPfoRequestOrderLine(FmsOrderItem orderItem, long lineNumber) {

    Price price = new Price();
    Amount amount = new Amount();
    amount.setValue(orderItem.getItemUnitPriceAmountValue());
    price.setAmount(amount);

    Product product = new Product();
    product.setId(Long.parseLong(orderItem.getConsumerItemNumber()));
    product.setDescription(orderItem.getCatalogInfo().getPickerItemDescription());
    setRichInformationOfItem(orderItem, product);
    product.setPrice(price);

    List<GlobalTradeItem> globalTradeItems =
        orderItem.getOrderItemUpcs().stream()
            .map(
                upc -> {
                  GlobalTradeItem globalTradeItem = new GlobalTradeItem();
                  globalTradeItem.setGtin(upc);
                  return globalTradeItem;
                })
            .collect(Collectors.toList());
    product.setGlobalTradeItem(globalTradeItems);

    PlaceFulfillmentOrderRequest.MessageBody.CustomerOrderInfo.CustomerOrder.FulfillmentOrders
            .OrderLines
        orderLine =
            new PlaceFulfillmentOrderRequest.MessageBody.CustomerOrderInfo.CustomerOrder
                .FulfillmentOrders.OrderLines();
    orderLine.setProduct(Collections.singletonList(product));
    orderLine.setPrice(price);
    orderLine.setLineNumber(lineNumber);

    Quantity quantity = new Quantity();
    quantity.setUom("EACH");
    quantity.setAmount(BigDecimal.valueOf(orderItem.getQuantity()));
    orderLine.setOrderLineQuantity(quantity);
    orderLine.setIsOrderByQuantity(true);
    orderLine.setIsSubstitutionAllowed(orderItem.isSubstitutionAllowed());
    orderLine.setIsSellbyDateRequired(orderItem.getCatalogInfo().isSellbyDateRequired());
    orderLine.setMinIdealDayQuantity(orderLine.getMinIdealDayQuantity());
    orderLine.setMaxIdealDayQuantity(orderLine.getMaxIdealDayQuantity());
    return orderLine;
  }

  private void setOriginatingNodeInformation(
      PlaceFulfillmentOrderRequest.MessageBody messageBody, Location location) {
    Node originatingNode = new Node();
    originatingNode.setNodeID(4715);
    originatingNode.setLocation(location);
    BaseDivision baseDivision = new BaseDivision();
    baseDivision.setCode("1");
    originatingNode.setBaseDivision(baseDivision);
    messageBody.getCustomerOrderInfo().setOriginatingNode(originatingNode);
  }

  private void setRichInformationOfItem(FmsOrderItem orderItem, Product product) {
    RichInformation richInformation = new RichInformation();
    richInformation.setPictureURL(orderItem.getCatalogInfo().getImageUrl());
    product.setRichInformation(Collections.singletonList(richInformation));
  }

  private Location getLocation() {
    Location location = new Location();
    location.setCountryCode("GB");
    location.setName("ASDA.COM");
    return location;
  }

  private TimeRange getTimeRange(FmsOrder fmsOrder) {
    TimeRange timeRange = new TimeRange();
    timeRange.setStart(
        getTime(
            fmsOrder.isMarketPlaceOrder()
                ? fmsOrder.getSchedulingInfo().getOrderDueTime()
                : fmsOrder.getSchedulingInfo().getSlotEndTime()));
    timeRange.setEnd(
        getTime(
            fmsOrder.isMarketPlaceOrder()
                ? fmsOrder.getSchedulingInfo().getOrderDueTime()
                : fmsOrder.getSchedulingInfo().getSlotEndTime()));
    return timeRange;
  }

  private void setFulfilmentOrderDispatchMethod(
      PlaceFulfillmentOrderRequest.MessageBody.CustomerOrderInfo.CustomerOrder.FulfillmentOrders
          fulfillmentOrder) {
    DispatchMethod dispatchMethod = new DispatchMethod();
    dispatchMethod.setCode("1");
    dispatchMethod.setName("Pickup in store");
    fulfillmentOrder.setDispatchMethod(dispatchMethod);
  }

  private void setFulfilmentOrderDeliveryDetails(
      FmsOrder fmsOrder,
      PlaceFulfillmentOrderRequest.MessageBody.CustomerOrderInfo.CustomerOrder.FulfillmentOrders
          fulfillmentOrder) {
    DeliveryDetails deliveryDetails = new DeliveryDetails();
    deliveryDetails.setDeliveryETA(getTime(fmsOrder.getSchedulingInfo().getOrderDueTime()));
    TimeRange timeRange = getTimeRange(fmsOrder);
    deliveryDetails.setDeliveryScheduleTimeSlot(timeRange);
    deliveryDetails.setShipETA(getTime(fmsOrder.getSchedulingInfo().getOrderDueTime()));
    fulfillmentOrder.setDeliveryDetails(deliveryDetails);
  }

  private void setFulfilmentOrderStatus(
      PlaceFulfillmentOrderRequest.MessageBody.CustomerOrderInfo.CustomerOrder.FulfillmentOrders
          fulfillmentOrder) {
    Status status = new Status();
    status.setCode("OR");
    fulfillmentOrder.setStatus(status);
  }

  private void setFulfilmentOrderNodeId(
      FmsOrder fmsOrder,
      PlaceFulfillmentOrderRequest.MessageBody.CustomerOrderInfo.CustomerOrder.FulfillmentOrders
          fulfillmentOrder,
      Location location) {
    Node node = new Node();
    node.setNodeID(Long.parseLong(null != fmsOrder.getStoreId() ? fmsOrder.getStoreId() : "9999"));
    node.setLocation(location);
    fulfillmentOrder.setNode(node);
  }

  private void setFulfilmentOrderPriority(
      PlaceFulfillmentOrderRequest.MessageBody.CustomerOrderInfo.CustomerOrder.FulfillmentOrders
          fulfillmentOrder) {
    // Marketplace order are set as "600/Express"
    Priority priority = new Priority();
    priority.setCode((long) 600);
    priority.setDescription("Express");
    fulfillmentOrder.setOrderPriority(priority);
  }

  public UpdateFulfillmentOrderRequest constructMarketPlaceForceOrderCancellationRequestXML(
      FmsOrder fmsOrder) {
    UpdateFulfillmentOrderRequest updateFulfillmentOrderRequest = null;

    if (null != fmsOrder) {
      updateFulfillmentOrderRequest = new UpdateFulfillmentOrderRequest();

      // Setting MessageHeader
      MessageHeader header = new MessageHeader();
      header.setSubId("SUB-ASDA-UFO-V1");
      header.setCnsmrId("CON-ASDA-HOS-V1");
      header.setSrvcNm("UpdateFulfillmentOrder.DirectCancel");
      header.setTranId(fmsOrder.getStoreOrderId());
      header.setVersion("2.5");

      updateFulfillmentOrderRequest.setMessageHeader(header);

      // Setting MessageBody
      UpdateFulfillmentOrderRequest.MessageBody messageBody =
          new UpdateFulfillmentOrderRequest.MessageBody();

      UpdateFulfillmentOrderRequest.MessageBody.CustomerOrder customerOrder =
          new UpdateFulfillmentOrderRequest.MessageBody.CustomerOrder();
      com.walmart.fms.infrastructure.integration.gateway.store.dto.forceordercancellation
              .OrderHeader
          orderHeader =
              new com.walmart.fms.infrastructure.integration.gateway.store.dto
                  .forceordercancellation.OrderHeader();
      orderHeader.setOrderNumber(BigInteger.valueOf(Long.parseLong(fmsOrder.getStoreOrderId())));
      customerOrder.setOrderHeader(orderHeader);
      messageBody.setCustomerOrder(customerOrder);

      com.walmart.fms.infrastructure.integration.gateway.store.dto.forceordercancellation.Status
          status =
              new com.walmart.fms.infrastructure.integration.gateway.store.dto
                  .forceordercancellation.Status();
      status.setCode("CAN");
      status.setEventTime(getTime(new Date()));
      status.setDescription("CANCELLED");

      com.walmart.fms.infrastructure.integration.gateway.store.dto.forceordercancellation.Location
          location =
              new com.walmart.fms.infrastructure.integration.gateway.store.dto
                  .forceordercancellation.Location();
      location.setCountryCode("GB");
      com.walmart.fms.infrastructure.integration.gateway.store.dto.forceordercancellation.Node
          node =
              new com.walmart.fms.infrastructure.integration.gateway.store.dto
                  .forceordercancellation.Node();
      node.setNodeID(
          Long.parseLong(
              StringUtils.isEmpty(fmsOrder.getStoreId()) ? "9999" : fmsOrder.getStoreId()));
      node.setLocation(location);

      UpdateFulfillmentOrderRequest.MessageBody.CustomerOrder.FulfillmentOrder fulfillmentOrder =
          new UpdateFulfillmentOrderRequest.MessageBody.CustomerOrder.FulfillmentOrder();
      fulfillmentOrder.setOrderHeader(orderHeader);
      fulfillmentOrder.setOrderType("GRP");
      fulfillmentOrder.setStatus(status);
      fulfillmentOrder.setNode(node);
      List<UpdateFulfillmentOrderRequest.MessageBody.CustomerOrder.FulfillmentOrder>
          fulfillmentOrders = Collections.singletonList(fulfillmentOrder);
      messageBody.getCustomerOrder().setFulfillmentOrder(fulfillmentOrders);

      com.walmart.fms.infrastructure.integration.gateway.store.dto.forceordercancellation.Node
          originatingNode =
              new com.walmart.fms.infrastructure.integration.gateway.store.dto
                  .forceordercancellation.Node();
      originatingNode.setNodeID(Long.parseLong("4715"));
      originatingNode.setLocation(location);

      messageBody.getCustomerOrder().setOriginatingNode(originatingNode);

      updateFulfillmentOrderRequest.setMessageBody(messageBody);
    }

    return updateFulfillmentOrderRequest;
  }

  protected <T> String marshalXMLtoString(Object object, Class<T> classType) {
    StringWriter sw = new StringWriter();
    try {
      JAXBContext ctx = JAXBContextUtil.getJAXBContext(classType);
      Marshaller marshaller = ctx.createMarshaller();
      marshaller.marshal(object, sw);
    } catch (Exception e) {
      log.error("Error in marshaling XML to string :", e);
    }
    return sw.toString();
  }

  private CarrierBag getCarrierBagFromFmsOrder(FmsOrder fmsOrder) {
    CarrierBag carrierBag = new CarrierBag();
    carrierBag.setIsCarrierBagRequested(fmsOrder.hasCarrierBag());
    carrierBag.setIsCarrierBagCountRequired(fmsOrder.hasCarrierBag());
    return carrierBag;
  }

  @PostConstruct
  public void initComponent() {

    this.sendToStorePool =
        new ThreadPoolExecutor(
            gatewayConfig.getPoolCoreSize(),
            gatewayConfig.getPoolMaxSize(),
            gatewayConfig.getPoolKeepAliveTime(),
            TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue<>(gatewayConfig.getPoolQueueSize()),
            new ThreadPoolExecutor.CallerRunsPolicy());
  }

  @PreDestroy
  public void beforeDestroy() {
    if (sendToStorePool != null) {
      sendToStorePool.shutdown();
    }
  }
}
