package com.walmart.oms.infrastructure.repository

import com.walmart.oms.infrastructure.configuration.OmsOrderConfig
import com.walmart.oms.order.aggregateroot.OmsOrder
import com.walmart.oms.order.domain.model.CreateDateSearchQuery

import com.walmart.oms.order.repository.IOmsOrderRepository
import spock.lang.Specification

import java.time.LocalDateTime

class OmsOrderReplayRepositoryImplTest extends Specification {
    String sourceOrderId = UUID.randomUUID().toString()
    IOmsOrderRepository omsOrderRepository = Mock()
    OrderUpdateEventPublisher orderUpdateEventPublisher = Mock()
    OmsOrderConfig omsOrderConfig = Mock()
    OmsOrderReplayRepositoryImpl omsOrderReplayService

    def setup() {
        omsOrderReplayService = new OmsOrderReplayRepositoryImpl(
                omsOrderRepository: omsOrderRepository,
                orderUpdateEventPublisher: orderUpdateEventPublisher,
                omsOrderConfig: omsOrderConfig
        )
    }

    def "test findAllOrderAndReplayToDwhKafkaTopic with single page"() {
        given:
        LocalDateTime createStartDateTime = LocalDateTime.now()
        LocalDateTime createEndDateTime = LocalDateTime.now()
        OmsOrder omsOrder = getOmsOrder("INITIAL")
        omsOrderConfig.getMaxOrderFetchLimit() >> 10
        List<OmsOrder> omsOrders = Arrays.asList(omsOrder)
        omsOrderRepository.findAllOrderByCreatedDateRange(mockSearchQuery(0)) >> omsOrders

        when:
        omsOrderReplayService.findAllOrderAndReplayToDwhKafkaTopic(createStartDateTime, createEndDateTime)

        then:
        1 * omsOrderRepository.findAllOrderByCreatedDateRange(_ as CreateDateSearchQuery) >> {
            return omsOrders
        }
        1 * orderUpdateEventPublisher.emitOrderUpdateEvent(_ as OmsOrder)
        omsOrders.size() == 1


    }

    def "test findAllOrderAndReplayToDwhKafkaTopic with multiple pages"() {
        given:
        LocalDateTime createStartDateTime = LocalDateTime.now()
        LocalDateTime createEndDateTime = LocalDateTime.now()
        OmsOrder omsOrder = getOmsOrder("INITIAL")
        omsOrderConfig.getMaxOrderFetchLimit() >> 10
        List<OmsOrder> omsOrders = Arrays.asList(omsOrder, omsOrder, omsOrder,
                omsOrder, omsOrder, omsOrder, omsOrder, omsOrder, omsOrder, omsOrder)
        List<OmsOrder> omsOrders1 = Arrays.asList(omsOrder, omsOrder, omsOrder,
                omsOrder, omsOrder, omsOrder, omsOrder, omsOrder, omsOrder)
        omsOrderRepository.findAllOrderByCreatedDateRange(mockSearchQuery(0)) >> omsOrders
        omsOrderRepository.findAllOrderByCreatedDateRange(mockSearchQuery(1)) >> omsOrders1

        when:
        omsOrderReplayService.findAllOrderAndReplayToDwhKafkaTopic(createStartDateTime, createEndDateTime)

        then:
        1 * omsOrderRepository.findAllOrderByCreatedDateRange(_ as CreateDateSearchQuery) >> {
            return omsOrders
        }
        1 * omsOrderRepository.findAllOrderByCreatedDateRange(_ as CreateDateSearchQuery) >> {
            return omsOrders1
        }
        19 * orderUpdateEventPublisher.emitOrderUpdateEvent(_ as OmsOrder)
        omsOrders.size() == 10
        omsOrders1.size() == 9


    }

    def "test findAllOrderAndReplayToDwhKafkaTopic exceptionally"() {
        given:
        LocalDateTime createStartDateTime = LocalDateTime.now()
        omsOrderConfig.getMaxOrderFetchLimit() >> 10

        when:
        omsOrderReplayService.findAllOrderAndReplayToDwhKafkaTopic(createStartDateTime, null)

        then:
        thrown(RuntimeException)


    }

    def "test findAllOrderAndReplayToDwhKafkaTopic with exception in publish"() {
        given:
        LocalDateTime createStartDateTime = LocalDateTime.now()
        LocalDateTime createEndDateTime = LocalDateTime.now()
        OmsOrder omsOrder = getOmsOrder("INITIAL")
        omsOrderConfig.getMaxOrderFetchLimit() >> 10
        List<OmsOrder> omsOrders = Arrays.asList(omsOrder)
        omsOrderRepository.findAllOrderByCreatedDateRange(mockSearchQuery(0)) >> omsOrders
        orderUpdateEventPublisher.emitOrderUpdateEvent(omsOrder) >> { throw new RuntimeException("Any Exception") }

        when:
        omsOrderReplayService.findAllOrderAndReplayToDwhKafkaTopic(createStartDateTime, createEndDateTime)

        then:
        1 * omsOrderRepository.findAllOrderByCreatedDateRange(_ as CreateDateSearchQuery) >> {
            return omsOrders
        }
        omsOrders.size() == 1

    }


    private OmsOrder getOmsOrder(String orderState) {
        return OmsOrder.builder()
                .sourceOrderId(sourceOrderId)
                .storeOrderId("123456789")
                .storeId("4401")
                .deliveryDate(new Date())
                .storeId("1234")
                .orderState(orderState).build()
    }

    private static CreateDateSearchQuery mockSearchQuery(int pageNumber) {
        return CreateDateSearchQuery.builder()
                .pageNumber(pageNumber)
                .createStartDateTime(LocalDateTime.now())
                .createEndDateTime(LocalDateTime.now())
                .build()
    }
}
