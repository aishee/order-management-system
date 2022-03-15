package com.walmart.oms.converter

import com.walmart.oms.dto.OmsOrderResponse
import com.walmart.oms.order.aggregateroot.OmsOrder
import spock.lang.Specification

import static com.walmart.oms.converter.OmsMockOrder.*
import static com.walmart.oms.converter.OmsResponseMapperAssertions.*

class OmsResponseMapperTest extends Specification {

    OmsResponseMapper omsResponseMapper

    def setup() {
        omsResponseMapper = new OmsResponseMapper()
    }

    def "Test ConvertToOrderResponse when all child entities are present"() {
        given:
        OmsOrder omsOrder = give_Oms_Order_all_child_entities_present()

        when:
        OmsOrderResponse omsOrderResponse = omsResponseMapper.convertToOrderResponse(omsOrder)

        then:
        assertOmsOrderAllEntities(omsOrderResponse, omsOrder)
        assertPickItems(omsOrderResponse, omsOrder)
        assertPickedItemsUpc(omsOrderResponse, omsOrder)
    }

    def "Test ConvertToOrderResponse when all child entities are not present"() {
        given:
        OmsOrder omsOrder = give_me_Oms_Order_all_child_entities_not_present()

        when:
        OmsOrderResponse omsOrderResponse = omsResponseMapper.convertToOrderResponse(omsOrder)

        then:
        assertOmsOrder(omsOrderResponse, omsOrder)
    }

    def "Test ConvertToOrderResponse when Item has no pickedItem"() {
        given:
        OmsOrder omsOrder = give_me_Oms_Order_no_picked_Item()

        when:
        OmsOrderResponse omsOrderResponse = omsResponseMapper.convertToOrderResponse(omsOrder)

        then:
        assertOmsOrder(omsOrderResponse, omsOrder)
        assertNoPickedItem(omsOrderResponse, omsOrder)
    }

    def "Test ConvertToOrderResponse when PickedItem has no Upcs"() {
        given:
        OmsOrder omsOrder = give_me_Oms_Order_picked_item_no_upcs()

        when:
        OmsOrderResponse omsOrderResponse = omsResponseMapper.convertToOrderResponse(omsOrder)

        then:
        assertOmsOrder(omsOrderResponse, omsOrder)
        assertPickItems(omsOrderResponse, omsOrder)
        assertNoPickedItemUpc(omsOrderResponse, omsOrder)
    }
}
