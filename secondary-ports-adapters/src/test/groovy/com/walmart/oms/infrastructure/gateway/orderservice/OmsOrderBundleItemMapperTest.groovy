package com.walmart.oms.infrastructure.gateway.orderservice

import com.walmart.common.domain.type.Currency
import com.walmart.marketplace.order.domain.entity.type.Vendor
import com.walmart.oms.order.aggregateroot.OmsOrder
import com.walmart.oms.order.domain.entity.OmsOrderBundledItem
import com.walmart.oms.order.domain.entity.OmsOrderItem
import com.walmart.oms.order.valueobject.ItemPriceInfo
import com.walmart.oms.order.valueobject.MarketPlaceInfo
import com.walmart.oms.order.valueobject.Money
import com.walmart.services.oms.order.common.model.OrderBundleItem
import spock.lang.Specification

class OmsOrderBundleItemMapperTest extends Specification {
    OmsOrderBundleItemMapper omsOrderBundleItemMapper;

    def setup() {
        omsOrderBundleItemMapper = new OmsOrderBundleItemMapper();
    }

    def "Test return empty when no bundles are present in order"() {
        when:
        OmsOrder omsOrder = mockOmsOrder()
        List<OrderBundleItem> orderBundleItemList = omsOrderBundleItemMapper.INSTANCE.mapToBundleItemInfo(omsOrder);
        then:
        orderBundleItemList != null
        orderBundleItemList.size() == 0
    }

    def "Test for bundle item"() {
        when:
        OmsOrder omsOrder = mockOmsOrderWithBundleItem()
        List<OrderBundleItem> orderBundleItemList = omsOrderBundleItemMapper.INSTANCE.mapToBundleItemInfo(omsOrder);
        then:
        orderBundleItemList != null
        orderBundleItemList.size() > 0

        orderBundleItemList.get(0).getBundleItemCount() == 7
        orderBundleItemList.get(0).getItemId() != null
        orderBundleItemList.get(0).getQuantity() == 2
        orderBundleItemList.get(0).getConsumerItemNumber() != null
        orderBundleItemList.get(0).getBundleItemReferences() != null
        orderBundleItemList.get(0).getUnitPrice() == 86.00
        orderBundleItemList.get(0).getWebItemDescription() != null
        orderBundleItemList.get(0).getWebItemDescription() == "combo 1"
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

    private mockOmsOrderWithBundleItem() {
        OmsOrder omsOrder = mockOmsOrder()
        OmsOrderBundledItem bundledItem = OmsOrderBundledItem.builder()
                .omsOrderItem(OmsOrderItem.builder()
                        .omsOrder(omsOrder)
                        .cin("123412")
                        .skuId("98")
                        .weight(12.0)
                        .uom("E")
                        .quantity(2)
                        .itemPriceInfo(new ItemPriceInfo(new Money(BigDecimal.TEN, Currency.GBP)
                                , new Money(BigDecimal.TEN, Currency.GBP), new Money(BigDecimal.TEN, Currency.GBP)))
                        .build())
                .bundleInstanceId("pqo-s1sha-19su")
                .bundleSkuId("2333444")
                .bundleDescription("combo 1")
                .bundleQuantity(2)
                .itemQuantity(4)
                .build();
        OmsOrderBundledItem bundledItem2 = OmsOrderBundledItem.builder()
                .omsOrderItem(OmsOrderItem.builder()
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
                .bundleInstanceId("pqo-s1sha-19su")
                .bundleSkuId("2333444")
                .bundleDescription("combo 1")
                .bundleQuantity(2)
                .itemQuantity(3)
                .build()
        OmsOrderItem orderItem1 = OmsOrderItem.builder()
                .omsOrder(omsOrder)
                .cin("123412")
                .skuId("98")
                .weight(12.0)
                .uom("E")
                .quantity(2)
                .itemPriceInfo(new ItemPriceInfo(new Money(BigDecimal.TEN, Currency.GBP)
                        , new Money(BigDecimal.TEN, Currency.GBP)))
                .bundledItemList(Arrays.asList(bundledItem))
                .build()

        OmsOrderItem orderItem2 = OmsOrderItem.builder()
                .omsOrder(omsOrder)
                .cin("123413")
                .skuId("99")
                .weight(12.0)
                .uom("E")
                .quantity(2)
                .itemPriceInfo(new ItemPriceInfo(new Money(BigDecimal.ONE, Currency.GBP)
                        , new Money(BigDecimal.ONE, Currency.GBP)))
                .bundledItemList(Arrays.asList(bundledItem2))
                .build()

        OmsOrderItem orderItem3 = OmsOrderItem.builder()
                .omsOrder(omsOrder)
                .cin("123415")
                .skuId("100")
                .weight(12.0)
                .uom("E")
                .quantity(1)
                .build()

        omsOrder.addItem(orderItem1);
        omsOrder.addItem(orderItem2);
        omsOrder.addItem(orderItem3);
        return omsOrder
    }
}
