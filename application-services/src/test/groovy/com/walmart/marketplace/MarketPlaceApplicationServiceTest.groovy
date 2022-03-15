package com.walmart.marketplace

import com.walmart.common.domain.type.CancellationSource
import com.walmart.common.domain.type.Currency
import com.walmart.common.domain.valueobject.CancellationDetails
import com.walmart.marketplace.commands.*
import com.walmart.marketplace.commands.extensions.ExternalMarketPlaceItem
import com.walmart.marketplace.commands.extensions.MarketPlacePayment
import com.walmart.marketplace.order.aggregateroot.MarketPlaceOrder
import com.walmart.marketplace.order.domain.MarketPlaceDomainService
import com.walmart.marketplace.order.domain.entity.MarketPlaceEvent
import com.walmart.marketplace.order.domain.entity.type.Vendor
import com.walmart.marketplace.order.domain.uber.MarketPlaceReportRequest
import com.walmart.marketplace.order.domain.uber.ReportType
import com.walmart.marketplace.order.domain.valueobject.MarketPlaceOrderContactInfo
import com.walmart.marketplace.order.domain.valueobject.MarketPlaceOrderPaymentInfo
import com.walmart.marketplace.order.domain.valueobject.Money
import com.walmart.marketplace.order.factory.MarketPlaceOrderFactory
import com.walmart.marketplace.order.repository.IMarketPlaceEventRepository
import com.walmart.marketplace.repository.MarketPlaceRepository
import com.walmart.oms.domain.error.exception.OMSBadRequestException
import spock.lang.Specification

import java.time.LocalDate

class MarketPlaceApplicationServiceTest extends Specification {

    String externalOrderId = UUID.randomUUID().toString()
    String externalNativeOrderId = UUID.randomUUID().toString()
    String vendorStoreId = "a48c2238-5f8d-48ad-917b-b91e2d577cdc"
    Vendor vendor = Vendor.UBEREATS
    String resourceUrl = "http://localhost:8080"
    String eventId = UUID.randomUUID().toString()

    MarketPlaceApplicationService marketPlaceApplicationService

    IMarketPlaceEventRepository eventRepository = Mock()

    MarketPlaceDomainService marketPlaceDomainService = Mock()

    MarketPlaceOrderFactory marketPlaceOrderFactory = Mock()

    MarketPlaceRepository marketPlaceRepository = Mock()

    def setup() {
        marketPlaceApplicationService = new MarketPlaceApplicationService(
                moFactory: marketPlaceOrderFactory,
                marketPlaceDomainService: marketPlaceDomainService,
                eventRepository: eventRepository,
                marketPlaceRepository: marketPlaceRepository)
    }

    def "Test CaptureWebHookEvent when event doesn't exist previously"() {

        given:
        String eventType = "orders.notify"
        String resourceUrl = "http://localhost:8080"

        MarketPlaceEvent marketPlaceEvent = new MarketPlaceEvent(eventId, resourceUrl, eventType, eventId, vendor)
        WebHookEventCommand webHookEventCommand = getWebHookEventCommand(resourceUrl, eventType)

        when:
        marketPlaceApplicationService.captureWebHookEvent(webHookEventCommand)

        then:
        1 * eventRepository.get(webHookEventCommand.getExternalOrderId()) >> null
        1 * eventRepository.save(_ as MarketPlaceEvent) >> { MarketPlaceEvent _marketPlaceEvent ->
            assert _marketPlaceEvent.externalOrderId == eventId
            assert _marketPlaceEvent.vendor == vendor
            assert _marketPlaceEvent.eventType == eventType
            assert _marketPlaceEvent.resourceURL == resourceUrl
            return marketPlaceEvent
        }
    }

    def "Test CaptureWebHookEvent when duplicate event is sent"() {
        given:
        String eventType = "orders.notify"
        String resourceUrl = "http://localhost:8080"
        MarketPlaceEvent marketPlaceEvent = new MarketPlaceEvent(eventId, eventId, resourceUrl, eventType, eventId, vendor)
        WebHookEventCommand webHookEventCommand = getWebHookEventCommand(resourceUrl, eventType)

        when:
        marketPlaceApplicationService.captureWebHookEvent(webHookEventCommand)

        then:
        1 * eventRepository.get(webHookEventCommand.getExternalOrderId()) >> marketPlaceEvent
        thrown(OMSBadRequestException)
    }


    def "Test CreateAndProcessMarketPlaceOrder with Gateway"() {
        given:
        String orderState = "CREATED"
        MarketPlaceOrder marketPlaceOrder = getMarketPlaceOrder(orderState)
        CreateMarketPlaceOrderFromAdapterCommand createMarketPlaceOrderFromAdapterCommand = getCreateMarketPlaceOrderFromAdapterCommand()

        when:
        marketPlaceApplicationService.createAndProcessMarketPlaceOrder(createMarketPlaceOrderFromAdapterCommand)

        then:
        1 * marketPlaceOrderFactory.getMarketPlaceOrderFromGateway(_ as String, _ as String, _ as Vendor) >> { String _vendorOrderId, String _resourceUrl, Vendor _vendor ->
            assert _vendorOrderId == externalOrderId
            assert _resourceUrl == resourceUrl
            assert _vendor == vendor
            return marketPlaceOrder
        }
        1 * marketPlaceDomainService.processMarketPlaceOrder(_ as MarketPlaceOrder) >> { MarketPlaceOrder _marketPlaceOrder ->
            assert _marketPlaceOrder.getVendorOrderId() == externalOrderId
            return marketPlaceOrder
        }
    }

    def "Test CreateAndProcessMarketPlaceOrder with command"() {
        given:
        String storeId = "4401"
        String externalItemId = UUID.randomUUID().toString()
        String expectedItemId = "4888484"
        String vendorStoreId = "a48c2238-5f8d-48ad-917b-b91e2d577cdc"

        MarketPlacePayment marketPlacePayment = getMarketPlacePayment()
        MarketPlaceOrder marketPlaceOrder = getMarketPlaceOrder("PICK_COMPLETE")
        ExternalMarketPlaceItem externalMarketPlaceItem = getExternalMarketPlaceItem(externalItemId, expectedItemId)
        MarketPlaceCreateOrderCommand marketPlaceCreateOrderCommand = getMarketPlaceCreateOrderCommand(storeId, vendorStoreId, externalMarketPlaceItem, marketPlacePayment)
        marketPlaceOrderFactory.getPaymentInfo(_ as Money, _ as Money, _ as Money, _ as Money, _ as Money, _ as Money) >> marketPlaceOrderPaymentInfo
        marketPlaceOrderFactory.getMarketPlaceOrderFromCommand(_ as String, _ as String, _ as String, _ as String, _ as String, _ as String, _ as Date, _ as Vendor, _ as Date, _ as MarketPlaceOrderPaymentInfo) >> marketPlaceOrder
        marketPlaceRepository.getNextIdentity() >> UUID.randomUUID().toString()

        when:
        marketPlaceApplicationService.createAndProcessMarketPlaceOrder(marketPlaceCreateOrderCommand)

        then:
        1 * marketPlaceOrderFactory.getMarketPlaceOrderFromCommand(_ as String, _ as String, _ as String, _ as String, _ as String, _ as String, _ as Date, _ as Vendor, _ as Date, _ as MarketPlaceOrderPaymentInfo) >> { String _externalOrderId, String _externalNativeOrderId, String _firstName, String _lastName, String _storeId, String _vendorStoreId, Date _sourceOrderCreationTime, Vendor _vendor, Date _estimatedPickuptime, MarketPlaceOrderPaymentInfo _marketPlacePaymentInfo ->
            assert _externalOrderId == externalOrderId
            assert _vendor == vendor
            return marketPlaceOrder
        }

        1 * marketPlaceDomainService.processMarketPlaceOrder(_ as MarketPlaceOrder) >> { MarketPlaceOrder _marketPlaceOrder ->
            assert _marketPlaceOrder.getVendorOrderId() == externalOrderId
            assert _marketPlaceOrder.getMarketPlaceItems().size() == 1
            assert _marketPlaceOrder.getMarketPlaceItems().get(0).getItemIdentifier().itemId == expectedItemId
            return marketPlaceOrder
        }
    }

    def "Test CreateAndProcessMarketPlaceOrder with command Bundles"() {
        given:
        String storeId = "4401"
        String externalItemId = UUID.randomUUID().toString()
        String expectedItemId = "4888484"
        String vendorStoreId = "a48c2238-5f8d-48ad-917b-b91e2d577cdc"

        MarketPlacePayment marketPlacePayment = getMarketPlacePayment()
        MarketPlaceOrder marketPlaceOrder = getMarketPlaceOrder("PICK_COMPLETE")
        ExternalMarketPlaceItem externalMarketPlaceItem = getExternalMarketPlaceItemBundles(externalItemId, expectedItemId)
        MarketPlaceCreateOrderCommand marketPlaceCreateOrderCommand = getMarketPlaceCreateOrderCommand(storeId, vendorStoreId, externalMarketPlaceItem, marketPlacePayment)
        marketPlaceOrderFactory.getPaymentInfo(_ as Money, _ as Money, _ as Money, _ as Money, _ as Money, _ as Money) >> marketPlaceOrderPaymentInfo
        marketPlaceOrderFactory.getMarketPlaceOrderFromCommand(_ as String, _ as String, _ as String, _ as String, _ as String, _ as String, _ as Date, _ as Vendor, _ as Date, _ as MarketPlaceOrderPaymentInfo) >> marketPlaceOrder
        marketPlaceRepository.getNextIdentity() >> UUID.randomUUID().toString()

        when:
        marketPlaceApplicationService.createAndProcessMarketPlaceOrder(marketPlaceCreateOrderCommand)

        then:
        1 * marketPlaceOrderFactory.getMarketPlaceOrderFromCommand(_ as String, _ as String, _ as String, _ as String, _ as String, _ as String, _ as Date, _ as Vendor, _ as Date, _ as MarketPlaceOrderPaymentInfo) >> { String _externalOrderId,String _externalNativeOrderId, String _firstName, String _lastName, String _storeId, String _vendorStoreId, Date _sourceOrderCreationTime, Vendor _vendor, Date _estimatedPickuptime, MarketPlaceOrderPaymentInfo _marketPlacePaymentInfo ->
            assert _externalOrderId == externalOrderId
            assert _vendor == vendor
            return marketPlaceOrder
        }

        1 * marketPlaceDomainService.processMarketPlaceOrder(_ as MarketPlaceOrder) >> { MarketPlaceOrder _marketPlaceOrder ->
            assert _marketPlaceOrder.getVendorOrderId() == externalOrderId
            assert _marketPlaceOrder.getMarketPlaceItems().size() == 1
            assert _marketPlaceOrder.getMarketPlaceItems().get(0).getItemIdentifier().itemId == expectedItemId
            return marketPlaceOrder
        }
    }

    def "Test CreateAndProcessMarketPlaceOrder when MarketPlace order is null"() {
        given:
        MarketPlaceOrder marketPlaceOrder = null
        CreateMarketPlaceOrderFromAdapterCommand createMarketPlaceOrderFromAdapterCommand = getCreateMarketPlaceOrderFromAdapterCommand()
        marketPlaceOrderFactory.getMarketPlaceOrderFromCommand(_ as String, _ as String, _ as String, _ as String, _ as String, _ as String, _ as Date, _ as Vendor, _ as Date, _ as MarketPlaceOrderPaymentInfo) >> marketPlaceOrder

        when:
        marketPlaceApplicationService.createAndProcessMarketPlaceOrder(createMarketPlaceOrderFromAdapterCommand)

        then:
        1 * marketPlaceOrderFactory.getMarketPlaceOrderFromGateway(_ as String, _ as String, _ as Vendor)
        0 * marketPlaceDomainService.processMarketPlaceOrder(_ as MarketPlaceOrder)
    }

    def "Test invoke report Successfully"() {
        given:
        MarketPlaceReportCommand reportCommand = createMarketplaceReportCommand()
        String workFlowJson = getWorkFlowJson()

        when:
        String workFlowIdJson = marketPlaceApplicationService.invokeMarketPlaceReport(reportCommand)

        then:
        1 * marketPlaceDomainService.invokeMarketPlaceReport(_ as MarketPlaceReportRequest) >> {
            MarketPlaceReportRequest marketPlaceReportRequest ->
                marketPlaceReportRequest.vendor == Vendor.UBEREATS
                marketPlaceReportRequest.reportType == ReportType.DOWNTIME_REPORT
                marketPlaceReportRequest.startDate == LocalDate.now().minusDays(15)
                marketPlaceReportRequest.endDate == LocalDate.now().minusDays(4)

                return workFlowJson
        }

        assert workFlowIdJson != null

    }

    def "Test cancel order"() {
        given:
        marketPlaceOrderFactory.getOrder(_) >> Optional.of(getMarketPlaceOrder("PICK_COMPLETE"))
        marketPlaceDomainService.cancelOrder(_ as MarketPlaceOrder, _ as CancellationDetails) >> getMarketPlaceOrder("CANCELLED")
        when:
        MarketPlaceOrder marketPlaceOrder = marketPlaceApplicationService.cancelOrder(getCancelMarketPlaceOrderCommand())

        then:
        assert marketPlaceOrder != null
    }

    def "Test get marketplace order"() {

        when:
        marketPlaceApplicationService.getOrder(externalOrderId)

        then:
        1 * marketPlaceRepository.get(_ as String);
    }

    private CancelMarketPlaceOrderCommand getCancelMarketPlaceOrderCommand() {
        return CancelMarketPlaceOrderCommand.builder()
                .sourceOrderId(externalOrderId)
                .cancellationDetails(CancellationDetails.builder()
                        .cancelledReasonCode("store")
                        .cancelledBy(CancellationSource.STORE)
                        .cancelledReasonDescription("cancelled at store")
                        .build())
                .build()
    }

    private MarketPlaceOrderContactInfo getMarketPlaceOrderContactInfo() {
        return MarketPlaceOrderContactInfo.builder()
                .firstName("John")
                .lastName("Doe")
                .build()
    }

    private MarketPlaceOrder getMarketPlaceOrder(String orderState) {
        return MarketPlaceOrder.builder()
                .vendorOrderId(externalOrderId)
                .vendorNativeOrderId(externalNativeOrderId)
                .orderDueTime(new Date())
                .orderState(orderState)
                .sourceModifiedDate(new Date())
                .storeId("4401")
                .vendorStoreId(vendorStoreId)
                .vendorId(Vendor.UBEREATS)
                .marketPlaceOrderContactInfo(getMarketPlaceOrderContactInfo())
                .paymentInfo(getMarketPlaceOrderPaymentInfo())
                .build()
    }

    private WebHookEventCommand getWebHookEventCommand(String resourceUrl, String eventType) {
        return WebHookEventCommand.builder()
                .externalOrderId(eventId)
                .resourceURL(resourceUrl)
                .eventType(eventType)
                .sourceEventId(eventId)
                .vendor(Vendor.UBEREATS)
                .build()
    }

    private CreateMarketPlaceOrderFromAdapterCommand getCreateMarketPlaceOrderFromAdapterCommand() {
        return CreateMarketPlaceOrderFromAdapterCommand.builder()
                .externalOrderId(externalOrderId)
                .resourceUrl(resourceUrl)
                .vendor(vendor)
                .build()
    }

    private MarketPlaceOrderPaymentInfo getMarketPlaceOrderPaymentInfo() {
        return MarketPlaceOrderPaymentInfo.builder()
                .total(new Money(50.0, Currency.GBP))
                .subTotal(new Money(45.0, Currency.GBP))
                .tax(new Money(5.0, Currency.GBP))
                .totalFeeTax(new Money(5.0, Currency.GBP))
                .bagFee(new Money(5.0, Currency.GBP))
                .totalFee(new Money(0.0, Currency.GBP))
                .build()
    }

    private MarketPlaceCreateOrderCommand getMarketPlaceCreateOrderCommand(String storeId, String vendorStoreId, ExternalMarketPlaceItem externalMarketPlaceItem, MarketPlacePayment marketPlacePayment) {
        return MarketPlaceCreateOrderCommand.builder()
                .externalOrderId(externalOrderId)
                .externalNativeOrderId(externalNativeOrderId)
                .storeId(storeId)
                .vendorStoreId(vendorStoreId)
                .firstName("John")
                .lastName("Doe")
                .marketPlaceItems([externalMarketPlaceItem])
                .sourceOrderCreationTime(new Date())
                .estimatedArrivalTime(new Date())
                .payment(marketPlacePayment)
                .vendor(vendor).build()
    }

    private ExternalMarketPlaceItem getExternalMarketPlaceItem(String externalItemId, String expectedItemId) {
        return ExternalMarketPlaceItem.builder()
                .externalItemId(externalItemId)
                .itemId(expectedItemId)
                .itemType("CIN")
                .totalPrice(1.0)
                .build()
    }

    private ExternalMarketPlaceItem getExternalMarketPlaceItemBundles(String externalItemId, String expectedItemId) {
        return ExternalMarketPlaceItem.builder()
                .externalItemId(externalItemId)
                .itemId(expectedItemId)
                .itemType("CIN")
                .totalPrice(1.0)
                .bundledItems(getBundledItemList())
                .build()
    }

    private List<ExternalMarketPlaceItem.ExternalMarketPlaceBundledItem> getBundledItemList() {
        return Arrays.asList(ExternalMarketPlaceItem.ExternalMarketPlaceBundledItem.builder()
                .bundleDescription("test")
                .bundleQuantity(1)
                .bundleSkuId("123")
                .bundleInstanceId(UUID.randomUUID().toString())
                .build())
    }

    private MarketPlacePayment getMarketPlacePayment() {
        return MarketPlacePayment.builder()
                .total(new Money(50.0, Currency.GBP))
                .subTotal(new Money(45.0, Currency.GBP))
                .tax(new Money(5.0, Currency.GBP))
                .totalFeeTax(new Money(5.0, Currency.GBP))
                .bagFee(new Money(5.0, Currency.GBP))
                .totalFee(new Money(0.0, Currency.GBP))
                .build()
    }

    MarketPlaceReportCommand createMarketplaceReportCommand() {
        return MarketPlaceReportCommand.builder()
                .vendor(Vendor.UBEREATS)
                .reportType(ReportType.DOWNTIME_REPORT)
                .startDate(LocalDate.now().minusDays(15))
                .endDate(LocalDate.now().minusDays(4))
                .build()
    }

    MarketPlaceReportRequest getMarketPlaceReportRequest(MarketPlaceReportCommand reportCommand) {
        return MarketPlaceReportRequest.builder()
                .reportType(reportCommand.getReportType())
                .startDate(reportCommand.getStartDate())
                .endDate(reportCommand.getEndDate())
                .vendor(reportCommand.getVendor())
                .build()
    }

    String getWorkFlowJson() {
        return "{ \"workflow_id\": \"818767ad-4035-4aeb-9fb6-ede9db61164b_848c4c6f-1f35-4ebc-af20-189ea63bbefb\" }"
    }
}