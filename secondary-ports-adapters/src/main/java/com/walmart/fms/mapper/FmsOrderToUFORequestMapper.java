package com.walmart.fms.mapper;

import static com.walmart.util.DateTimeUtil.getTime;

import com.walmart.fms.infrastructure.integration.gateway.store.dto.forceordercancellation.MessageHeader;
import com.walmart.fms.infrastructure.integration.gateway.store.dto.forceordercancellation.UpdateFulfillmentOrderRequest;
import com.walmart.fms.order.aggregateroot.FmsOrder;
import java.math.BigInteger;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

/** An object to object mapper between {@link FmsOrder} and {@link UpdateFulfillmentOrderRequest} */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class FmsOrderToUFORequestMapper {

  public static UpdateFulfillmentOrderRequest map(FmsOrder fmsOrder) {
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
}
