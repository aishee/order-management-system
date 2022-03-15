package com.walmart.fms.repository

import com.walmart.common.domain.type.Currency
import com.walmart.common.domain.type.SubstitutionOption
import com.walmart.common.domain.type.Tenant
import com.walmart.common.domain.type.Vertical
import com.walmart.fms.domain.error.exception.FMSBadRequestException
import com.walmart.fms.order.aggregateroot.FmsOrder
import com.walmart.fms.order.domain.entity.FmsAddressInfo
import com.walmart.fms.order.domain.entity.FmsCustomerContactInfo
import com.walmart.fms.order.domain.entity.FmsOrderItem
import com.walmart.fms.order.domain.entity.FmsSchedulingInfo
import com.walmart.fms.order.valueobject.*
import com.walmart.fms.repository.mssql.IFmsOrderSqlServerRepository
import com.walmart.marketplace.order.domain.entity.type.Vendor
import spock.lang.Specification

class FmsOrderRepositoryTest extends Specification {

    IFmsOrderSqlServerRepository fmsOrderSqlServerRepository = Mock()
    FmsOrderRepository fmsOrderRepository

    String sourceOrderId = UUID.randomUUID().toString()
    String vendorOrderId = UUID.randomUUID().toString()

    def setup() {
        fmsOrderRepository = new FmsOrderRepository(
                fmsOrderSqlServerRepository: fmsOrderSqlServerRepository)

    }

    def " Test GetOrderByMarketPlaceId"() {
        given:
        FmsOrder fmsOrder = mockFmsOrder()


        when:
        fmsOrderRepository.getOrderByMarketPlaceId(vendorOrderId, Tenant.ASDA, Vertical.MARKETPLACE)

        then:
        1 * fmsOrderSqlServerRepository.findByMarketPlaceInfo_VendorOrderIdAndTenantAndVertical(
                _ as String, _ as Tenant, _ as Vertical) >> { String _vendorOrderId, Tenant _tenant, Vertical _vertical ->
            assert _vendorOrderId == vendorOrderId
            return fmsOrder
        }

    }

    FmsOrder mockFmsOrder() {


        FmsOrder testFmsOrder = FmsOrder.builder()
                .vertical(Vertical.ASDAGR)
                .tenant(Tenant.ASDA)
                .storeId("4401")
                .sourceOrderId(sourceOrderId)
                .pickupLocationId("4401")
                .deliveryDate(new Date())
                .marketPlaceInfo(MarketPlaceInfo.builder().vendor(Vendor.UBEREATS).vendorOrderId(vendorOrderId).build())
                .orderState(FmsOrder.OrderStatus.RECEIVED_AT_STORE.getName())
                .priceInfo(OrderPriceInfo.builder()
                        .webOrderTotal(40.0).build()).build()


        testFmsOrder.addAddressInfo(FmsAddressInfo.builder()
                .order(testFmsOrder)
                .addressOne("test addressOne")
                .addressTwo("test addressTwo")
                .addressThree("Test adddressThree")
                .city("test city")
                .latitude("57.2252552")
                .longitude("-6.24343").build())


        testFmsOrder.addSchedulingInfo(FmsSchedulingInfo.builder()
                .order(testFmsOrder)
                .tripId("Trip_Id_34234234234").build())

        testFmsOrder.addContactInfo(FmsCustomerContactInfo.builder()
                .order(testFmsOrder)
                .fullName(new FullName(null, "John", null, "Doe"))
                .phoneNumberOne(new TelePhone("0123456789")).build())


        testFmsOrder.addItem(mockFmsItem(testFmsOrder))

        return testFmsOrder

    }

    def " Test GetOrder"() {
        given:
        FmsOrder fmsOrder = mockFmsOrder()

        when:
        fmsOrderRepository.getOrder(sourceOrderId, Tenant.ASDA, Vertical.MARKETPLACE)

        then:
        1 * fmsOrderSqlServerRepository.findBySourceOrderIdAndTenantAndVertical(
                _ as String, _ as Tenant, _ as Vertical) >> { String _soureOrderId, Tenant _tenant, Vertical _vertical ->
            assert _soureOrderId == sourceOrderId
            return fmsOrder
        }
    }

    def " Test GetOrder with no pre-existing order"() {

        when:
        fmsOrderRepository.getOrder(sourceOrderId, Tenant.ASDA, Vertical.MARKETPLACE)
        fmsOrderSqlServerRepository.findBySourceOrderIdAndTenantAndVertical(_ as String, _ as Tenant, _ as Vertical) >> null

        then:
        thrown(FMSBadRequestException.class)

    }

    def " Test Save"() {
        given:
        FmsOrder fmsOrder = mockFmsOrder()

        when:
        fmsOrderRepository.save(fmsOrder)

        then:
        1 * fmsOrderSqlServerRepository.save(_ as FmsOrder) >> { FmsOrder _fmsOrder ->
            assert fmsOrder == _fmsOrder
            return fmsOrder
        }

    }

    def " Test GetNextIdentify"() {

        when:
        String id = fmsOrderRepository.getNextIdentity()

        then:
        UUID uuid = UUID.fromString(id)
        assert uuid != null
    }


    private static ItemCatalogInfo mockItemCatalogInfo() {
        return new ItemCatalogInfo("EACH", "E", "Asda Fresh ",
                "https://i.groceries.asda.com/image.jpg", 3,
                4, "Ambient", true);


    }

    private static FmsOrderItem mockFmsItem(FmsOrder fmsOrder) {
        FmsOrderItem fmsItem = FmsOrderItem.builder()
                .fmsOrder(fmsOrder)
                .consumerItemNumber("464646")
                .itemPriceInfo(new ItemPriceInfo(new Money(BigDecimal.valueOf(2.5), Currency.GBP)))
                .quantity(2)
                .substitutionOption(SubstitutionOption.DO_NOT_SUBSTITUTE)
                .build()
        fmsItem.addCatalogInfo(mockItemCatalogInfo());
        fmsItem.addUpcInfo(mockFmsItemUpcInfo())
        return fmsItem

    }

    private static ItemUpcInfo mockFmsItemUpcInfo() {
        return ItemUpcInfo.builder()
                .upcNumbers(["22233", "44455"])
                .build()
    }
}
