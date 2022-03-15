package com.walmart.oms.order.domain.entity

import com.walmart.common.domain.type.Currency
import com.walmart.oms.order.aggregateroot.OmsOrder
import com.walmart.oms.order.valueobject.ItemPriceInfo
import com.walmart.oms.order.valueobject.Money
import spock.lang.Specification

class OmsOrderBundledItemTest extends Specification {

    def "test successful build of oms order bundled item object"() {
        given:
        OmsOrderItem omsOrderItem = OmsOrderItem.builder()
                .omsOrder(new OmsOrder())
                .cin("123412")
                .skuId("98")
                .weight(12.0)
                .uom("E")
                .quantity(2)
                .itemPriceInfo(new ItemPriceInfo(new Money(BigDecimal.TEN, Currency.GBP), new Money(BigDecimal.TEN, Currency.GBP)
                        , new Money(BigDecimal.TEN, Currency.GBP)))
                .build()


        when:
        OmsOrderBundledItem omsOrderBundledItem1 = OmsOrderBundledItem.builder()
                .bundleInstanceId("abc")
                .bundleSkuId("2222")
                .bundleDescription("combo 1")
                .bundleQuantity(2)
                .itemQuantity(1)
                .id("any")
                .omsOrderItem(omsOrderItem).build()
        then:
        assert omsOrderBundledItem1.getBundleInstanceId() == "abc"
        assert omsOrderBundledItem1.getBundleQuantity() == 2
        assert omsOrderBundledItem1.getItemQuantity() == 1
        assert omsOrderBundledItem1.getBundleSkuId() == "2222"
        assert omsOrderBundledItem1.getOmsOrderItem() != null
        assert omsOrderBundledItem1.getId() == "any"
        assert omsOrderBundledItem1.toString() != null
        assert omsOrderBundledItem1.getBundleItemTotalPrice() == 20.00
        assert omsOrderBundledItem1.getOrderItemSkuId() == "98"
        assert omsOrderBundledItem1.getBundleDescription() == "combo 1"
        omsOrderBundledItem1.initializeInnerEntitiesEagerly()
    }
}
