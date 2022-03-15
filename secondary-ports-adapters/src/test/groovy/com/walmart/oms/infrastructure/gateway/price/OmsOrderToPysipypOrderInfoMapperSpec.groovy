package com.walmart.oms.infrastructure.gateway.price

import com.walmart.common.domain.type.Currency
import com.walmart.common.domain.type.Vertical
import com.walmart.marketplace.order.domain.entity.type.Vendor
import com.walmart.oms.domain.event.messages.OrderCancelledDomainEventMessage
import com.walmart.oms.infrastructure.converter.OmsOrderToPysipypOrderInfoMapper
import com.walmart.oms.infrastructure.gateway.price.dto.OrderInformation
import com.walmart.oms.order.aggregateroot.OmsOrder
import com.walmart.oms.order.domain.entity.*
import com.walmart.oms.order.valueobject.*
import com.walmart.tax.calculator.dto.Amount
import com.walmart.tax.calculator.dto.Tax
import spock.lang.Specification

class OmsOrderToPysipypOrderInfoMapperSpec extends Specification {
    PYSIPYPServiceConfiguration pysipypServiceConfiguration = mockPYSIPYPServiceConfiguration()
    OmsOrderToPysipypOrderInfoMapper omsOrderToPYSIPYPOrderInfoMapper

    def setup() {
        omsOrderToPYSIPYPOrderInfoMapper = new OmsOrderToPysipypOrderInfoMapper(
                pysipypServiceConfiguration: pysipypServiceConfiguration
        )
    }

    def "When OmsOrder is valid,then return valid OrderInformation"() {
        given:
        OmsOrder omsOrder = mockValidOmsOrder()

        Map<String, Tax> taxInfoMap = new HashMap<>();
        taxInfoMap.put("1261726", new Tax(123, new Amount(new BigDecimal("50"), Amount.Type.PERCENT),
                new Amount(new BigDecimal("1000"), Amount.Type.FIXED)))
        omsOrder.orderItemList.get(0).itemPriceInfo.vendorUnitPrice = new Money(BigDecimal.ONE, Currency.GBP)

        when:
        OrderInformation pysipypRequest = omsOrderToPYSIPYPOrderInfoMapper.buildPysipypRequestforMarketPlaceOrder(omsOrder, taxInfoMap)

        then:
        pysipypRequest != null
        pysipypRequest.header != null
        pysipypRequest.consumerId == "GIF"
        pysipypRequest.detailLine.get(0).storeTotalPrice != null
        pysipypRequest.orderedItems.get(0).priceInfo.finalAmount > 0
        pysipypRequest.orderedItems.get(1).priceInfo.finalAmount > 0
        //verify bigDecimal to double has 2 decimal precision
        BigDecimal.valueOf(pysipypRequest.orderedItems.get(1).priceInfo.finalAmount).scale() == 2
        pysipypRequest.orderedItems.get(0).priceInfo.upliftedListPrice == 1
    }

    def "When OmsOrderItem price  is zero,then return valid pickedItem price"() {
        given:
        OmsOrder omsOrder = mockValidOmsOrder()
        omsOrder.orderItemList.get(0).itemPriceInfo.unitPrice.amount = BigDecimal.ZERO
        omsOrder.orderItemList.get(0).itemPriceInfo.vendorUnitPrice = new Money(BigDecimal.ONE, Currency.GBP)

        Map<String, Tax> taxInfoMap = new HashMap<>();
        taxInfoMap.put("1261726", new Tax(123, new Amount(new BigDecimal("50"), Amount.Type.PERCENT),
                new Amount(new BigDecimal("1000"), Amount.Type.FIXED)))
        omsOrder.orderItemList.get(0).itemPriceInfo.vendorUnitPrice = new Money(BigDecimal.ONE, Currency.GBP)

        when:
        OrderInformation pysipypRequest = omsOrderToPYSIPYPOrderInfoMapper.buildPysipypRequestforMarketPlaceOrder(omsOrder, taxInfoMap)

        then:
        pysipypRequest != null
        pysipypRequest.header != null
        pysipypRequest.consumerId == "GIF"
        pysipypRequest.detailLine.get(0).storeTotalPrice != null
        pysipypRequest.orderedItems.get(0).priceInfo.finalAmount > 0
        pysipypRequest.orderedItems.get(0).priceInfo.listPrice > 0
        pysipypRequest.orderedItems.get(0).priceInfo.upliftedListPrice == 1
    }

    def "When OmsOrderItem price and pickedItemprice is zero"() {
        given:
        OmsOrder omsOrder = mockValidOmsOrder()
        omsOrder.orderItemList.get(0).itemPriceInfo.unitPrice.amount = BigDecimal.ZERO
        omsOrder.orderItemList.get(0).itemPriceInfo.vendorUnitPrice = new Money(BigDecimal.ONE, Currency.GBP)
        omsOrder.orderItemList.get(0).pickedItem.pickedItemPriceInfo.unitPrice.amount = BigDecimal.ZERO

        Map<String, Tax> taxInfoMap = new HashMap<>();
        taxInfoMap.put("1261726", new Tax(123, new Amount(new BigDecimal("50"), Amount.Type.PERCENT),
                new Amount(new BigDecimal("1000"), Amount.Type.FIXED)))
        taxInfoMap.put("102791", new Tax(123, new Amount(new BigDecimal("50"), Amount.Type.PERCENT),
                new Amount(new BigDecimal("1000"), Amount.Type.FIXED)))

        when:
        OrderInformation pysipypRequest = omsOrderToPYSIPYPOrderInfoMapper.buildPysipypRequestforMarketPlaceOrder(omsOrder, taxInfoMap)

        then:
        pysipypRequest != null
        pysipypRequest.header != null
        pysipypRequest.consumerId == "GIF"
        pysipypRequest.detailLine.get(0).storeTotalPrice == "0.00"
        pysipypRequest.orderedItems.get(0).priceInfo.finalAmount == 0.00
        pysipypRequest.orderedItems.get(0).priceInfo.listPrice == 0.00
        pysipypRequest.orderedItems.get(0).priceInfo.upliftedListPrice == 1
        pysipypRequest.detailLine.get(0).substitutedItems.get(0).wmItemNum == "5555"
        pysipypRequest.detailLine.get(0).substitutedItems.get(0).storeUnitPrice == 1.00
        pysipypRequest.detailLine.get(0).substitutedItems.get(0).storeTotalPrice == 1.00
        pysipypRequest.detailLine.get(0).substitutedItems.get(0).department == "68"
        pysipypRequest.detailLine.get(0).substitutedItems.get(0).code == "S"
        pysipypRequest.detailLine.get(0).substitutedItems.get(0).pickedBy == "CONSTANTS"
        pysipypRequest.detailLine.get(0).substitutedItems.get(0).description == "Substituted item"
        pysipypRequest.detailLine.get(0).substitutedItems.get(0).posDesc == "Substituted item"
        pysipypRequest.detailLine.get(0).substitutedItems.get(0).upc == "102791"
        pysipypRequest.detailLine.get(0).substitutedItems.get(0).uom == "E"
    }

    def "When OmsOrder is valid,then successfully return reverse sale request"() {
        given:
        OrderCancelledDomainEventMessage orderCancelledDomainEventMessage =
                mockValidCancelledEventMessage()

        when:
        OrderInformation pysipypRequest = omsOrderToPYSIPYPOrderInfoMapper.
                buildReverseSaleRequest(orderCancelledDomainEventMessage)

        then:
        pysipypRequest != null
        pysipypRequest.header != null
        pysipypRequest.header.messageType == "reverseSale"
    }

    def " zero priced nil pick are not included in DetailLine and OrderedItemList"() {
        given:
        OmsOrder omsOrder = mockValidOmsOrder()

        String cinNilPick = "6000"
        BigDecimal unitPriceNilPick = 0.toBigDecimal()
        BigDecimal storePriceNilPick = 0.toBigDecimal()
        BigDecimal vendorUnitPrice = BigDecimal.ONE
        long orderedQtyNilPick = 6
        long pickedQtyNilPick = 0
        addItemToOrder(omsOrder, cinNilPick, storePriceNilPick, unitPriceNilPick, orderedQtyNilPick, pickedQtyNilPick, vendorUnitPrice)
        omsOrder.orderItemList.get(0).itemPriceInfo.vendorUnitPrice = new Money(BigDecimal.ONE, Currency.GBP)

        Map<String, Tax> taxInfoMap = createTaxMockMap()

        when:
        OrderInformation pysipypRequest = omsOrderToPYSIPYPOrderInfoMapper.buildPysipypRequestforMarketPlaceOrder(omsOrder, taxInfoMap)

        then:
        pysipypRequest != null
        pysipypRequest.orderedItems.get(0).priceInfo.finalAmount != 0
        pysipypRequest.orderedItems.get(0).priceInfo.listPrice != 0
        pysipypRequest.orderedItems.get(0).priceInfo.rawTotalPrice != 0
        pysipypRequest.orderedItems.get(0).priceInfo.upliftedListPrice == 1

        //Zero Price Nil Pick Item
        pysipypRequest.detailLine.find { it.productID == cinNilPick } == null
        pysipypRequest.orderedItems.find { it.cin == cinNilPick } == null
    }

    def "Regular priced partial, nil pick and fully picked OMS order items takes IRO unit price"() {
        given:
        OmsOrder omsOrder = mockValidOmsOrder()
        String cinPartial = "5000"
        BigDecimal unitPricePartial = 2.2.toBigDecimal()
        BigDecimal storePricePartial = 5.5.toBigDecimal()
        long orderedQtyPartial = 6
        long pickedQtyPartial = 5
        BigDecimal vendorUnitPrice = BigDecimal.ONE
        addItemToOrder(omsOrder, cinPartial, storePricePartial, unitPricePartial, orderedQtyPartial, pickedQtyPartial, vendorUnitPrice)

        String cinNilPick = "6000"
        BigDecimal unitPriceNilPick = 2.2.toBigDecimal()
        BigDecimal storePriceNilPick = 0.toBigDecimal()
        long orderedQtyNilPick = 6
        long pickedQtyNilPick = 0
        addItemToOrder(omsOrder, cinNilPick, storePriceNilPick, unitPriceNilPick, orderedQtyNilPick, pickedQtyNilPick, vendorUnitPrice)

        String cinFullyPicked = "7000"
        BigDecimal unitPriceFullyPicked = 2.2.toBigDecimal()
        BigDecimal storePriceFullyPicked = 5.5.toBigDecimal()
        long orderedQtyFullyPicked = 6
        long pickedQtyFullyPicked = 6
        addItemToOrder(omsOrder, cinFullyPicked, storePriceFullyPicked, unitPriceFullyPicked, orderedQtyFullyPicked, pickedQtyFullyPicked, vendorUnitPrice)


        Map<String, Tax> taxInfoMap = createTaxMockMap()

        when:
        OrderInformation pysipypRequest = omsOrderToPYSIPYPOrderInfoMapper.buildPysipypRequestforMarketPlaceOrder(omsOrder, taxInfoMap)

        then:
        // Regular Price Partial Item
        pysipypRequest.detailLine.find { it.productID == cinPartial } != null
        pysipypRequest.orderedItems.find { it.cin == cinPartial } != null
        pysipypRequest.orderedItems.find { it.cin == cinPartial }.priceInfo.listPrice == unitPricePartial
        pysipypRequest.orderedItems.find { it.cin == cinPartial }.priceInfo.finalAmount == unitPricePartial * orderedQtyPartial
        pysipypRequest.orderedItems.find { it.cin == cinPartial }.priceInfo.rawTotalPrice == unitPricePartial * orderedQtyPartial

        //Regular Price Nil Pick Item
        pysipypRequest.detailLine.find { it.productID == cinNilPick } != null
        pysipypRequest.orderedItems.find { it.cin == cinNilPick } != null
        pysipypRequest.orderedItems.find { it.cin == cinNilPick }.priceInfo.listPrice == unitPriceNilPick
        pysipypRequest.orderedItems.find { it.cin == cinNilPick }.priceInfo.finalAmount == unitPriceNilPick * orderedQtyNilPick
        pysipypRequest.orderedItems.find { it.cin == cinNilPick }.priceInfo.rawTotalPrice == unitPriceNilPick * orderedQtyNilPick

        // Regular Price FullYPicked
        pysipypRequest.detailLine.find { it.productID == cinFullyPicked } != null
        pysipypRequest.orderedItems.find { it.cin == cinFullyPicked } != null
        pysipypRequest.orderedItems.find { it.cin == cinFullyPicked }.priceInfo.listPrice == unitPriceFullyPicked
        pysipypRequest.orderedItems.find { it.cin == cinFullyPicked }.priceInfo.finalAmount == unitPriceFullyPicked * orderedQtyFullyPicked
        pysipypRequest.orderedItems.find { it.cin == cinFullyPicked }.priceInfo.rawTotalPrice == unitPriceFullyPicked * orderedQtyFullyPicked
        pysipypRequest.orderedItems.find { it.cin == cinFullyPicked }.priceInfo.listPrice != 0.0
        pysipypRequest.orderedItems.find { it.cin == cinFullyPicked }.priceInfo.finalAmount != 0.0
        pysipypRequest.orderedItems.find { it.cin == cinFullyPicked }.priceInfo.rawTotalPrice != 0.0
        pysipypRequest.orderedItems.find { it.cin == cinFullyPicked }.priceInfo.upliftedListPrice == 1
    }

    private static OmsOrder mockValidOmsOrder() {

        return new OmsOrder(
                vertical: Vertical.MARKETPLACE,
                storeId: "5555",
                sourceOrderId: "12345",
                marketPlaceInfo: new MarketPlaceInfo(
                        vendor: Vendor.UBEREATS
                ),
                orderItemList: [
                        new OmsOrderItem(
                                cin: "1001",
                                skuId: "1002",
                                quantity: 1003,
                                itemPriceInfo: new ItemPriceInfo(
                                        unitPrice: new Money(
                                                amount: 12.1987.toBigDecimal()
                                        )
                                ),
                                uom: "E",
                                pickedItem: new PickedItem(
                                        id: 100,
                                        departmentID: 10,
                                        quantity: 11,
                                        pickedItemPriceInfo: new PickedItemPriceInfo(
                                                unitPrice: new Money(
                                                        amount: 12.toBigDecimal()
                                                )
                                        ),
                                        pickedItemUpcList: [
                                                new PickedItemUpc(
                                                        upc: "100",
                                                        weight: 13,
                                                        quantity: 14,
                                                        win: "15"
                                                )
                                        ],
                                        substitutedItems: [
                                                new SubstitutedItem(
                                                        id: 111,
                                                        description: "Substituted item",
                                                        department: "68",
                                                        consumerItemNumber: "1002",
                                                        walmartItemNumber: "5555",
                                                        quantity: 1,
                                                        weight: 0.0,
                                                        substitutedItemPriceInfo: new SubstitutedItemPriceInfo(
                                                                unitPrice: BigDecimal.ONE,
                                                                totalPrice: BigDecimal.ONE
                                                        ),
                                                        upcs: [
                                                                new SubstitutedItemUpc(
                                                                        id: 102,
                                                                        upc: "102791",
                                                                        uom: "E"
                                                                )
                                                        ]
                                                )
                                        ]

                                )

                        ),
                        new OmsOrderItem(
                                cin: "2001",
                                skuId: "2002",
                                quantity: 2003,
                                itemPriceInfo: new ItemPriceInfo(
                                        unitPrice: new Money(
                                                amount: 22.22.toBigDecimal()
                                        )
                                ),
                                uom: "E",
                                pickedItem: new PickedItem(
                                        id: 200,
                                        departmentID: 20,
                                        quantity: 21,
                                        pickedItemPriceInfo: new PickedItemPriceInfo(
                                                unitPrice: new Money(
                                                        amount: 22.toBigDecimal()
                                                )
                                        ),
                                        pickedItemUpcList: [
                                                new PickedItemUpc(
                                                        upc: "200",
                                                        weight: 23,
                                                        quantity: 24,
                                                        win: "25"
                                                )
                                        ]

                                )

                        )

                ]
        )

    }

    Map<String, Tax> createTaxMockMap() {
        Map<String, Tax> taxMap = new HashMap<>()
        taxMap.put("100", new Tax(
                taxCode: 10,
                rate: new Amount(
                        value: 11
                ),
                rateId: 12
        ))
        taxMap.put("200", new Tax(
                taxCode: 20,
                rate: new Amount(
                        value: 21
                ),
                rateId: 22
        ))
        taxMap.put("300", new Tax(
                taxCode: 20,
                rate: new Amount(
                        value: 21
                ),
                rateId: 22
        ))
        return taxMap
    }

    OmsOrder addItemToOrder(OmsOrder order, String cin, BigDecimal storePrice, BigDecimal iroUnitPrice, long orderedQty, long pickedQty,
                            BigDecimal vendorUnitPrice) {
        order.addItem(
                new OmsOrderItem(
                        cin: cin,
                        skuId: "5001",
                        quantity: orderedQty,
                        itemPriceInfo: new ItemPriceInfo(
                                unitPrice: new Money(
                                        amount: iroUnitPrice
                                ),
                                vendorUnitPrice: new Money(
                                        amount: vendorUnitPrice
                                )
                        ),
                        uom: "E",
                        pickedItem: new PickedItem(
                                id: 500,
                                departmentID: 10,
                                quantity: pickedQty,
                                orderedCin: cin,
                                pickedItemPriceInfo: new PickedItemPriceInfo(
                                        unitPrice: new Money(
                                                amount: storePrice
                                        )
                                ),
                                pickedItemUpcList: [
                                        new PickedItemUpc(
                                                upc: "300",
                                                weight: 0,
                                                quantity: pickedQty,
                                                win: "15"
                                        )
                                ]

                        )

                ))
        return order
    }

    private PYSIPYPServiceConfiguration mockPYSIPYPServiceConfiguration() {
        PYSIPYPServiceConfiguration config = new PYSIPYPServiceConfiguration()
        config.setAccessCode("ahgshagshagshgashgshg")
        config.setConnTimeout(1000)
        config.setLoggingEnabled(true)
        config.setReadTimeout(1000)
        config.setPysipypServiceUriForOrders("http://10.117.144.20:8080/asda-services/rest/orders/")
        config.setPysipypServiceUri("http://10.117.144.20:8080/asda-services/rest/order")
        return config
    }

    private mockValidCancelledEventMessage() {
        return new OrderCancelledDomainEventMessage()
                .builder().storeOrderId("12334").storeId("5755").build();
    }
}