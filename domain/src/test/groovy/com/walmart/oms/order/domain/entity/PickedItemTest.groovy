package com.walmart.oms.order.domain.entity

import com.walmart.oms.order.valueobject.SubstitutedItemPriceInfo
import spock.lang.Specification

class PickedItemTest extends Specification {

    def "test picked item"() {
        given:
        OmsOrderItem omsOrderItem = Mock()
        PickedItemUpc pickedItemUpc = PickedItemUpc.builder().uom("12").upc("12").win("23").build()
        PickedItem pickedItem = PickedItem.builder()
                .omsOrderItem(omsOrderItem).quantity(11).pickedItemUpcList(Arrays.asList(pickedItemUpc))
                .departmentID("1").orderedCin("1").pickedItemDescription("this is test item")
                .build()

        assert pickedItem.toString() != null
        assert pickedItem.getOmsOrderItem() != null
        assert pickedItem.getQuantity() == 11
    }

    def "test substituted item vendor price when substitutedItemList is not null"() {
        given:
        OmsOrderItem omsOrderItem = Mock()

        PickedItemUpc pickedItemUpc = PickedItemUpc.builder().uom("12").upc("12").win("23").build()
        PickedItem pickedItem = PickedItem.builder()
                .omsOrderItem(omsOrderItem).quantity(10).pickedItemUpcList(Arrays.asList(pickedItemUpc))
                .departmentID("1").orderedCin("1")
                .pickedItemDescription("this is test item")
                .build()
        SubstitutedItem substitutedItem = SubstitutedItem.builder()
                .consumerItemNumber("123")
                .quantity(1).department("123")
                .description("description")
                .upcs(Arrays.asList(SubstitutedItemUpc.builder().uom("KG").upc("12").build()))
                .substitutedItemPriceInfo(SubstitutedItemPriceInfo.builder().totalPrice(BigDecimal.TEN).unitPrice(BigDecimal.ONE).build())
                .build()
        substitutedItem.substitutedItemPriceInfo.vendorUnitPrice = BigDecimal.TEN;
        List<SubstitutedItem> list = Arrays.asList(substitutedItem)
        pickedItem.substitutedItems = list

        when:
        Optional<BigDecimal> subItemVendorPrice = pickedItem.getSubstitutedItemVendorPrice()

        then:
        assert subItemVendorPrice != Optional.empty()
        assert subItemVendorPrice.get() == (BigDecimal.TEN)
    }

    def "test substituted item vendor prices when substitutedItemList is null"() {
        given:
        OmsOrderItem omsOrderItem = Mock()

        PickedItemUpc pickedItemUpc = PickedItemUpc.builder().uom("12").upc("12").win("23").build()
        PickedItem pickedItem = PickedItem.builder()
                .omsOrderItem(omsOrderItem).quantity(11).pickedItemUpcList(Arrays.asList(pickedItemUpc))
                .departmentID("1").orderedCin("1")
                .pickedItemDescription("this is test item")
                .build()

        when:
        Optional<BigDecimal> subItemVendorPrice =  pickedItem.getSubstitutedItemVendorPrice()
        Optional<SubstitutedItem> substitutedItem = pickedItem.getSubstitutedItem();
        then:
        assert subItemVendorPrice == Optional.empty()
        assert substitutedItem == Optional.empty()

    }
}
