package com.walmart.fms.mapper

import com.walmart.fms.infrastructure.integration.gateway.store.dto.forceordercancellation.UpdateFulfillmentOrderRequest
import com.walmart.oms.order.valueobject.events.FmsOrderItemvalueObject
import spock.lang.Specification
import com.walmart.fms.order.aggregateroot.FmsOrder

import javax.xml.bind.JAXBContext;

class FmsOrderToUFORequestMapperSpec extends Specification {

    JAXBContext updateFulfillmentOrderJaxbContext = JAXBContext.newInstance(UpdateFulfillmentOrderRequest.class)

    def "test FmsOrderToUFORequestMapper " () {
        given:
        FmsOrder fmsOrder = getFmsOrder()
        String xmlString = this.getClass().getResource('/fms/mappers/UpdateFulfillmentOrderRequest.xml').text
        UpdateFulfillmentOrderRequest updateFulfillmentOrderRequest =
                updateFulfillmentOrderJaxbContext.createUnmarshaller().unmarshal(new StringReader(xmlString))

        when:
        UpdateFulfillmentOrderRequest convertedUpdateFulfillmentOrderRequest =
                FmsOrderToUFORequestMapper.map(fmsOrder)

        then:
        convertedUpdateFulfillmentOrderRequest.messageHeader.subId == "SUB-ASDA-UFO-V1"
        convertedUpdateFulfillmentOrderRequest.messageBody.customerOrder.orderHeader.orderNumber == 123456789
        convertedUpdateFulfillmentOrderRequest.messageBody.customerOrder.fulfillmentOrder[0].node.nodeID == 5755
        convertedUpdateFulfillmentOrderRequest.messageBody.customerOrder.orderHeader.orderNumber == updateFulfillmentOrderRequest.messageBody.customerOrder.orderHeader.orderNumber
    }

    private static FmsOrder getFmsOrder(){
        return new FmsOrder(
                storeOrderId: "123456789",
                storeId: "5755",
        )
    }
}
