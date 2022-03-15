package com.walmart.fms.integration.kafka.processors.orderupdateprocessors;

import com.walmart.fms.commands.FmsOrderConfirmationCommand;
import com.walmart.fms.eventprocessors.FmsOrderConfirmationCommandService;
import com.walmart.fms.integration.converters.FMSOrderConfirmationCommandMapper;
import com.walmart.fms.integration.xml.beans.orderconfirm.UpdateOrderFulfillmentBeginStatusRequest;
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
public class OrderConfirmProcessor implements GIFOrderUpdateEventProcessor {

  @Autowired FmsOrderConfirmationCommandService fmsOrderConfirmationCommandService;

  private JAXBContext orderConfirmJaxbContext;

  @PostConstruct
  public void init() {
    orderConfirmJaxbContext =
        JAXBContextUtil.getJAXBContext(UpdateOrderFulfillmentBeginStatusRequest.class);
  }

  @Override
  public void process(String message) throws JAXBException {
    UpdateOrderFulfillmentBeginStatusRequest updateOrderFulfillmentBeginStatusRequest =
        (UpdateOrderFulfillmentBeginStatusRequest)
            orderConfirmJaxbContext.createUnmarshaller().unmarshal(new StringReader(message));
    FmsOrderConfirmationCommand fmsOrderConfirmationCommand =
        FMSOrderConfirmationCommandMapper.INSTANCE.convertToOrderConfirmation(
            updateOrderFulfillmentBeginStatusRequest);
    log.info(
        "OrderConfirmProcessor Received the Message for STORE_ORDER_ID: {}",
        fmsOrderConfirmationCommand.getStoreOrderId());
    fmsOrderConfirmationCommandService.orderConfirmedAtStore(fmsOrderConfirmationCommand);
  }
}
