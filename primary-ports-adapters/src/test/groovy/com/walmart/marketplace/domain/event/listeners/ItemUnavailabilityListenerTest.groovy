package com.walmart.marketplace.domain.event.listeners

import com.walmart.fms.domain.event.message.ItemUnavailabilityMessage
import com.walmart.marketplace.MarketPlaceApplicationService
import com.walmart.marketplace.order.aggregateroot.MarketPlaceOrder
import com.walmart.marketplace.order.domain.entity.UpdateItemInfo
import com.walmart.marketplace.order.domain.entity.type.Vendor
import com.walmart.marketplace.order.gateway.IMarketPlaceGatewayFinder
import com.walmart.marketplace.order.repository.IMarketPlaceGateWay
import com.walmart.oms.domain.error.exception.OMSBadRequestException
import spock.lang.Specification

import java.util.concurrent.CompletableFuture

class ItemUnavailabilityListenerTest extends Specification {

    IMarketPlaceGatewayFinder marketPlaceGatewayFinder = Mock()
    MarketPlaceApplicationService marketPlaceApplicationService = Mock()
    IMarketPlaceGateWay gateWay = Mock()
    ItemUnavailabilityListener listener

    def setup() {
        listener = new ItemUnavailabilityListener(marketPlaceGatewayFinder, marketPlaceApplicationService)
    }

    def "Item unavailability API success scenario"() {

        given:
        ItemUnavailabilityMessage itemUnavailabilityMessage = mockItemUnavailabilityMessage(Vendor.JUSTEAT)
        MarketPlaceOrder marketPlaceOrder = Mock()
        marketPlaceOrder.getVendorStoreId() >> { return "5756" }

        when:
        listener.listen(itemUnavailabilityMessage)

        then:
        1 * marketPlaceApplicationService.getOrder(_ as String) >> { return marketPlaceOrder }
        1 * marketPlaceGatewayFinder.getMarketPlaceGateway(_ as Vendor) >> { return gateWay }
        1 * gateWay.updateItem(_ as UpdateItemInfo) >> { UpdateItemInfo _updateItemInfo ->
            assert _updateItemInfo.vendorOrderId == "1231241413"
            assert _updateItemInfo.getStoreId() == "5755"
            assert _updateItemInfo.getVendorStoreId() == "5756"
            assert _updateItemInfo.getOutOfStockItemIds().size() == 1
            assert _updateItemInfo.getOutOfStockItemIds().get(0).equalsIgnoreCase("1234")
            return CompletableFuture.completedFuture(true)

        }
    }

    def "Item unavailability API failure due to Invalid order"() {

        given:
        ItemUnavailabilityMessage itemUnavailabilityMessage = mockItemUnavailabilityMessage(Vendor.JUSTEAT)

        when:
        listener.listen(itemUnavailabilityMessage)

        then:
        thrown(OMSBadRequestException)
        1 * marketPlaceApplicationService.getOrder(_ as String) >> { throw new OMSBadRequestException("Invalid order id") }
        0 * marketPlaceGatewayFinder.getMarketPlaceGateway(_ as Vendor) >> { return gateWay }
        0 * gateWay.updateItem(_ as UpdateItemInfo) >> { UpdateItemInfo _updateItemInfo ->
            assert _updateItemInfo.vendorOrderId == "1231241413"
            assert _updateItemInfo.getStoreId() == "5755"
            assert _updateItemInfo.getVendorStoreId() == "5756"
            assert _updateItemInfo.getOutOfStockItemIds().size() == 1
            assert _updateItemInfo.getOutOfStockItemIds().get(0).equalsIgnoreCase("1234")
            return CompletableFuture.completedFuture(true)

        }
    }

    def "Item unavailability API not triggered for Test vendor scenario"() {

        given:
        ItemUnavailabilityMessage itemUnavailabilityMessage = mockItemUnavailabilityMessage(Vendor.TESTVENDOR)
        MarketPlaceOrder marketPlaceOrder = Mock()
        marketPlaceOrder.getVendorStoreId() >> { return "5756" }

        when:
        listener.listen(itemUnavailabilityMessage)

        then:
        0 * marketPlaceApplicationService.getOrder(_ as String) >> { return marketPlaceOrder }
        0 * marketPlaceGatewayFinder.getMarketPlaceGateway(_ as Vendor) >> { return gateWay }
        0 * gateWay.updateItem(_ as UpdateItemInfo) >> { UpdateItemInfo _updateItemInfo ->
            assert _updateItemInfo.vendorOrderId == "1231241413"
            assert _updateItemInfo.getStoreId() == "5755"
            assert _updateItemInfo.getVendorStoreId() == "5756"
            assert _updateItemInfo.getOutOfStockItemIds().size() == 1
            assert _updateItemInfo.getOutOfStockItemIds().get(0).equalsIgnoreCase("1234")
            return CompletableFuture.completedFuture(true)

        }
    }

    private static ItemUnavailabilityMessage mockItemUnavailabilityMessage(Vendor vendor) {
        return ItemUnavailabilityMessage.builder().storeId("5755").storeOrderId("123455677").vendorId(vendor).vendorOrderId("1231241413").outOfStockItemIds(Collections.singletonList("1234")).build()
    }

}