package com.walmart.fms.integration.kafka.processors.orderupdateprocessors;

import com.walmart.fms.domain.error.exception.FMSBadRequestException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class OrderUpdateProcessorFactory {

  @Autowired PickCompletedProcessor pickCompletedProcessor;
  @Autowired PickStartedProcessor pickStartedProcessor;
  @Autowired OrderConfirmProcessor orderConfirmProcessor;

  private static final String ORDER_CONFIRM_HEADER =
      "UpdateOrderFulfillmentStatus.updateOrderFulfillmentBeginStatus";
  private static final String PICK_STARTED =
      "UpdateOrderFulfillmentStatus.updateOrderPickingBeginStatus";
  private static final String PICK_COMPLETED =
      "UpdateOrderFulfillmentStatus.updateOrderPickedStatus";

  public GIFOrderUpdateEventProcessor getOrderUpdateEventProcessor(String result) {

    if (ORDER_CONFIRM_HEADER.equalsIgnoreCase(result)) {
      return orderConfirmProcessor;
    } else if (PICK_COMPLETED.equalsIgnoreCase(result)) {
      return pickCompletedProcessor;
    } else if (PICK_STARTED.equalsIgnoreCase(result)) {
      return pickStartedProcessor;
    }
    throw new FMSBadRequestException("Invalid UpdateOrder Request");
  }
}
