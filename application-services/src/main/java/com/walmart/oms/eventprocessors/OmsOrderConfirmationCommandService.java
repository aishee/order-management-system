package com.walmart.oms.eventprocessors;

import com.walmart.common.domain.messaging.DomainEvent;
import com.walmart.common.domain.messaging.DomainEventPublisher;
import com.walmart.common.domain.type.Domain;
import com.walmart.common.domain.type.DomainEventType;
import com.walmart.common.metrics.MetricService;
import com.walmart.oms.commands.OrderConfirmationCommand;
import com.walmart.oms.domain.error.exception.OMSBadRequestException;
import com.walmart.oms.order.aggregateroot.OmsOrder;
import com.walmart.oms.order.factory.OmsOrderFactory;
import com.walmart.oms.order.repository.IOmsOrderRepository;
import com.walmart.oms.order.valueobject.mappers.OMSOrderToMarketPlaceOrderValueObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Slf4j
public class OmsOrderConfirmationCommandService {

  private static final String DESCRIPTION = "An order has been delivered to customer.";
  private static final String DESTINATION = "OMS_ORDER_UPDATES";
  @Autowired private OmsOrderFactory omsOrderFactory;
  @Autowired private IOmsOrderRepository omsOrderRepository;
  @Autowired private DomainEventPublisher omsDomainEventPublisher;
  @Autowired private MetricService metricService;

  @Transactional
  public OmsOrder orderConfirmedAtStore(OrderConfirmationCommand orderConfirmationCommand) {
    try {
      OmsOrder omsOrder =
          omsOrderFactory.getOmsOrderBySourceOrder(
              orderConfirmationCommand.getSourceOrderId(),
              orderConfirmationCommand.getTenant(),
              orderConfirmationCommand.getVertical());

      if (omsOrder != null && !omsOrder.isTransientState()) {
        if (omsOrder.isOrderStatusUpdatable(OmsOrder.OrderStatus.RECEIVED_AT_STORE.getName())) {
          omsOrder.markOrderAsReceivedAtStore();
          omsOrderRepository.save(omsOrder);

          omsDomainEventPublisher.publish(
              new DomainEvent.EventBuilder(DomainEventType.OMS_ORDER_CONFIRM, DESCRIPTION)
                  .from(Domain.OMS)
                  .to(Domain.MARKETPLACE)
                  .addMessage(
                      OMSOrderToMarketPlaceOrderValueObjectMapper.INSTANCE
                          .convertOmsOrderToMarketPlaceOrderValueObject(omsOrder))
                  .build(),
              DESTINATION);
        } else {
          String message =
              String.format(
                  "Received OrderConfirm Event but Order: %s already in: %s",
                  omsOrder.getStoreOrderId(), omsOrder.getOrderState());
          log.error(message);
          throw new OMSBadRequestException(message);
        }
      } else {
        String message =
            String.format(
                "Order doesn't exist with source order id : %s",
                orderConfirmationCommand.getSourceOrderId());
        log.error(message);
        throw new OMSBadRequestException(message);
      }
      return omsOrder;
    } catch (Exception e) {
      String message =
          String.format(
              "Exception occurred in order : %s", orderConfirmationCommand.getSourceOrderId());
      log.error(message, e);
      String exceptionType = e.getClass().getSimpleName();
      metricService.incrementCounterByType("fms_inbound_exception_counter", exceptionType);
      throw e;
    } finally {
      metricService.incrementCounterByType("inbound_order_count", Domain.FMS.getDomainName());
      MDC.clear();
    }
  }
}
