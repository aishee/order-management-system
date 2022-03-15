package com.walmart.fms.integration.kafka.processors;

import com.walmart.fms.domain.error.exception.FMSBadRequestException;
import com.walmart.fms.integration.config.FMSKafkaConsumersConfig;
import com.walmart.fms.integration.config.KafkaConsumerConfig;
import com.walmart.fms.integration.kafka.processors.orderupdateprocessors.GIFOrderUpdateEventProcessor;
import com.walmart.fms.integration.kafka.processors.orderupdateprocessors.OrderUpdateProcessorFactory;
import com.walmart.fms.kafka.GIFErrorQueuePublisher;
import io.strati.configuration.annotation.ManagedConfiguration;
import java.io.IOException;
import java.io.StringReader;
import javax.annotation.PostConstruct;
import javax.xml.XMLConstants;
import javax.xml.bind.JAXBException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import reactor.core.publisher.Flux;
import reactor.kafka.receiver.ReceiverRecord;

@Slf4j
@Service
public class GIFOrderUpdateConsumer extends GIFBaseConsumer {

  private static final String HEADER_SRVC_NAME = "hdr:SrvcNm";

  @Autowired OrderUpdateProcessorFactory orderUpdateProcessorFactory;

  @ManagedConfiguration private FMSKafkaConsumersConfig fmsKafkaConsumersConfig;

  @Autowired private GIFErrorQueuePublisher gifErrorQueuePublisher;

  private static final String ERROR_MESSAGE =
      "GIFOrderUpdateConsumer Message " + "Failed While Consuming With The Error";
  private static final String DISABLE_EXTERNAL_DTD =
      "http://apache.org/xml/features/disallow-doctype-decl";
  private static final String EXTERNAL_GENERAL_ENTIITIES =
      "http://xml.org/sax/features/external-general-entities";
  private static final String EXTERNAL_PARAMETER_ENTITIES =
      "http://xml.org/sax/features/external-parameter-entities";
  private static final String LOAD_EXTERNAL_DTD =
      "http://apache.org/xml/features/nonvalidating/load-external-dtd";
  private static final String GIF_UPDATE_CONSUMER_RETRY = "GIFUPDATECONSUMER";

  /** Create reactive kafka receiver on initialization. */
  @PostConstruct
  public void init() {

    KafkaConsumerConfig kafkaConsumerConfig =
        fmsKafkaConsumersConfig.getOrderUpdatesKafkaConsumerConfig();
    initialize(kafkaConsumerConfig);
  }

  /**
   * Update order processing using Kafka.
   *
   * @param kafkaMessage
   */
  @Override
  public void accept(ReceiverRecord<String, String> kafkaMessage) throws JAXBException {
    String message = kafkaMessage.value();
    DocumentBuilderFactory df = DocumentBuilderFactory.newInstance();
    try {
      setDocumentFactoryFeatures(df);
      DocumentBuilder builder = df.newDocumentBuilder();
      InputSource src = new InputSource();
      src.setCharacterStream(new StringReader(message));
      Document doc = builder.parse(src);
      if (doc.getElementsByTagName(HEADER_SRVC_NAME).getLength() == 0) {
        throw new FMSBadRequestException(HEADER_SRVC_NAME + " is not present in the message");
      }
      String result = doc.getElementsByTagName(HEADER_SRVC_NAME).item(0).getTextContent();
      GIFOrderUpdateEventProcessor gifOrderUpdateEventProcessor =
          orderUpdateProcessorFactory.getOrderUpdateEventProcessor(result);
      gifOrderUpdateEventProcessor.process(message);
    } catch (ParserConfigurationException
        | FMSBadRequestException
        | SAXException
        | IOException exception) {
      log.error(ERROR_MESSAGE, exception);
      throw new FMSBadRequestException(exception.getMessage());
    }
  }

  @Override
  public Flux<Boolean> publishToErrorQueue(String msg) {
    return gifErrorQueuePublisher.publishMessageToUpdateErrorQueue(msg);
  }

  @Override
  protected String getRetryConsumerName() {
    return GIF_UPDATE_CONSUMER_RETRY;
  }

  private void setDocumentFactoryFeatures(DocumentBuilderFactory df)
      throws ParserConfigurationException {
    df.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, ""); // Compliant
    df.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, ""); // compliant
    df.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
    df.setFeature(DISABLE_EXTERNAL_DTD, true);
    df.setFeature(EXTERNAL_GENERAL_ENTIITIES, false);
    df.setFeature(EXTERNAL_PARAMETER_ENTITIES, false);
    df.setFeature(LOAD_EXTERNAL_DTD, false);
    df.setXIncludeAware(false);
    df.setExpandEntityReferences(false);
  }
}
