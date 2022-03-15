package com.walmart.fms.kafka.OrderUpdateProcessors

import com.walmart.fms.domain.error.exception.FMSBadRequestException
import com.walmart.fms.eventprocessors.FmsPickStartedCommandService
import com.walmart.fms.integration.kafka.processors.orderupdateprocessors.PickStartedProcessor
import com.walmart.fms.integration.xml.beans.orderpickbegin.UpdateOrderPickingBeginStatusRequest
import com.walmart.fms.order.aggregateroot.FmsOrder
import com.walmart.util.JAXBContextUtil
import org.apache.kafka.clients.consumer.ConsumerRecord
import reactor.kafka.receiver.ReceiverRecord
import spock.lang.Specification

import javax.xml.bind.JAXBContext

class PickStartedProcessorTest extends Specification {
    FmsPickStartedCommandService fmsPickStartedCommandService = Mock()
    private JAXBContext pickStartedJaxbContext
    private PickStartedProcessor pickStartedProcessor = new PickStartedProcessor()
    private String topic = "WMT.UKGR.FULFILLMENT.ORDER_STATUS_UPDATES"

    def setup() {
        pickStartedJaxbContext =
                JAXBContextUtil.getJAXBContext(UpdateOrderPickingBeginStatusRequest.class)
        pickStartedProcessor.@fmsPickStartedCommandService = fmsPickStartedCommandService
        pickStartedProcessor.@pickStartedJaxbContext = pickStartedJaxbContext
    }

    def testInit() {
        when:
        pickStartedProcessor.init()
        then:
        noExceptionThrown()
    }

    def testProcess() {
        given:
        String xmlString = this.getClass().getResource('/fms/processors/PickStartedMsg.xml').text
        ConsumerRecord<String, String> consumerRecord = new ConsumerRecord<>(topic, 1, 123L, "key", xmlString)
        ReceiverRecord<String, String> receiverRecord = new ReceiverRecord<>(consumerRecord, null)
        String message = receiverRecord.value()
        FmsOrder fmsOrder = new FmsOrder()

        when:
        pickStartedProcessor.process(message)

        then:
        1 * fmsPickStartedCommandService.orderPickStartedStore(_) >> fmsOrder
        noExceptionThrown()
    }

    def testProcessFailure() {
        given:
        String xmlString = this.getClass().getResource('/fms/processors/PickStartedMsg.xml').text
        ConsumerRecord<String, String> consumerRecord = new ConsumerRecord<>(topic, 1, 123L, "key", xmlString)
        ReceiverRecord<String, String> receiverRecord = new ReceiverRecord<>(consumerRecord, null)
        String message = receiverRecord.value()
        fmsPickStartedCommandService.orderPickStartedStore(_) >> { throw new FMSBadRequestException() }

        when:
        pickStartedProcessor.process(message)

        then:
        thrown(FMSBadRequestException)
    }
}
