package com.walmart.oms.order.domain

import com.walmart.common.config.POSConfig
import com.walmart.common.domain.type.Currency
import com.walmart.oms.order.aggregateroot.OmsOrder
import com.walmart.oms.order.domain.entity.OmsOrderItem
import com.walmart.oms.order.domain.entity.PickedItem
import com.walmart.oms.order.domain.entity.PickedItemUpc
import com.walmart.oms.order.factory.OmsOrderFactory
import com.walmart.oms.order.gateway.IPricingGateway
import com.walmart.oms.order.gateway.ITaxGateway
import com.walmart.oms.order.repository.IOmsOrderRepository
import com.walmart.oms.order.valueobject.*
import com.walmart.tax.calculator.dto.Tax
import spock.lang.Specification

class OmsOrderPickCompleteDomainServiceSpec extends Specification {

    String sourceOrderId = UUID.randomUUID().toString()

    ITaxGateway taxGateway = Mock()

    OmsOrderFactory omsOrderFactory = Mock()

    IPricingGateway pricingGateway = Mock()

    IOmsOrderRepository omsOrderRepository = Mock()

    POSConfig posConfig = Mock()

    OmsOrderPickCompleteDomainService omsOrderPickCompleteDomainService

    def setup() {
        omsOrderPickCompleteDomainService = new OmsOrderPickCompleteDomainService(
                taxGateway: taxGateway,
                omsOrderFactory: omsOrderFactory,
                pricingGateway: pricingGateway,
                omsOrderRepository: omsOrderRepository,
                posConfig: posConfig
        )
    }


    def "PerformPricingOnTheOrder"() {
        given:
        OmsOrder testOmsOrder = getOmsOrder("INITIAL")
        testOmsOrder.addPriceInfo(getOrderPriceInfo())
        OmsOrderItem omsOrderItem = getOmsOrderItem(testOmsOrder)
        PickedItemUpc pickedItemUpc = getPickedItemUpc()
        PickedItem pickedItem = getPickedItem(pickedItemUpc)
        testOmsOrder.addItem(omsOrderItem)
        taxGateway.fetchTaxData(_ as List) >> new HashMap<String, Tax>()
        omsOrderFactory.createOrderedItem(_, _, _, _, _, _, _) >> omsOrderItem
        omsOrderFactory.createPickedItem(_, _, _, _, _) >> pickedItem
        omsOrderFactory.createPickedItemUpc(_, _, _, _, _) >> pickedItemUpc

        when:
        omsOrderPickCompleteDomainService.performPricingOnTheOrder(testOmsOrder)

        then:
        1 * taxGateway.fetchTaxData(_ as List, _ as String) >> new HashMap<String, Tax>()
        1 * pricingGateway.priceOrder(_, _) >> getPricingResponse()
        assert testOmsOrder.getOrderItemList().size() == 2
    }

    def "PerformPricingOnTheOrder for no carrier bag case"() {
        given:
        OmsOrder testOmsOrder = getOmsOrder("INITIAL")
        testOmsOrder.addPriceInfo(OrderPriceInfo.builder().orderTotal(10.0).build())
        OmsOrderItem omsOrderItem = getOmsOrderItem(testOmsOrder)
        PickedItemUpc pickedItemUpc = getPickedItemUpc()
        PickedItem pickedItem = getPickedItem(pickedItemUpc)
        pickedItemUpc.addPickedItem(pickedItem)

        testOmsOrder.addItem(omsOrderItem)
        pricingGateway.priceOrder(_, _) >> Optional.empty()
        taxGateway.fetchTaxData(_ as List) >> new HashMap<String, Tax>()
        omsOrderFactory.createOrderedItem(_, _, _, _, _, _,_) >> omsOrderItem
        omsOrderFactory.createPickedItem(_, _, _, _, _) >> pickedItem
        omsOrderFactory.createPickedItemUpc(_, _, _, _, _) >> pickedItemUpc

        when:
        omsOrderPickCompleteDomainService.performPricingOnTheOrder(testOmsOrder)

        then:
        1 * taxGateway.fetchTaxData(_ as List, _ as String)
        1 * pricingGateway.priceOrder(_, _) >> Optional.empty()
        assert testOmsOrder.getOrderItemList().size() == 1
        assert pickedItemUpc.getUnitPriceAmount().get() == 1
        assert pickedItemUpc.getPickedItem() != null
    }

    private PickedItem getPickedItem(PickedItemUpc pickedItemUpc) {
        return PickedItem.builder()
                .id("12313")
                .departmentID("78")
                .orderedCin("17332")
                .pickedItemDescription("Item picked")
                .picker(new Picker("bagdfd"))
                .pickedItemUpcList(Arrays.asList(pickedItemUpc))
                .build()
    }

    private static OrderPriceInfo getOrderPriceInfo() {
        return OrderPriceInfo.builder()
                .carrierBagCharge(0.4d)
                .orderTotal(10.0)
                .build()
    }

    private PickedItemUpc getPickedItemUpc() {
        return PickedItemUpc.builder()
                .id(omsOrderRepository.getNextIdentity())
                .quantity(1l)
                .upc("msadh")
                .uom("NHD")
                .storeUnitPrice(new Money(BigDecimal.ONE, Currency.GBP))
                .win("msdmsnd")
                .build()
    }

    private static OmsOrderItem getOmsOrderItem(OmsOrder testOmsOrder) {
        Money money = new Money(BigDecimal.ONE, Currency.GBP)
        return OmsOrderItem.builder()
                .cin("1000")
                .quantity(1l)
                .itemDescription("Mushrooms")
                .salesUnit("1")
                .skuId("999")
                .omsOrder(testOmsOrder)
                .itemPriceInfo(new ItemPriceInfo(money, money, money))
                .uom("E").weight(500).build()
    }

    private OmsOrder getOmsOrder(String orderState) {
        return OmsOrder.builder()
                .sourceOrderId(sourceOrderId)
                .storeOrderId("123456789")
                .storeId("4401")
                .deliveryDate(new Date())
                .orderState(orderState).build()
    }

    private Optional<PricingResponse> getPricingResponse() {
        Double posOrderTotalPrice = 10.5
        Map<String, PricingResponse.ItemPriceService> itemPriceServiceMap = new HashMap<>()
        PricingResponse.ItemPriceService itemPriceService = new PricingResponse.ItemPriceService()
        itemPriceService.setAdjustedPrice(12.9)
        itemPriceService.setAdjustedPriceExVat(7.8)
        itemPriceService.setDisplayPrice(10.5)
        itemPriceService.setVatAmount(10.9)
        itemPriceService.setWebAdjustedPrice(12.8)
        itemPriceServiceMap.put("pricing", itemPriceService)
        PricingResponse pricingResponse = new PricingResponse()
        pricingResponse.setItemPriceServiceMap(itemPriceServiceMap)
        pricingResponse.setPosOrderTotalPrice(posOrderTotalPrice)
        return Optional.of(pricingResponse)
    }
}
