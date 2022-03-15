package com.walmart.fms.eventprocessors;

import com.walmart.fms.commands.FmsCancelOrderCommand;
import com.walmart.fms.domain.error.exception.FMSBadRequestException;
import com.walmart.fms.order.aggregateroot.FmsOrder;
import com.walmart.fms.order.domain.FmsOrderDomainService;
import com.walmart.fms.order.factory.FmsOrderFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Slf4j
public class FmsCancelledCommandService {

  @Autowired private FmsOrderFactory fmsOrderFactory;

  @Autowired private FmsOrderDomainService fmsOrderDomainService;

  @Transactional
  public FmsOrder cancelOrder(FmsCancelOrderCommand storeCancelledOrderCommand) {

    FmsOrder fmsOrder =
        fmsOrderFactory.getFmsOrderByStoreOrder(
            storeCancelledOrderCommand.getData().getStoreOrderId());

    if (fmsOrder != null && !fmsOrder.isTransientState()) {
      if (fmsOrder.isValidOrderStatusSequence(FmsOrder.OrderStatus.CANCELLED.getName())) {
        fmsOrder =
            fmsOrderDomainService.cancelFmsOrder(
                fmsOrder,
                storeCancelledOrderCommand.getData().getCancelledReasonCode(),
                storeCancelledOrderCommand.getData().getCancellationSource(),
                storeCancelledOrderCommand.getData().getCancelledReasonDescription());
      } else {
        throw new FMSBadRequestException(
            "Received CancelOrder Event But Order:"
                + fmsOrder.getStoreOrderId()
                + " already in: "
                + fmsOrder.getOrderState());
      }
    } else {
      log.error(
          "Order doesn't exist with source order id :{}",
          storeCancelledOrderCommand.getData().getStoreOrderId());
      throw new FMSBadRequestException(
          "Order doesn't exist with store order id "
              + storeCancelledOrderCommand.getData().getStoreOrderId());
    }
    return fmsOrder;
  }
}
