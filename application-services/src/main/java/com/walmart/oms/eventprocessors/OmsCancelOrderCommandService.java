package com.walmart.oms.eventprocessors;

import com.walmart.oms.commands.OmsCancelOrderCommand;
import com.walmart.oms.commands.mappers.CancellationDetailsMapper;
import com.walmart.oms.domain.error.exception.OMSBadRequestException;
import com.walmart.oms.order.aggregateroot.OmsOrder;
import com.walmart.oms.order.domain.OmsOrderDomainService;
import com.walmart.oms.order.factory.OmsOrderFactory;
import com.walmart.oms.order.valueobject.CancelDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class OmsCancelOrderCommandService {

  private final OmsOrderFactory omsOrderFactory;

  private final OmsOrderDomainService omsOrderDomainService;

  @Transactional
  public OmsOrder cancelOrder(OmsCancelOrderCommand storeCancelledOrderCommand) {
    OmsOrder omsOrder =
        omsOrderFactory.getOmsOrderBySourceOrder(
            storeCancelledOrderCommand.getSourceOrderId(),
            storeCancelledOrderCommand.getTenant(),
            storeCancelledOrderCommand.getVertical());

    validateOrderForCancel(omsOrder, storeCancelledOrderCommand);

    CancelDetails cancelDetails = CancellationDetailsMapper
        .INSTANCE.convertToValueObject(
            storeCancelledOrderCommand.getCancellationDetails());

    omsOrderDomainService.cancelOrderByCancellationSource(omsOrder, cancelDetails);

    return omsOrder;
  }

  private void validateOrderForCancel(OmsOrder omsOrder, OmsCancelOrderCommand storeCancelledOrderCommand) {
    if (omsOrder == null) {
      String message =
          String.format(
              "Order doesn't exist with source order id : %s",
              storeCancelledOrderCommand.getSourceOrderId());
      log.error(message);
      throw new OMSBadRequestException(message);
    } else if (!omsOrder.isOrderStatusUpdatable(OmsOrder.OrderStatus.CANCELLED.getName())) {
      String message =
          String.format(
              "Received Cancel Order Event but Order: %s already in: %s",
              omsOrder.getSourceOrderId(), omsOrder.getOrderState());
      log.error(message);
      throw new OMSBadRequestException(message);
    }
  }

}