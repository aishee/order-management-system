package com.walmart.marketplace.converter

import com.walmart.common.domain.type.CancellationSource
import com.walmart.marketplace.commands.CancelMarketPlaceOrderCommand
import com.walmart.marketplace.commands.CreateMarketPlaceOrderFromAdapterCommand
import com.walmart.marketplace.commands.WebHookEventCommand
import com.walmart.marketplace.commands.DownloadReportEventCommand
import com.walmart.marketplace.commands.MarketPlaceCreateOrderCommand;
import com.walmart.marketplace.dto.request.CreateMarketPlaceOrderRequest;
import com.walmart.marketplace.order.domain.entity.type.Vendor
import com.walmart.marketplace.order.domain.uber.ReportType
import com.walmart.marketplace.uber.dto.Meta
import com.walmart.marketplace.uber.dto.UberWebHookRequest
import spock.lang.Specification

import java.time.Instant

class RequestToCommandMapperTest extends Specification {

    RequestToCommandMapper requestToCommandMapper;

    def setup() {
        requestToCommandMapper = new RequestToCommandMapper()
    }

    def "Test CreateWebHookCommand with a valid web hook request"() {

        given:

        String eventId = UUID.randomUUID().toString();
        Vendor vendor = Vendor.UBEREATS;
        String eventType = "orders.notify"
        String resourceUrl = "http://localhost:8080"

        UberWebHookRequest webHookRequest = new UberWebHookRequest()
        webHookRequest.setEventId(eventId)
        webHookRequest.setEventType(eventType)
        webHookRequest.setResourceHref(resourceUrl)
        webHookRequest.setEventTime(Instant.now().toEpochMilli())

        Meta meta = new Meta()
        meta.setResourceId(eventId)

        webHookRequest.setMeta(meta)

        when:
        WebHookEventCommand webHookEventCommand = requestToCommandMapper.createWebHookCommand(webHookRequest)

        then:
        assert webHookEventCommand.externalOrderId == eventId
        assert webHookEventCommand.resourceURL == resourceUrl
        assert webHookEventCommand.eventType == eventType
    }

    def "CreateMarketPlaceOrderCmd"() {

        given:

        String eventId = UUID.randomUUID().toString();
        Vendor vendor = Vendor.UBEREATS;
        String eventType = "orders.notify"
        String resourceUrl = "http://localhost:8080"

        UberWebHookRequest webHookRequest = new UberWebHookRequest()
        webHookRequest.setEventId(eventId)
        webHookRequest.setEventType(eventType)
        webHookRequest.setResourceHref(resourceUrl)
        webHookRequest.setEventTime(Instant.now().toEpochMilli())

        when:
        CreateMarketPlaceOrderFromAdapterCommand createMarketPlaceOrderFromAdapterCommand = requestToCommandMapper.createMarketPlaceOrderCmd(eventId, resourceUrl, vendor)

        then:
        assert createMarketPlaceOrderFromAdapterCommand.externalOrderId == eventId
        assert createMarketPlaceOrderFromAdapterCommand.resourceUrl == resourceUrl
        assert createMarketPlaceOrderFromAdapterCommand.vendor == vendor

    }

    def "Test Create MarketPlaceCancellationCommand"() {

        given:

        String eventId = UUID.randomUUID().toString();
        Vendor vendor = Vendor.UBEREATS;
        String eventType = "orders.notify"
        String resourceUrl = "http://localhost:8080"
        String id = UUID.randomUUID().toString()
        String cancelledReasonCode = "VENDOR"
        String cancelledReasonDescription = "cancelled by Vendor"

        UberWebHookRequest webHookRequest = new UberWebHookRequest()
        webHookRequest.setEventId(eventId)
        webHookRequest.setEventType(eventType)
        webHookRequest.setResourceHref(resourceUrl)
        webHookRequest.setEventTime(Instant.now().toEpochMilli())

        when:
        CancelMarketPlaceOrderCommand cancelMarketPlaceOrderCommand = requestToCommandMapper.createMarketPlaceCancelCommand(id, cancelledReasonCode, cancelledReasonDescription, resourceUrl, vendor)

        then:
        assert cancelMarketPlaceOrderCommand.sourceOrderId == id
        assert cancelMarketPlaceOrderCommand.resourceUrl == resourceUrl
        assert cancelMarketPlaceOrderCommand.vendor == vendor
        assert cancelMarketPlaceOrderCommand.cancellationDetails != null
        assert cancelMarketPlaceOrderCommand.cancellationDetails.cancelledReasonDescription == cancelledReasonDescription
        assert cancelMarketPlaceOrderCommand.cancellationDetails.cancelledBy == CancellationSource.VENDOR
        assert cancelMarketPlaceOrderCommand.cancellationDetails.cancelledReasonCode == cancelledReasonCode
    }

    def "Test Create DownloadReportEventCommand"() {

        given:

        String eventId = UUID.randomUUID().toString()
        String eventType = "eats.report.success"
        String resourceUrl = "http://localhost:8080"

        UberWebHookRequest webHookRequest = new UberWebHookRequest()
        webHookRequest.setEventId(eventId)
        webHookRequest.setEventType(eventType)
        webHookRequest.setResourceHref(resourceUrl)
        webHookRequest.setEventTime(Instant.now().toEpochMilli())
        webHookRequest.setReportType("DOWNTIME_REPORT")

        when:
        DownloadReportEventCommand downloadReportEventCommand = requestToCommandMapper.createReportWebHookCommand(webHookRequest)

        then:
        assert downloadReportEventCommand.reportType == ReportType.DOWNTIME_REPORT

    }

    def "createMarketPlaceOrderFromRequest"() {
        given:
        CreateMarketPlaceOrderRequest createMarketPlaceOrderRequest = CreateMarketPlaceOrderRequest.builder().data(
                CreateMarketPlaceOrderRequest.CreateMarketPlaceOrderRequestData.builder()
                        .storeId("5731")
                        .firstName("John")
                        .lastName("Doe")
                        .vendor(Vendor.UBEREATS)
                        .externalOrderId(UUID.randomUUID().toString())
                        .payment(buildPaymentInfo())
                        .sourceOrderCreationTime(new Date())
                        .marketPlaceItems(buildMarketPlaceItemsList())
                        .build()).build()

        when:
        MarketPlaceCreateOrderCommand marketPlaceCreateOrderCommand = requestToCommandMapper.createMarketPlaceOrderFromRequest(createMarketPlaceOrderRequest)

        then:
        marketPlaceCreateOrderCommand.vendor == Vendor.UBEREATS
        marketPlaceCreateOrderCommand.firstName == "John"
        marketPlaceCreateOrderCommand.storeId == "5731"
        marketPlaceCreateOrderCommand.marketPlaceItems.size() == 1
        marketPlaceCreateOrderCommand.marketPlaceItems[0].bundledItems.size() == 1
    }

    private List<CreateMarketPlaceOrderRequest.MarketPlaceRequestItemData> buildMarketPlaceItemsList() {
        return Arrays.asList(CreateMarketPlaceOrderRequest.MarketPlaceRequestItemData.builder()
                .itemId("12345")
                .itemType("CIN")
                .externalItemId(UUID.randomUUID().toString())
                .totalPrice(5.0)
                .unitPrice(2.5)
                .quantity(2)
                .itemDescription("Test description")
                .baseUnitPrice(2.5)
                .baseTotalPrice(5.0)
                .marketPlaceBundledItems(getBundledItems()).build())
    }

    private List<CreateMarketPlaceOrderRequest.MarketPlaceRequestBundledItemData> getBundledItems() {
        return Arrays.asList(CreateMarketPlaceOrderRequest.MarketPlaceRequestBundledItemData.builder()
                .bundleDescription("Test description")
                .bundleQuantity(1)
                .bundleInstanceId(UUID.randomUUID().toString())
                .bundleSkuId("123").build())
    }

    private CreateMarketPlaceOrderRequest.PaymentInfo buildPaymentInfo() {
        return CreateMarketPlaceOrderRequest.PaymentInfo.builder()
                .total(50.0)
                .totalFee(5.0)
                .tax(1.25)
                .bagFee(2.5)
                .totalFeeTax(1.25)
                .subTotal(45.0)
                .build()
    }
}