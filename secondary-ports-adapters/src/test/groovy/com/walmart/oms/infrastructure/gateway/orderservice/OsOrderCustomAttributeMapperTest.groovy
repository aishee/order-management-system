package com.walmart.oms.infrastructure.gateway.orderservice

import com.walmart.common.domain.type.Currency
import com.walmart.marketplace.order.domain.entity.type.Vendor
import com.walmart.oms.order.aggregateroot.OmsOrder
import com.walmart.oms.order.domain.entity.OmsOrderItem
import com.walmart.oms.order.valueobject.ItemPriceInfo
import com.walmart.oms.order.valueobject.MarketPlaceInfo
import com.walmart.oms.order.valueobject.Money
import spock.lang.Specification

class OsOrderCustomAttributeMapperTest extends Specification {
    OsOrderCustomAttributeMapper osOrderCustomAttributeMapper;

    def setup() {
        osOrderCustomAttributeMapper = new OsOrderCustomAttributeMapper();
    }

    def "Test return empty when null order is passed"() {
        when:
        Map<String, String> customAttributeMap = osOrderCustomAttributeMapper.INSTANCE.createCustomAttributeMap(null);
        then:
        customAttributeMap != null
        customAttributeMap.size() == 0
    }

    def "Test return Non null when order  is present"() {
        when:
        Map<String, String> customAttributeMap = osOrderCustomAttributeMapper.INSTANCE.createCustomAttributeMap(mockOmsOrderWithoutBundleItem());
        then:
        customAttributeMap != null
        customAttributeMap.size() > 0
    }

    private mockOmsOrder() {
        OmsOrder omsOrder =
                OmsOrder.builder()
                        .marketPlaceInfo(MarketPlaceInfo.builder()
                                .vendor(Vendor.UBEREATS)
                                .vendorOrderId("12345")
                                .build())
                        .storeId("1234")
                        .orderState("PICK_COMPLETE")
                        .storeOrderId("111")
                        .sourceOrderId("12")
                        .deliveryDate(new Date())
                        .build()
        return omsOrder
    }

    private mockOmsOrderWithoutBundleItem() {
        OmsOrder omsOrder = mockOmsOrder()
        omsOrder.addItem(OmsOrderItem.builder()
                .omsOrder(omsOrder)
                .cin("123413")
                .skuId("99")
                .weight(12.0)
                .uom("E")
                .quantity(2)
                .itemPriceInfo(new ItemPriceInfo(new Money(BigDecimal.ONE, Currency.GBP)
                        , new Money(BigDecimal.ONE, Currency.GBP),
                        new Money(BigDecimal.ONE, Currency.GBP)))
                .build())
        return omsOrder
    }

}
