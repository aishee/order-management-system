package com.walmart.fms.integration.kafka.processors.orderupdateprocessors;

import com.walmart.fms.commands.FmsPickStartedOrderCommand;
import com.walmart.fms.eventprocessors.FmsPickStartedCommandService;
import com.walmart.fms.integration.converters.FMSPickStartCommandMapper;
import com.walmart.fms.integration.xml.beans.orderpickbegin.UpdateOrderPickingBeginStatusRequest;
import com.walmart.util.JAXBContextUtil;
import java.io.StringReader;
import javax.annotation.PostConstruct;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class PickStartedProcessor implements GIFOrderUpdateEventProcessor {

  @Autowired FmsPickStartedCommandService fmsPickStartedCommandService;

  private JAXBContext pickStartedJaxbContext;

  @PostConstruct
  public void init() {
    pickStartedJaxbContext =
        JAXBContextUtil.getJAXBContext(UpdateOrderPickingBeginStatusRequest.class);
  }

  @Override
  public void process(String message) throws JAXBException {
    UpdateOrderPickingBeginStatusRequest updateOrderFulfillmentBeginStatusRequest =
        (UpdateOrderPickingBeginStatusRequest)
            pickStartedJaxbContext.createUnmarshaller().unmarshal(new StringReader(message));
    FmsPickStartedOrderCommand fmsPickStartedOrderCommand =
        FMSPickStartCommandMapper.INSTANCE.convertToPickStartedCommand(
            updateOrderFulfillmentBeginStatusRequest);
    log.info(
        "PickStartedProcessor Received the Message for STORE_ORDER_ID: {}",
        fmsPickStartedOrderCommand.getStoreOrderId());
    fmsPickStartedCommandService.orderPickStartedStore(fmsPickStartedOrderCommand);
  }
}
