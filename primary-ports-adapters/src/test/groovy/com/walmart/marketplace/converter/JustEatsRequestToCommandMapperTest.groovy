package com.walmart.marketplace.converter

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.walmart.common.constants.CommonConstants
import com.walmart.marketplace.commands.MarketPlaceCreateOrderCommand
import com.walmart.marketplace.commands.WebHookEventCommand
import com.walmart.marketplace.commands.extensions.ExternalMarketPlaceItem
import com.walmart.marketplace.justeats.request.Item
import com.walmart.marketplace.justeats.request.JustEatsWebHookRequest
import com.walmart.marketplace.order.domain.entity.type.Vendor
import spock.lang.Specification

import java.time.Instant

class JustEatsRequestToCommandMapperTest extends Specification {

    JustEatsRequestToCommandMapper justEatsRequestToCommandMapper

    def setup() {
        justEatsRequestToCommandMapper = new JustEatsRequestToCommandMapper()
    }

    def "JustEats Request to Marketplace Event Command Mapper"() {
        given:
        JustEatsWebHookRequest justEatsWebHookRequest = new JustEatsWebHookRequest()
        justEatsWebHookRequest.setId("1234")
        justEatsWebHookRequest.setThirdPartyOrderReference("5555")
        justEatsWebHookRequest.setType("Order Creation")
        justEatsWebHookRequest.setCreatedAt("1606780145")

        when:
        WebHookEventCommand webHookEventCommand = justEatsRequestToCommandMapper.createWebHookCommand(justEatsWebHookRequest)

        then:
        webHookEventCommand.getEventType() == (justEatsWebHookRequest.getType())
        webHookEventCommand.getExternalOrderId() == (justEatsWebHookRequest.getThirdPartyOrderReference())
        webHookEventCommand.getSourceEventId() == (justEatsWebHookRequest.getId())
        webHookEventCommand.getResourceURL() == ""
        webHookEventCommand.getRequestTime() == Instant.ofEpochSecond(1606780145)
        webHookEventCommand.getVendor() == Vendor.JUSTEAT
    }

    def "JustEats Request to Marketplace Order Command Mapper"() {
        given:
        JustEatsWebHookRequest justEatsWebHookRequest = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .readValue(new File("src/test/resources/justeats/justEatsWebHookRequestPayload.json"), JustEatsWebHookRequest.class);

        when:
        MarketPlaceCreateOrderCommand marketPlaceCreateOrderCommand = justEatsRequestToCommandMapper.createMarketPlaceOrderCmd(justEatsWebHookRequest)
        ExternalMarketPlaceItem marketPlaceItem = marketPlaceCreateOrderCommand.getMarketPlaceItems().get(0)
        Item item = justEatsWebHookRequest.getItems().get(0)

        then:
        marketPlaceCreateOrderCommand.getExternalOrderId() == (justEatsWebHookRequest.getThirdPartyOrderReference())
        marketPlaceCreateOrderCommand.getExternalNativeOrderId() == (justEatsWebHookRequest.getId())
        marketPlaceCreateOrderCommand.getEstimatedArrivalTime() == justEatsWebHookRequest.getArrivalTime()
        marketPlaceCreateOrderCommand.getFirstName() == justEatsWebHookRequest.getCustomerFirstName()
        marketPlaceCreateOrderCommand.getLastName() == justEatsWebHookRequest.getCustomerLastName()
        marketPlaceCreateOrderCommand.getStoreId() == justEatsWebHookRequest.getPosLocationId()
        marketPlaceCreateOrderCommand.getVendorStoreId() == justEatsWebHookRequest.getPosLocationId()
        marketPlaceCreateOrderCommand.getStoreId() == justEatsWebHookRequest.getPosLocationId()
        marketPlaceCreateOrderCommand.getSourceOrderCreationTime() == justEatsWebHookRequest.getOrderCreationTime()
        marketPlaceCreateOrderCommand.getBagFee().getAmount() == BigDecimal.valueOf(1.99)
        marketPlaceCreateOrderCommand.getTotalFee() == CommonConstants.ZERO_MONEY
        marketPlaceCreateOrderCommand.getTax() == justEatsWebHookRequest.getTaxAmount()
        marketPlaceCreateOrderCommand.getTotalFeeTax() == CommonConstants.ZERO_MONEY
        marketPlaceCreateOrderCommand.getSubTotal() == justEatsWebHookRequest.getSubTotal()
        marketPlaceCreateOrderCommand.getVendor() == Vendor.JUSTEAT
        marketPlaceItem.getItemId() == item.getPlu()
        marketPlaceItem.getExternalItemId() == item.getPlu()
        marketPlaceItem.getItemDescription() == item.getDescription()
        marketPlaceItem.getItemType() == "CIN"
        marketPlaceItem.getVendorInstanceId() == item.getPlu()
        marketPlaceItem.getBaseTotalPrice().doubleValue() == 17.0
        marketPlaceItem.getBaseUnitPrice().doubleValue() == 17.0
        marketPlaceItem.getUnitPrice().doubleValue() == 17.0
        marketPlaceItem.getQuantity() == 1
    }

    def "JustEats Request with Multi quantity item to Marketplace Order Command Mapper"() {
        given:
        JustEatsWebHookRequest justEatsWebHookRequest = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .readValue(new File("src/test/resources/justeats/justEatsMultiQuantityWebHookRequestPayload.json"), JustEatsWebHookRequest.class);

        when:
        MarketPlaceCreateOrderCommand marketPlaceCreateOrderCommand = justEatsRequestToCommandMapper.createMarketPlaceOrderCmd(justEatsWebHookRequest)
        ExternalMarketPlaceItem marketPlaceItem = marketPlaceCreateOrderCommand.getMarketPlaceItems().get(0)

        then:
        marketPlaceCreateOrderCommand.getExternalOrderId() == (justEatsWebHookRequest.getThirdPartyOrderReference())
        marketPlaceCreateOrderCommand.getExternalNativeOrderId() == (justEatsWebHookRequest.getId())
        marketPlaceCreateOrderCommand.getEstimatedArrivalTime() == justEatsWebHookRequest.getArrivalTime()
        marketPlaceCreateOrderCommand.getFirstName() == justEatsWebHookRequest.getCustomerFirstName()
        marketPlaceCreateOrderCommand.getLastName() == justEatsWebHookRequest.getCustomerLastName()
        marketPlaceCreateOrderCommand.getStoreId() == justEatsWebHookRequest.getPosLocationId()
        marketPlaceCreateOrderCommand.getVendorStoreId() == justEatsWebHookRequest.getPosLocationId()
        marketPlaceCreateOrderCommand.getStoreId() == justEatsWebHookRequest.getPosLocationId()
        marketPlaceCreateOrderCommand.getSourceOrderCreationTime() == justEatsWebHookRequest.getOrderCreationTime()
        marketPlaceCreateOrderCommand.getBagFee().getAmount() == BigDecimal.valueOf(1.99)
        marketPlaceCreateOrderCommand.getTotalFee() == CommonConstants.ZERO_MONEY
        marketPlaceCreateOrderCommand.getTax() == justEatsWebHookRequest.getTaxAmount()
        marketPlaceCreateOrderCommand.getTotalFeeTax() == CommonConstants.ZERO_MONEY
        marketPlaceCreateOrderCommand.getSubTotal() == justEatsWebHookRequest.getSubTotal()
        marketPlaceCreateOrderCommand.getVendor() == Vendor.JUSTEAT
        marketPlaceItem.getItemId() == "6325531"
        marketPlaceItem.getExternalItemId() == "6325531"
        marketPlaceItem.getItemDescription() == ""
        marketPlaceItem.getItemType() == "CIN"
        marketPlaceItem.getVendorInstanceId() == "6325531"
        marketPlaceItem.getBaseTotalPrice().doubleValue() == 77.5
        marketPlaceItem.getBaseUnitPrice().doubleValue() == 15.5
        marketPlaceItem.getUnitPrice().doubleValue() == 15.5
        marketPlaceItem.getQuantity() == 5
    }

}
