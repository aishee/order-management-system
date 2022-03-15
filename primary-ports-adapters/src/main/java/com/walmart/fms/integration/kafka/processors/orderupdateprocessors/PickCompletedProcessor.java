package com.walmart.fms.integration.kafka.processors.orderupdateprocessors;

import com.walmart.fms.commands.FmsPickCompleteCommand;
import com.walmart.fms.eventprocessors.FmsPickCompleteCommandService;
import com.walmart.fms.integration.converters.FMSPickCompleteCommandMapper;
import com.walmart.fms.integration.xml.beans.orderpickcomplete.UpdateOrderPickedStatusRequest;
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
public class PickCompletedProcessor implements GIFOrderUpdateEventProcessor {

  @Autowired FmsPickCompleteCommandService fmsPickCompleteCommandService;

  private JAXBContext pickCompletedJaxbContext;

  @PostConstruct
  public void init() {
    pickCompletedJaxbContext = JAXBContextUtil.getJAXBContext(UpdateOrderPickedStatusRequest.class);
  }

  @Override
  public void process(String message) throws JAXBException {
    UpdateOrderPickedStatusRequest updateOrderFulfillmentBeginStatusRequest =
        (UpdateOrderPickedStatusRequest)
            pickCompletedJaxbContext.createUnmarshaller().unmarshal(new StringReader(message));
    FmsPickCompleteCommand fmsPickCompleteCommand =
        FMSPickCompleteCommandMapper.INSTANCE.convertToPickcompleteCommand(
            updateOrderFulfillmentBeginStatusRequest);
    log.info(
        "PickCompletedProcessor Received the Message for STORE_ORDER_ID: {}",
        fmsPickCompleteCommand.getData().getStoreOrderId());
    fmsPickCompleteCommandService.pickCompleteOrder(fmsPickCompleteCommand);
  }
}
