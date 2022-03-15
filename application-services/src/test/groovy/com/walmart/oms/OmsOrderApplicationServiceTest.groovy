package com.walmart.oms

import com.walmart.common.domain.type.Tenant
import com.walmart.common.domain.type.Vertical
import com.walmart.marketplace.order.domain.entity.type.Vendor
import com.walmart.oms.commands.CreateOmsOrderCommand
import com.walmart.oms.commands.extensions.OrderInfo
import com.walmart.oms.domain.error.exception.OMSBadRequestException
import com.walmart.oms.order.aggregateroot.OmsOrder
import com.walmart.oms.order.domain.OmsOrderDomainService
import com.walmart.oms.order.domain.entity.OmsOrderBundledItem
import com.walmart.oms.order.domain.entity.OmsOrderItem
import com.walmart.oms.order.factory.OmsOrderFactory
import com.walmart.oms.order.repository.IOmsOrderRepository
import spock.lang.Specification

class OmsOrderApplicationServiceTest extends Specification {

    String sourceOrderId = UUID.randomUUID().toString()
    String vendorOrderId = UUID.randomUUID().toString()
    Tenant tenant = Tenant.ASDA
    Vertical vertical = Vertical.MARKETPLACE

    OmsOrderFactory omsOrderFactory = Mock()

    OmsOrderDomainService omsOrderDomainService = Mock()

    IOmsOrderRepository omsOrderRepository = Mock()

    OmsOrderApplicationService omsOrderApplicationService

    def setup() {
        omsOrderApplicationService = new OmsOrderApplicationService(
                omsOrderFactory: omsOrderFactory,
                omsOrderRepository: omsOrderRepository,
                omsOrderDomainService: omsOrderDomainService
        )
    }

    def "CreateAndProcessMarketPlaceOrder"() {

        given:
        OmsOrder testOmsOrder = new OmsOrder("INITIAL")
        CreateOmsOrderCommand createOmsOrderCommand = CreateOmsOrderCommand.builder()
                .data(CreateOmsOrderCommand.OmsOrderData.builder()
                        .orderInfo(getOrderInfo()).build()).build()

        omsOrderFactory.getOmsOrder(_ as String, _ as Tenant, _ as Vertical) >> testOmsOrder
        omsOrderFactory.createOmsOrder(_ as String, _ as String, _ as String, _ as String, _ as String, _ as Date, _ as Tenant, _ as Vertical) >> testOmsOrder

        when:
        omsOrderApplicationService.createOmsOrderFromCommand(createOmsOrderCommand)

        then:
        thrown(OMSBadRequestException.class)
    }

    def "CreateAndProcessMarketPlaceOrder with right info"() {

        given:
        OmsOrder testOmsOrder = getOmsOrder("INITIAL")
        CreateOmsOrderCommand createOmsOrderCommand = getCreateOmsOrderCommand()
        omsOrderFactory.getOmsOrder(_ as String, _ as Tenant, _ as Vertical) >> testOmsOrder
        1 * omsOrderFactory.createOmsOrder(_ as String, _ as String, _ as String, _ as String, _ as String, _ as Date, _ as Tenant, _ as Vertical) >> testOmsOrder

        when:
        omsOrderApplicationService.createOmsOrderFromCommand(createOmsOrderCommand)

        then:
        1 * omsOrderDomainService.processOmsOrder(_ as OmsOrder) >> { OmsOrder _omsOrder ->
            assert _omsOrder.sourceOrderId == sourceOrderId
        }
        1 * omsOrderDomainService.publishOrderCreatedDomainEvent(_ as OmsOrder)
    }

    def "CreateAndProcessMarketPlaceOrder with pre existing order"() {

        given:
        OmsOrder testOmsOrder = getOmsOrder("RECD_AT_STORE")
        CreateOmsOrderCommand createOmsOrderCommand = getCreateOmsOrderCommand()
        omsOrderFactory.getOmsOrder(_ as String, _ as Tenant, _ as Vertical) >> testOmsOrder
        1 * omsOrderFactory.createOmsOrder(_ as String, _ as String, _ as String, _ as String, _ as String, _ as Date, _ as Tenant, _ as Vertical) >> testOmsOrder
        omsOrderDomainService.processOmsOrder(_ as OmsOrder) >> testOmsOrder

        when:
        OmsOrder actualOrder = omsOrderApplicationService.createOmsOrderFromCommand(createOmsOrderCommand)

        then:
        assert actualOrder != null
    }

    def "CreateAndProcessMarketPlaceOrder with Complete CreateOrder command"() {

        given:
        OmsOrder testOmsOrder = getOmsOrder("INITIAL")
        CreateOmsOrderCommand createOmsOrderCommand = getCreateOmsOrderCommand()
        omsOrderFactory.getOmsOrder(_ as String, _ as Tenant, _ as Vertical) >> testOmsOrder
        1 * omsOrderFactory.createOmsOrder(_ as String, _ as String, _ as String, _ as String, _ as String, _ as Date, _ as Tenant, _ as Vertical) >> testOmsOrder

        when:
        omsOrderApplicationService.createOmsOrderFromCommand(createOmsOrderCommand)

        then:
        1 * omsOrderDomainService.processOmsOrder(_ as OmsOrder) >> { OmsOrder _omsOrder ->
            assert _omsOrder.sourceOrderId == sourceOrderId
        }
        1 * omsOrderDomainService.publishOrderCreatedDomainEvent(_ as OmsOrder)
    }

    def " Test GetOrder"() {

        given:
        OmsOrder testOmsOrder = getOmsOrder("INITIAL")
        omsOrderRepository.getOrder(_ as String, _ as Tenant, _ as Vertical) >> testOmsOrder

        when:
        omsOrderApplicationService.getOrder(sourceOrderId, tenant, vertical)

        then:
        1 * omsOrderRepository.getOrder(_ as String, _ as Tenant, _ as Vertical) >> { String _sourceOrderId, Tenant _tenant, Vertical _vertical ->
            assert _sourceOrderId == sourceOrderId
            assert _tenant == tenant
            assert _vertical == vertical
        }
    }

    def " Test GetOrder without order being present"() {

        given:
        omsOrderRepository.getOrder(_ as String, _ as Tenant, _ as Vertical) >> {
            throw new OMSBadRequestException("Unable to find order for id:" + sourceOrderId)
        }

        when:
        omsOrderApplicationService.getOrder(sourceOrderId, tenant, vertical)

        then:
        thrown(OMSBadRequestException.class)
    }

    private OmsOrder getOmsOrder(String orderState) {
        return OmsOrder.builder()
                .sourceOrderId(sourceOrderId)
                .storeOrderId("123456789")
                .storeId("4401")
                .deliveryDate(new Date())
                .storeId("1234")
                .orderState(orderState).build()
    }

    private OrderInfo getOrderInfo() {
        return OrderInfo.builder()
                .sourceOrderId(sourceOrderId)
                .storeOrderId("123456789")
                .storeId("4401")
                .spokeStoreId("4401")
                .pickupLocationId("4401")
                .deliveryDate(new Date())
                .tenant(Tenant.ASDA)
                .vertical(Vertical.MARKETPLACE).build()
    }

    private static CreateOmsOrderCommand.MarketPlaceInfo getMarketPlaceInfo(String vendorOrderId) {
        return CreateOmsOrderCommand.MarketPlaceInfo.builder()
                .vendor(Vendor.UBEREATS)
                .vendorOrderId(vendorOrderId)
                .build()
    }

    private static CreateOmsOrderCommand.ContactInfo getContactInfo() {
        return CreateOmsOrderCommand.ContactInfo.builder()
                .firstName("John")
                .lastName("Doe")
                .phoneNumberOne("0123456789")
                .build()
    }

    private List<CreateOmsOrderCommand.AddressInfo> getAddressInfoList() {
        return Arrays.asList(CreateOmsOrderCommand.AddressInfo.builder()
                .addressOne("test addressOne")
                .addressTwo("test addressTwo")
                .addressThree("Test address Three")
                .city("test city")
                .latitude("57.2252552")
                .longitude("-6.24343").build())
    }

    private CreateOmsOrderCommand getCreateOmsOrderCommand() {
        return CreateOmsOrderCommand.builder()
                .data(CreateOmsOrderCommand.OmsOrderData.builder()
                        .orderInfo(getOrderInfo())
                        .marketPlaceInfo(getMarketPlaceInfo(vendorOrderId))
                        .addressInfos(getAddressInfoList())
                        .contactinfo(getContactInfo())
                        .schedulingInfo(getSchedulingInfo())
                        .items(getOrderItemInfo())
                        .priceInfo(getPriceInfo())
                        .build()).build()
    }

    private CreateOmsOrderCommand getCreateOmsOrderCommandForBundles() {
        return CreateOmsOrderCommand.builder()
                .data(CreateOmsOrderCommand.OmsOrderData.builder()
                        .orderInfo(getOrderInfo())
                        .marketPlaceInfo(getMarketPlaceInfo(vendorOrderId))
                        .addressInfos(getAddressInfoList())
                        .contactinfo(getContactInfo())
                        .schedulingInfo(getSchedulingInfo())
                        .items(getOrderItemInfoForBundles())
                        .priceInfo(getPriceInfo())
                        .build()).build()
    }

    private static CreateOmsOrderCommand.PriceInfo getPriceInfo() {
        return CreateOmsOrderCommand.PriceInfo.builder()
                .carrierBagCharge(0.04)
                .orderTotal(10.0)
                .build()
    }

    private static List<CreateOmsOrderCommand.OrderItemInfo> getOrderItemInfo() {
        return Arrays.asList(CreateOmsOrderCommand.OrderItemInfo.builder()
                .weight(0.0)
                .quantity(2)
                .unitPrice(5.0)
                .uom("K")
                .cin("12356")
                .vendorTotalPrice(12.0)
                .vendorUnitPrice(6.0)
                .build())
    }

    private List<CreateOmsOrderCommand.OrderItemInfo> getOrderItemInfoForBundles() {
        CreateOmsOrderCommand.BundleItem bundleItem = new CreateOmsOrderCommand.BundleItem();
        bundleItem.setBundleSkuId("12356")
        bundleItem.setBundleQuantity(2)
        bundleItem.setBundleInstanceId("bundle1")
        bundleItem.setBundleDescription("burger combo")

        return Arrays.asList(CreateOmsOrderCommand.OrderItemInfo.builder()
                .weight(2.0)
                .quantity(2)
                .unitPrice(5.0)
                .uom("K")
                .cin("12355")
                .itemDescription("burger")
                .vendorTotalPrice(10.0)
                .vendorUnitPrice(10.0)
                .bundledItemList(Arrays.asList(bundleItem))
                .build(),
                CreateOmsOrderCommand.OrderItemInfo.builder()
                        .weight(1.0)
                        .quantity(2)
                        .unitPrice(5.0)
                        .uom("K")
                        .itemDescription("pepsi")
                        .cin("1235")
                        .vendorTotalPrice(20.0)
                        .vendorUnitPrice(20.0)
                        .bundledItemList(Arrays.asList(bundleItem))
                        .build()
        )
    }

    private static CreateOmsOrderCommand.SchedulingInfo getSchedulingInfo() {
        return CreateOmsOrderCommand.SchedulingInfo.builder()
                .tripId("123")
                .loadNumber("123")
                .vanId("1212")
                .scheduleNumber("89")
                .build()
    }

    def "CreateAndProcessMarketPlaceOrder with Complete CreateOrder command for bundles"() {
        given:
        OmsOrder testOmsOrder = getOmsOrder("INITIAL")
        CreateOmsOrderCommand createOmsOrderCommand = getCreateOmsOrderCommandForBundles()
        omsOrderFactory.getOmsOrder(_ as String, _ as Tenant, _ as Vertical) >> testOmsOrder
        omsOrderFactory.createOmsOrder(_ as String, _ as String, _ as String, _ as String, _ as String, _ as Date, _ as Tenant, _ as Vertical) >> testOmsOrder
        omsOrderFactory.createOrderedItem(_, _, _, _, _, _, _) >> new OmsOrderItem()
        omsOrderFactory.createOmsOrderBundleItem(_, _, _, _, _, _) >> new OmsOrderBundledItem()

        when:
        OmsOrder omsOrder = omsOrderApplicationService.createOmsOrderFromCommand(createOmsOrderCommand)

        then:
        omsOrder.getOrderItemList().size() == 2
        omsOrder.getOrderItemsContainingBundles().size() == 2
        1 * omsOrderDomainService.processOmsOrder(_ as OmsOrder) >> { OmsOrder _omsOrder ->
            assert _omsOrder.sourceOrderId == sourceOrderId
        }
        1 * omsOrderDomainService.publishOrderCreatedDomainEvent(_ as OmsOrder)
    }

}
