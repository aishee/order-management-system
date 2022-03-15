package com.walmart.oms.domain.event.messages;

import com.walmart.common.domain.event.processing.Message;
import com.walmart.oms.order.gateway.orderservice.OrdersEvent;
import com.walmart.services.oms.order.common.model.OmsOrder;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class DwhOrderEventMessage implements Message {
  private String storeOrderId;
  private OrdersEvent<OmsOrder> omsOrderOrdersEvent;
}
