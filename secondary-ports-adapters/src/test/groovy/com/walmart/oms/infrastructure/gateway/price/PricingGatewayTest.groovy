package com.walmart.oms.infrastructure.gateway.price

import com.walmart.oms.domain.event.messages.OrderCancelledDomainEventMessage
import com.walmart.oms.infrastructure.gateway.price.dto.DetailLine
import com.walmart.oms.infrastructure.gateway.price.dto.OrderInformation
import com.walmart.oms.infrastructure.gateway.price.dto.PYSIPYPHeader
import com.walmart.oms.infrastructure.gateway.price.validators.PYSIPYPRequestValidator
import com.walmart.oms.order.aggregateroot.OmsOrder
import com.walmart.oms.order.valueobject.PricingResponse
import com.walmart.tax.calculator.dto.Amount
import com.walmart.tax.calculator.dto.Tax
import spock.lang.Specification

class PricingGatewayTest extends Specification {

    PricingGateway pricingGateway = Mock()
    PYSIPYPWebClient pysipypWebClient = Mock()
    PYSIPYPRequestValidator pysipypRequestValidator = Mock()

    def setup() {
        pricingGateway = new PricingGateway(
                pysipypWebClient: pysipypWebClient,
                pysipypRequestValidator: pysipypRequestValidator
        )
    }


    def "When Get price data fails Validation"() {
        given:
        OmsOrder omsOrder = new OmsOrder()
        Map<String, Tax> taxInfoMap = new HashMap<>()
        pysipypRequestValidator.isValidRecordSaleRequest(_, _) >> false

        when:

        Optional<PricingResponse> pysipypResponse = pricingGateway.priceOrder(omsOrder, taxInfoMap)

        then:
        !pysipypResponse.isPresent()
    }

    def "When OmsOrder is invalid,then return false for reverse sale call"() {
        given:
        OrderCancelledDomainEventMessage orderCancelledDomainEventMessage = null
        Map<String, Tax> taxInfoMap = null
        pysipypWebClient.reverseSale(_) >> Optional.empty()

        when:
        boolean pricingResponse = pricingGateway.reverseSale(orderCancelledDomainEventMessage)

        then:
        !pricingResponse
    }


    def "When valid OmsOrder is given, map to PricingResponse valueObject"() {
        given:
        OmsOrder omsOrder = new OmsOrder()
        Map<String, Tax> taxInfoMap = createTaxMockMap()
        OrderInformation pysipypMockResponse = createPysipypResponse()
        pysipypWebClient.getPriceData(_ as OmsOrder, taxInfoMap) >> { return pysipypMockResponse }
        pysipypRequestValidator.isValidRecordSaleRequest(_,_) >> true
        when:
        Optional<PricingResponse> pricingResponseOptional = pricingGateway.priceOrder(omsOrder, taxInfoMap)


        then:
        pricingResponseOptional.isPresent()
        PricingResponse pricingResponse = pricingResponseOptional.get()
        pricingResponse.posOrderTotalPrice == pysipypMockResponse.posOrderTotalPrice.toDouble()
        pricingResponse.itemPriceServiceMap.size() == 2
        pricingResponse.itemPriceServiceMap.get("100").adjustedPrice == pysipypMockResponse.getDetailLine().get(0).adjustedPrice.toDouble()
        pricingResponse.itemPriceServiceMap.get("100").adjustedPriceExVat == pysipypMockResponse.getDetailLine().get(0).adjPriceExVAT.toDouble()
        pricingResponse.itemPriceServiceMap.get("100").vatAmount == pysipypMockResponse.getDetailLine().get(0).vatAmount.toDouble()
        pricingResponse.itemPriceServiceMap.get("100").webAdjustedPrice == pysipypMockResponse.getDetailLine().get(0).webAdjustedPrice.toDouble()
        pricingResponse.itemPriceServiceMap.get("100").displayPrice == pysipypMockResponse.getDetailLine().get(0).displayPrice.toDouble()

    }

    OrderInformation createPysipypResponse() {
        return new OrderInformation(
                consumerId: "GIF",
                posOrderTotalPrice: "100.0",
                header: new PYSIPYPHeader(
                        accessCode: "123123",
                        messageType: "ODSCalcTotal",
                        countryCode: "GB",
                        storeNumber: "5755",
                        orderNumber: "1234"
                ),
                cardTypeUsed: "CreditCard",
                detailLine: [
                        new DetailLine(
                                productID: "100",
                                adjustedPrice: "51.0",
                                adjPriceExVAT: "51.1",
                                vatAmount: "51.2",
                                webAdjustedPrice: "51.3",
                                displayPrice: "51.4"
                        ),
                        new DetailLine(
                                productID: "101",
                                adjustedPrice: "52.0",
                                adjPriceExVAT: "52.1",
                                vatAmount: "52.2",
                                webAdjustedPrice: "52.3",
                                displayPrice: "52.4"
                        )
                ],
                minBasketChargeAppliedOnPricing: false,
                minOrderAmount: "95"
        )
    }

    Map<String, Tax> createTaxMockMap() {

        Map<String, Tax> taxMap = new HashMap<>()
        taxMap.put("100", new Tax(
                taxCode: 10,
                rate: new Amount(
                        value: 11
                ),
                rateId: 12
        ))
        taxMap.put("101", new Tax(
                taxCode: 20,
                rate: new Amount(
                        value: 21
                ),
                rateId: 22
        ))
        return taxMap
    }

    def "Test ReverseSale for when valid OmsOrder is provided and we get a successful response from pricing"() {
        given:
        OrderCancelledDomainEventMessage orderCancelledDomainEventMessage =
                new OrderCancelledDomainEventMessage(
                        storeId: "4401",
                        storeOrderId: "32323233"
                )

        pysipypWebClient.reverseSale(_ as OrderCancelledDomainEventMessage) >> mockReverseSaleResponse()
        pysipypRequestValidator.isValidReverseSaleRequest(_) >> true
        when:
        boolean actualResult = pricingGateway.reverseSale(orderCancelledDomainEventMessage)

        then:
        assert actualResult


    }


    OrderInformation mockReverseSaleResponse() {

        OrderInformation orderInformation = new OrderInformation()

        PYSIPYPHeader header = new PYSIPYPHeader()
        header.setCountryCode("GB")
        header.setMessageType("REVERSE_SALE")
        header.setAccessCode("testAccessCode")
        header.setSaleType("R")
        header.setStoreNumber("4401")
        header.setOrderNumber("32323233")
        orderInformation.setHeader(header)
        orderInformation.setTransactionCode("SUCCESS")
        return orderInformation

    }
}
