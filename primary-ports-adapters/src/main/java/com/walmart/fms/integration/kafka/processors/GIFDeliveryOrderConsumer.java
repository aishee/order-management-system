package com.walmart.fms.integration.kafka.processors;

import com.walmart.fms.commands.FmsDeliveredOrderCommand;
import com.walmart.fms.eventprocessors.FmsDeliveredCommandService;
import com.walmart.fms.integration.config.FMSKafkaConsumersConfig;
import com.walmart.fms.integration.config.KafkaConsumerConfig;
import com.walmart.fms.integration.converters.FMSOrderedDeliverdCommandMapper;
import com.walmart.fms.integration.xml.beans.uods.UpdateOrderDispensedStatusRequest;
import com.walmart.fms.kafka.GIFErrorQueuePublisher;
import com.walmart.util.JAXBContextUtil;
import io.strati.configuration.annotation.ManagedConfiguration;
import java.io.StringReader;
import javax.annotation.PostConstruct;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.kafka.receiver.ReceiverRecord;

@Slf4j
@Service
public class GIFDeliveryOrderConsumer extends GIFBaseConsumer {

  @Autowired FmsDeliveredCommandService fmsDeliveredCommandService;

  @ManagedConfiguration private FMSKafkaConsumersConfig fmsKafkaConsumersConfig;

  @Autowired private GIFErrorQueuePublisher gifErrorQueuePublisher;

  private JAXBContext orderUodsJaxbContext;
  private static final String GIF_DELIVERY_CONSUMER_RETRY = "GIFDELIVERYCONSUMER";

  /** Create reactive kafka receiver on initialization. */
  @PostConstruct
  public void init() {
    orderUodsJaxbContext = JAXBContextUtil.getJAXBContext(UpdateOrderDispensedStatusRequest.class);

    KafkaConsumerConfig kafkaConsumerConfig =
        fmsKafkaConsumersConfig.getOrderDeliveryKafkaConsumerConfig();

    initialize(kafkaConsumerConfig);
  }

  /**
   * order delivery processing using Kafka.
   *
   * @param kafkaMessage
   */
  @Override
  public void accept(ReceiverRecord<String, String> kafkaMessage) throws JAXBException {
    String message = kafkaMessage.value();
    UpdateOrderDispensedStatusRequest updateOrderDispensedStatusRequest =
        (UpdateOrderDispensedStatusRequest)
            orderUodsJaxbContext.createUnmarshaller().unmarshal(new StringReader(message));
    FmsDeliveredOrderCommand fmsDeliveredOrderCommand =
        FMSOrderedDeliverdCommandMapper.INSTANCE.convertToOrderDeliveryCommand(
            updateOrderDispensedStatusRequest);
    log.info(
        "{} Received the Message for STORE_ORDER_ID: {}",
        getClassName(),
        fmsDeliveredOrderCommand.getData().getStoreOrderId());
    fmsDeliveredCommandService.deliverOrder(fmsDeliveredOrderCommand);
  }

  @Override
  public Flux<Boolean> publishToErrorQueue(String msg) {
    return gifErrorQueuePublisher.publishMessageToDeliverErrorQueue(msg);
  }

  @Override
  protected String getRetryConsumerName() {
    return GIF_DELIVERY_CONSUMER_RETRY;
  }
}
