package com.walmart.oms.controller

import com.walmart.common.domain.type.Tenant
import com.walmart.common.domain.type.Vertical
import com.walmart.common.metrics.MetricService
import com.walmart.oms.OmsOrderApplicationService
import com.walmart.oms.converter.OmsResponseMapper
import com.walmart.oms.domain.error.Error
import com.walmart.oms.domain.error.ErrorResponse
import com.walmart.oms.domain.error.ErrorType
import com.walmart.oms.domain.error.exception.OMSBadRequestException
import com.walmart.oms.dto.OmsOrderDto
import com.walmart.oms.dto.OmsOrderResponse
import com.walmart.oms.order.aggregateroot.OmsOrder
import spock.lang.Specification

class OrderControllerTest extends Specification {

    OmsResponseMapper omsResponseMapper = Mock()

    OmsOrderApplicationService orderApplicationService = Mock()

    OrderController orderController

    MetricService metricService = Mock();


    def setup() {
        orderController = new OrderController(
                orderApplicationService: orderApplicationService,
                omsResponseMapper: omsResponseMapper,
                metricService: metricService)
    }

    def "Test Get order"() {

        given:

        String sourceOrderId = UUID.randomUUID().toString()
        Tenant tenant = Tenant.ASDA
        Vertical vertical = Vertical.MARKETPLACE

        OmsOrderResponse expectedResponse = OmsOrderResponse.builder()
                .data(OmsOrderResponse.OmsOrderResponseData.builder()
                        .order(OmsOrderDto.builder()
                                .externalOrderId(sourceOrderId)
                                .build())
                        .build())
                .build()

        when:
        OmsOrderResponse actualResponse = orderController.getOrder(sourceOrderId, tenant, vertical)

        then:
        1 * orderApplicationService.getOrder(_ as String, _ as Tenant, _ as Vertical) >> new OmsOrder("CREATED")
        1 * omsResponseMapper.convertToOrderResponse(_ as OmsOrder) >> expectedResponse

        assert actualResponse == expectedResponse
    }

    def "Test Get order with no order present"() {

        given:

        String sourceOrderId = UUID.randomUUID().toString()
        Tenant tenant = Tenant.ASDA
        Vertical vertical = Vertical.MARKETPLACE

        OmsOrderResponse expectedResponse = OmsOrderResponse.builder()
                .data(OmsOrderResponse.OmsOrderResponseData.builder()
                        .order(OmsOrderDto.builder()
                                .externalOrderId(sourceOrderId)
                                .build())
                        .build())
                .build()

        when:
        orderController.getOrder(sourceOrderId, tenant, vertical)

        then:
        1 * orderApplicationService.getOrder(_ as String, _ as Tenant, _ as Vertical) >> {
            throw new OMSBadRequestException("Unable to find order for id:" + sourceOrderId)
        }
        0 * omsResponseMapper.convertToOrderResponse(_ as OmsOrder) >> expectedResponse

        OMSBadRequestException omsBadRequestException = thrown()

        ErrorResponse errorResponse = omsBadRequestException.errorResponse
        errorResponse.errors.size() == 1

        Error error = errorResponse.errors.first()
        error.code == 400
        error.type == ErrorType.INVALID_REQUEST_EXCEPTION
        error.message == "Unable to find order for id:" + sourceOrderId
    }
}
