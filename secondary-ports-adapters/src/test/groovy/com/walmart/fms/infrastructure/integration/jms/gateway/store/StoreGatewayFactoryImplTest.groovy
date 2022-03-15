package com.walmart.fms.infrastructure.integration.jms.gateway.store

import com.walmart.common.infrastructure.integration.events.processing.IdempotentEgressEventProcessor
import com.walmart.fms.infrastructure.integration.gateway.StoreGatewayFactoryImpl
import com.walmart.fms.infrastructure.integration.gateway.store.GifStoreGateway
import com.walmart.fms.order.aggregateroot.FmsOrder
import com.walmart.fms.order.gateway.StoreGateway
import com.walmart.fms.order.gateway.StoreGatewayFactory
import spock.lang.Specification

class StoreGatewayFactoryImplTest extends Specification {

    StoreGatewayFactory storeGatewayFactory
    GifStoreGateway gifStoreGateway

    def setup() {
        gifStoreGateway = new GifStoreGateway()
        storeGatewayFactory = new StoreGatewayFactoryImpl(gifMaasStoreGateway: gifStoreGateway)
    }

    def "If null type is passed return DefaultStoreGateway implementation"() {
        when:
        StoreGateway storeGateway = storeGatewayFactory.getGatewayFor(null)
        FmsOrder fmsOrder = Mock()
        storeGateway.sendOrderCancellation(fmsOrder)
        storeGateway.sendOrderConfirmation(fmsOrder)
        storeGateway.getActionByCode("event")
        then:
        storeGateway != null
        storeGateway instanceof StoreGatewayFactoryImpl.DefaultStoreGateway
    }

    def "If type GIF is passed return a GifStoreGateway"() {
        when:
        StoreGateway storeGateway = storeGatewayFactory.getGatewayFor(FmsOrder.FulfillmentApp.GIF_MAAS)
        then:
        storeGateway != null
        storeGateway instanceof GifStoreGateway
        storeGateway instanceof IdempotentEgressEventProcessor
    }
}
