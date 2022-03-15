package com.walmart.fms

import com.walmart.common.domain.type.Currency
import com.walmart.common.domain.type.SubstitutionOption
import com.walmart.common.domain.type.Tenant
import com.walmart.common.domain.type.Vertical
import com.walmart.fms.commands.CreateFmsOrderCommand
import com.walmart.fms.commands.extensions.OrderInfo
import com.walmart.fms.domain.error.exception.FMSBadRequestException
import com.walmart.fms.order.aggregateroot.FmsOrder
import com.walmart.fms.order.domain.FmsOrderDomainService
import com.walmart.fms.order.domain.entity.FmsAddressInfo
import com.walmart.fms.order.domain.entity.FmsCustomerContactInfo
import com.walmart.fms.order.domain.entity.FmsOrderItem
import com.walmart.fms.order.domain.entity.FmsSchedulingInfo
import com.walmart.fms.order.factory.FmsOrderFactory
import com.walmart.fms.order.repository.IFmsOrderRepository
import com.walmart.fms.order.valueobject.ItemCatalogInfo
import com.walmart.fms.order.valueobject.TelePhone
import com.walmart.marketplace.order.domain.entity.type.Vendor
import com.walmart.fms.order.valueobject.FullName
import com.walmart.fms.order.valueobject.ItemPriceInfo
import com.walmart.fms.order.valueobject.MobilePhone
import com.walmart.fms.order.valueobject.Money
import spock.lang.Specification

class FmsOrderApplicationServiceTest extends Specification {

    String sourceOrderId = UUID.randomUUID().toString()
    String storeOrderId = UUID.randomUUID().toString()
    String vendorOrderId = UUID.randomUUID().toString()
    Tenant tenant = Tenant.ASDA
    Vertical vertical = Vertical.MARKETPLACE

    FmsOrderFactory fmsOrderFactory = Mock()
    FmsOrderDomainService fmsOrderDomainService = Mock()
    IFmsOrderRepository fmsOrderRepository = Mock()

    FmsOrderApplicationService fmsOrderApplicationService

    def setup() {
        fmsOrderApplicationService = new FmsOrderApplicationService(
                foFactory: fmsOrderFactory,
                fulfillmentRepository: fmsOrderRepository,
                fulfillmentDomainService: fmsOrderDomainService)
    }

    def "CreateAndProcessFulfillmentOrder"() {

        given:
        FmsOrder testFmsOrder = new FmsOrder("INITIAL")
        CreateFmsOrderCommand createFmsOrderCommand = getCreateFmsOrderCommand()
        fmsOrderFactory.getFmsOrder(_ as String, _ as Tenant, _ as Vertical) >> testFmsOrder
        fmsOrderFactory.createFmsOrder(_ as String, _ as String, _ as String, _ as Date, _ as Tenant, _ as Vertical, _ as String) >> testFmsOrder

        when:
        fmsOrderApplicationService.createAndProcessFulfillmentOrder(createFmsOrderCommand)

        then:
        thrown(FMSBadRequestException.class)
    }

    def "CreateAndProcessFulfillmentOrder Market Place"() {

        given:
        FmsOrder testFmsOrder = FmsOrder.builder()
                .sourceOrderId(sourceOrderId)
                .storeId("4401")
                .deliveryDate(new Date())
                .orderState("INITIAL")
                .tenant(Tenant.ASDA)
                .vertical(Vertical.MARKETPLACE).build()

        CreateFmsOrderCommand createFmsOrderCommand = CreateFmsOrderCommand.builder()
                .data(CreateFmsOrderCommand.FulfillmentOrderData.builder()
                        .orderInfo(getOrderInfo())
                        .marketPlaceInfo(getMarketPlaceInfo())
                        .build()).build()

        fmsOrderFactory.getFmsOrder(_ as String, _ as Tenant, _ as Vertical) >> testFmsOrder
        fmsOrderFactory.createFmsOrder(_ as String, _ as String, _ as String, _ as Date, _ as Tenant, _ as Vertical, _ as String) >> testFmsOrder

        when:
        fmsOrderApplicationService.createAndProcessFulfillmentOrder(createFmsOrderCommand)

        then:
        thrown(FMSBadRequestException.class)
    }

    def "CreateAndProcessFulfillmentOrder Market Place null"() {

        given:
        CreateFmsOrderCommand createFmsOrderCommand = CreateFmsOrderCommand.builder()
                .build()

        when:
        fmsOrderApplicationService.createAndProcessFulfillmentOrder(createFmsOrderCommand)

        then:
        thrown(FMSBadRequestException.class)
    }

    def "CreateAndProcessFulfillmentOrder with order already present and not open"() {

        given:
        FmsOrder testFmsOrder = getFmsOrder(null)
        CreateFmsOrderCommand createFmsOrderCommand = getCreateFmsOrderCommand()
        fmsOrderFactory.getFmsOrder(_ as String, _ as Tenant, _ as Vertical) >> testFmsOrder

        when:
        fmsOrderApplicationService.createAndProcessFulfillmentOrder(createFmsOrderCommand)

        then:
        thrown(FMSBadRequestException.class)
    }

    def "CreateAndProcessFulfillmentOrder with right info"() {

        given:
        FmsOrder testFmsOrder = FmsOrder.builder()
                .sourceOrderId(sourceOrderId)
                .storeId("4401")
                .deliveryDate(new Date())
                .orderState("INITIAL").build()

        CreateFmsOrderCommand createFmsOrderCommand = CreateFmsOrderCommand.builder()
                .data(CreateFmsOrderCommand.FulfillmentOrderData.builder()
                        .orderInfo(getOrderInfo())
                        .marketPlaceInfo(getMarketPlaceInfo())
                        .build()).build()

        fmsOrderFactory.getFmsOrder(_ as String, _ as Tenant, _ as Vertical) >> testFmsOrder
        fmsOrderFactory.createFmsOrder(_ as String, _ as String, _ as String, _ as Date, _ as Tenant, _ as Vertical, _ as String) >> testFmsOrder

        when:
        fmsOrderApplicationService.createAndProcessFulfillmentOrder(createFmsOrderCommand)

        then:
        1 * fmsOrderDomainService.processFmsOrder(_ as FmsOrder) >> { FmsOrder _fmsOrder ->
            assert _fmsOrder.sourceOrderId == sourceOrderId
        }
    }

    def "CreateAndProcessFulfillmentOrder with pre existing order"() {

        given:
        FmsOrder testFmsOrder = FmsOrder.builder()
                .sourceOrderId(sourceOrderId)
                .storeOrderId(storeOrderId)
                .storeId("4401")
                .deliveryDate(new Date())
                .orderState(FmsOrder.OrderStatus.RECEIVED_AT_STORE.getName()).build()

        CreateFmsOrderCommand createFmsOrderCommand = CreateFmsOrderCommand.builder()
                .data(CreateFmsOrderCommand.FulfillmentOrderData.builder()
                        .orderInfo(getOrderInfo())
                        .marketPlaceInfo(getMarketPlaceInfo())
                        .build()).build()

        fmsOrderFactory.getFmsOrder(_ as String, _ as Tenant, _ as Vertical) >> testFmsOrder
        fmsOrderFactory.createFmsOrder(_ as String, _ as String, _ as String, _ as Date, _ as Tenant, _ as Vertical, _ as String) >> testFmsOrder

        when:
        FmsOrder actualOrder = fmsOrderApplicationService.createAndProcessFulfillmentOrder(createFmsOrderCommand)

        then:
        assert actualOrder == null
    }

    def "CreateAndProcessFulfillmentOrder with Complete CreateOrder command"() {

        given:
        FmsOrder testFmsOrder = getFmsOrder(storeOrderId)
        CreateFmsOrderCommand createFmsOrderCommand = getCreateFmsOrderCommandComplete()

        fmsOrderFactory.getFmsOrder(_ as String, _ as Tenant, _ as Vertical) >> testFmsOrder

        fmsOrderFactory.createFmsOrder(_ as String, _ as String, _ as String, _ as Date, _ as Tenant, _ as Vertical, _ as String) >> testFmsOrder

        fmsOrderFactory.createSchedulingInfo(_ as FmsOrder, _ as String, _ as Integer, _ as Date, _ as Date, _ as Date,
                _ as String, _ as String, _ as String) >> mockSchedulingInfo(createFmsOrderCommand, testFmsOrder)

        fmsOrderFactory.createAddressInfo(_ as FmsOrder, _ as String, _ as String, _ as String, _ as String, _ as String,
                _ as String, _ as String, _ as String, _ as String, _ as String, _ as String) >> mockAddressInfo(createFmsOrderCommand, testFmsOrder)

        fmsOrderFactory.createContactInfo(*_) >> mockContactInfo(createFmsOrderCommand, testFmsOrder)

        fmsOrderFactory.createOrderedItem(*_) >> mockOrderedItem(createFmsOrderCommand, testFmsOrder)

        when:
        fmsOrderApplicationService.createAndProcessFulfillmentOrder(createFmsOrderCommand)

        then:

        1 * fmsOrderDomainService.processFmsOrder(_ as FmsOrder) >> { FmsOrder _fmsOrder ->
            assert _fmsOrder.sourceOrderId == sourceOrderId
            assert _fmsOrder.storeOrderId == storeOrderId
            assert _fmsOrder.storeId == createFmsOrderCommand.data.orderInfo.storeId

            assert _fmsOrder.schedulingInfo.tripId == createFmsOrderCommand.data.schedulingInfo.tripId
            assert _fmsOrder.schedulingInfo.doorStepTime == createFmsOrderCommand.data.schedulingInfo.doorStepTime
            assert _fmsOrder.schedulingInfo.loadNumber == createFmsOrderCommand.data.schedulingInfo.loadNumber
            assert _fmsOrder.schedulingInfo.slotEndTime == createFmsOrderCommand.data.schedulingInfo.slotEndTime
            assert _fmsOrder.schedulingInfo.slotStartTime == createFmsOrderCommand.data.schedulingInfo.slotStartTime
            assert _fmsOrder.schedulingInfo.scheduleNumber == createFmsOrderCommand.data.schedulingInfo.scheduleNumber
            assert _fmsOrder.schedulingInfo.vanId == createFmsOrderCommand.data.schedulingInfo.vanId

            assert _fmsOrder.addressInfo.addressOne == createFmsOrderCommand.data.addressInfo.addressOne
            assert _fmsOrder.addressInfo.addressTwo == createFmsOrderCommand.data.addressInfo.addressTwo
            assert _fmsOrder.addressInfo.addressThree == createFmsOrderCommand.data.addressInfo.addressThree
            assert _fmsOrder.addressInfo.county == createFmsOrderCommand.data.addressInfo.county
            assert _fmsOrder.addressInfo.city == createFmsOrderCommand.data.addressInfo.city
            assert _fmsOrder.addressInfo.state == createFmsOrderCommand.data.addressInfo.state
            assert _fmsOrder.addressInfo.postalCode == createFmsOrderCommand.data.addressInfo.postalCode
            assert _fmsOrder.addressInfo.longitude == createFmsOrderCommand.data.addressInfo.longitude
            assert _fmsOrder.addressInfo.longitude == createFmsOrderCommand.data.addressInfo.longitude

            assert _fmsOrder.contactInfo.email == createFmsOrderCommand.data.contactinfo.email
            assert _fmsOrder.contactInfo.fullName.firstName == createFmsOrderCommand.data.contactinfo.firstName
            assert _fmsOrder.contactInfo.fullName.lastName == createFmsOrderCommand.data.contactinfo.lastName
            assert _fmsOrder.contactInfo.phoneNumberOne.number == createFmsOrderCommand.data.contactinfo.phoneNumberOne

            assert _fmsOrder.fmsOrderItems[0].itemId == createFmsOrderCommand.data.items[0].itemId
            assert _fmsOrder.fmsOrderItems[0].consumerItemNumber == createFmsOrderCommand.data.items[0].consumerItemNumber
            assert _fmsOrder.fmsOrderItems[0].itemPriceInfo.unitPrice.amount == createFmsOrderCommand.data.items[0].unitPrice
            assert _fmsOrder.fmsOrderItems[0].quantity == createFmsOrderCommand.data.items[0].quantity
            assert _fmsOrder.fmsOrderItems[0].weight == createFmsOrderCommand.data.items[0].weight
            assert _fmsOrder.fmsOrderItems[0].catalogInfo.unitOfMeasurement == createFmsOrderCommand.data.items[0].unitOfMeasurement
            assert _fmsOrder.fmsOrderItems[0].catalogInfo.pickerItemDescription == createFmsOrderCommand.data.items[0].pickerItemDescription
            assert _fmsOrder.fmsOrderItems[0].catalogInfo.isSellbyDateRequired == createFmsOrderCommand.data.items[0].isSellbyDateRequired
        }
    }

    def " Test GetOrder"() {
        given:
        FmsOrder testFmsOrder = getFmsOrder(storeOrderId)
        fmsOrderRepository.getOrder(_ as String, _ as Tenant, _ as Vertical) >> testFmsOrder

        when:
        fmsOrderApplicationService.getOrder(sourceOrderId, tenant, vertical)

        then:
        1 * fmsOrderRepository.getOrder(_ as String, _ as Tenant, _ as Vertical) >> { String _sourceOrderId, Tenant _tenant, Vertical _vertical ->
            assert _sourceOrderId == sourceOrderId
            assert _tenant == tenant
            assert _vertical == vertical
        }
    }

    def " Test GetOrder without order being present"() {

        given:
        fmsOrderRepository.getOrder(_ as String, _ as Tenant, _ as Vertical) >> {
            throw new FMSBadRequestException("Unable to find order for id:" + sourceOrderId)
        }

        when:
        fmsOrderApplicationService.getOrder(sourceOrderId, tenant, vertical)

        then:
        thrown(FMSBadRequestException.class)
    }

    private FmsSchedulingInfo mockSchedulingInfo(CreateFmsOrderCommand command, FmsOrder order) {
        return new FmsSchedulingInfo(
                order: order,
                tripId: command.data.schedulingInfo.tripId,
                doorStepTime: command.data.schedulingInfo.doorStepTime,
                slotStartTime: command.data.schedulingInfo.slotStartTime,
                slotEndTime: command.data.schedulingInfo.slotEndTime,
                orderDueTime: command.data.schedulingInfo.orderDueTime,
                vanId: command.data.schedulingInfo.vanId,
                scheduleNumber: command.data.schedulingInfo.scheduleNumber,
                loadNumber: command.data.schedulingInfo.loadNumber
        )
    }

    private FmsAddressInfo mockAddressInfo(CreateFmsOrderCommand command, FmsOrder order) {
        return new FmsAddressInfo(
                order: order,
                addressOne: command.data.addressInfo.addressOne,
                addressTwo: command.data.addressInfo.addressTwo,
                addressThree: command.data.addressInfo.addressThree,
                addressType: command.data.addressInfo.addressType,
                city: command.data.addressInfo.city,
                county: command.data.addressInfo.county,
                state: command.data.addressInfo.state,
                postalCode: command.data.addressInfo.postalCode,
                longitude: command.data.addressInfo.longitude,
                latitude: command.data.addressInfo.latitude,
                country: command.data.addressInfo.country
        )

    }

    private FmsCustomerContactInfo mockContactInfo(CreateFmsOrderCommand command, FmsOrder order) {
        return new FmsCustomerContactInfo(
                order: order,
                customerId: command.data.contactinfo.customerId,
                phoneNumberOne: new TelePhone(command.data.contactinfo.phoneNumberOne),
                mobileNumber: new MobilePhone(command.data.contactinfo.mobileNumber),
                fullName: new FullName(command.data.contactinfo.title,
                        command.data.contactinfo.firstName,
                        command.data.contactinfo.middleName,
                        command.data.contactinfo.lastName)
        )

    }

    private FmsOrderItem mockOrderedItem(CreateFmsOrderCommand command, FmsOrder order) {
        return new FmsOrderItem(
                fmsOrder: order,
                itemId: command.data.items[0].itemId,
                consumerItemNumber: command.data.items[0].consumerItemNumber,
                quantity: command.data.items[0].quantity,
                weight: command.data.items[0].weight,
                itemPriceInfo: new ItemPriceInfo(new Money(command.data.items[0].unitPrice, Currency.GBP)),
                catalogInfo: new ItemCatalogInfo(
                        unitOfMeasurement: command.data.items[0].unitOfMeasurement,
                        pickerItemDescription: command.data.items[0].pickerItemDescription,
                        isSellbyDateRequired: command.data.items[0].isSellbyDateRequired

                )
        )

    }

    private CreateFmsOrderCommand getCreateFmsOrderCommand() {
        return CreateFmsOrderCommand.builder()
                .data(CreateFmsOrderCommand.FulfillmentOrderData.builder()
                        .orderInfo(getOrderInfo())
                        .build())
                .build()
    }

    private CreateFmsOrderCommand getCreateFmsOrderCommandComplete() {
        return CreateFmsOrderCommand.builder()
                .data(CreateFmsOrderCommand.FulfillmentOrderData.builder()
                        .orderInfo(getOrderInfo())
                        .marketPlaceInfo(getMarketPlaceInfo())
                        .addressInfo(getAddressInfo())
                        .schedulingInfo(getSchedulingInfo())
                        .orderTimestamps(getOrderTimestamps())
                        .items(getFmsItemInfoList())
                        .contactinfo(getContactInfo())
                        .priceInfo(getPriceInfo())
                        .build()).build()
    }

    private CreateFmsOrderCommand.PriceInfo getPriceInfo() {
        return CreateFmsOrderCommand.PriceInfo.builder().webOrderTotal(10.0).orderVATAmount(2.0).build()
    }

    private CreateFmsOrderCommand.ContactInfo getContactInfo() {
        return CreateFmsOrderCommand.ContactInfo.builder().firstName("John").lastName("Doe").phoneNumberOne("0123456789").build()
    }

    private ArrayList<CreateFmsOrderCommand.FmsItemInfo> getFmsItemInfoList() {
        return Arrays.asList(CreateFmsOrderCommand.FmsItemInfo.builder()
                .itemId("33333")
                .consumerItemNumber("22222")
                .unitPrice(new BigDecimal(12.22))
                .unitOfMeasurement("EACH")
                .pickerItemDescription("Asda Biscuits")
                .quantity(3)
                .isSellbyDateRequired(true)
                .substitutionOption(SubstitutionOption.DO_NOT_SUBSTITUTE)
                .build())
    }

    private CreateFmsOrderCommand.OrderTimestamps getOrderTimestamps() {
        return CreateFmsOrderCommand.OrderTimestamps.builder()
                .pickCompleteTime(new Date())
                .build()
    }

    private CreateFmsOrderCommand.SchedulingInfo getSchedulingInfo() {
        return CreateFmsOrderCommand.SchedulingInfo.builder()
                .tripId("TripId-2342")
                .doorStepTime(23)
                .loadNumber("2222222")
                .slotEndTime(new Date())
                .slotStartTime(new Date())
                .orderDueTime(new Date())
                .vanId("33333")
                .scheduleNumber("233333")
                .build()
    }

    private CreateFmsOrderCommand.AddressInfo getAddressInfo() {
        return CreateFmsOrderCommand.AddressInfo.builder().addressOne("test addressOne")
                .addressTwo("test addressTwo")
                .addressThree("Test adddressThree")
                .addressType("Home")
                .county("London")
                .country("UK")
                .city("test city")
                .state("State")
                .postalCode("DN211ZA")
                .latitude("57.2252552")
                .longitude("-6.24343").build()
    }

    private FmsOrder getFmsOrder(String storeOrderId) {
        return FmsOrder.builder()
                .sourceOrderId(sourceOrderId)
                .storeOrderId(storeOrderId)
                .storeId("4401")
                .deliveryDate(new Date())
                .orderState("INITIAL").build()
    }

    private OrderInfo getOrderInfo() {
        return OrderInfo.builder()
                .sourceOrderId(sourceOrderId)
                .storeOrderId(storeOrderId)
                .storeId("4401")
                .pickupLocationId("4401")
                .deliveryDate(new Date())
                .tenant(Tenant.ASDA)
                .vertical(Vertical.MARKETPLACE).build()
    }

    private CreateFmsOrderCommand.MarketPlaceInfo getMarketPlaceInfo() {
        return CreateFmsOrderCommand.MarketPlaceInfo.builder()
                .vendor(Vendor.UBEREATS)
                .vendorOrderId(vendorOrderId)
                .build()
    }
}
