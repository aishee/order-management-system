package com.walmart.oms.infrastructure.gateway.tax

import com.walmart.oms.domain.error.exception.OMSThirdPartyException
import com.walmart.oms.order.domain.entity.OmsOrderItem
import com.walmart.tax.calculator.dto.*
import spock.lang.Specification

class CalculatorTaxGatewayTest extends Specification {

    String sourceOrderId = UUID.randomUUID().toString()
    CalculateTaxHttpWebClient calculateTaxHttpWebClient = Mock()
    CalculatorTaxGateway calculatorTaxGateway

    def setup() {
        calculatorTaxGateway = new CalculatorTaxGateway(
                calculateTaxHttpWebClient: calculateTaxHttpWebClient
        )
    }

    def "When OrderedItem is empty"() {
        when:
        List<OmsOrderItem> orderedItemList = new ArrayList<>()
        Map<String, Tax> taxInfoMap = calculatorTaxGateway.fetchTaxData(orderedItemList, sourceOrderId)

        then:
        taxInfoMap.keySet().size() == 0
    }

    def "When we get NULL CalculateTaxResponse"() {
        given:
        calculateTaxHttpWebClient.executeTaxCall(_ as List, _ as String) >> { return null }

        when:
        Map<String, Tax> taxInfoMap = calculatorTaxGateway.fetchTaxData(_ as List, _ as String)

        then:
        taxInfoMap.keySet().size()==0
    }

    def "When we tax call throws exception"() {
        given:
        calculateTaxHttpWebClient.executeTaxCall(_ as List, _ as String) >> { throw new OMSThirdPartyException("Error after retries failed") }

        when:
        calculatorTaxGateway.fetchTaxData(_ as List, _ as String)

        then:
        thrown(OMSThirdPartyException)
    }

    def "When we get null Order object in CalculateTaxResponse"() {
        given:
        calculateTaxHttpWebClient.executeTaxCall(_ as List, _ as String) >> { return new CalculateTaxResponse() }

        when:
        Map<String, Tax> taxInfoMap = calculatorTaxGateway.fetchTaxData(_ as List, _ as String)

        then:
        taxInfoMap.keySet().size()==0
    }

    def "When we get empty Order lines object in CalculateTaxResponse"() {
        given:
        CalculateTaxResponse calculateTaxResponse = createCalculateTaxResponseEmptyOrderLines()
        calculateTaxHttpWebClient.executeTaxCall(_ as List, _ as String) >> { return calculateTaxResponse }

        when:
        Map<String, Tax> taxInfoMap = calculatorTaxGateway.fetchTaxData(_ as List, _ as String)

        then:
        taxInfoMap.keySet().size()==0
    }

    def "When we get empty Items List object in CalculateTaxResponse"() {
        given:
        CalculateTaxResponse calculateTaxResponse = createCalculateTaxResponseEmptyItems()
        calculateTaxHttpWebClient.executeTaxCall(_ as List, _ as String) >> { return calculateTaxResponse }

        when:
        Map<String, Tax> taxInfoMap = calculatorTaxGateway.fetchTaxData(_ as List, _ as String)

        then:
        taxInfoMap.keySet().size()==0
    }

    def "When we get Valid CalculateTaxResponse"() {
        given:
        CalculateTaxResponse calculateTaxResponse = createCalculateTaxResponse()
        calculateTaxHttpWebClient.executeTaxCall(_ as List, _ as String) >> { return calculateTaxResponse }

        when:
        Map<String, Tax> taxInfoMap = calculatorTaxGateway.fetchTaxData(_ as List, _ as String)

        then:
        taxInfoMap.keySet().size() != 0
    }

    def "Valid CalculateTaxResponse field mapping test"() {
        given:
        CalculateTaxResponse calculateTaxResponse = createCalculateTaxResponse()
        calculateTaxHttpWebClient.executeTaxCall(_ as List, _ as String) >> { return calculateTaxResponse }

        when:
        Map<String, Tax> taxInfoMap = calculatorTaxGateway.fetchTaxData(_ as List, _ as String)

        then:
        taxInfoMap.keySet().size() != 0
        Tax expectedTax = taxInfoMap.get("5000168188775")
        Tax actualTax = calculateTaxResponse.getOrder().getOrderLines().get(0).getItems().get(0).getTaxes().get(0)

        expectedTax == actualTax
    }

    CalculateTaxResponse createCalculateTaxResponse() {
        CalculateTaxResponse calculateTaxResponse = new CalculateTaxResponse()

        Amount rate = new Amount()
        rate.setType(Amount.Type.PERCENT)
        rate.setValue(6.4)
        Amount total = new Amount()
        total.setType(Amount.Type.FIXED)
        total.setValue(BigDecimal.ZERO)

        Tax tax = new Tax()
        tax.setTaxCode(1)
        tax.setRateId("68")
        tax.setRate(rate)
        tax.setDescription("VAT")
        tax.setTotal(total)

        List<Tax> taxList = new ArrayList<>()
        taxList.add(tax)

        Item item = new Item()
        item.setGtin("5000168188775")
        item.setTaxes(taxList)

        List<Item> itemList = new ArrayList<>()
        itemList.add(item)

        OrderLine orderLine = new OrderLine()
        orderLine.setItems(itemList)

        List<OrderLine> orderLineList = new ArrayList<>()
        orderLineList.add(orderLine)

        Order order = new Order()
        order.setOrderLines(orderLineList)

        calculateTaxResponse.setOrder(order)

        return calculateTaxResponse
    }

    CalculateTaxResponse createCalculateTaxResponseEmptyOrderLines() {
        CalculateTaxResponse calculateTaxResponse = new CalculateTaxResponse()
        List<OrderLine> orderLineList = new ArrayList<>()

        Order order = new Order()
        order.setOrderLines(orderLineList)

        calculateTaxResponse.setOrder(order)
        return calculateTaxResponse
    }

    CalculateTaxResponse createCalculateTaxResponseEmptyItems() {
        CalculateTaxResponse calculateTaxResponse = new CalculateTaxResponse()

        List<Item> itemList = new ArrayList<>()
        OrderLine orderLine = new OrderLine()
        orderLine.setItems(itemList)

        List<OrderLine> orderLineList = new ArrayList<>()
        orderLineList.add(orderLine)

        Order order = new Order()
        order.setOrderLines(orderLineList)

        calculateTaxResponse.setOrder(order)
        return calculateTaxResponse
    }
}
