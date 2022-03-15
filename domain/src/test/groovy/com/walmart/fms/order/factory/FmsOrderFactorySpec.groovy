package com.walmart.fms.order.factory

import com.walmart.common.domain.type.Currency
import com.walmart.common.domain.type.SubstitutionOption
import com.walmart.common.domain.type.Tenant
import com.walmart.common.domain.type.Vertical
import com.walmart.fms.order.aggregateroot.FmsOrder
import com.walmart.fms.order.domain.entity.FmsAddressInfo
import com.walmart.fms.order.domain.entity.FmsCustomerContactInfo
import com.walmart.fms.order.domain.entity.FmsOrderItem
import com.walmart.fms.order.domain.entity.FmsOrderTimestamps
import com.walmart.fms.order.domain.entity.FmsPickedItem
import com.walmart.fms.order.domain.entity.FmsPickedItemUpc
import com.walmart.fms.order.domain.entity.FmsSchedulingInfo
import com.walmart.fms.order.domain.entity.FmsSubstitutedItem
import com.walmart.fms.order.domain.entity.FmsSubstitutedItemUpc
import com.walmart.fms.order.repository.IFmsOrderRepository
import com.walmart.fms.order.valueobject.*
import com.walmart.marketplace.order.domain.entity.type.Vendor
import spock.lang.Ignore
import spock.lang.Specification

class FmsOrderFactorySpec extends Specification {

    FmsOrderFactory fmsOrderFactory

    IFmsOrderRepository fmsOrderRepository = Mock()

    def setup() {
        fmsOrderFactory = new FmsOrderFactory(
                fmsOrderRepository:fmsOrderRepository
        )
    }

    def "Test CreateFmsOrder with all fields supplied"() {
        given:
        String externalOrderId = UUID.randomUUID().toString()
        String sourceOrderId = UUID.randomUUID().toString()
        String storeOrderId = UUID.randomUUID().toString()
        String storeId = "4433"
        Date deliveryDate = new Date()
        Tenant tenant = Tenant.ASDA
        Vertical vertical = Vertical.MARKETPLACE

        fmsOrderRepository.getNextIdentity() >> externalOrderId

        when:
        FmsOrder actualOrder = fmsOrderFactory.createFmsOrder(sourceOrderId, storeId,storeOrderId,  deliveryDate, tenant, vertical, storeId)

        then:
        assert null != actualOrder
        assert actualOrder.getSourceOrderId() == sourceOrderId
        assert actualOrder.getId() == externalOrderId

    }

    def "Test CreateFmsOrder without mandatory storeId"() {
        given:
        String externalOrderId = UUID.randomUUID().toString()
        String sourceOrderId = UUID.randomUUID().toString()
        String storeOrderId = UUID.randomUUID().toString()
        String storeId = null
        Date deliveryDate = new Date()
        Tenant tenant = Tenant.ASDA
        Vertical vertical = Vertical.MARKETPLACE

        fmsOrderRepository.getNextIdentity() >> externalOrderId

        when:
        fmsOrderFactory.createFmsOrder(sourceOrderId, storeId,storeOrderId,  deliveryDate, tenant, vertical, storeId)

        then:
        thrown(IllegalArgumentException)

    }

    def "Test CreateFmsOrder without mandatory source store id"() {
        given:
        String externalOrderId = UUID.randomUUID().toString()
        String sourceOrderId = null
        String storeOrderId = null
        String storeId = null
        Date deliveryDate = new Date()
        Tenant tenant = Tenant.ASDA
        Vertical vertical = Vertical.MARKETPLACE

        fmsOrderRepository.getNextIdentity() >> externalOrderId

        when:
        fmsOrderFactory.createFmsOrder(sourceOrderId, storeId, storeOrderId,  deliveryDate, tenant, vertical, storeId)

        then:
        thrown(IllegalArgumentException)

    }

    def "Test getFmsOrder with pre existing order"() {
        given:
        String externalOrderId = UUID.randomUUID().toString()
        FmsOrder expectedFmsOrder = new FmsOrder("READY_FOR_STORE")
        Tenant tenant = Tenant.ASDA
        Vertical vertical = Vertical.MARKETPLACE
        fmsOrderRepository.getOrderByMarketPlaceId(_ as String, _ as Tenant, _ as Vertical) >> expectedFmsOrder

        when:
        FmsOrder fmsOrder = fmsOrderFactory.getFmsOrder(externalOrderId, tenant, vertical)

        then:
        assert fmsOrder == expectedFmsOrder
    }

    def "Test getFmsOrder with new order"() {
        given:
        String externalOrderId = UUID.randomUUID().toString()
        Tenant tenant = Tenant.ASDA
        Vertical vertical = Vertical.MARKETPLACE
        fmsOrderRepository.getOrderByMarketPlaceId(_ as String, _ as Tenant, _ as Vertical) >> null

        when:
        FmsOrder fmsOrder = fmsOrderFactory.getFmsOrder(externalOrderId, tenant, vertical)

        then:
        assert fmsOrder.isTransientState()
    }

    def "CreateContactInfo with all mandatory params"() {
        given:
        FmsOrder parentFmsOrder = FmsOrder.builder().storeId("4401")
        .sourceOrderId(UUID.randomUUID().toString())
        .deliveryDate(new Date()).build()
        TelePhone phoneNumberOne = new TelePhone("0123456789")
        MobilePhone mobilePhone = new MobilePhone("0123456789")
        String customerId = "234242342342"
        FullName customerName = new FullName("Mr", "John", null, "Doe")


        when:
        FmsCustomerContactInfo customerContactInfo = fmsOrderFactory.createContactInfo(parentFmsOrder,customerId, customerName, mobilePhone, phoneNumberOne, null)

        then:
        assert null != customerContactInfo
        assert customerContactInfo.getFullName().getFirstName() == "John"
        assert customerContactInfo.getPhoneNumberOne() == phoneNumberOne
        assert customerContactInfo.getPhoneNoOne() == "0123456789"
        assert customerContactInfo.getPhoneNoTwo() == null
        assert customerContactInfo.getCustomerTitle() == "Mr"
        assert customerContactInfo.getEmailAddr() == null
        assert customerContactInfo.getFirstlNameVal() == "John"
        assert customerContactInfo.getLastNameVal() == "Doe"
        assert customerContactInfo.getMiddleNameVal() == null
        assert customerContactInfo.getMobileNo() == "0123456789"

    }

    def "CreateContactInfo without any phone information"() {
        given:
        FmsOrder parentFmsOrder = FmsOrder.builder().storeId("4401")
                .sourceOrderId(UUID.randomUUID().toString())
                .deliveryDate(new Date()).build()
        String customerId = "234242342342"

        FullName customerName = new FullName("Mr", "John", null, "Doe")


        when:
        fmsOrderFactory.createContactInfo(parentFmsOrder,customerId, customerName, null, null, null)

        then:
        thrown(IllegalArgumentException)

    }

    def "CreateContactInfo without first name and last name "() {
        given:
        FmsOrder parentFmsOrder = FmsOrder.builder().storeId("4401")
                .sourceOrderId(UUID.randomUUID().toString())
                .deliveryDate(new Date()).build()
        String customerId = "22222"
        TelePhone phoneNumberOne = new TelePhone("0123456789")
        MobilePhone mobilePhone = new MobilePhone("0123456789")


        when:
        fmsOrderFactory.createContactInfo(parentFmsOrder, customerId, null, mobilePhone, phoneNumberOne, null)

        then:
        thrown(IllegalArgumentException)

    }

    def "Test CreateAddressInfo"() {
        given:
        FmsOrder parentFmsOrder = FmsOrder.builder().storeId("4401")
                .sourceOrderId(UUID.randomUUID().toString())
                .deliveryDate(new Date()).build()
        String addressOne = "Address Line One"
        String addressTwo = "Address Line Two"
        String addressThree = "Address Line Three"
        String city = "Brisbane"
        String country = "UK"
        String postCode = "DN211ZA"



        when:
        FmsAddressInfo addressInfo = fmsOrderFactory.createAddressInfo(parentFmsOrder, addressOne, addressTwo, addressThree, null, city, country, null, null, postCode, null, null
        )
        then:
        assert null != addressInfo
        assert addressInfo.getAddressOne() == addressOne
        assert addressInfo.getAddressThree() == addressThree
        assert addressInfo.getPostalCode() == postCode


    }

    def " Test CreateMarketPlaceInfo"() {
        given:
        Vendor expectedVendor = Vendor.UBEREATS
        String expectedVendorOrderId = UUID.randomUUID().toString()

        when:
        MarketPlaceInfo actualMarketPlaceInfo =fmsOrderFactory.createMarketPlaceInfo(expectedVendor, expectedVendorOrderId)

        then:
        assert actualMarketPlaceInfo.getVendorOrderId() == expectedVendorOrderId
        assert actualMarketPlaceInfo.getVendor() == expectedVendor

    }

    def "Test CreateItem with all mandatory params"() {
        given:
        FmsOrder parentFmsOrder = FmsOrder.builder().storeId("4401")
                .sourceOrderId(UUID.randomUUID().toString())
                .deliveryDate(new Date()).build()
        String expectedCin = "46463636"
        String expectedItemId  = "22224333"
        long quantity = 2
        double weight = 2.2
        String tempratureZone = "E"
        long minIdealDayValue = 3
        long maxIdealDayValue = 4
        long quanity = 2
        long nipPickQuanitity = 2
        String unitOfMeasurememnt = "E"

        Money unitPrice = new Money(BigDecimal.valueOf(2.5), Currency.GBP);

        when:
        FmsOrderItem actualOrderItem = fmsOrderFactory.createOrderedItem(expectedItemId,expectedCin, quantity, weight, unitPrice, mockUpcList(), mockItemCatalogInfo(), parentFmsOrder, SubstitutionOption.DO_NOT_SUBSTITUTE)

        then:
        assert actualOrderItem.getConsumerItemNumber() == expectedCin
        assert actualOrderItem.getItemId() == expectedItemId
        assert actualOrderItem.getPickedItemQuantity() == 0
        assert actualOrderItem.unitOfMeasurement == unitOfMeasurememnt
        assert actualOrderItem.getQuantity() == quanity
        assert actualOrderItem.getNilPickQuantity() == nipPickQuanitity
        assert actualOrderItem.getMaxIdealDayValue() == maxIdealDayValue
        assert actualOrderItem.getMinIdealDayValue() == minIdealDayValue
        assert actualOrderItem.getTemparatureZone() == tempratureZone


    }

    @Ignore
    def "Test CreateItem without order"() {

        given:

        String expectedCin = "46463636"
        String expectedItemId  = "22224333"
        long quantity = 2
        double weight = 2.2
        Money unitPrice = new Money(BigDecimal.valueOf(2.5), Currency.GBP);

        when:
        fmsOrderFactory.createOrderedItem(expectedItemId, expectedCin, quantity, weight, unitPrice, mockUpcList(), mockItemCatalogInfo(), null, SubstitutionOption.DO_NOT_SUBSTITUTE)

        then:
        thrown(NullPointerException)

    }

    @Ignore
    def "Test CreateItem without invalid unit Price"() {
        given:
        FmsOrder parentFmsOrder = FmsOrder.builder().storeId("4401")
                .sourceOrderId(UUID.randomUUID().toString())
                .deliveryDate(new Date()).build()
        String expectedCin = "46463636"
        String expectedItemId = "22224333"
        long quantity = 2
        double weight = 2.2
        Money unitPrice = new Money(BigDecimal.valueOf(0), Currency.GBP);

        when:
        FmsOrderItem actualOrderItem = fmsOrderFactory.createOrderedItem(expectedItemId, expectedCin, quantity, weight, unitPrice, mockUpcList(), mockItemCatalogInfo(), parentFmsOrder, SubstitutionOption.DO_NOT_SUBSTITUTE)

        then:
        thrown(IllegalArgumentException)

    }

    def "Test CreateItem without cin"() {
        given:
        FmsOrder parentFmsOrder = FmsOrder.builder().storeId("4401")
                .sourceOrderId(UUID.randomUUID().toString())
                .deliveryDate(new Date()).build()

        String expectedCin = null
        String expectedItemId = "2213333"
        long quantity = 2
        Money unitPrice = new Money(BigDecimal.valueOf(2.5), Currency.GBP);

        when:
        fmsOrderFactory.createOrderedItem(expectedItemId,expectedCin, quantity, 0, unitPrice,mockUpcList(), mockItemCatalogInfo(), parentFmsOrder, SubstitutionOption.DO_NOT_SUBSTITUTE)

        then:
        thrown(IllegalArgumentException)


    }

    def "Test CreateItem with zero quantity"() {

        given:
        FmsOrder parentFmsOrder = FmsOrder.builder().storeId("4401")
                .sourceOrderId(UUID.randomUUID().toString())
                .deliveryDate(new Date()).build()
        String expectedCin = "46463636"
        String expectedItemId = "222344"
        long quantity = 0
        double weight = 0
        Money unitPrice = new Money(BigDecimal.valueOf(2.5), Currency.GBP);

        when:
        fmsOrderFactory.createOrderedItem(expectedItemId, expectedCin, quantity,weight, unitPrice, mockUpcList(), mockItemCatalogInfo(), parentFmsOrder, SubstitutionOption.DO_NOT_SUBSTITUTE)

        then:
        thrown(IllegalArgumentException)

    }

    def " Test GetFmsOrderBySourceOrder with pre exisiting order"() {

        given:
        FmsOrder parentFmsOrder = FmsOrder.builder().storeId("4401")
                .sourceOrderId(UUID.randomUUID().toString())
                .deliveryDate(new Date())
                .orderState("READY_FOR_STORE").build()
        Tenant tenant = Tenant.ASDA
        Vertical vertical = Vertical.MARKETPLACE
        String sourceOrderId = UUID.randomUUID().toString()
        fmsOrderRepository.getOrderByMarketPlaceId(_ as String, _ as Tenant, _ as Vertical) >> parentFmsOrder

        when:
        FmsOrder actualFmsOrder = fmsOrderFactory.getFmsOrderBySourceOrder(sourceOrderId, tenant, vertical)

        then:
        assert actualFmsOrder.getOrderState() == "READY_FOR_STORE"
        assert actualFmsOrder == parentFmsOrder

    }

    def " Test GetFmsOrderBySourceOrder without pre existing order"() {

        given:
        String sourceOrderId = UUID.randomUUID().toString()
        Tenant tenant = Tenant.ASDA
        Vertical vertical = Vertical.MARKETPLACE
        fmsOrderRepository.getOrderByMarketPlaceId(_ as String, _ as Tenant, _ as Vertical) >> null

        when:
        FmsOrder actualFmsOrder = fmsOrderFactory.getFmsOrderBySourceOrder(sourceOrderId, tenant, vertical)

        then:
        assert actualFmsOrder.getOrderState() == "INITIAL"

    }

    def "Test createItemUpcInfo " () {
        when:
        ItemUpcInfo itemUpcInfo =  fmsOrderFactory.createItemUpcInfo(new ArrayList<String>())

        then:
        assert itemUpcInfo == null

    }

    def "Test createSchedulingInfo " () {
        given:
        FmsOrder fmsOrder = FmsOrder.builder().storeId("4401")
                .sourceOrderId(UUID.randomUUID().toString())
                .deliveryDate(new Date()).build()
        FmsSchedulingInfo fmsSchedulingInfo = FmsSchedulingInfo.builder().order(fmsOrder)
            .tripId("1234").doorStepTime(123456).vanId("abcde")
                .scheduleNumber("ab123").vanId("sampleVanId").build()

        when:
        FmsSchedulingInfo factoryBuiltObj =  fmsOrderFactory.createSchedulingInfo(fmsOrder, "1234", 123456, new Date(), new Date(),
            new Date(), "sampleVanId", "ab123", "qwerty")

        then:
        assert fmsSchedulingInfo.getTripId() == factoryBuiltObj.getTripId()
        assert fmsSchedulingInfo.getDoorStepTime() == factoryBuiltObj.getDoorStepTime()
        assert fmsSchedulingInfo.getVanId() == factoryBuiltObj.getVanId()

    }

    def "Test createOrderTimestamps " () {
        given:
        FmsOrder fmsOrder = FmsOrder.builder().storeId("4401")
                .sourceOrderId(UUID.randomUUID().toString())
                .deliveryDate(new Date()).build()
        Date date = new Date()

        when:
        FmsOrderTimestamps fmsOrderTimestamps = fmsOrderFactory.createOrderTimestamps(fmsOrder,
        date, date, date, date, date, date, date)

        then:
        assert fmsOrderTimestamps.getCancelledTime() != null
        assert fmsOrderTimestamps.getOrderDeliveredTime() != null
        assert  fmsOrderTimestamps.getCancelledTime() != null
        assert fmsOrderTimestamps.getPickCompleteTime() != null

    }

    def "Test createPickedItem" () {
        when:
        FmsPickedItem createPickedItem = fmsOrderFactory.createPickedItem("sampleDescription",
            "sampleDepartment", "sampleCin", "samplePicker", new ArrayList<FmsPickedItemUpc>())

        then:
        assert createPickedItem.getPickedItemDescription() == "sampleDescription"
        assert createPickedItem.getDepartmentID() == "sampleDepartment"
        assert createPickedItem.getCin() == "sampleCin"
        assert createPickedItem.getPicker().getPickerUserName() == "samplePicker"
    }

    def "Test createPickedItemUpc" () {
        when:
        FmsPickedItemUpc createPickedItemUpc = fmsOrderFactory.createPickedItemUpc(10,
                new BigDecimal(100), "E","111", "1212")

        then:
        assert createPickedItemUpc.getQuantity() == 10
        assert createPickedItemUpc.getUom() == "E"
        assert createPickedItemUpc.getWin() == "111"
        assert createPickedItemUpc.getUpc() == "1212"
    }

    def "Test createOrderPriceInfo" () {
        when:
        OrderPriceInfo orderPriceInfo = fmsOrderFactory.createOrderPriceInfo(10.00, 1.00)

        then:
        assert orderPriceInfo.getCarrierBagCharge() == 1.00
        assert orderPriceInfo.getWebOrderTotal() == 10.00
    }


    def "createSubstitutedItem"() {
        when:
        FmsSubstitutedItem substitutedItem = fmsOrderFactory.createSubstitutedItem("12",
        "13",
        "14",
        "WINGS",
        BigDecimal.TEN,
        2L,
        Arrays.asList(FmsSubstitutedItemUpc.builder().upc("1213").uom("KG").build()),
        4.0)

        then:
        assert substitutedItem.getDescription() == "WINGS"
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

    def "buildSubstitutedItemUpc"() {
        when:
        FmsSubstitutedItemUpc substitutedItemUpc = fmsOrderFactory.buildSubstitutedItemUpcs("1","E");
        then :
        assert substitutedItemUpc.uom == "E"
        assert substitutedItemUpc.upc == "1"
    }

    private static List<String> mockUpcList(){
        return  ["22222", "33333"]
    }

    private static ItemCatalogInfo mockItemCatalogInfo() {
        return new ItemCatalogInfo("EACH", "E", "Asda Fresh ",
                "https://i.groceries.asda.com/image.jpg", 3,
                4, "Ambient", true);


    }

}
