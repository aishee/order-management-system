package com.walmart.marketplace.domain.event.listeners

import com.walmart.common.domain.type.CancellationSource
import com.walmart.marketplace.domain.event.listeners.InitiateVendorCancellationListener
import com.walmart.marketplace.domain.event.messages.MarketPlaceOrderCancelMessage
import com.walmart.marketplace.order.domain.entity.type.Vendor
import com.walmart.marketplace.order.gateway.IMarketPlaceGatewayFinder
import com.walmart.marketplace.order.repository.IMarketPlaceGateWay
import spock.lang.Specification

class InitiateVendorCancellationListenerTest extends Specification {

    IMarketPlaceGatewayFinder marketPlaceGatewayFinder = Mock()

    IMarketPlaceGateWay gateWay = Mock()

    InitiateVendorCancellationListener listener

    def setup() {
        listener = new InitiateVendorCancellationListener(marketPlaceGatewayFinder)
    }

    def "test when cancel source is STORE , message is sent to UBER"() {
        given:
        MarketPlaceOrderCancelMessage message = mockMarketPlaceCancelMessageForSourceAsStore()
        when:
        listener.listen(message)
        then:


        1 * marketPlaceGatewayFinder.getMarketPlaceGateway(_ as Vendor) >> { return gateWay }
        1 * gateWay.cancelOrder(_ as String, _ as String) >> { String _vendorOrderId, String _reason ->
            assert _vendorOrderId == message.getVendorOrderId()
            assert _reason == "STORE_CANCELLED"
            return true
        }

    }

    def "test when cancel source is VENDOR , message is not sent to UBER"() {
        given:
        MarketPlaceOrderCancelMessage message = mockMarketPlaceCancelMessageForSourceAsVendor()
        when:
        listener.listen(message)
        then:

        0 * marketPlaceGatewayFinder.getMarketPlaceGateway(_ as Vendor)
        0 * gateWay.cancelOrder(_ as String, _ as String)
    }

    def "test when order is testOrder , message is not sent to UBER"() {
        given:
        MarketPlaceOrderCancelMessage message = mockMarketPlaceCancelMessageForSourceAsStore()
        message.isTestOrder = true
        when:
        listener.listen(message)
        then:

        0 * marketPlaceGatewayFinder.getMarketPlaceGateway(_ as Vendor)
        0 * gateWay.cancelOrder(_ as String, _ as String)
    }

    private static MarketPlaceOrderCancelMessage mockMarketPlaceCancelMessageForSourceAsStore() {
        return MarketPlaceOrderCancelMessage.builder()
                .vendorOrderId(UUID.randomUUID().toString())
                .cancellationSource(CancellationSource.STORE)
                .vendor(Vendor.UBEREATS)
                .isTestOrder(false)
                .build()
    }

    private static MarketPlaceOrderCancelMessage mockMarketPlaceCancelMessageForSourceAsVendor() {
        return MarketPlaceOrderCancelMessage.builder()
                .vendorOrderId(UUID.randomUUID().toString())
                .cancellationSource(CancellationSource.VENDOR)
                .vendor(Vendor.UBEREATS)
                .isTestOrder(false)
                .build()
    }


}
