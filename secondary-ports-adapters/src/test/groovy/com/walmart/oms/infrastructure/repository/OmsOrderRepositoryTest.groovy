package com.walmart.oms.infrastructure.repository

import com.walmart.common.domain.type.Currency
import com.walmart.common.domain.type.FulfillmentType
import com.walmart.common.domain.type.Tenant
import com.walmart.common.domain.type.Vertical
import com.walmart.marketplace.order.domain.entity.type.Vendor
import com.walmart.oms.domain.error.exception.OMSBadRequestException
import com.walmart.oms.infrastructure.repository.infrastructure.mssql.IOmsOrderSqlServerRepository
import com.walmart.oms.infrastructure.repository.infrastructure.mssql.OmsSqlServerBaseRepository
import com.walmart.oms.order.aggregateroot.OmsOrder
import com.walmart.oms.order.domain.entity.*
import com.walmart.oms.order.domain.model.CreateDateSearchQuery
import com.walmart.oms.order.valueobject.*
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import spock.lang.Specification

import java.time.LocalDateTime

class OmsOrderRepositoryTest extends Specification {

    IOmsOrderSqlServerRepository omsOrderSqlServerRepository = Mock()
    OmsOrderRepository omsOrderRepository
    OrderUpdateEventPublisher orderUpdateEventPublisher = Mock()
    OmsSqlServerBaseRepository omsOrderIOmsSqlServerBaseRepository = Mock()
    String sourceOrderId = UUID.randomUUID().toString()
    String vendorOrderId = UUID.randomUUID().toString()

    def setup() {
        omsOrderRepository = new OmsOrderRepository(
                omsOrderSqlServerRepository: omsOrderSqlServerRepository,
                omsOrderIOmsSqlServerBaseRepository: omsOrderIOmsSqlServerBaseRepository,
                orderUpdateEventPublisher: orderUpdateEventPublisher)
    }

    def " Test GetOrderByMarketPlaceId"() {
        given:
        OmsOrder omsOrder = mockOmsOrder()

        when:
        omsOrderRepository.getOrderByMarketPlaceId(vendorOrderId, Tenant.ASDA, Vertical.MARKETPLACE)

        then:
        1 * omsOrderSqlServerRepository.findByMarketPlaceInfo_VendorOrderIdAndTenantAndVertical(
                _ as String, _ as Tenant, _ as Vertical) >> { String _vendorOrderId, Tenant _tenant, Vertical _vertical ->
            assert _vendorOrderId == vendorOrderId
            return omsOrder
        }

    }

    OmsOrder mockOmsOrder() {
        OmsOrder testOmsOrder = OmsOrder.builder()
                .vertical(Vertical.ASDAGR)
                .tenant(Tenant.ASDA)
                .storeId("4401")
                .sourceOrderId(sourceOrderId)
                .storeOrderId("123456789")
                .pickupLocationId("4401")
                .spokeStoreId("4401")
                .deliveryDate(new Date())
                .fulfillmentType(FulfillmentType.INSTORE_PICKUP)
                .marketPlaceInfo(MarketPlaceInfo.builder().vendor(Vendor.UBEREATS).vendorOrderId(vendorOrderId).build())
                .orderState("RECD_AT_STORE")
                .priceInfo(OrderPriceInfo.builder()
                        .orderSubTotal(40.0).build()).build()


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
                .plannedDueTime(new Date()).build())

        testOmsOrder.addContactInfo(CustomerContactInfo.builder()
                .order(testOmsOrder)
                .fullName(new FullName(null, "John", null, "Doe"))
                .phoneNumberOne(new TelePhone("0123456789")).build())

        OmsOrderBundledItem omsOrderBundledItem = new OmsOrderBundledItem(new OmsOrderItem(), "iafd", 2, 1, "agdd", "dsads", "c1")

        testOmsOrder.addItem(OmsOrderItem.builder()
                .omsOrder(testOmsOrder)
                .cin("464646")
                .itemPriceInfo(new ItemPriceInfo(new Money(BigDecimal.valueOf(2.5), Currency.GBP), new Money(BigDecimal.valueOf(5.0), Currency.GBP)))
                .quantity(2)
                .salesUnit("EACH")
                .uom("E")
                .bundledItemList(Arrays.asList(omsOrderBundledItem))
                .build())

        return testOmsOrder
    }

    def " Test GetOrder"() {
        given:
        OmsOrder omsOrder = mockOmsOrder()

        when:
        omsOrderRepository.getOrder(sourceOrderId, Tenant.ASDA, Vertical.MARKETPLACE)

        then:
        1 * omsOrderSqlServerRepository.findBySourceOrderIdAndTenantAndVertical(
                _ as String, _ as Tenant, _ as Vertical) >> { String _soureOrderId, Tenant _tenant, Vertical _vertical ->
            assert _soureOrderId == sourceOrderId
            return omsOrder
        }
    }

    def " Test GetOrder with no pre-existing order"() {

        when:
        omsOrderRepository.getOrder(sourceOrderId, Tenant.ASDA, Vertical.MARKETPLACE)
        omsOrderSqlServerRepository.findBySourceOrderIdAndTenantAndVertical(_ as String, _ as Tenant, _ as Vertical) >> null

        then:
        thrown(OMSBadRequestException.class)

    }

    def " Test Save"() {
        given:
        OmsOrder omsOrder = mockOmsOrder()

        when:
        omsOrderRepository.save(omsOrder)

        then:
        1 * omsOrderSqlServerRepository.save(_ as OmsOrder) >> { OmsOrder _omsOrder ->
            assert omsOrder == _omsOrder
            return omsOrder
        }
        1 * orderUpdateEventPublisher.emitOrderUpdateEvent(_ as OmsOrder) >> {
            OmsOrder _omsOrder ->
                assert omsOrder == _omsOrder
                return omsOrder
        }

    }

    def " Test GetNextIdentify"() {

        when:
        String id = omsOrderRepository.getNextIdentity()

        then:
        UUID uuid = UUID.fromString(id)
        assert uuid != null
    }

    def "test findAllOrderByCreatedDateRange"() {
        given:
        CreateDateSearchQuery searchQuery = mockSearchQuery()
        OmsOrder omsOrder = mockOmsOrder()
        Page<OmsOrder> omsOrderPage = new PageImpl<>(Arrays.asList(omsOrder), PageRequest.of(0, 1), 1)

        when:
        List<OmsOrder> omsOrders = omsOrderRepository.findAllOrderByCreatedDateRange(searchQuery)

        then:
        1 * omsOrderIOmsSqlServerBaseRepository.findAllByCreatedDateRange(_ as Date, _ as Date, _ as Pageable) >> {
            return omsOrderPage
        }
        omsOrders.size() == 1
        omsOrders.get(0).getStoreId() == "4401"
    }

    private static CreateDateSearchQuery mockSearchQuery() {
        return CreateDateSearchQuery.builder()
                .pageNumber(0)
                .maxFetchLimit(10)
                .createStartDateTime(LocalDateTime.now())
                .createEndDateTime(LocalDateTime.now())
                .build()
    }
}
