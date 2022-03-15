package com.walmart.oms.infrastructure.gateway.orderservice

import com.walmart.common.domain.type.SubstitutionOption
import com.walmart.oms.order.aggregateroot.OmsOrder
import com.walmart.oms.order.domain.entity.OmsOrderItem
import com.walmart.oms.order.domain.entity.PickedItem
import com.walmart.oms.order.domain.entity.PickedItemUpc
import com.walmart.oms.order.domain.entity.SubstitutedItem
import com.walmart.oms.order.domain.entity.SubstitutedItemUpc
import com.walmart.oms.order.valueobject.SubstitutedItemPriceInfo
import spock.lang.Specification

class OsOrderLineCustomAttributeMapperTest extends Specification {

    OsOrderLineCustomAttributeMapper osOrderLineCustomAttributeMapper;

    def setup(){
        osOrderLineCustomAttributeMapper = new OsOrderLineCustomAttributeMapper()
    }

    def "Test when there is no substituted item" () {
        OmsOrderItem omsOrderItem = OmsOrderItem.builder().omsOrder(new OmsOrder()).quantity(1).id("ad").cin("12233")
            .build();

        when:
        Map<String, String> attributesMap = osOrderLineCustomAttributeMapper.INSTANCE.createCustomAttributeMap(omsOrderItem)

        then:
        assert attributesMap.get("order.substituteItem.substitutionOption") == SubstitutionOption.DO_NOT_SUBSTITUTE.name()
        assert  attributesMap.keySet().size() == 1
    }

    def "Test when there is substituted item" () {
        OmsOrderItem omsOrderItem = OmsOrderItem.builder().substitutionOption(SubstitutionOption.SUBSTITUTE).omsOrder(new OmsOrder()).quantity(1).id("ad").cin("12233")
                .build()
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
        substitutedItem.substitutedItemPriceInfo.vendorUnitPrice = BigDecimal.TEN
        List<SubstitutedItem> list = Arrays.asList(substitutedItem)
        pickedItem.substitutedItems = list
        omsOrderItem.pickedItem = pickedItem

        when:
        Map<String, String> attributesMap = osOrderLineCustomAttributeMapper.INSTANCE.createCustomAttributeMap(omsOrderItem)

        then:
        assert attributesMap.get("order.substituteItem.substitutionOption") == SubstitutionOption.SUBSTITUTE.name()
        assert attributesMap.get("order.substituteItem.vendorPrice") == "10"
        assert  attributesMap.keySet().size() == 2
    }

    def "Test when omsOrderItem is null return empty custom attributes map"() {

        when:
        Map<String, String> attributesMap = osOrderLineCustomAttributeMapper.INSTANCE.createCustomAttributeMap(null)

        then:
        assert attributesMap.keySet().size() == 0
    }

}
