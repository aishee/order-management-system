package com.walmart.marketplace.domain.event.listeners

import com.walmart.marketplace.domain.event.messages.MarketPlaceItemAttributes
import com.walmart.marketplace.domain.event.messages.MarketPlacePickCompleteMessage
import com.walmart.marketplace.domain.event.messages.MarketplaceBundledItemAttributes
import com.walmart.marketplace.order.domain.entity.type.Vendor
import com.walmart.marketplace.order.domain.uber.PatchCartInfo
import com.walmart.marketplace.order.gateway.IMarketPlaceGatewayFinder
import com.walmart.marketplace.order.repository.IMarketPlaceGateWay
import spock.lang.Specification

import java.util.concurrent.CompletableFuture

class PickCompletePatchCartListenerSpec extends Specification {

    String sourceOrderId = UUID.randomUUID().toString()
    String vendorOrderId = UUID.randomUUID().toString()
    String instanceId = UUID.randomUUID().toString()
    String instanceId2 = "testInstanceId2"
    String externalItemId1 = UUID.randomUUID().toString()
    String expectedItemId1 = "4888484"
    String expectedItemId2 = "4888455"
    String storeId = "4401"
    String vendorStoreId = UUID.randomUUID().toString()
    Vendor vendor = Vendor.UBEREATS
    IMarketPlaceGatewayFinder marketPlaceGatewayFinder = Mock()
    IMarketPlaceGateWay gateWay = Mock()

    PickCompletePatchCartListener listener

    def setup() {
        listener = new PickCompletePatchCartListener(marketPlaceGatewayFinder)
    }

    def "Invoke Patch Cart For Order Containing Bundled & Non-Bundled Items both having Nil Picks"() {
        given:
        List<MarketPlaceItemAttributes> marketPlaceItemAttributesList = new LinkedList<>()
        MarketPlaceItemAttributes marketPlaceItemAttributes1 = MarketPlaceItemAttributes.builder().
                orderedQuantity(5).pickedQuantity(0).itemId(expectedItemId1)
                .vendorInstanceId(instanceId2).build()
        marketPlaceItemAttributesList.add(marketPlaceItemAttributes1)
        MarketplaceBundledItemAttributes marketplaceBundledItemAttributes =
                MarketplaceBundledItemAttributes.builder().
                        bundledQuantity(2)
                        .bundleInstanceId(instanceId).build()
        List<MarketplaceBundledItemAttributes> marketplaceBundledItemAttributesList = new LinkedList<>()
        marketplaceBundledItemAttributesList.add(marketplaceBundledItemAttributes)
        MarketPlaceItemAttributes marketPlaceItemAttributes2 = MarketPlaceItemAttributes.builder().
                orderedQuantity(5)
                .pickedQuantity(2)
                .itemId(expectedItemId2)
                .marketplaceBundledItemAttributesList(marketplaceBundledItemAttributesList)
                .vendorInstanceId(instanceId).build()
        marketPlaceItemAttributesList.add(marketPlaceItemAttributes2)
        MarketPlacePickCompleteMessage marketPlacePickCompleteMessage =
                createMarketPlacePickCompleteMessage(marketPlaceItemAttributesList)
        when:
        listener.listen(marketPlacePickCompleteMessage)
        then:
        1 * marketPlaceGatewayFinder.getMarketPlaceGateway(_ as Vendor) >> { return gateWay }
        1 * gateWay.patchCart(_ as PatchCartInfo) >> { PatchCartInfo _patchCartInfo ->
            assert _patchCartInfo.vendorOrderId == marketPlacePickCompleteMessage.vendorOrderId
            assert _patchCartInfo.getNilPickInstanceIdsList().size() == 2
            assert _patchCartInfo.getNilPickInstanceIds().containsAll([instanceId, instanceId2])
            return CompletableFuture.completedFuture(true)
        }
    }

    def "Invoke Patch Cart For Order Containing Bundled & Non-Bundled Items both having Nil Picks-2"() {
        given:
        List<MarketPlaceItemAttributes> marketPlaceItemAttributesList = new LinkedList<>()
        MarketPlaceItemAttributes marketPlaceItemAttributes1 = MarketPlaceItemAttributes.builder().
                orderedQuantity(5).pickedQuantity(0).itemId(expectedItemId1)
                .vendorInstanceId(instanceId2).build()
        marketPlaceItemAttributesList.add(marketPlaceItemAttributes1)
        MarketplaceBundledItemAttributes marketplaceBundledItemAttributes =
                MarketplaceBundledItemAttributes.builder().
                        bundledQuantity(2)
                        .bundleInstanceId(instanceId).build()
        List<MarketplaceBundledItemAttributes> marketplaceBundledItemAttributesList = new LinkedList<>()
        marketplaceBundledItemAttributesList.add(marketplaceBundledItemAttributes)
        MarketPlaceItemAttributes marketPlaceItemAttributes2 = MarketPlaceItemAttributes.builder().
                orderedQuantity(5)
                .pickedQuantity(1)
                .itemId(expectedItemId2)
                .marketplaceBundledItemAttributesList(marketplaceBundledItemAttributesList)
                .vendorInstanceId(instanceId).build()
        marketPlaceItemAttributesList.add(marketPlaceItemAttributes2)
        MarketPlacePickCompleteMessage marketPlacePickCompleteMessage =
                createMarketPlacePickCompleteMessage(marketPlaceItemAttributesList)
        when:
        listener.listen(marketPlacePickCompleteMessage)
        then:
        1 * marketPlaceGatewayFinder.getMarketPlaceGateway(_ as Vendor) >> { return gateWay }
        1 * gateWay.patchCart(_ as PatchCartInfo) >> { PatchCartInfo _patchCartInfo ->
            assert _patchCartInfo.vendorOrderId == marketPlacePickCompleteMessage.vendorOrderId
            assert _patchCartInfo.getNilPickInstanceIdsList().size() == 2
            assert _patchCartInfo.getPartialPickInstanceIds().size() == 0
            assert _patchCartInfo.getNilPickInstanceIds().containsAll([instanceId, instanceId2])
            return CompletableFuture.completedFuture(true)
        }
    }

    def "Invoke Patch Cart For Order Containing Bundled & Non-Bundled Items both having Nil Picks-3"() {
        given:
        List<MarketPlaceItemAttributes> marketPlaceItemAttributesList = new LinkedList<>()
        MarketPlaceItemAttributes marketPlaceItemAttributes1 = MarketPlaceItemAttributes.builder().
                orderedQuantity(5).pickedQuantity(0).itemId(expectedItemId1)
                .vendorInstanceId(instanceId2).build()
        marketPlaceItemAttributesList.add(marketPlaceItemAttributes1)
        MarketplaceBundledItemAttributes marketplaceBundledItemAttributes =
                MarketplaceBundledItemAttributes.builder().
                        bundledQuantity(2)
                        .bundleInstanceId(instanceId).build()
        List<MarketplaceBundledItemAttributes> marketplaceBundledItemAttributesList = new LinkedList<>()
        marketplaceBundledItemAttributesList.add(marketplaceBundledItemAttributes)
        MarketPlaceItemAttributes marketPlaceItemAttributes2 = MarketPlaceItemAttributes.builder().
                orderedQuantity(5)
                .pickedQuantity(0)
                .itemId(expectedItemId2)
                .marketplaceBundledItemAttributesList(marketplaceBundledItemAttributesList)
                .vendorInstanceId(instanceId).build()
        marketPlaceItemAttributesList.add(marketPlaceItemAttributes2)
        MarketPlacePickCompleteMessage marketPlacePickCompleteMessage =
                createMarketPlacePickCompleteMessage(marketPlaceItemAttributesList)
        when:
        listener.listen(marketPlacePickCompleteMessage)
        then:
        1 * marketPlaceGatewayFinder.getMarketPlaceGateway(_ as Vendor) >> { return gateWay }
        1 * gateWay.patchCart(_ as PatchCartInfo) >> { PatchCartInfo _patchCartInfo ->
            assert _patchCartInfo.vendorOrderId == marketPlacePickCompleteMessage.vendorOrderId
            assert _patchCartInfo.getNilPickInstanceIdsList().size() == 2
            assert _patchCartInfo.getPartialPickInstanceIds().size() == 0
            assert _patchCartInfo.getNilPickInstanceIds().containsAll([instanceId, instanceId2])
            return CompletableFuture.completedFuture(true)
        }
    }

    def "Invoke Patch Cart For Order Containing Bundled & Non-Bundled Items with partial pick"() {
        given:
        List<MarketPlaceItemAttributes> marketPlaceItemAttributesList = new LinkedList<>()
        MarketPlaceItemAttributes marketPlaceItemAttributes1 = MarketPlaceItemAttributes.builder().
                orderedQuantity(5).pickedQuantity(2).itemId(expectedItemId1)
                .vendorInstanceId(instanceId2).build()
        marketPlaceItemAttributesList.add(marketPlaceItemAttributes1)
        MarketplaceBundledItemAttributes marketplaceBundledItemAttributes =
                MarketplaceBundledItemAttributes.builder().
                        bundledQuantity(2)
                        .bundleInstanceId(instanceId).build()
        List<MarketplaceBundledItemAttributes> marketplaceBundledItemAttributesList = new LinkedList<>()
        marketplaceBundledItemAttributesList.add(marketplaceBundledItemAttributes)
        MarketPlaceItemAttributes marketPlaceItemAttributes2 = MarketPlaceItemAttributes.builder().
                orderedQuantity(5)
                .pickedQuantity(3)
                .itemId(expectedItemId2)
                .marketplaceBundledItemAttributesList(marketplaceBundledItemAttributesList)
                .vendorInstanceId(instanceId).build()
        marketPlaceItemAttributesList.add(marketPlaceItemAttributes2)
        MarketPlacePickCompleteMessage marketPlacePickCompleteMessage =
                createMarketPlacePickCompleteMessage(marketPlaceItemAttributesList)
        when:
        listener.listen(marketPlacePickCompleteMessage)
        then:
        1 * marketPlaceGatewayFinder.getMarketPlaceGateway(_ as Vendor) >> { return gateWay }
        1 * gateWay.patchCart(_ as PatchCartInfo) >> { PatchCartInfo _patchCartInfo ->
            assert _patchCartInfo.vendorOrderId == marketPlacePickCompleteMessage.vendorOrderId
            assert _patchCartInfo.getNilPickInstanceIdsList().size() == 0
            assert _patchCartInfo.getPartialPickInstanceIds().size() == 2
            assert _patchCartInfo.getPartialPickInstanceIds().containsKey(instanceId2)
            assert _patchCartInfo.getPartialPickInstanceIds().containsKey(instanceId)
            return CompletableFuture.completedFuture(true)
        }
    }

    def "Invoke Patch Cart For Order Containing nil pick Bundled & partial pick Non-Bundled Items"() {
        given:
        List<MarketPlaceItemAttributes> marketPlaceItemAttributesList = new LinkedList<>()
        MarketPlaceItemAttributes marketPlaceItemAttributes1 = MarketPlaceItemAttributes.builder().
                orderedQuantity(5).pickedQuantity(2).itemId(expectedItemId1)
                .vendorInstanceId(instanceId2).build()
        marketPlaceItemAttributesList.add(marketPlaceItemAttributes1)
        MarketplaceBundledItemAttributes marketplaceBundledItemAttributes =
                MarketplaceBundledItemAttributes.builder().
                        bundledQuantity(2)
                        .bundleInstanceId(instanceId).build()
        List<MarketplaceBundledItemAttributes> marketplaceBundledItemAttributesList = new LinkedList<>()
        marketplaceBundledItemAttributesList.add(marketplaceBundledItemAttributes)
        MarketPlaceItemAttributes marketPlaceItemAttributes2 = MarketPlaceItemAttributes.builder().
                orderedQuantity(5)
                .pickedQuantity(0)
                .itemId(expectedItemId2)
                .marketplaceBundledItemAttributesList(marketplaceBundledItemAttributesList)
                .vendorInstanceId(instanceId).build()
        marketPlaceItemAttributesList.add(marketPlaceItemAttributes2)
        MarketPlacePickCompleteMessage marketPlacePickCompleteMessage =
                createMarketPlacePickCompleteMessage(marketPlaceItemAttributesList)
        when:
        listener.listen(marketPlacePickCompleteMessage)
        then:
        1 * marketPlaceGatewayFinder.getMarketPlaceGateway(_ as Vendor) >> { return gateWay }
        1 * gateWay.patchCart(_ as PatchCartInfo) >> { PatchCartInfo _patchCartInfo ->
            assert _patchCartInfo.vendorOrderId == marketPlacePickCompleteMessage.vendorOrderId
            assert _patchCartInfo.getNilPickInstanceIdsList().size() == 1
            assert _patchCartInfo.getPartialPickInstanceIds().size() == 1
            assert _patchCartInfo.getPartialPickInstanceIds().containsKey(instanceId2)
            assert _patchCartInfo.getNilPickInstanceIds().contains(instanceId)
            return CompletableFuture.completedFuture(true)
        }
    }

    def "Invoke Patch Cart For Order Containing Bundled & Non-Bundled Items with nil and partial pick"() {
        given:
        List<MarketPlaceItemAttributes> marketPlaceItemAttributesList = new LinkedList<>()
        MarketPlaceItemAttributes nilPickMarketPlaceItemAttributes = MarketPlaceItemAttributes.builder().
                orderedQuantity(5).pickedQuantity(0).itemId(expectedItemId1)
                .vendorInstanceId(instanceId2).build()
        marketPlaceItemAttributesList.add(nilPickMarketPlaceItemAttributes)
        MarketplaceBundledItemAttributes marketplaceBundledItemAttributes =
                MarketplaceBundledItemAttributes.builder().
                        bundledQuantity(2)
                        .bundleInstanceId(instanceId).build()
        List<MarketplaceBundledItemAttributes> marketplaceBundledItemAttributesList = new LinkedList<>()
        marketplaceBundledItemAttributesList.add(marketplaceBundledItemAttributes)
        MarketPlaceItemAttributes partialPickMarketPlaceItemAttributes = MarketPlaceItemAttributes.builder().
                orderedQuantity(5)
                .pickedQuantity(3)
                .itemId(expectedItemId2)
                .marketplaceBundledItemAttributesList(marketplaceBundledItemAttributesList)
                .vendorInstanceId(instanceId).build()
        marketPlaceItemAttributesList.add(partialPickMarketPlaceItemAttributes)
        MarketPlacePickCompleteMessage marketPlacePickCompleteMessage =
                createMarketPlacePickCompleteMessage(marketPlaceItemAttributesList)
        when:
        listener.listen(marketPlacePickCompleteMessage)
        then:
        1 * marketPlaceGatewayFinder.getMarketPlaceGateway(_ as Vendor) >> { return gateWay }
        1 * gateWay.patchCart(_ as PatchCartInfo) >> { PatchCartInfo _patchCartInfo ->
            assert _patchCartInfo.vendorOrderId == marketPlacePickCompleteMessage.vendorOrderId
            assert _patchCartInfo.getNilPickInstanceIdsList().size() == 1
            assert _patchCartInfo.getPartialPickInstanceIds().size() == 1
            assert _patchCartInfo.getPartialPickInstanceIds().containsKey(instanceId)
            assert _patchCartInfo.getPartialPickInstanceIds().get(instanceId) == 1
            assert _patchCartInfo.getNilPickInstanceIds().contains(instanceId2)
            return CompletableFuture.completedFuture(true)
        }
    }

    def "Don't Invoke Patch Cart For Order Containing Bundled & Non-Bundled Items both fulfilled"() {
        given:
        List<MarketPlaceItemAttributes> marketPlaceItemAttributesList = new LinkedList<>()
        MarketPlaceItemAttributes nilPickMarketPlaceItemAttributes = MarketPlaceItemAttributes.builder().
                orderedQuantity(5).pickedQuantity(5).itemId(expectedItemId1)
                .vendorInstanceId(instanceId2).build()
        marketPlaceItemAttributesList.add(nilPickMarketPlaceItemAttributes)
        MarketplaceBundledItemAttributes marketplaceBundledItemAttributes =
                MarketplaceBundledItemAttributes.builder().
                        bundledQuantity(2)
                        .bundleInstanceId(instanceId).build()
        List<MarketplaceBundledItemAttributes> marketplaceBundledItemAttributesList = new LinkedList<>()
        marketplaceBundledItemAttributesList.add(marketplaceBundledItemAttributes)
        MarketPlaceItemAttributes partialPickMarketPlaceItemAttributes = MarketPlaceItemAttributes.builder().
                orderedQuantity(2)
                .pickedQuantity(2)
                .itemId(expectedItemId2)
                .marketplaceBundledItemAttributesList(marketplaceBundledItemAttributesList)
                .vendorInstanceId(instanceId).build()
        marketPlaceItemAttributesList.add(partialPickMarketPlaceItemAttributes)
        MarketPlacePickCompleteMessage marketPlacePickCompleteMessage =
                createMarketPlacePickCompleteMessage(marketPlaceItemAttributesList)
        when:
        listener.listen(marketPlacePickCompleteMessage)
        then:
        0 * marketPlaceGatewayFinder.getMarketPlaceGateway(_ as Vendor) >> { return gateWay }
        0 * gateWay.patchCart(_ as PatchCartInfo) >> { PatchCartInfo _patchCartInfo ->
            return CompletableFuture.completedFuture(true)
        }
    }

    def "Don't Invoke Patch Cart For Order Containing Only Bundled Items"() {
        given:
        List<MarketPlaceItemAttributes> marketPlaceItemAttributesList = new LinkedList<>();
        MarketplaceBundledItemAttributes marketplaceBundledItemAttributes =
                MarketplaceBundledItemAttributes.builder().
                        bundledQuantity(5)
                        .bundleInstanceId(instanceId).build()
        List<MarketplaceBundledItemAttributes> marketplaceBundledItemAttributesList = new LinkedList<>()
        marketplaceBundledItemAttributesList.add(marketplaceBundledItemAttributes)
        MarketPlaceItemAttributes partialPickMarketPlaceItemAttributes = MarketPlaceItemAttributes.builder().
                orderedQuantity(5)
                .pickedQuantity(3)
                .itemId(expectedItemId2)
                .marketplaceBundledItemAttributesList(marketplaceBundledItemAttributesList)
                .vendorInstanceId(instanceId).build()
        marketPlaceItemAttributesList.add(partialPickMarketPlaceItemAttributes)
        MarketPlacePickCompleteMessage marketPlacePickCompleteMessage =
                createMarketPlacePickCompleteMessage(marketPlaceItemAttributesList)
        when:
        listener.listen(marketPlacePickCompleteMessage)
        then:
        0 * marketPlaceGatewayFinder.getMarketPlaceGateway(_ as Vendor) >> { return gateWay }
        0 * gateWay.patchCart(_ as PatchCartInfo) >> { PatchCartInfo _patchCartInfo ->
            return CompletableFuture.completedFuture(true)
        }
    }

    def "When patch cart is invoked successfully"() {
        given:
        List<MarketPlaceItemAttributes> marketPlaceItemAttributesList = new LinkedList<>();
        MarketPlaceItemAttributes marketPlaceItemAttributes1 = MarketPlaceItemAttributes.builder().
                orderedQuantity(5).pickedQuantity(0).itemId(expectedItemId1)
                .vendorInstanceId(instanceId).build()
        marketPlaceItemAttributesList.add(marketPlaceItemAttributes1)
        MarketPlaceItemAttributes marketPlaceItemAttributes2 = MarketPlaceItemAttributes.builder().
                orderedQuantity(5).pickedQuantity(2).itemId(expectedItemId2)
                .vendorInstanceId(instanceId).build()
        marketPlaceItemAttributesList.add(marketPlaceItemAttributes2)
        MarketPlacePickCompleteMessage marketPlacePickCompleteMessage =
                createMarketPlacePickCompleteMessage(marketPlaceItemAttributesList)

        when:
        listener.listen(marketPlacePickCompleteMessage)

        then:
        1 * marketPlaceGatewayFinder.getMarketPlaceGateway(_ as Vendor) >> { return gateWay }
        1 * gateWay.patchCart(_ as PatchCartInfo) >> { PatchCartInfo _patchCartInfo ->
            assert _patchCartInfo.vendorOrderId == marketPlacePickCompleteMessage.vendorOrderId
            return CompletableFuture.completedFuture(true)
        }
    }

    def "When patch cart is not invoked for bundled items"() {
        given:
        List<MarketPlaceItemAttributes> marketPlaceItemAttributesList = new LinkedList<>()
        List<MarketplaceBundledItemAttributes> marketplaceBundledItemAttributesList = new LinkedList<>()
        MarketplaceBundledItemAttributes marketplaceBundledItemAttributes =
                MarketplaceBundledItemAttributes.builder()
                        .bundleInstanceId(instanceId).bundledQuantity(5)
                        .build()
        marketplaceBundledItemAttributesList.add(marketplaceBundledItemAttributes)
        MarketPlaceItemAttributes marketPlaceItemAttributes2 = MarketPlaceItemAttributes.builder().
                orderedQuantity(5).pickedQuantity(2).itemId(expectedItemId2)
                .marketplaceBundledItemAttributesList(marketplaceBundledItemAttributesList)
                .vendorInstanceId(instanceId).build()
        marketPlaceItemAttributesList.add(marketPlaceItemAttributes2)
        MarketPlacePickCompleteMessage marketPlacePickCompleteMessage =
                createMarketPlacePickCompleteMessage(marketPlaceItemAttributesList)

        when:
        listener.listen(marketPlacePickCompleteMessage)

        then:
        0 * marketPlaceGatewayFinder.getMarketPlaceGateway(_ as Vendor) >> { return gateWay }
        0 * gateWay.patchCart(_ as PatchCartInfo) >> { PatchCartInfo _patchCartInfo ->
            assert _patchCartInfo.vendorOrderId == marketPlacePickCompleteMessage.vendorOrderId
            return CompletableFuture.completedFuture(true)
        }
    }

    def "When there are no nil or partial picks"() {
        given:
        List<MarketPlaceItemAttributes> marketPlaceItemAttributesList = new LinkedList<>();
        MarketPlaceItemAttributes marketPlaceItemAttributes1 = MarketPlaceItemAttributes.builder().
                orderedQuantity(5).pickedQuantity(5).itemId(expectedItemId1)
                .vendorInstanceId(instanceId).build()
        marketPlaceItemAttributesList.add(marketPlaceItemAttributes1)
        MarketPlacePickCompleteMessage marketPlacePickCompleteMessage =
                createMarketPlacePickCompleteMessage(marketPlaceItemAttributesList)

        when:
        listener.listen(marketPlacePickCompleteMessage)

        then:
        0 * marketPlaceGatewayFinder.getMarketPlaceGateway(_ as Vendor) >> { return gateWay }
        0 * gateWay.patchCart(_ as PatchCartInfo) >> { PatchCartInfo _patchCartInfo ->
            assert _patchCartInfo.vendorOrderId == marketPlacePickCompleteMessage.vendorOrderId
            return CompletableFuture.completedFuture(true)
        }
    }

    def "When patch cart is invoked for TEST VENDOR"() {
        given:
        List<MarketPlaceItemAttributes> marketPlaceItemAttributesList = new LinkedList<>();
        MarketPlaceItemAttributes marketPlaceItemAttributes1 = MarketPlaceItemAttributes.builder().
                orderedQuantity(5).pickedQuantity(5).itemId(expectedItemId1)
                .vendorInstanceId(instanceId).build()
        marketPlaceItemAttributesList.add(marketPlaceItemAttributes1)
        MarketPlacePickCompleteMessage marketPlacePickCompleteMessage =
                createMarketPlacePickCompleteMessage(marketPlaceItemAttributesList)
        marketPlacePickCompleteMessage.vendorId = Vendor.TESTVENDOR

        when:
        listener.listen(marketPlacePickCompleteMessage)

        then:
        0 * marketPlaceGatewayFinder.getMarketPlaceGateway(_ as Vendor)
        0 * gateWay.patchCart(_ as PatchCartInfo)
    }

    private MarketPlacePickCompleteMessage createMarketPlacePickCompleteMessage(
            List<MarketPlaceItemAttributes> marketPlaceItemAttributesList) {
        return MarketPlacePickCompleteMessage.builder()
                .marketPlaceItemAttributes(marketPlaceItemAttributesList).sourceOrderId(sourceOrderId)
                .vendorOrderId(vendorOrderId).storeId(storeId).vendorId(vendor).build()
    }
}
