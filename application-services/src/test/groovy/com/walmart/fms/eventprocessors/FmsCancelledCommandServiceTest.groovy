package com.walmart.fms.eventprocessors


import com.walmart.common.domain.type.CancellationSource
import com.walmart.common.domain.type.Currency
import com.walmart.common.domain.type.SubstitutionOption
import com.walmart.common.domain.type.Tenant
import com.walmart.common.domain.type.Vertical
import com.walmart.fms.commands.FmsCancelOrderCommand
import com.walmart.fms.domain.error.exception.FMSBadRequestException
import com.walmart.fms.order.aggregateroot.FmsOrder
import com.walmart.fms.order.domain.FmsOrderDomainService
import com.walmart.fms.order.domain.entity.FmsOrderItem
import com.walmart.fms.order.factory.FmsOrderFactory
import com.walmart.fms.order.repository.IFmsOrderRepository
import com.walmart.fms.order.valueobject.ItemPriceInfo
import com.walmart.fms.order.valueobject.MarketPlaceInfo
import com.walmart.fms.order.valueobject.Money
import com.walmart.marketplace.order.domain.entity.type.Vendor
import spock.lang.Specification

class FmsCancelledCommandServiceTest extends Specification {

    FmsCancelledCommandService fmsCancelledCommandService

    private FmsOrderFactory fmsOrderFactory = Mock()

    private IFmsOrderRepository fmsOrderRepository = Mock()


    FmsOrderDomainService fmsOrderDomainService = Mock()

    String storeOrderId = UUID.randomUUID().toString()
    String vendorOrderId = UUID.randomUUID().toString()

    def setup() {
        fmsCancelledCommandService = new FmsCancelledCommandService(
                fmsOrderFactory: fmsOrderFactory,
                fmsOrderDomainService: fmsOrderDomainService)
    }

    def " Test Store Cancel Order with pre existing order"() {
        given:
        FmsOrder testFmsOrder = mockFmsOrder()
        FmsCancelOrderCommand storeCancelOrderCommand = getStoreCancelledOrderCommand()
        fmsOrderFactory.getFmsOrderByStoreOrder(_ as String) >> testFmsOrder

        when:
        fmsCancelledCommandService.cancelOrder(storeCancelOrderCommand)

        then:
        1 * fmsOrderDomainService.cancelFmsOrder(_ as FmsOrder, _ as String, _ as CancellationSource , _ as String) >> { FmsOrder _fmsOrder, String _cancelledReasonCode, CancellationSource _cancellationSource , String desc->
            assert _fmsOrder.storeOrderId == storeOrderId
            assert _cancellationSource == CancellationSource.STORE
            assert desc == "cancelled at store"
        }

    }

    def " Test Store Cancel without pre existing order"() {
        given:
        FmsCancelOrderCommand storeCancelledOrderCommand = getStoreCancelledOrderCommand()
        fmsOrderFactory.getFmsOrderByStoreOrder(_ as String) >> null

        when:
        fmsCancelledCommandService.cancelOrder(storeCancelledOrderCommand)

        then:
        thrown(FMSBadRequestException.class)
    }


    def " Test Store Cancel existing DELIVERED order"() {
        given:
        FmsOrder testFmsOrder = mockDeliveredFmsOrder()
        FmsCancelOrderCommand storeCancelledOrderCommand = getStoreCancelledOrderCommand()
        fmsOrderFactory.getFmsOrderByStoreOrder(_ as String) >> testFmsOrder

        when:
        fmsCancelledCommandService.cancelOrder(storeCancelledOrderCommand)

        then:
        thrown(FMSBadRequestException.class)
    }


    def " Test Store Cancel already CANCELLED order"() {
        given:
        FmsOrder testFmsOrder = mockCancelledFmsOrder()
        FmsCancelOrderCommand storeCancelledOrderCommand = getStoreCancelledOrderCommand()
        fmsOrderFactory.getFmsOrderByStoreOrder(_ as String) >> testFmsOrder

        when:
        fmsCancelledCommandService.cancelOrder(storeCancelledOrderCommand)

        then:
        thrown(FMSBadRequestException.class)
    }

    private FmsCancelOrderCommand getStoreCancelledOrderCommand() {
        return FmsCancelOrderCommand.builder()
                .data(FmsCancelOrderCommand.FmsCancelOrderCommandData.builder()
                        .storeOrderId(storeOrderId)
                        .cancelledReasonCode("STORE")
                        .cancelledReasonDescription("cancelled at store")
                        .cancellationSource(CancellationSource.STORE)
                        .build())
                .build()
    }

    FmsOrder mockFmsOrder() {
        String testCin = "4647474"
        FmsOrder fmsOrder = FmsOrder.builder()
                .tenant(Tenant.ASDA)
                .vertical(Vertical.MARKETPLACE)
                .orderState(FmsOrder.OrderStatus.PICK_COMPLETE.getName())
                .deliveryDate(new Date())
                .sourceOrderId("3333333")
                .storeOrderId(storeOrderId)
                .storeId("4401")
                .marketPlaceInfo(new MarketPlaceInfo(Vendor.UBEREATS, vendorOrderId))
                .build()

        fmsOrder.addItem(getFmsOrderItem(testCin, fmsOrder))
        return fmsOrder
    }

    private FmsOrderItem getFmsOrderItem(String testCin, FmsOrder fmsOrder) {
        return FmsOrderItem.builder().quantity(2)
                .consumerItemNumber(testCin)
                .fmsOrder(fmsOrder)
                .itemPriceInfo(new ItemPriceInfo(new Money(BigDecimal.valueOf(2.5), Currency.GBP)))
                .id(UUID.randomUUID().toString())
                .substitutionOption(SubstitutionOption.DO_NOT_SUBSTITUTE)
                .build()
    }

    FmsOrder mockDeliveredFmsOrder() {
        FmsOrder mockOrder = mockFmsOrder()
        mockOrder.orderState = "DELIVERED"
        return mockOrder
    }

    FmsOrder mockCancelledFmsOrder() {
        FmsOrder mockOrder = mockFmsOrder()
        mockOrder.orderState = "CANCELLED"
        return mockOrder
    }
}