package com.walmart.oms.infrastructure.gateway.price

import com.walmart.common.domain.type.FulfillmentType
import com.walmart.common.domain.type.Vertical
import com.walmart.marketplace.order.domain.entity.type.Vendor
import com.walmart.oms.domain.event.messages.OrderCancelledDomainEventMessage
import com.walmart.oms.infrastructure.gateway.price.validators.PYSIPYPRequestValidator
import com.walmart.oms.order.aggregateroot.OmsOrder
import com.walmart.oms.order.valueobject.MarketPlaceInfo
import com.walmart.tax.calculator.dto.Tax
import spock.lang.Specification

class PYSIPYPRequestValidatorSpec extends Specification {

    PYSIPYPRequestValidator pysipypRequestValidator

    def setup() {
        pysipypRequestValidator = new PYSIPYPRequestValidator()
    }

    def "When OmsOrder is null,then return false"() {
        given:
        OmsOrder omsOrder = null
        Map<String, Tax> taxInfoMap = null

        when:
        boolean res = pysipypRequestValidator.isValidRecordSaleRequest(omsOrder, taxInfoMap)

        then:
        !res
    }

    def "When storeId is null,then return false"() {
        given:
        MarketPlaceInfo marketPlaceInfo = new MarketPlaceInfo(
                vendorOrderId: "1123232333"
        )
        OmsOrder omsOrder = new OmsOrder(
                storeOrderId: "181817361355",
                orderState: "PICK_COMPLETE",
                marketPlaceInfo: marketPlaceInfo
        )
        Map<String, Tax> taxInfoMap = null


        when:
        boolean res = pysipypRequestValidator.isValidRecordSaleRequest(omsOrder, taxInfoMap)

        then:
        !res
    }

    def "When OmsOrder is not marketPlace order,then return false"() {
        given:
        OmsOrder omsOrder = OmsOrder.builder().vertical(Vertical.ASDAGR)
                .orderState("PICK_COMPLETE")
                .id(UUID.randomUUID().toString())
                .pickupLocationId("5755")
                .storeId("5755")
                .authStatus("AUTHENTICATED")
                .sourceOrderId("e2066983-7793-4017-ac06-74785bfeff15")
                .storeOrderId("71615242412")
                .deliveryDate(new Date())
                .fulfillmentType(FulfillmentType.INSTORE_PICKUP)
                .marketPlaceInfo(MarketPlaceInfo.builder().vendorOrderId("12312312323232")
                        .vendor(Vendor.UBEREATS).build())
                .build()
        Map<String, Tax> taxInfoMap = null

        when:
        boolean res = pysipypRequestValidator.isValidRecordSaleRequest(omsOrder, taxInfoMap)

        then:
        !res
    }

    def "When OmsOrder is valid but taxInfo is empty ,then return false"() {
        given:
        OmsOrder omsOrder = OmsOrder.builder().vertical(Vertical.MARKETPLACE)
                .orderState("PICK_COMPLETE")
                .id(UUID.randomUUID().toString())
                .pickupLocationId("5755")
                .storeId("5755")
                .authStatus("AUTHENTICATED")
                .sourceOrderId("e2066983-7793-4017-ac06-74785bfeff15")
                .storeOrderId("71615242412")
                .deliveryDate(new Date())
                .fulfillmentType(FulfillmentType.INSTORE_PICKUP)
                .marketPlaceInfo(MarketPlaceInfo.builder().vendor(Vendor.UBEREATS).vendorOrderId("12312312323232").build())
                .build()
        Map<String, Tax> taxInfoMap = null

        when:
        boolean res = pysipypRequestValidator.isValidRecordSaleRequest(omsOrder, taxInfoMap)

        then:
        !res
    }

    def "When OMS order is valid but TaxInfo has no element ,then return false"() {
        given:
        OmsOrder omsOrder = OmsOrder.builder().vertical(Vertical.MARKETPLACE)
                .orderState("PICK_COMPLETE")
                .id(UUID.randomUUID().toString())
                .pickupLocationId("5755")
                .storeId("5755")
                .authStatus("AUTHENTICATED")
                .sourceOrderId("e2066983-7793-4017-ac06-74785bfeff15")
                .storeOrderId("71615242412")
                .deliveryDate(new Date())
                .fulfillmentType(FulfillmentType.INSTORE_PICKUP)
                .marketPlaceInfo(MarketPlaceInfo.builder().vendor(Vendor.UBEREATS).vendorOrderId("12312312323232").build())
                .build()
        Map<String, Tax> taxInfoMap = new HashMap<>()

        when:
        boolean res = pysipypRequestValidator.isValidRecordSaleRequest(omsOrder, taxInfoMap)

        then:
        !res
    }

    def "When record sale request objects are valid,then return true"() {
        given:
        OmsOrder omsOrder = OmsOrder.builder().vertical(Vertical.MARKETPLACE)
                .orderState("PICK_COMPLETE")
                .id(UUID.randomUUID().toString())
                .pickupLocationId("5755")
                .storeId("5755")
                .authStatus("AUTHENTICATED")
                .sourceOrderId("e2066983-7793-4017-ac06-74785bfeff15")
                .storeOrderId("71615242412")
                .deliveryDate(new Date())
                .marketPlaceInfo(MarketPlaceInfo.builder().vendor(Vendor.UBEREATS).vendorOrderId("12312312323232").build())
                .fulfillmentType(FulfillmentType.INSTORE_PICKUP)
                .build()
        Map<String, Tax> taxInfoMap = new HashMap<>()
        taxInfoMap.put("ABCD", new Tax())

        when:
        boolean res = pysipypRequestValidator.isValidRecordSaleRequest(omsOrder, taxInfoMap)

        then:
        res
    }

    def "validate ReverseSale when OmsOrder is null,then return false "() {
        given:
        OrderCancelledDomainEventMessage orderCancelledDomainEventMessage = null

        when:
        boolean res = pysipypRequestValidator.isValidReverseSaleRequest(orderCancelledDomainEventMessage)

        then:
        !res
    }

    def "validate ReverseSale when omsOrder is valid,then return true"() {
        given:
        MarketPlaceInfo marketPlaceInfo = new MarketPlaceInfo(
                vendorOrderId: "1123232333"
        )
        OrderCancelledDomainEventMessage orderCancelledDomainEventMessage =
                new OrderCancelledDomainEventMessage(
                        storeOrderId: "5755113223",
                        storeId: "5755",
                )

        when:
        boolean res = pysipypRequestValidator.isValidReverseSaleRequest(orderCancelledDomainEventMessage)

        then:
        res
    }
}
