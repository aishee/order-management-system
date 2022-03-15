package com.walmart.marketplace.order.domain.uber.mapper

import com.walmart.common.domain.type.CancellationSource
import com.walmart.fms.domain.event.message.ItemUnavailabilityMessage
import com.walmart.marketplace.domain.event.messages.MarketPlaceItemAttributes
import com.walmart.marketplace.domain.event.messages.MarketPlaceOrderCancelMessage
import com.walmart.marketplace.domain.event.messages.MarketPlacePickCompleteMessage
import com.walmart.marketplace.order.domain.entity.UpdateItemInfo
import com.walmart.marketplace.order.domain.entity.type.Vendor
import spock.lang.Specification

class UpdateItemInfoMapperTest extends Specification {

    String sourceOrderId = UUID.randomUUID().toString()
    String vendorOrderId = UUID.randomUUID().toString()
    String externalItemId1 = UUID.randomUUID().toString()
    String expectedItemId1 = "4888484"
    String expectedItemId2 = "4888455"
    String storeId = "4401"
    String vendorStoreId = UUID.randomUUID().toString()
    String instanceId = UUID.randomUUID().toString()
    Vendor vendor = Vendor.UBEREATS
    UpdateItemInfoMapper updateItemInfoMapper

    def setup() {
        updateItemInfoMapper = new UpdateItemInfoMapperImpl()
    }

    def "Successful mapping with convertToUpdateItemInfo"() {
        given:
        List<MarketPlaceItemAttributes> marketPlaceItemAttributesList = new LinkedList<>();
        MarketPlaceItemAttributes marketPlaceItemAttributes1 = MarketPlaceItemAttributes.builder().
                orderedQuantity(5).pickedQuantity(1).
                vendorInstanceId(instanceId).itemId(externalItemId1).externalItemId(externalItemId1).build()
        MarketPlaceItemAttributes marketPlaceItemAttributes2 = MarketPlaceItemAttributes.builder().
                orderedQuantity(5).pickedQuantity(0).
                vendorInstanceId(instanceId).itemId(expectedItemId2).externalItemId(externalItemId1).build()
        marketPlaceItemAttributesList.add(marketPlaceItemAttributes1)
        marketPlaceItemAttributesList.add(marketPlaceItemAttributes2)
        MarketPlacePickCompleteMessage marketPlacePickCompleteMessage =
                createMarketPlacePickCompleteMessage(marketPlaceItemAttributesList)

        when:
        UpdateItemInfo updateItemInfo = updateItemInfoMapper.convertToUpdateItemInfo(marketPlacePickCompleteMessage)

        then:
        assert updateItemInfo.getVendorId() == vendor
        assert updateItemInfo.getVendorStoreId() == vendorStoreId
        assert updateItemInfo.getOutOfStockItemsCount() == 2
        assert updateItemInfo.getOutOfStockItemIds().get(0) == externalItemId1
        assert updateItemInfo.getStoreId() == storeId
        assert updateItemInfo.suspendUntil != 0
        assert updateItemInfo.vendorOrderId == marketPlacePickCompleteMessage.getVendorOrderId()
        assert updateItemInfo.reason == "OUT_OF_STOCK"
        assert updateItemInfo.isValidVendor()
        assert updateItemInfo.containsOutOfStockItems()
    }

    def "Successful mapping with convertToUpdateItemInfo test vendor"() {
        given:
        List<MarketPlaceItemAttributes> marketPlaceItemAttributesList = new LinkedList<>();
        MarketPlaceItemAttributes marketPlaceItemAttributes1 = MarketPlaceItemAttributes.builder().
                orderedQuantity(5).pickedQuantity(1).
                vendorInstanceId(instanceId).itemId(expectedItemId2).externalItemId(externalItemId1).build()
        MarketPlaceItemAttributes marketPlaceItemAttributes2 = MarketPlaceItemAttributes.builder().
                orderedQuantity(5).pickedQuantity(0).
                vendorInstanceId(instanceId).itemId(expectedItemId1).externalItemId(externalItemId1).build()
        marketPlaceItemAttributesList.add(marketPlaceItemAttributes1)
        marketPlaceItemAttributesList.add(marketPlaceItemAttributes2)
        MarketPlacePickCompleteMessage marketPlacePickCompleteMessage =
                createMarketPlacePickCompleteMessageForTestVendor(marketPlaceItemAttributesList)

        when:
        UpdateItemInfo updateItemInfo = updateItemInfoMapper.convertToUpdateItemInfo(marketPlacePickCompleteMessage)

        then:
        assert updateItemInfo.getVendorId() == Vendor.TESTVENDOR
        assert updateItemInfo.getVendorStoreId() == vendorStoreId
        assert updateItemInfo.getOutOfStockItemsCount() == 2
        assert updateItemInfo.getOutOfStockItemIds().get(0) == externalItemId1
        assert updateItemInfo.getStoreId() == storeId
        assert updateItemInfo.suspendUntil != 0
        assert updateItemInfo.vendorOrderId == marketPlacePickCompleteMessage.getVendorOrderId()
        assert updateItemInfo.reason == "OUT_OF_STOCK"
        assert !updateItemInfo.isValidVendor()
        assert updateItemInfo.containsOutOfStockItems()
    }

    def "Successful mapping with convertToUpdateItemInfo test vendor ItemId null"() {
        given:
        List<MarketPlaceItemAttributes> marketPlaceItemAttributesList = new LinkedList<>();
        MarketPlaceItemAttributes marketPlaceItemAttributes1 = MarketPlaceItemAttributes.builder().
                orderedQuantity(5).pickedQuantity(1).
                vendorInstanceId(instanceId).externalItemId(externalItemId1).build()
        MarketPlaceItemAttributes marketPlaceItemAttributes2 = MarketPlaceItemAttributes.builder().
                orderedQuantity(5).pickedQuantity(0).
                vendorInstanceId(instanceId).externalItemId(externalItemId1).build()
        marketPlaceItemAttributesList.add(marketPlaceItemAttributes1)
        marketPlaceItemAttributesList.add(marketPlaceItemAttributes2)
        MarketPlacePickCompleteMessage marketPlacePickCompleteMessage =
                createMarketPlacePickCompleteMessageForTestVendor(marketPlaceItemAttributesList)

        when:
        UpdateItemInfo updateItemInfo = updateItemInfoMapper.convertToUpdateItemInfo(marketPlacePickCompleteMessage)

        then:
        assert updateItemInfo.getVendorId() == Vendor.TESTVENDOR
        assert updateItemInfo.getVendorStoreId() == vendorStoreId
        assert updateItemInfo.getOutOfStockItemsCount() == 0
        assert updateItemInfo.getStoreId() == storeId
        assert updateItemInfo.suspendUntil != 0
        assert updateItemInfo.vendorOrderId == marketPlacePickCompleteMessage.getVendorOrderId()
        assert updateItemInfo.reason == "OUT_OF_STOCK"
        assert !updateItemInfo.isValidVendor()
    }

    def "Mapping with convertToUpdateItemInfo marketPlaceItemAttributes as empty"() {
        given:
        List<MarketPlaceItemAttributes> marketPlaceItemAttributesList = new LinkedList<>()
        MarketPlacePickCompleteMessage marketPlacePickCompleteMessage =
                createMarketPlacePickCompleteMessage(marketPlaceItemAttributesList)

        when:
        UpdateItemInfo updateItemInfo = updateItemInfoMapper.convertToUpdateItemInfo(marketPlacePickCompleteMessage)

        then:
        assert updateItemInfo.getVendorId() == vendor
        assert updateItemInfo.getVendorStoreId() == vendorStoreId
        assert updateItemInfo.getOutOfStockItemsCount() == 0
        assert updateItemInfo.getStoreId() == storeId
    }

    def "Successful Mapping with convertToUpdateItemInfo when there are no nil Picks or partial Picks"() {
        given:
        List<MarketPlaceItemAttributes> marketPlaceItemAttributesList = new LinkedList<>();
        MarketPlaceItemAttributes marketPlaceItemAttributes1 = MarketPlaceItemAttributes.builder().
                orderedQuantity(5).pickedQuantity(5).
                vendorInstanceId(instanceId).externalItemId(externalItemId1).build()
        marketPlaceItemAttributesList.add(marketPlaceItemAttributes1)
        MarketPlacePickCompleteMessage marketPlacePickCompleteMessage =
                createMarketPlacePickCompleteMessage(marketPlaceItemAttributesList)

        when:
        UpdateItemInfo updateItemInfo = updateItemInfoMapper.convertToUpdateItemInfo(marketPlacePickCompleteMessage)

        then:
        assert updateItemInfo.getVendorId() == vendor
        assert updateItemInfo.getVendorStoreId() == vendorStoreId
        assert updateItemInfo.getOutOfStockItemsCount() == 0
        assert updateItemInfo.getStoreId() == storeId
    }

    def "Successful Mapping with convertToUpdateItemInfo when there are only partial Picks"() {
        given:
        List<MarketPlaceItemAttributes> marketPlaceItemAttributesList = new LinkedList<>();
        MarketPlaceItemAttributes marketPlaceItemAttributes1 = MarketPlaceItemAttributes.builder().
                orderedQuantity(5).pickedQuantity(2).
                vendorInstanceId(instanceId).itemId(expectedItemId1).externalItemId(externalItemId1).build()
        marketPlaceItemAttributesList.add(marketPlaceItemAttributes1)
        MarketPlacePickCompleteMessage marketPlacePickCompleteMessage =
                createMarketPlacePickCompleteMessage(marketPlaceItemAttributesList)

        when:
        UpdateItemInfo updateItemInfo = updateItemInfoMapper.convertToUpdateItemInfo(marketPlacePickCompleteMessage)

        then:
        assert updateItemInfo.getVendorId() == vendor
        assert updateItemInfo.getVendorStoreId() == vendorStoreId
        assert updateItemInfo.getOutOfStockItemsCount() == 1
        assert updateItemInfo.getOutOfStockItemIds().get(0) == externalItemId1
        assert updateItemInfo.getStoreId() == storeId
    }

    def "Successful Mapping with convertToUpdateItemInfo for ItemUnavailability Message"() {
        given:
        ItemUnavailabilityMessage itemUnavailabilityMessage = mockItemUnavailabilityMessage()

        when:
        UpdateItemInfo updateItemInfo = updateItemInfoMapper.convertToUpdateItemInfo(itemUnavailabilityMessage)

        then:
        assert updateItemInfo.getVendorId() == vendor
        assert updateItemInfo.getVendorStoreId() == storeId
        assert updateItemInfo.getVendorOrderId() == vendorOrderId
        assert updateItemInfo.getOutOfStockItemsCount() == 1
        assert updateItemInfo.getOutOfStockItemIds().get(0) == externalItemId1
        assert updateItemInfo.getStoreId() == storeId
    }

    def "Successful Mapping with convertToUpdateItemInfo when there are only nil Picks"() {
        given:
        List<MarketPlaceItemAttributes> marketPlaceItemAttributesList = new LinkedList<>();
        MarketPlaceItemAttributes marketPlaceItemAttributes1 = MarketPlaceItemAttributes.builder().
                orderedQuantity(5).pickedQuantity(0).
                vendorInstanceId(instanceId).itemId(expectedItemId1).externalItemId(externalItemId1).build()
        marketPlaceItemAttributesList.add(marketPlaceItemAttributes1)
        MarketPlacePickCompleteMessage marketPlacePickCompleteMessage =
                createMarketPlacePickCompleteMessage(marketPlaceItemAttributesList)

        when:
        UpdateItemInfo updateItemInfo = updateItemInfoMapper.convertToUpdateItemInfo(marketPlacePickCompleteMessage)

        then:
        assert updateItemInfo.getVendorId() == vendor
        assert updateItemInfo.getVendorStoreId() == vendorStoreId
        assert updateItemInfo.getOutOfStockItemsCount() == 1
        assert updateItemInfo.getOutOfStockItemIds().get(0) == externalItemId1
        assert updateItemInfo.getStoreId() == storeId
    }

    def "test null object mapping"() {
        when:
        MarketPlaceOrderCancelMessage message = null
        UpdateItemInfo updateItemInfo = updateItemInfoMapper.convertToUpdateItemInfo(message as MarketPlaceOrderCancelMessage)

        then:
        updateItemInfo == null
    }

    def "test null object mapping for pick complete message"() {
        when:
        MarketPlacePickCompleteMessage message = null;
        UpdateItemInfo updateItemInfo = updateItemInfoMapper.convertToUpdateItemInfo(message as MarketPlacePickCompleteMessage)

        then:
        updateItemInfo == null
    }

    def "test  mapping for MarketPlaceOrderCancelMessage"() {
        when:
        MarketPlaceOrderCancelMessage marketPlaceOrderCancelMessage = MarketPlaceOrderCancelMessage.builder()
                .vendorStoreId("1")
                .vendorOrderId("12")
                .vendor(Vendor.UBEREATS)
                .storeId("2")
                .cancellationSource(CancellationSource.STORE)
                .cancelledReasonCode("A")
                .externalItemIds(Arrays.asList("1", "2"))
                .build()
        UpdateItemInfo updateItemInfo = updateItemInfoMapper.convertToUpdateItemInfo(marketPlaceOrderCancelMessage)

        then:
        updateItemInfo != null
        updateItemInfo.containsOutOfStockItems()
        updateItemInfo.storeId == "2"
        updateItemInfo.vendorOrderId == "12"
        updateItemInfo.vendorStoreId == "1"
        updateItemInfo.reason == "OUT_OF_STOCK"
    }

    private MarketPlacePickCompleteMessage createMarketPlacePickCompleteMessage(List<MarketPlaceItemAttributes> marketPlaceItemAttributesList) {
        return MarketPlacePickCompleteMessage.builder()
                .marketPlaceItemAttributes(marketPlaceItemAttributesList).sourceOrderId(sourceOrderId)
                .vendorOrderId(vendorOrderId).storeId(storeId).vendorId(vendor).vendorStoreId(vendorStoreId)
                .build()
    }

    private MarketPlacePickCompleteMessage createMarketPlacePickCompleteMessageForTestVendor(List<MarketPlaceItemAttributes> marketPlaceItemAttributesList) {
        return MarketPlacePickCompleteMessage.builder()
                .marketPlaceItemAttributes(marketPlaceItemAttributesList).sourceOrderId(sourceOrderId)
                .vendorOrderId(vendorOrderId).storeId(storeId).vendorId(Vendor.TESTVENDOR).vendorStoreId(vendorStoreId)
                .build()
    }

    private ItemUnavailabilityMessage mockItemUnavailabilityMessage() {
        return ItemUnavailabilityMessage.builder().storeId(storeId).storeOrderId(sourceOrderId).vendorId(vendor).vendorOrderId(vendorOrderId).outOfStockItemIds(Collections.singletonList(externalItemId1)).build()
    }
}