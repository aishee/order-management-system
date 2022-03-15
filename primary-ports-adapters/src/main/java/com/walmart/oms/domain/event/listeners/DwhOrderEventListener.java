package com.walmart.oms.domain.event.listeners;

import com.walmart.common.domain.event.processing.MessageListener;
import com.walmart.oms.domain.error.exception.OMSBadRequestException;
import com.walmart.oms.domain.event.messages.DwhOrderEventMessage;
import com.walmart.oms.infrastructure.configuration.OmsKafkaProducerConfig;
import com.walmart.oms.kafka.OrderDwhMessagePublisher;
import com.walmart.util.JsonConverterUtil;
import io.strati.configuration.annotation.ManagedConfiguration;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class DwhOrderEventListener implements MessageListener<DwhOrderEventMessage> {

  public static final String ORDER_SERVICE_DESTINATION = "order.services.config";
  @ManagedConfiguration private OmsKafkaProducerConfig omsKafkaProducerConfig;
  @Autowired private OrderDwhMessagePublisher orderDwhMessagePublisher;

  @EventListener
  @Override
  @Async
  public void listen(DwhOrderEventMessage dwhOrderEventMessage) {
    try {
      log.info(
          "Received order id event from OMS: {} for orderID: {}",
          dwhOrderEventMessage.getOmsOrderOrdersEvent(),
          dwhOrderEventMessage.getStoreOrderId());

      if (omsKafkaProducerConfig.getConfigForTopic(ORDER_SERVICE_DESTINATION) != null) {
        String message =
            JsonConverterUtil.convertToString(dwhOrderEventMessage.getOmsOrderOrdersEvent());
        orderDwhMessagePublisher
            .publishMessage(
                omsKafkaProducerConfig.getConfigForTopic(ORDER_SERVICE_DESTINATION).getTopic(),
                message)
            .subscribe();
      } else {
        String message =
            String.format(
                "Not able to found the Producer Topic Config for orderID: %s",
                dwhOrderEventMessage.getStoreOrderId());
        log.error(message);
        throw new OMSBadRequestException(message);
      }
    } catch (Exception ex) {
      String message =
          String.format(
              "Exception occurred for orderID: %s", dwhOrderEventMessage.getStoreOrderId());
      log.error(message, ex);
    }
  }
}
