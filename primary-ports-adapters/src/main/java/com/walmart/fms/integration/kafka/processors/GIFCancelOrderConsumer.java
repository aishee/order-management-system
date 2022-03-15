package com.walmart.fms.integration.kafka.processors;

import com.walmart.fms.commands.FmsCancelOrderCommand;
import com.walmart.fms.eventprocessors.FmsCancelledCommandService;
import com.walmart.fms.integration.config.FMSKafkaConsumersConfig;
import com.walmart.fms.integration.config.KafkaConsumerConfig;
import com.walmart.fms.integration.converters.FMSStoreCancelOrderCommandMapper;
import com.walmart.fms.integration.xml.beans.cfo.CancelFulfillmentOrderRequest;
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
public class GIFCancelOrderConsumer extends GIFBaseConsumer {

  @Autowired FmsCancelledCommandService fmsCancelledCommandService;
  @ManagedConfiguration private FMSKafkaConsumersConfig fmsKafkaConsumersConfig;
  private JAXBContext orderCancelJaxbContext;
  private static final String GIF_CANCEL_CONSUMER_RETRY = "GIFCANCELCONSUMER";
  @Autowired private GIFErrorQueuePublisher gifErrorQueuePublisher;

  /** Create reactive kafka receiver on initialization. */
  @PostConstruct
  public void init() {
    orderCancelJaxbContext = JAXBContextUtil.getJAXBContext(CancelFulfillmentOrderRequest.class);

    KafkaConsumerConfig kafkaConsumerConfig =
        fmsKafkaConsumersConfig.getOrderCancellationKafkaConsumerConfig();

    initialize(kafkaConsumerConfig);
  }

  /**
   * Cancel order processing using Kafka.
   *
   * @param kafkaMessage
   */
  @Override
  public void accept(ReceiverRecord<String, String> kafkaMessage) throws JAXBException {
    String message = kafkaMessage.value();
    CancelFulfillmentOrderRequest cancelFulfillmentOrderRequest =
        (CancelFulfillmentOrderRequest)
            orderCancelJaxbContext.createUnmarshaller().unmarshal(new StringReader(message));
    FmsCancelOrderCommand fmsCancelOrderCommand =
        FMSStoreCancelOrderCommandMapper.INSTANCE.convertToStoreCancelOrderCommand(
            cancelFulfillmentOrderRequest);
    log.info(
        "{} Received the Message for STORE_ORDER_ID: {}",
        getClassName(),
        fmsCancelOrderCommand.getData().getStoreOrderId());
    fmsCancelledCommandService.cancelOrder(fmsCancelOrderCommand);
  }

  @Override
  public Flux<Boolean> publishToErrorQueue(String msg) {
    return gifErrorQueuePublisher.publishMessageToCancelErrorQueue(msg);
  }

  @Override
  protected String getRetryConsumerName() {
    return GIF_CANCEL_CONSUMER_RETRY;
  }
}
