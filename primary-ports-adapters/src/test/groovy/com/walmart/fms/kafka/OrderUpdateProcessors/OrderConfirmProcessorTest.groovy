package com.walmart.fms.kafka.OrderUpdateProcessors

import com.walmart.fms.domain.error.exception.FMSBadRequestException
import com.walmart.fms.eventprocessors.FmsOrderConfirmationCommandService
import com.walmart.fms.integration.kafka.processors.orderupdateprocessors.OrderConfirmProcessor
import com.walmart.fms.integration.xml.beans.orderconfirm.UpdateOrderFulfillmentBeginStatusRequest
import com.walmart.fms.order.aggregateroot.FmsOrder
import com.walmart.util.JAXBContextUtil
import org.apache.kafka.clients.consumer.ConsumerRecord
import reactor.kafka.receiver.ReceiverRecord
import spock.lang.Specification

import javax.xml.bind.JAXBContext

class OrderConfirmProcessorTest extends Specification {
    FmsOrderConfirmationCommandService fmsOrderConfirmationCommandService = Mock()
    private JAXBContext orderConfirmJaxbContext
    private OrderConfirmProcessor orderConfirmProcessor = new OrderConfirmProcessor()
    private String topic = "WMT.UKGR.FULFILLMENT.ORDER_STATUS_UPDATES"

    def setup() {
        orderConfirmJaxbContext = JAXBContextUtil.getJAXBContext(UpdateOrderFulfillmentBeginStatusRequest.class)
        orderConfirmProcessor.@fmsOrderConfirmationCommandService = fmsOrderConfirmationCommandService
        orderConfirmProcessor.@orderConfirmJaxbContext = orderConfirmJaxbContext
    }

    def testInit() {
        when:
        orderConfirmProcessor.init()
        then:
        noExceptionThrown()
    }

    def testProcess() {
        given:
        String xmlString = this.getClass().getResource('/fms/processors/OrderUpdateMsg.xml').text
        ConsumerRecord<String, String> consumerRecord = new ConsumerRecord<>(topic, 1, 123L, "key", xmlString)
        ReceiverRecord<String, String> receiverRecord = new ReceiverRecord<>(consumerRecord, null)
        String message = receiverRecord.value()
        FmsOrder fmsOrder = new FmsOrder()

        when:
        orderConfirmProcessor.process(message)

        then:
        1 * fmsOrderConfirmationCommandService.orderConfirmedAtStore(_) >> fmsOrder
        noExceptionThrown()
    }

    def testProcessFailure() {
        given:
        String xmlString = this.getClass().getResource('/fms/processors/OrderUpdateMsg.xml').text
        ConsumerRecord<String, String> consumerRecord = new ConsumerRecord<>(topic, 1, 123L, "key", xmlString)
        ReceiverRecord<String, String> receiverRecord = new ReceiverRecord<>(consumerRecord, null)
        String message = receiverRecord.value()
        fmsOrderConfirmationCommandService.orderConfirmedAtStore(_) >> { throw new FMSBadRequestException() }

        when:
        orderConfirmProcessor.process(message)

        then:
        thrown(FMSBadRequestException)
    }
}
