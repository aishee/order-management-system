package com.walmart.oms.eventprocessors

import com.walmart.common.domain.type.*
import com.walmart.common.domain.valueobject.CancellationDetails
import com.walmart.marketplace.order.domain.entity.type.Vendor
import com.walmart.oms.commands.OmsCancelOrderCommand
import com.walmart.oms.domain.error.exception.OMSBadRequestException
import com.walmart.oms.order.aggregateroot.OmsOrder
import com.walmart.oms.order.domain.OmsOrderDomainService
import com.walmart.oms.order.domain.entity.OmsOrderItem
import com.walmart.oms.order.factory.OmsOrderFactory
import com.walmart.oms.order.valueobject.CancelDetails
import com.walmart.oms.order.valueobject.ItemPriceInfo
import com.walmart.oms.order.valueobject.MarketPlaceInfo
import com.walmart.oms.order.valueobject.Money
import spock.lang.Specification

class OmsCancelOrderCommandServiceTest extends Specification {

    OmsCancelOrderCommandService omsStoreCancelledCommandService

    OmsOrderDomainService omsOrderDomainService = Mock()

    OmsOrderFactory omsOrderFactory = Mock()

    String sourceOrderId = UUID.randomUUID().toString()
    String vendorOrderId = UUID.randomUUID().toString()

    def setup() {
        omsStoreCancelledCommandService = new OmsCancelOrderCommandService(
                omsOrderFactory,
                omsOrderDomainService)

    }

    def "Test CancelOrder with pre existing order"() {

        given:
        OmsOrder testOmsOrder = mockOmsOrder()
        OmsCancelOrderCommand storeCancelOrderCommand = getStoreCancelledOrderCommand()
        omsOrderFactory.getOmsOrderBySourceOrder(_ as String, _ as Tenant, _ as Vertical) >> testOmsOrder

        when:
        omsStoreCancelledCommandService.cancelOrder(storeCancelOrderCommand)

        then:
        1 * omsOrderDomainService.cancelOrderByCancellationSource(_ as OmsOrder, _ as CancelDetails) >> { OmsOrder _omsOrder,
                                                                                                          CancelDetails _cancellationDetails ->
            assert _omsOrder.sourceOrderId == sourceOrderId
            return testOmsOrder
        }

    }

    def " Test Store Cancel without pre existing order"() {
        given:
        OmsOrder testOmsOrder = mockOmsOrder()
        OmsCancelOrderCommand storeCancelledOrderCommand = getStoreCancelledOrderCommand()
        omsOrderFactory.getOmsOrderBySourceOrder(_ as String, _ as Tenant, _ as Vertical) >> null

        when:
        omsStoreCancelledCommandService.cancelOrder(storeCancelledOrderCommand)

        then:
        thrown(OMSBadRequestException.class)
    }

    def "Test CancelOrder with pre existing Cancelled order"() {
        given:
        OmsOrder testOmsOrder = mockOmsCanceledOrder()
        OmsCancelOrderCommand storeCancelOrderCommand = getStoreCancelledOrderCommand()
        omsOrderFactory.getOmsOrderBySourceOrder(_ as String, _ as Tenant, _ as Vertical) >> testOmsOrder
        when:
        omsStoreCancelledCommandService.cancelOrder(storeCancelOrderCommand)
        then:
        thrown(OMSBadRequestException.class)

    }

    def "Test CancelOrder with pre existing Delivered order"() {
        given:
        OmsOrder testOmsOrder = mockOmsDeliveredOrder()
        OmsCancelOrderCommand storeCancelOrderCommand = getStoreCancelledOrderCommand()
        omsOrderFactory.getOmsOrderBySourceOrder(_ as String, _ as Tenant, _ as Vertical) >> testOmsOrder
        when:
        omsStoreCancelledCommandService.cancelOrder(storeCancelOrderCommand)
        then:
        thrown(OMSBadRequestException.class)

    }

    OmsOrder mockOmsDeliveredOrder() {
        OmsOrder omsOrder = mockOmsOrder()
        omsOrder.orderState = OmsOrder.OrderStatus.DELIVERED
        return omsOrder
    }

    OmsOrder mockOmsCanceledOrder() {
        OmsOrder omsOrder = mockOmsOrder()
        omsOrder.orderState = OmsOrder.OrderStatus.CANCELLED
        return omsOrder
    }

    OmsOrder mockOmsOrder() {
        String testCin = "4647474"
        OmsOrder omsOrder = getOmsOrder()
        omsOrder.addItem(getOmsOrderItem(testCin, omsOrder))

        return omsOrder
    }

    private OmsOrder getOmsOrder() {
        return OmsOrder.builder()
                .tenant(Tenant.ASDA)
                .vertical(Vertical.MARKETPLACE)
                .orderState("RECD_AT_STORE")
                .deliveryDate(new Date())
                .sourceOrderId(sourceOrderId)
                .storeId("4401")
                .storeOrderId("7373737")
                .fulfillmentType(FulfillmentType.INSTORE_PICKUP)
                .marketPlaceInfo(new MarketPlaceInfo(Vendor.UBEREATS, vendorOrderId))
                .spokeStoreId("4401")
                .build()
    }

    private OmsOrderItem getOmsOrderItem(String testCin, OmsOrder omsOrder) {
        return OmsOrderItem.builder().quantity(2)
                .itemDescription("test description")
                .cin(testCin)
                .omsOrder(omsOrder)
                .itemPriceInfo(new ItemPriceInfo(new Money(BigDecimal.valueOf(2.5), Currency.GBP), new Money(BigDecimal.valueOf(5.0), Currency.GBP)))
                .id(UUID.randomUUID().toString())
                .build()
    }

    private OmsCancelOrderCommand getStoreCancelledOrderCommand() {
        return OmsCancelOrderCommand.builder()
                .sourceOrderId(sourceOrderId)
                .cancellationDetails(CancellationDetails.builder()
                        .cancelledReasonCode("STORE")
                        .cancelledReasonDescription("cancelled at store")
                        .cancelledBy(CancellationSource.STORE).build())
                .vertical(Vertical.MARKETPLACE)
                .tenant(Tenant.ASDA)
                .build()
    }
}