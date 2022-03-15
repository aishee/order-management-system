package com.walmart.oms.domain.mapper;

import com.walmart.oms.domain.event.messages.DwhOrderEventMessage;
import com.walmart.oms.domain.event.messages.OrderCancelledDomainEventMessage;
import com.walmart.oms.domain.event.messages.OrderCreatedDomainEventMessage;
import com.walmart.oms.domain.event.messages.PickCompleteDomainEventMessage;
import com.walmart.oms.order.aggregateroot.OmsOrder;
import com.walmart.oms.order.gateway.orderservice.OrdersEvent;
import lombok.experimental.UtilityClass;

@UtilityClass
public class OmsDomainToEventMessageMapper {

  /**
   * Mapper method to create pick complete application event message.
   *
   * @param omsOrder Oms Order entity record.
   * @return Pick complete domain event message.
   */
  public static PickCompleteDomainEventMessage mapToPickCompleteDomainEventMessage(
      OmsOrder omsOrder) {
    return PickCompleteDomainEventMessage.builder()
        .sourceOrderId(omsOrder.getSourceOrderId())
        .tenant(omsOrder.getTenant())
        .vertical(omsOrder.getVertical())
        .build();
  }

  public static OrderCreatedDomainEventMessage mapToOrderCreatedDomainEventMessage(
      OmsOrder omsOrder) {
    return OrderCreatedDomainEventMessage.builder()
        .sourceOrderId(omsOrder.getSourceOrderId())
        .tenant(omsOrder.getTenant())
        .vertical(omsOrder.getVertical())
        .build();
  }

  public static OrderCancelledDomainEventMessage mapToOrderCancelledDomainEventMessage(
      OmsOrder omsOrder) {
    return OrderCancelledDomainEventMessage.builder()
        .sourceOrderId(omsOrder.getSourceOrderId())
        .tenant(omsOrder.getTenant())
        .vertical(omsOrder.getVertical())
        .storeId(omsOrder.getStoreId())
        .storeOrderId(omsOrder.getStoreOrderId())
        .vendorOrderId(omsOrder.getVendorOrderId())
        .isCancelOrder(omsOrder.isCancelValid())
        .build();
  }

  public static DwhOrderEventMessage mapToDwhOrderEventMessage(
      OrdersEvent<com.walmart.services.oms.order.common.model.OmsOrder> omsOrderOrdersEvent,
      String storeOrderId) {
    return DwhOrderEventMessage.builder()
        .storeOrderId(storeOrderId)
        .omsOrderOrdersEvent(omsOrderOrdersEvent)
        .build();
  }
}
