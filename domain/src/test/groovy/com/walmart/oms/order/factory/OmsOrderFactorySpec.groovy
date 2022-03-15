package com.walmart.oms.order.factory

import com.walmart.common.domain.type.Currency
import com.walmart.common.domain.type.SubstitutionOption
import com.walmart.common.domain.type.Tenant
import com.walmart.common.domain.type.Vertical
import com.walmart.marketplace.order.domain.entity.type.Vendor
import com.walmart.oms.order.aggregateroot.OmsOrder
import com.walmart.oms.order.domain.entity.*
import com.walmart.oms.order.repository.IOmsOrderRepository
import com.walmart.oms.order.valueobject.*
import spock.lang.Ignore
import spock.lang.Specification

class OmsOrderFactorySpec extends Specification {

    OmsOrderFactory omsOrderFactory

    IOmsOrderRepository omsOrderRepository = Mock()

    def setup() {
        omsOrderFactory = new OmsOrderFactory(
                omsOrderRepository: omsOrderRepository
        )
    }

    def "Test CreateOmsOrder with all fields supplied"() {
        given:
        String externalOrderId = UUID.randomUUID().toString()
        String sourceOrderId = UUID.randomUUID().toString()
        String storeOrderId = "123456789"
        String storeId = "4401"
        String pickUpLocationId = "4863"
        String spokeStoreId = "7892"
        Date deliveryDate = new Date()
        Tenant tenant = Tenant.ASDA
        Vertical vertical = Vertical.MARKETPLACE

        omsOrderRepository.getNextIdentity() >> externalOrderId

        when:
        OmsOrder actualOrder = omsOrderFactory.createOmsOrder(sourceOrderId, storeOrderId, storeId, pickUpLocationId, spokeStoreId, deliveryDate, tenant, vertical)

        then:
        assert null != actualOrder
        assert actualOrder.getSourceOrderId() == sourceOrderId
        assert actualOrder.getId() == externalOrderId

    }

    def "Test CreateOmsOrder without mandatory storeId"() {
        given:
        String externalOrderId = UUID.randomUUID().toString()
        String sourceOrderId = UUID.randomUUID().toString()
        String storeOrderId = "123456789"
        String storeId = null
        String pickUpLocationId = "4863"
        String spokeStoreId = "7892"
        Date deliveryDate = new Date()
        Tenant tenant = Tenant.ASDA
        Vertical vertical = Vertical.MARKETPLACE

        omsOrderRepository.getNextIdentity() >> externalOrderId

        when:
        omsOrderFactory.createOmsOrder(sourceOrderId, storeOrderId, storeId, pickUpLocationId, spokeStoreId, deliveryDate, tenant, vertical)

        then:
        thrown(IllegalArgumentException)

    }

    def "Test CreateOmsOrder without mandatory source store id"() {
        given:
        String externalOrderId = UUID.randomUUID().toString()
        String sourceOrderId = null
        String storeOrderId = "123456789"
        String storeId = "4401"
        String pickUpLocationId = "4863"
        String spokeStoreId = "7892"
        Date deliveryDate = new Date()
        Tenant tenant = Tenant.ASDA
        Vertical vertical = Vertical.MARKETPLACE

        omsOrderRepository.getNextIdentity() >> externalOrderId

        when:
        omsOrderFactory.createOmsOrder(sourceOrderId, storeOrderId, storeId, pickUpLocationId, spokeStoreId, deliveryDate, tenant, vertical)

        then:
        thrown(IllegalArgumentException)

    }

    def "Test getOmsOrder with pre existing order"() {
        given:
        String externalOrderId = UUID.randomUUID().toString()
        OmsOrder expectedOmsOrder = new OmsOrder("READY_FOR_STORE")
        Tenant tenant = Tenant.ASDA
        Vertical vertical = Vertical.MARKETPLACE
        omsOrderRepository.getOrderByMarketPlaceId(_ as String, _ as Tenant, _ as Vertical) >> expectedOmsOrder

        when:
        OmsOrder omsOrder = omsOrderFactory.getOmsOrder(externalOrderId, tenant, vertical)

        then:
        assert omsOrder == expectedOmsOrder
    }

    def "Test getOmsOrder with new order"() {
        given:
        String externalOrderId = UUID.randomUUID().toString()
        Tenant tenant = Tenant.ASDA
        Vertical vertical = Vertical.MARKETPLACE
        omsOrderRepository.getOrderByMarketPlaceId(_ as String, _ as Tenant, _ as Vertical) >> null

        when:
        OmsOrder omsOrder = omsOrderFactory.getOmsOrder(externalOrderId, tenant, vertical)

        then:
        assert omsOrder.isTransientState()
    }

    def "CreateContactInfo with all mandatory params"() {
        given:
        OmsOrder parentOmsOrder = OmsOrder.builder().storeId("4401")
                .sourceOrderId(UUID.randomUUID().toString())
                .storeOrderId("123456789")
                .deliveryDate(new Date()).build()
        TelePhone phoneNumberOne = new TelePhone("0123456789")
        MobilePhone mobilePhone = new MobilePhone("0123456789")

        FullName customerName = new FullName("Mr", "John", null, "Doe")


        when:
        CustomerContactInfo customerContactInfo = omsOrderFactory.createContactInfo(parentOmsOrder, customerName, mobilePhone, phoneNumberOne, null)

        then:
        assert null != customerContactInfo
        assert customerContactInfo.getFullName().getFirstName() == "John"
        assert customerContactInfo.getPhoneNumberOne() == phoneNumberOne

    }

    def "CreateContactInfo without any phone information"() {
        given:
        OmsOrder parentOmsOrder = OmsOrder.builder().storeId("4401")
                .sourceOrderId(UUID.randomUUID().toString())
                .storeOrderId("123456789")
                .deliveryDate(new Date()).build()

        FullName customerName = new FullName("Mr", "John", null, "Doe")


        when:
        omsOrderFactory.createContactInfo(parentOmsOrder, customerName, null, null, null)

        then:
        thrown(IllegalArgumentException)

    }

    def "CreateContactInfo without first name and last name "() {
        given:
        OmsOrder parentOmsOrder = OmsOrder.builder().storeId("4401")
                .sourceOrderId(UUID.randomUUID().toString())
                .storeOrderId("123456789")
                .deliveryDate(new Date()).build()

        TelePhone phoneNumberOne = new TelePhone("0123456789")
        MobilePhone mobilePhone = new MobilePhone("0123456789")


        when:
        omsOrderFactory.createContactInfo(parentOmsOrder, null, mobilePhone, phoneNumberOne, null)

        then:
        thrown(IllegalArgumentException)

    }

    def "CreateAddressInfo"() {
    }

    def " Test CreateMarketPlaceInfo"() {
        given:
        Vendor expectedVendor = Vendor.UBEREATS
        String expectedVendorOrderId = UUID.randomUUID().toString()

        when:
        MarketPlaceInfo actualMarketPlaceInfo = omsOrderFactory.createMarketPlaceInfo(expectedVendor, expectedVendorOrderId)

        then:
        assert actualMarketPlaceInfo.getVendorOrderId() == expectedVendorOrderId
        assert actualMarketPlaceInfo.getVendor() == expectedVendor

    }

    def "Test CreateItem with all mandatory params"() {
        given:
        OmsOrder parentOmsOrder = OmsOrder.builder().storeId("4401")
                .sourceOrderId(UUID.randomUUID().toString())
                .storeOrderId("123456789")
                .deliveryDate(new Date()).build()
        String expectedCin = "46463636"
        String expectedItemDescription = "Test Description"
        long quantity = 2
        Money expectedVendorUnitPrice = new Money(BigDecimal.valueOf(2.5), Currency.GBP)
        Money expectedVendorTotalPrice = new Money(BigDecimal.valueOf(5.0), Currency.GBP)

        when:
        OmsOrderItem actualOrderItem = omsOrderFactory.createOrderedItem(parentOmsOrder, expectedCin, expectedItemDescription, quantity, expectedVendorUnitPrice, expectedVendorTotalPrice,
                SubstitutionOption.DO_NOT_SUBSTITUTE)

        then:
        assert actualOrderItem.getCin() == expectedCin
        assert actualOrderItem.getItemPriceInfo().getVendorUnitPrice() == expectedVendorUnitPrice
        assert actualOrderItem.getItemPriceInfo().getVendorTotalPrice() == expectedVendorTotalPrice


    }

    @Ignore
    def "Test CreateItem without order"() {

        given:
        String expectedCin = "46463636"
        String expectedItemDescription = "Test Description"
        long quantity = 2
        Money expectedVendorUnitPrice = new Money(BigDecimal.valueOf(2.5), Currency.GBP)
        Money expectedVendorTotalPrice = new Money(BigDecimal.valueOf(5.0), Currency.GBP)

        when:
        omsOrderFactory.createOrderedItem(null, expectedCin, expectedItemDescription, quantity, expectedVendorUnitPrice, expectedVendorTotalPrice,SubstitutionOption.DO_NOT_SUBSTITUTE)

        then:
        thrown(NullPointerException)

    }

    def "Test CreateItem without cin"() {
        given:
        OmsOrder parentOmsOrder = OmsOrder.builder().storeId("4401")
                .sourceOrderId(UUID.randomUUID().toString())
                .storeOrderId("123456789")
                .deliveryDate(new Date()).build()

        String expectedCin = null
        String expectedItemDescription = "Test Description"
        long quantity = 2
        Money expectedVendorUnitPrice = new Money(BigDecimal.valueOf(2.5), Currency.GBP)
        Money expectedVendorTotalPrice = new Money(BigDecimal.valueOf(5.0), Currency.GBP)

        when:
        omsOrderFactory.createOrderedItem(parentOmsOrder, expectedCin, expectedItemDescription, quantity, expectedVendorUnitPrice, expectedVendorTotalPrice,SubstitutionOption.DO_NOT_SUBSTITUTE)

        then:
        thrown(IllegalArgumentException)


    }

    def "Test CreateItem without market place prices"() {

        given:
        OmsOrder parentOmsOrder = OmsOrder.builder().storeId("4401")
                .sourceOrderId(UUID.randomUUID().toString())
                .storeOrderId("123456789")
                .deliveryDate(new Date()).build()
        String expectedCin = "46463636"
        String expectedItemDescription = "Test Description"
        long quantity = 2

        when:
        omsOrderFactory.createOrderedItem(parentOmsOrder, expectedCin, expectedItemDescription, quantity, null, null,SubstitutionOption.DO_NOT_SUBSTITUTE)

        then:
        thrown(NullPointerException)

    }

    def "Test CreateItem with zero quantity"() {

        given:
        OmsOrder parentOmsOrder = OmsOrder.builder().storeId("4401")
                .sourceOrderId(UUID.randomUUID().toString())
                .storeOrderId("123456789")
                .deliveryDate(new Date()).build()
        String expectedCin = "46463636"
        String expectedItemDescription = "Test Description"
        long quantity = 0

        Money expectedVendorUnitPrice = new Money(BigDecimal.valueOf(2.5), Currency.GBP)
        Money expectedVendorTotalPrice = new Money(BigDecimal.valueOf(5.0), Currency.GBP)

        when:
        omsOrderFactory.createOrderedItem(parentOmsOrder, expectedCin, expectedItemDescription, quantity, expectedVendorUnitPrice, expectedVendorTotalPrice,SubstitutionOption.DO_NOT_SUBSTITUTE)

        then:
        thrown(IllegalArgumentException)

    }

    def " Test GetOmsOrderBySourceOrder with pre exisiting order"() {

        given:
        OmsOrder parentOmsOrder = OmsOrder.builder().storeId("4401")
                .sourceOrderId(UUID.randomUUID().toString())
                .storeOrderId("123456789")
                .deliveryDate(new Date())
                .orderState("READY_FOR_STORE").build()
        Tenant tenant = Tenant.ASDA
        Vertical vertical = Vertical.MARKETPLACE
        String sourceOrderId = UUID.randomUUID().toString()
        omsOrderRepository.getOrder(_ as String, _ as Tenant, _ as Vertical) >> parentOmsOrder

        when:
        OmsOrder actualOmsOrder = omsOrderFactory.getOmsOrderBySourceOrder(sourceOrderId, tenant, vertical)

        then:
        assert actualOmsOrder.getOrderState() == "READY_FOR_STORE"
        assert actualOmsOrder == parentOmsOrder

    }

    def " Test GetOmsOrderBySourceOrder without pre existing order"() {

        given:
        String sourceOrderId = UUID.randomUUID().toString()
        Tenant tenant = Tenant.ASDA
        Vertical vertical = Vertical.MARKETPLACE
        omsOrderRepository.getOrder(_ as String, _ as Tenant, _ as Vertical) >> null

        when:
        OmsOrder actualOmsOrder = omsOrderFactory.getOmsOrderBySourceOrder(sourceOrderId, tenant, vertical)

        then:
        assert actualOmsOrder.getOrderState() == "INITIAL"

    }


    def "Test CreatePickedItem"() {
        given:
        String description = "Test Picker description"
        String departmentId = "64"
        String cin = "45627"
        String pickedBy = "storeuser"
        String winOne = "37378"
        String upcOne = "2828282222"
        String winTwo = "37379"
        String upcTwo = "2828282223"

        List pickedUpcs = Arrays.asList((PickedItemUpc.builder()
                .win(winOne).upc(upcOne)
                .uom("EACH")
                .storeUnitPrice(new Money(3.0, Currency.GBP)).quantity(2).build()), (PickedItemUpc.builder()
                .win(winTwo).upc(upcTwo)
                .uom("EACH")
                .storeUnitPrice(new Money(3.5, Currency.GBP)).quantity(2).build()))

        when:
        PickedItem pickedItem = omsOrderFactory.createPickedItem(description, departmentId, cin, pickedBy, pickedUpcs)

        then:
        pickedItem.pickedItemDescription == description
        pickedItem.departmentID == departmentId
        pickedItem.orderedCin == cin
        pickedItem.picker.pickerUserName == pickedBy
        pickedItem.pickedItemUpcList.size() == 2
        pickedItem.pickedItemUpcList[0].upc == upcOne
        pickedItem.pickedItemUpcList[0].win == winOne
        pickedItem.pickedItemUpcList[1].upc == upcTwo
        pickedItem.pickedItemUpcList[1].win == winTwo

    }

    def "Test createPriceInfo"() {
        when:
        OrderPriceInfo orderPriceInfo = omsOrderFactory.createPriceInfo(10.0, 0.40)

        then:
        orderPriceInfo != null
        orderPriceInfo.orderTotal == 10.0
        orderPriceInfo.carrierBagCharge == 0.40
    }

    def "Test CreatePickedItemUpc"() {
        given:
        long pickedQuantity = 10
        BigDecimal unitPrice = 10.9
        String uom = "uom"
        String win = "win"
        String upc = "upc"

        when:
        PickedItemUpc pickedItemUpc = omsOrderFactory.createPickedItemUpc(pickedQuantity, unitPrice, uom, win, upc)

        then:
        pickedItemUpc.getUom() == "uom"

    }

    def "Test CreateSchedulingInfo"() {
        given:
        OmsOrder omsOrder = Mock()
        String tripId = "tripId"
        int doorStepTime = 12
        Date plannedDueTime = new Date()
        String vanId = "vanId"
        String scheduleNumber = "scheduleNumber"
        String loadNumber = "loadNumber"

        when:
        SchedulingInfo schedulingInfo = omsOrderFactory.createSchedulingInfo(omsOrder, tripId, doorStepTime, plannedDueTime, vanId, scheduleNumber, loadNumber)

        then:
        schedulingInfo.getDoorStepTime() == 12

    }


    def "Test CreateAddressInfo"() {
        given:
        OmsOrder omsOrder = Mock()
        String addressOne = "addressOne"
        String addressTwo = "addressTwo"
        String addressThree = "addressThree"
        String addressType = "addressType"
        String city = "city"
        String country = "country"
        String latitude = "latitude"
        String longitude = "longitude"
        String postCode = "postCode"
        String county = "county"
        String state = "state"

        when:
        AddressInfo addressInfo = omsOrderFactory.createAddressInfo(omsOrder, addressOne, addressTwo, addressThree, addressType, city, country, latitude, longitude, postCode, county, state)

        then:
        addressInfo.getState() == "state"
    }

    def "Test createBundleItemInfo"() {
        given:
        OmsOrderItem omsOrderItem = Mock()

        when:
        OmsOrderBundledItem bundleItem = omsOrderFactory.createOmsOrderBundleItem(
                omsOrderItem, 1, 1,"11", "22", "combo1")

        then:
        bundleItem.getBundleInstanceId() == "22"
        bundleItem.getBundleQuantity() == 1
        bundleItem.getItemQuantity() == 1
        bundleItem.getBundleSkuId() == "11"
    }

    def "Test createSubstitutedItem"() {
        when:
        SubstitutedItem substitutedItem = omsOrderFactory.createSubstitutedItem("12",
                "13",
                "14",
                "COKE",
                BigDecimal.TEN,
                BigDecimal.valueOf(20L),
                2L,
                Arrays.asList(SubstitutedItemUpc.builder().upc("1213").uom("KG").build()),
                4.0)

        then:
        assert substitutedItem.getDescription() == "COKE"
        assert substitutedItem.getWalmartItemNumber() == "13"
        assert substitutedItem.getConsumerItemNumber() == "12"
        assert substitutedItem.getDepartment() == "14"
        assert substitutedItem.getSubstitutedItemPriceInfo().getUnitPrice() == BigDecimal.TEN
        assert substitutedItem.getQuantity() == 2L
        assert substitutedItem.getSubstitutedItemPriceInfo().getTotalPrice() == BigDecimal.valueOf(20L)
        assert substitutedItem.getUpcs().size() == 1
        assert substitutedItem.getUpcs().get(0).getUom() == "KG"
        assert substitutedItem.getUpcs().get(0).getUpc() == "1213"
    }

    def "test buildSubstitutedItemUpc"() {
        when:
        SubstitutedItemUpc substitutedItemUpc = omsOrderFactory.buildSubstitutedItemUpcs("1", "E");
        then:
        assert substitutedItemUpc.uom == "E"
        assert substitutedItemUpc.upc == "1"
    }
}
