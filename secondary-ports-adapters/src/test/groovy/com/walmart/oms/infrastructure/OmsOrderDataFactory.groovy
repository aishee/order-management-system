package com.walmart.oms.infrastructure

import com.walmart.common.domain.type.Currency
import com.walmart.common.domain.type.FulfillmentType
import com.walmart.common.domain.type.Tenant
import com.walmart.common.domain.type.Vertical
import com.walmart.marketplace.order.domain.entity.type.Vendor
import com.walmart.oms.order.aggregateroot.OmsOrder
import com.walmart.oms.order.domain.entity.AddressInfo
import com.walmart.oms.order.domain.entity.CustomerContactInfo
import com.walmart.oms.order.domain.entity.OmsOrderItem
import com.walmart.oms.order.domain.entity.PickedItem
import com.walmart.oms.order.domain.entity.PickedItemUpc
import com.walmart.oms.order.domain.entity.SchedulingInfo
import com.walmart.oms.order.valueobject.CatalogItem
import com.walmart.oms.order.valueobject.EmailAddress
import com.walmart.oms.order.valueobject.FullName
import com.walmart.oms.order.valueobject.ItemPriceInfo
import com.walmart.oms.order.valueobject.MarketPlaceInfo
import com.walmart.oms.order.valueobject.Money
import com.walmart.oms.order.valueobject.OrderPriceInfo
import com.walmart.oms.order.valueobject.Picker
import com.walmart.oms.order.valueobject.PricingResponse
import com.walmart.oms.order.valueobject.TelePhone

class OmsOrderDataFactory {

    static OmsOrder a_valid_oms_order(String sourceOrderId) {

        OmsOrder testOmsOrder = OmsOrder.builder()
                .id(UUID.randomUUID().toString())
                .vertical(Vertical.ASDAGR)
                .tenant(Tenant.ASDA)
                .storeId("4401")
                .sourceOrderId(sourceOrderId)
                .storeOrderId("123456789")
                .pickupLocationId("4401")
                .spokeStoreId("4401")
                .deliveryDate(new Date())
                .fulfillmentType(FulfillmentType.INSTORE_PICKUP)
                .marketPlaceInfo(MarketPlaceInfo.builder().vendor(Vendor.UBEREATS).vendorOrderId(UUID.randomUUID().toString()).build())
                .orderState("EPOS_COMPLETE")
                .priceInfo(OrderPriceInfo.builder()
                        .orderSubTotal(40.0).build()).build()
        testOmsOrder.modifiedDate = new Date()
        testOmsOrder.createdDate = new Date()
        testOmsOrder.addAddress(AddressInfo.builder()
                .omsOrder(testOmsOrder)
                .addressOne("test addressOne")
                .addressTwo("test addressTwo")
                .addressThree("Test adddressThree")
                .city("test city")
                .latitude("57.2252552")
                .longitude("-6.24343").build())


        testOmsOrder.addSchedulingInfo(SchedulingInfo.builder()
                .order(testOmsOrder)
                .plannedDueTime(new Date())
                .scheduleNumber("222")
                .vanId("999")
                .loadNumber("123")
                .doorStepTime(0)
                .tripId("1234567")
                .build())


        testOmsOrder.addContactInfo(CustomerContactInfo.builder()
                .order(testOmsOrder)
                .fullName(new FullName(null, "John", null, "Doe"))
                .email(new EmailAddress("abc@email.com"))
                .phoneNumberOne(new TelePhone("0123456789")).build())


        testOmsOrder.addItem(OmsOrderItem.builder()
                .omsOrder(testOmsOrder)
                .cin("464646")
                .itemPriceInfo(new ItemPriceInfo(new Money(BigDecimal.valueOf(2.5), Currency.GBP), new Money(BigDecimal.valueOf(5.0), Currency.GBP), new Money(BigDecimal.valueOf(2.0), Currency.GBP)))
                .quantity(2)
                .salesUnit("EACH")
                .uom("E")
                .build())

        CatalogItem catalogItem= new CatalogItem("123","464646",["1","2"],Boolean.FALSE,"ASDA","itemName1","name","pickerDesc","EACH","http://largeImageURL.jpg","http://smallImageURL.jpg",["4401","4402"],"3",0,0,Boolean.FALSE, "3","False","3.0",Boolean.FALSE,"2.5")
        testOmsOrder.orderItemList.get(0).enrichItemWithCatalogItemData(catalogItem)

        List<PickedItemUpc> pickedItemUpcList = new ArrayList<>()
        PickedItemUpc pickedItemUpc= new PickedItemUpc("","12345","E","12",2,3,new Money(BigDecimal.valueOf(2.5), Currency.GBP))
        pickedItemUpcList.add(pickedItemUpc)

        PickedItem pickedItem = new PickedItem(testOmsOrder.orderItemList.get(0),"1","Dept","464646",2,"Picked_Desc", new Picker("Picker123"),pickedItemUpcList)
        Map<String,PickedItem> pickedItemMap= new HashMap<>();
        pickedItemMap.put("464646",pickedItem)

        testOmsOrder.updatePickedItemsFromStore(pickedItemMap)

        PricingResponse.ItemPriceService itemPriceService = new PricingResponse.ItemPriceService()
        itemPriceService.setAdjustedPrice(2.5);
        itemPriceService.setAdjustedPriceExVat(2.6);
        itemPriceService.setWebAdjustedPrice(2.7);
        itemPriceService.setDisplayPrice(2.8);
        itemPriceService.setVatAmount(2.9);

        Map<String, PricingResponse.ItemPriceService> itemPriceServiceMap = new HashMap<>()
        itemPriceServiceMap.put("464646", itemPriceService)

        PricingResponse pricingResponse= new PricingResponse()
        pricingResponse.setPosOrderTotalPrice(10)
        pricingResponse.setItemPriceServiceMap(itemPriceServiceMap)
        testOmsOrder.enrichPickedItemsAfterPricing(testOmsOrder,pricingResponse)


        return testOmsOrder
    }


}
