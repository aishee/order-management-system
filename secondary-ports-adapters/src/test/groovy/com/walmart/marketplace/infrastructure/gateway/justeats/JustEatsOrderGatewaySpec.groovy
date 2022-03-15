package com.walmart.marketplace.infrastructure.gateway.justeats

import com.walmart.marketplace.infrastructure.gateway.justeats.dto.request.DenialErrorCode
import com.walmart.marketplace.order.aggregateroot.MarketPlaceOrder
import com.walmart.marketplace.order.domain.entity.UpdateItemInfo
import com.walmart.marketplace.order.domain.uber.MarketPlaceReportRequest
import com.walmart.marketplace.order.domain.uber.PatchCartInfo
import com.walmart.marketplace.repository.MarketPlaceRepository
import com.walmart.oms.domain.error.exception.OMSBadRequestException
import spock.lang.Specification

import java.util.concurrent.CompletableFuture

class JustEatsOrderGatewaySpec extends Specification {

    JustEatsOrderStatusUpdateClient justEatsOrderStatusUpdateClient = Mock()
    JustEatsItemAvailabilityUpdateClient justEatsItemAvailabilityUpdateClient = Mock()
    JustEatsOrderGateway justEatsOrderGateway = Mock()
    MarketPlaceRepository marketPlaceRepository = Mock()

    def setup() {
        justEatsOrderGateway = new JustEatsOrderGateway(
                justEatsOrderStatusUpdateClient: justEatsOrderStatusUpdateClient,
                justEatsItemAvailabilityUpdateClient: justEatsItemAvailabilityUpdateClient,
                marketPlaceRepository: marketPlaceRepository
        )
    }

    def "When Valid Order ID is passed to Accept JustEats Order"() {
        given:
        MarketPlaceOrder marketPlaceOrder = Mock()
        marketPlaceOrder.getVendorNativeOrderId() >> { return "abc" }
        justEatsOrderStatusUpdateClient.acceptOrder(_ as String) >> { return true }
        when:
        boolean validOrderAccepted = justEatsOrderGateway.acceptOrder(marketPlaceOrder)
        then:
        validOrderAccepted
    }

    def "When Valid Order ID is passed to Reject JustEats Order"() {
        given:
        MarketPlaceOrder marketPlaceOrder = Mock()
        marketPlaceOrder.getVendorNativeOrderId() >> { return "abc" }
        justEatsOrderStatusUpdateClient.rejectOrder(_ as String, _ as DenialErrorCode) >> { return true }
        when:
        boolean validOrderAccepted = justEatsOrderGateway.rejectOrder(marketPlaceOrder, "IN_USE")
        then:
        validOrderAccepted
    }

    def "Update Item API Invocation"() {
        given:
        UpdateItemInfo updateItemInfo = Mock()
        justEatsItemAvailabilityUpdateClient.updateItemInfo(_ as UpdateItemInfo) >> { return Collections.singletonList(true) }
        when:
        CompletableFuture<List<Boolean>> updateItemStatus = justEatsOrderGateway.updateItem(updateItemInfo)
        then:
        updateItemStatus.isDone()
        updateItemStatus.get().get(0)
    }

    def "Get Order from Repository"() {
        given:
        MarketPlaceOrder marketPlaceOrder = Mock()
        marketPlaceRepository.get(_ as String) >> { return marketPlaceOrder }
        when:
        MarketPlaceOrder fetchedOrder = justEatsOrderGateway.getOrder("12498", "not useful")
        then:
        fetchedOrder == marketPlaceOrder
    }

    def "Cancel Order API invoked for JustEats"() {
        when:
        boolean defaultCancelOrderResult = justEatsOrderGateway.cancelOrder("alfhisal", "IN_USE")
        then:
        defaultCancelOrderResult
    }

    def "Patch Cart API invoked for JustEats"() {
        when:
        CompletableFuture<Boolean> defaultPatchCartResult = justEatsOrderGateway.patchCart(Mock(PatchCartInfo.class))
        then:
        defaultPatchCartResult.get()
    }

    def "Report API invoked for JustEats"() {
        when:
        justEatsOrderGateway.invokeMarketPlaceReport(Mock(MarketPlaceReportRequest.class))
        then:
        thrown OMSBadRequestException
    }
}
