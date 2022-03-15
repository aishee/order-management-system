package com.walmart.marketplace.order.domain.uber.mapper

import com.walmart.marketplace.domain.event.messages.MarketPlaceItemAttributes
import com.walmart.marketplace.domain.event.messages.MarketPlacePickCompleteMessage
import com.walmart.marketplace.order.domain.entity.MarketPlaceItem
import com.walmart.marketplace.order.domain.entity.type.Vendor
import com.walmart.marketplace.order.domain.uber.PatchCartInfo
import com.walmart.marketplace.order.domain.valueobject.ItemIdentifier
import spock.lang.Specification

class PatchCartInfoMapperTest extends Specification {
    String sourceOrderId = UUID.randomUUID().toString()
    String vendorOrderId = UUID.randomUUID().toString()
    String instanceId = UUID.randomUUID().toString()
    String externalItemId1 = UUID.randomUUID().toString()
    String expectedItemId1 = "4888484"
    String expectedItemId2 = "4888455"
    String storeId = "4401"
    String testItemId = UUID.randomUUID().toString()
    String vendorStoreId = UUID.randomUUID().toString()
    Vendor vendor = Vendor.UBEREATS
    PatchCartInfoMapper patchCartInfoMapper

    def setup() {
        patchCartInfoMapper = new PatchCartInfoMapperImpl()
    }

    def "Successful mapping with convertToPatchCartInfo for nil Picks"() {
        given:
        List<MarketPlaceItemAttributes> marketPlaceItemAttributesList = new LinkedList<>();
        MarketPlaceItemAttributes marketPlaceItemAttributes = MarketPlaceItemAttributes.builder().
                orderedQuantity(5).pickedQuantity(0).
                vendorInstanceId(instanceId).build()
        marketPlaceItemAttributesList.add(marketPlaceItemAttributes)
        MarketPlacePickCompleteMessage marketPlacePickCompleteMessage =
                createMarketPlacePickCompleteMessage(marketPlaceItemAttributesList)

        when:
        PatchCartInfo patchCartInfo = patchCartInfoMapper.convertToPatchCartInfo(marketPlacePickCompleteMessage)

        then:
        assert patchCartInfo.getVendorId() == vendor
        assert patchCartInfo.getVendorOrderId() == vendorOrderId
        assert patchCartInfo.getNilPicksCount() == 1
        assert patchCartInfo.getNilPickInstanceIds().get(0) == instanceId
        assert patchCartInfo.getPartialPicksCount() == 0
        assert patchCartInfo.getStoreId() == storeId
    }

    def "Mapping with convertToPatchCartInfo when MarketPlacePickCompleteMessage is null"() {
        given:
        List<String> nilPickCin = new ArrayList<>();
        nilPickCin.add(expectedItemId1)
        MarketPlacePickCompleteMessage marketPlacePickCompleteMessage = null
        when:
        PatchCartInfo patchCartInfo = patchCartInfoMapper.convertToPatchCartInfo(marketPlacePickCompleteMessage)

        then:
        patchCartInfo == null
    }

    def "Mapping with convertToPatchCartInfo for marketPlaceItemAttributes as empty"() {
        given:
        List<String> nilPickCin = new ArrayList<>();
        nilPickCin.add(expectedItemId1)
        Map<String, Long> partialPickCin = new HashMap<>()
        MarketPlacePickCompleteMessage marketPlacePickCompleteMessage =
                createMarketPlacePickCompleteMessage(new LinkedList<>())
        marketPlacePickCompleteMessage.marketPlaceItemAttributes = Collections.emptyList()

        when:
        PatchCartInfo patchCartInfo = patchCartInfoMapper.convertToPatchCartInfo(marketPlacePickCompleteMessage)

        then:
        assert patchCartInfo.getVendorId() == vendor
        assert patchCartInfo.getVendorOrderId() == vendorOrderId
        assert patchCartInfo.getNilPicksCount() == 0
        assert patchCartInfo.getPartialPicksCount() == 0
        assert patchCartInfo.getStoreId() == storeId
    }

    def "Successful Mapping with convertToUpdateItemInfo when there are no nil or partial Picks"() {
        given:
        List<MarketPlaceItemAttributes> marketPlaceItemAttributesList = new LinkedList<>();
        MarketPlaceItemAttributes marketPlaceItemAttributes = MarketPlaceItemAttributes.builder().
                orderedQuantity(5).pickedQuantity(1).
                itemId(testItemId)
                .vendorInstanceId(instanceId).build()
        marketPlaceItemAttributesList.add(marketPlaceItemAttributes)
        MarketPlacePickCompleteMessage marketPlacePickCompleteMessage =
                createMarketPlacePickCompleteMessage(marketPlaceItemAttributesList)

        when:
        PatchCartInfo patchCartInfo = patchCartInfoMapper.convertToPatchCartInfo(marketPlacePickCompleteMessage)

        then:
        assert patchCartInfo.getVendorId() == vendor
        assert patchCartInfo.getVendorOrderId() == vendorOrderId
        assert patchCartInfo.getNilPicksCount() == 0
        assert patchCartInfo.getPartialPicksCount() == 1
        assert patchCartInfo.getStoreId() == storeId
    }

    def "Successful mapping with convertToPatchCartInfo for Partial Picks"() {
        given:
        List<MarketPlaceItemAttributes> marketPlaceItemAttributesList = new LinkedList<>();
        MarketPlaceItemAttributes marketPlaceItemAttributes = MarketPlaceItemAttributes.builder().
                orderedQuantity(5).pickedQuantity(1).
                itemId(testItemId)
                .vendorInstanceId(instanceId).build()
        marketPlaceItemAttributesList.add(marketPlaceItemAttributes)
        MarketPlacePickCompleteMessage marketPlacePickCompleteMessage =
                createMarketPlacePickCompleteMessage(marketPlaceItemAttributesList)

        when:
        PatchCartInfo patchCartInfo = patchCartInfoMapper.convertToPatchCartInfo(marketPlacePickCompleteMessage)

        then:
        assert patchCartInfo.getVendorId() == vendor
        assert patchCartInfo.getVendorOrderId() == vendorOrderId
        assert patchCartInfo.getNilPicksCount() == 0
        assert patchCartInfo.getPartialPicksCount() == 1
        assert patchCartInfo.getStoreId() == storeId
        assert patchCartInfo.getPartialPickInstanceIds().containsKey(instanceId)
    }

    def "Successful Mapping with convertToUpdateItemInfo when there are partial and nil Picks"() {
        given:
        List<MarketPlaceItemAttributes> marketPlaceItemAttributesList = new LinkedList<>();
        MarketPlaceItemAttributes partialPickedMarketPlaceItemAttributes = MarketPlaceItemAttributes.builder().
                orderedQuantity(5).pickedQuantity(1).
                itemId(testItemId)
                .vendorInstanceId(instanceId).build()
        MarketPlaceItemAttributes nillPickedMarketPlaceItemAttributes = MarketPlaceItemAttributes.builder().
                orderedQuantity(5).pickedQuantity(0).
                itemId(testItemId)
                .vendorInstanceId(instanceId).build()
        marketPlaceItemAttributesList.add(partialPickedMarketPlaceItemAttributes)
        marketPlaceItemAttributesList.add(nillPickedMarketPlaceItemAttributes)
        MarketPlacePickCompleteMessage marketPlacePickCompleteMessage =
                createMarketPlacePickCompleteMessage(marketPlaceItemAttributesList)

        when:
        PatchCartInfo patchCartInfo = patchCartInfoMapper.convertToPatchCartInfo(marketPlacePickCompleteMessage)

        then:
        assert patchCartInfo.getVendorId() == vendor
        assert patchCartInfo.getVendorOrderId() == vendorOrderId
        assert patchCartInfo.getNilPicksCount() == 1
        assert patchCartInfo.getPartialPicksCount() == 1
        assert patchCartInfo.getStoreId() == storeId
        assert patchCartInfo.getPartialPickInstanceIds().containsKey(instanceId)
        assert patchCartInfo.getNilPickInstanceIds().get(0) == instanceId
    }

    def "Successful Mapping with convertToUpdateItemInfo when there are partial and nil Picks itemID null"() {
        given:
        List<MarketPlaceItemAttributes> marketPlaceItemAttributesList = new LinkedList<>();
        MarketPlaceItemAttributes partialPickedMarketPlaceItemAttributes = MarketPlaceItemAttributes.builder().
                orderedQuantity(5).pickedQuantity(1).
                itemId(null)
                .vendorInstanceId(null).build()
        MarketPlaceItemAttributes nillPickedMarketPlaceItemAttributes = MarketPlaceItemAttributes.builder().
                orderedQuantity(5).pickedQuantity(0).
                itemId(null)
                .vendorInstanceId(null).build()
        marketPlaceItemAttributesList.add(partialPickedMarketPlaceItemAttributes)
        marketPlaceItemAttributesList.add(nillPickedMarketPlaceItemAttributes)
        MarketPlacePickCompleteMessage marketPlacePickCompleteMessage =
                createMarketPlacePickCompleteMessage(marketPlaceItemAttributesList)

        when:
        PatchCartInfo patchCartInfo = patchCartInfoMapper.convertToPatchCartInfo(marketPlacePickCompleteMessage)

        then:
        assert patchCartInfo.getVendorId() == vendor
        assert patchCartInfo.getVendorOrderId() == vendorOrderId
        assert patchCartInfo.getNilPicksCount() == 0
        assert patchCartInfo.getPartialPicksCount() == 0
        assert patchCartInfo.getStoreId() == storeId
    }

    def "Successful Mapping with convertToUpdateItemInfo when there are partial and nil Picks vendorInstanceId null"() {
        given:
        List<MarketPlaceItemAttributes> marketPlaceItemAttributesList = new LinkedList<>();
        MarketPlaceItemAttributes partialPickedMarketPlaceItemAttributes = MarketPlaceItemAttributes.builder().
                orderedQuantity(5).pickedQuantity(1).
                itemId(testItemId)
                .vendorInstanceId(null).build()
        MarketPlaceItemAttributes nillPickedMarketPlaceItemAttributes = MarketPlaceItemAttributes.builder().
                orderedQuantity(5).pickedQuantity(0).
                itemId(testItemId)
                .vendorInstanceId(null).build()
        marketPlaceItemAttributesList.add(partialPickedMarketPlaceItemAttributes)
        marketPlaceItemAttributesList.add(nillPickedMarketPlaceItemAttributes)
        MarketPlacePickCompleteMessage marketPlacePickCompleteMessage =
                createMarketPlacePickCompleteMessage(marketPlaceItemAttributesList)

        when:
        PatchCartInfo patchCartInfo = patchCartInfoMapper.convertToPatchCartInfo(marketPlacePickCompleteMessage)

        then:
        assert patchCartInfo.getVendorId() == vendor
        assert patchCartInfo.getVendorOrderId() == vendorOrderId
        assert patchCartInfo.getNilPicksCount() == 0
        assert patchCartInfo.getPartialPicksCount() == 0
        assert patchCartInfo.getStoreId() == storeId
    }

    private MarketPlacePickCompleteMessage createMarketPlacePickCompleteMessage(
            List<MarketPlaceItemAttributes> marketPlaceItemAttributesList) {
        return MarketPlacePickCompleteMessage.builder()
                .marketPlaceItemAttributes(marketPlaceItemAttributesList).sourceOrderId(sourceOrderId)
                .vendorOrderId(vendorOrderId).storeId(storeId).vendorId(vendor).build()
    }

    private List<MarketPlaceItemAttributes> addMarketPlaceItemAttributes_itemIdNull(ArrayList<String> nilPickCin, HashMap<String, Integer> partialPickCin) {
        ItemIdentifier itemIdentifier1 = getItemIdentifier(null)
        ItemIdentifier itemIdentifier2 = getItemIdentifier(null)
        List<MarketPlaceItemAttributes> list = new ArrayList<>();
        list.add(getItemAttributes(itemIdentifier1, nilPickCin, partialPickCin))
        list.add(getItemAttributes(itemIdentifier2, nilPickCin, partialPickCin))
        return list
    }

    private List<MarketPlaceItemAttributes> addMarketPlaceItemAttributes_instanceIdNull(ArrayList<String> nilPickCin, HashMap<String, Integer> partialPickCin) {
        ItemIdentifier itemIdentifier1 = getItemIdentifier(expectedItemId1)
        ItemIdentifier itemIdentifier2 = getItemIdentifier(expectedItemId2)
        List<MarketPlaceItemAttributes> list = new ArrayList<>();
        list.add(getItemAttributes_vendorInstanceIdNull(itemIdentifier1, nilPickCin, partialPickCin))
        list.add(getItemAttributes_vendorInstanceIdNull(itemIdentifier2, nilPickCin, partialPickCin))
        return list
    }

    private List<MarketPlaceItemAttributes> addMarketPlaceItemAttributes_item1(ArrayList<String> nilPickCin, HashMap<String, Integer> partialPickCin) {
        ItemIdentifier itemIdentifier1 = getItemIdentifier(expectedItemId1)
        ItemIdentifier itemIdentifier2 = getItemIdentifier(expectedItemId2)
        List<MarketPlaceItemAttributes> list = new ArrayList<>();
        list.add(getItemAttributes(itemIdentifier1, nilPickCin, partialPickCin))
        list.add(getItemAttributes_BundledItem(itemIdentifier2, nilPickCin, partialPickCin))
        return list
    }

    private List<MarketPlaceItemAttributes> addMarketPlaceItemAttributes(ArrayList<String> nilPickCin, HashMap<String, Integer> partialPickCin) {
        ItemIdentifier itemIdentifier1 = getItemIdentifier(expectedItemId1)
        ItemIdentifier itemIdentifier2 = getItemIdentifier(expectedItemId2)
        List<MarketPlaceItemAttributes> list = new ArrayList<>();
        list.add(getItemAttributes(itemIdentifier1, nilPickCin, partialPickCin))
        list.add(getItemAttributes(itemIdentifier2, nilPickCin, partialPickCin))
        return list
    }

    private List<MarketPlaceItemAttributes> addMarketPlaceItemAttributes_item2(ArrayList<String> nilPickCin, HashMap<String, Integer> partialPickCin) {
        ItemIdentifier itemIdentifier1 = getItemIdentifier(expectedItemId1)
        ItemIdentifier itemIdentifier2 = getItemIdentifier(expectedItemId2)
        List<MarketPlaceItemAttributes> list = new ArrayList<>();
        list.add(getItemAttributes_BundledItem(itemIdentifier1, nilPickCin, partialPickCin))
        list.add(getItemAttributes(itemIdentifier2, nilPickCin, partialPickCin))
        return list
    }

    private MarketPlaceItemAttributes getItemAttributes(ItemIdentifier itemIdentifier1, ArrayList<String> nilPickCin, HashMap<String, Integer> partialPickCin) {
        MarketPlaceItemAttributes.builder()
                .vendorInstanceId(instanceId)
                .externalItemId(externalItemId1)
                .bundledItemId(null)
                .itemId(itemIdentifier1.itemId)
                .nilPicked(nilPickCin.contains(itemIdentifier1.itemId))
                .partialPicked(partialPickCin.containsKey(itemIdentifier1.itemId))
                .partialPickQty(partialPickCin.getOrDefault(itemIdentifier1.itemId, 0))
                .build()
    }

    private MarketPlaceItemAttributes getItemAttributes_BundledItem(ItemIdentifier itemIdentifier1, ArrayList<String> nilPickCin, HashMap<String, Integer> partialPickCin) {
        MarketPlaceItemAttributes.builder()
                .vendorInstanceId(instanceId)
                .externalItemId(externalItemId1)
                .bundledItemId("1234")
                .itemId(itemIdentifier1.itemId)
                .nilPicked(nilPickCin.contains(itemIdentifier1.itemId))
                .partialPicked(partialPickCin.containsKey(itemIdentifier1.itemId))
                .partialPickQty(partialPickCin.getOrDefault(itemIdentifier1.itemId, 0))
                .build()
    }

    private MarketPlaceItemAttributes getItemAttributes_vendorInstanceIdNull(ItemIdentifier itemIdentifier1, ArrayList<String> nilPickCin, HashMap<String, Integer> partialPickCin) {
        return MarketPlaceItemAttributes.builder()
                .vendorInstanceId(null)
                .externalItemId(externalItemId1)
                .bundledItemId(null)
                .itemId(itemIdentifier1.itemId)
                .nilPicked(nilPickCin.contains(itemIdentifier1.itemId))
                .partialPicked(partialPickCin.containsKey(itemIdentifier1.itemId))
                .partialPickQty(partialPickCin.getOrDefault(itemIdentifier1.itemId, 0))
                .build()
    }

    private static ItemIdentifier getItemIdentifier(String expectedItemId1) {
        return ItemIdentifier.builder().itemId(expectedItemId1).itemType("CIN").build()
    }

    private MarketPlacePickCompleteMessage getMarketPlacePickCompleteMessage(List<MarketPlaceItem> list) {
        return MarketPlacePickCompleteMessage.builder()
                .sourceOrderId(sourceOrderId)
                .vendorOrderId(vendorOrderId)
                .vendorId(vendor)
                .vendorStoreId(vendorStoreId)
                .storeId(storeId)
                .marketPlaceItemAttributes(list as List<MarketPlaceItemAttributes>)
                .build()
    }
}
