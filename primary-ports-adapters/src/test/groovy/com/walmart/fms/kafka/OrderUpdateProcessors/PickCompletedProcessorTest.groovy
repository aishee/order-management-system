package com.walmart.fms.kafka.OrderUpdateProcessors

import com.walmart.fms.domain.error.exception.FMSBadRequestException
import com.walmart.fms.eventprocessors.FmsPickCompleteCommandService
import com.walmart.fms.integration.kafka.processors.orderupdateprocessors.PickCompletedProcessor
import com.walmart.fms.integration.xml.beans.orderpickcomplete.UpdateOrderPickedStatusRequest
import com.walmart.fms.order.aggregateroot.FmsOrder
import com.walmart.util.JAXBContextUtil
import org.apache.kafka.clients.consumer.ConsumerRecord
import reactor.kafka.receiver.ReceiverRecord
import spock.lang.Specification

import javax.xml.bind.JAXBContext

class PickCompletedProcessorTest extends Specification {
    FmsPickCompleteCommandService fmsPickCompleteCommandService = Mock()
    private JAXBContext pickCompletedJaxbContext
    private PickCompletedProcessor pickCompletedProcessor = new PickCompletedProcessor()
    private String topic = "WMT.UKGR.FULFILLMENT.ORDER_STATUS_UPDATES"

    def setup() {
        pickCompletedJaxbContext =
                JAXBContextUtil.getJAXBContext(UpdateOrderPickedStatusRequest.class)
        pickCompletedProcessor.@fmsPickCompleteCommandService = fmsPickCompleteCommandService
        pickCompletedProcessor.@pickCompletedJaxbContext = pickCompletedJaxbContext
    }

    def testInit() {
        when:
        pickCompletedProcessor.init()
        then:
        noExceptionThrown()
    }

    def testProcess() {
        given:
        String xmlString = this.getClass().getResource('/fms/processors/PickCompleteMsg.xml').text
        ConsumerRecord<String, String> consumerRecord = new ConsumerRecord<>(topic, 1, 123L, "key", xmlString)
        ReceiverRecord<String, String> receiverRecord = new ReceiverRecord<>(consumerRecord, null)
        String message = receiverRecord.value()
        FmsOrder fmsOrder = new FmsOrder()

        when:
        pickCompletedProcessor.process(message)

        then:
        1 * fmsPickCompleteCommandService.pickCompleteOrder(_) >> fmsOrder
        noExceptionThrown()
    }

    def testProcessFailure() {
        given:
        String xmlString = this.getClass().getResource('/fms/processors/PickCompleteMsg.xml').text
        ConsumerRecord<String, String> consumerRecord = new ConsumerRecord<>(topic, 1, 123L, "key", xmlString)
        ReceiverRecord<String, String> receiverRecord = new ReceiverRecord<>(consumerRecord, null)
        String message = receiverRecord.value()
        fmsPickCompleteCommandService.pickCompleteOrder(_) >> { throw new FMSBadRequestException() }

        when:
        pickCompletedProcessor.process(message)

        then:
        thrown(FMSBadRequestException)
    }
}
