package com.walmart.fms.controller

import com.walmart.common.domain.type.Tenant
import com.walmart.common.domain.type.Vertical
import com.walmart.common.metrics.MetricService
import com.walmart.fms.FmsOrderApplicationService
import com.walmart.fms.converter.FmsResponseMapper
import com.walmart.fms.domain.error.Error
import com.walmart.fms.domain.error.ErrorResponse
import com.walmart.fms.domain.error.ErrorType
import com.walmart.fms.domain.error.exception.FMSBadRequestException
import com.walmart.fms.dto.FmsOrderDto
import com.walmart.fms.dto.FmsOrderResponse
import com.walmart.fms.order.aggregateroot.FmsOrder
import spock.lang.Specification

class FmsOrderControllerTest extends Specification {

    FmsResponseMapper fmsResponseMapper = Mock()

    FmsOrderApplicationService fmsOrderApplicationService = Mock()

    FmsOrderController orderController

    MetricService metricService = Mock()


    def setup() {
        orderController = new FmsOrderController(
                fmsOrderApplicationService: fmsOrderApplicationService,
                responseMapper: fmsResponseMapper,
                metricService: metricService)

    }

    def "Test Get order"() {

        given:

        String sourceOrderId = UUID.randomUUID().toString()
        Tenant tenant = Tenant.ASDA
        Vertical vertical = Vertical.MARKETPLACE

        FmsOrderResponse expectedResponse = FmsOrderResponse.builder()
                .data(FmsOrderResponse.FmsOrderResponseData.builder()
                .order(FmsOrderDto.builder()
                .vendorOrderId(sourceOrderId)
                .build())
                .build())
                .build()

        when:
        FmsOrderResponse actualResponse = orderController.getOrder(sourceOrderId, tenant, vertical)

        then:
        1 * fmsOrderApplicationService.getOrder(_ as String, _ as Tenant, _ as Vertical) >> new FmsOrder("CREATED")
        1 * fmsResponseMapper.convertToOrderResponse(_ as FmsOrder) >> expectedResponse

        assert actualResponse == expectedResponse
    }

    def "Test Get order with no order present"() {

        given:

        String sourceOrderId = UUID.randomUUID().toString()
        Tenant tenant = Tenant.ASDA
        Vertical vertical = Vertical.MARKETPLACE

        FmsOrderResponse expectedResponse = FmsOrderResponse.builder()
                .data(FmsOrderResponse.FmsOrderResponseData.builder()
                .order(FmsOrderDto.builder()
                .vendorOrderId(sourceOrderId)
                .build())
                .build())
                .build()

        when:
        orderController.getOrder(sourceOrderId, tenant, vertical)

        then:
        1 * fmsOrderApplicationService.getOrder(_ as String, _ as Tenant, _ as Vertical) >> {
            throw new FMSBadRequestException("Unable to find order for id:" +sourceOrderId)
        }
        0 * fmsResponseMapper.convertToOrderResponse(_ as FmsOrder) >> expectedResponse

        FMSBadRequestException fmsBadRequestException = thrown()

        ErrorResponse errorResponse = fmsBadRequestException.errorResponse
        errorResponse.errors.size() == 1

        Error error = errorResponse.errors.first()
        error.code == 400
        error.type == ErrorType.INVALID_REQUEST_EXCEPTION
        error.message == "Unable to find order for id:" + sourceOrderId
    }
}
