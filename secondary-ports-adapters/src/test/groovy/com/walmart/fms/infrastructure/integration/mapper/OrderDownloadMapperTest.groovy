package com.walmart.fms.infrastructure.integration.mapper


import com.walmart.fms.infrastructure.integration.gateway.store.dto.orderdownload.PlaceFulfillmentOrderRequest
import com.walmart.fms.infrastructure.integration.mapper.OrderDownloadMapper
import com.walmart.fms.order.aggregateroot.FmsOrder
import com.walmart.fms.order.domain.entity.FmsCustomerContactInfo
import com.walmart.fms.order.valueobject.EmailAddress
import com.walmart.fms.order.valueobject.FullName
import com.walmart.fms.order.valueobject.MarketPlaceInfo
import spock.lang.Specification

class OrderDownloadMapperTest extends Specification {

    OrderDownloadMapper orderDownloadMapper;


    def setup() {
        orderDownloadMapper = OrderDownloadMapper.INSTANCE;
    }

    def "Empty FmsOrder model"() {

        given:
        FmsOrder fmsOrder = null

        when:
        PlaceFulfillmentOrderRequest pfoRequest = orderDownloadMapper.toPlaceFulfillmentOrderRequest(fmsOrder)

        then:
        pfoRequest == null
    }

    def "Valid FmsOrder model"() {

        given:
        FmsOrder fmsOrder = mockFmsOrder()

        when:
        PlaceFulfillmentOrderRequest pfoRequest = orderDownloadMapper.toPlaceFulfillmentOrderRequest(fmsOrder)

        then:
        pfoRequest.messageHeader.tranId == fmsOrder.storeOrderId
        pfoRequest.messageBody.customerOrderInfo.customerInfo.customerDetails.customer.id == "UBEREATS"
        pfoRequest.messageBody.customerOrderInfo.customerInfo.customerDetails.customer.contact.email == fmsOrder.contactInfo.email.address
        pfoRequest.messageBody.customerOrderInfo.customerInfo.customerDetails.customer.firstName == fmsOrder.contactInfo.fullName.firstName
        pfoRequest.messageBody.customerOrderInfo.customerInfo.customerDetails.customer.lastName == fmsOrder.contactInfo.fullName.lastName +
                fmsOrder.marketPlaceInfo.vendorOrderId.substring(fmsOrder.marketPlaceInfo.vendorOrderId.length() - 5)
    }

    def "Vendor Order Id is null"() {

        given:
        FmsOrder fmsOrder = mockFmsOrder()
        fmsOrder.marketPlaceInfo.vendorOrderId = null

        when:
        PlaceFulfillmentOrderRequest pfoRequest = orderDownloadMapper.toPlaceFulfillmentOrderRequest(fmsOrder)

        then:
        pfoRequest.messageBody.customerOrderInfo.customerInfo.customerDetails.customer.lastName == fmsOrder.contactInfo.fullName.lastName
    }

    def "Vendor Order Id is less than 5 chars"() {

        given:
        FmsOrder fmsOrder = mockFmsOrder()
        fmsOrder.marketPlaceInfo.vendorOrderId = "2222"

        when:
        PlaceFulfillmentOrderRequest pfoRequest = orderDownloadMapper.toPlaceFulfillmentOrderRequest(fmsOrder)

        then:
        pfoRequest.messageBody.customerOrderInfo.customerInfo.customerDetails.customer.lastName == fmsOrder.contactInfo.fullName.lastName + fmsOrder.marketPlaceInfo.vendorOrderId
    }

    FmsOrder mockFmsOrder() {
        return new FmsOrder(
                sourceOrderId: "5362B51A-DE26-4011-A4CF-219BD68CA508",
                storeOrderId: "34554453333443",
                storeId: "5555",
                marketPlaceInfo: new MarketPlaceInfo(
                        vendor: "UBEREATS",
                        vendorOrderId: "548f7a1e-d6d4-9b4f-ae19-4d820faf86c7"
                ),
                contactInfo: new FmsCustomerContactInfo(
                        fullName: new FullName(
                                firstName: "John",
                                lastName: "Peter"
                        ),
                        email: new EmailAddress(
                                address: "john.peter.test@asda.com"
                        ),
                        customerId: "4aa8c2cf-74ba-9499-93dd-7e494995efa8"
                )
        )
    }


}
