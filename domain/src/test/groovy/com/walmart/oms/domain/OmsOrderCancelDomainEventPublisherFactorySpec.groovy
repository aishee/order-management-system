package com.walmart.oms.domain

import com.walmart.common.domain.type.CancellationSource
import com.walmart.oms.order.domain.DefaultCancelledOrderDomainEventPublisher
import com.walmart.oms.order.domain.OmsOrderCancelDomainEventPublisher
import com.walmart.oms.order.domain.OmsStoreCancelledOrderDomainEventPublisher
import com.walmart.oms.order.domain.OmsVendorCancelledOrderDomainEventPublisher
import com.walmart.oms.order.domain.factory.OmsOrderCancelDomainEventPublisherFactory
import spock.lang.Specification

class OmsOrderCancelDomainEventPublisherFactorySpec extends Specification {
    OmsOrderCancelDomainEventPublisherFactory omsCancelOrderDomainServiceFactory;
    OmsVendorCancelledOrderDomainEventPublisher omsVendorCancelledOrderDomainService = Mock()
    OmsStoreCancelledOrderDomainEventPublisher omsStoreCancelledOrderDomainService = Mock()
    DefaultCancelledOrderDomainEventPublisher defaultCancelledOrderDomainService = Mock()

    def setup() {
        omsCancelOrderDomainServiceFactory = new OmsOrderCancelDomainEventPublisherFactory(
                omsStoreCancelledOrderDomainService,
                omsVendorCancelledOrderDomainService,
                defaultCancelledOrderDomainService)
    }

    def "Test factory for store cancelled"() {
        when:
        OmsOrderCancelDomainEventPublisher omsOrderCancelDomainService =
                omsCancelOrderDomainServiceFactory.getOrderCancelDomainEventPublisher(CancellationSource.STORE)
        then:
        omsOrderCancelDomainService instanceof OmsStoreCancelledOrderDomainEventPublisher
    }

    def "Test factory for vendor cancelled"() {
        when:
        OmsOrderCancelDomainEventPublisher omsOrderCancelDomainService =
                omsCancelOrderDomainServiceFactory.getOrderCancelDomainEventPublisher(CancellationSource.VENDOR)
        then:
        omsOrderCancelDomainService instanceof OmsVendorCancelledOrderDomainEventPublisher
    }

    def "Test factory for oms cancelled"() {
        when:
        OmsOrderCancelDomainEventPublisher omsOrderCancelDomainService =
                omsCancelOrderDomainServiceFactory.getOrderCancelDomainEventPublisher(CancellationSource.OMS)
        then:
        omsOrderCancelDomainService instanceof DefaultCancelledOrderDomainEventPublisher
    }

}
