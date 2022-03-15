package com.walmart.marketplace.controller

import com.walmart.common.metrics.MetricService
import com.walmart.marketplace.MarketPlaceApplicationService
import com.walmart.marketplace.commands.MarketPlaceReportCommand
import com.walmart.marketplace.converter.RequestToCommandMapper
import com.walmart.marketplace.order.domain.entity.type.Vendor
import com.walmart.marketplace.order.domain.uber.ReportType
import com.walmart.oms.domain.error.exception.OMSBadRequestException
import spock.lang.Specification

import java.time.LocalDate

class MarketPlaceReportControllerTest extends Specification {
    MarketPlaceReportController controller

    RequestToCommandMapper mapper = Spy()

    MarketPlaceApplicationService marketPlaceApplicationService = Mock()

    MetricService metricService = Mock()

    def setup() {
        controller = new MarketPlaceReportController(
                mapper: mapper,
                marketPlaceApplicationService: marketPlaceApplicationService,
                metricService: metricService
        )
    }


    def "Test Invoke Report Successfully (DOWNTIME)"() {
        given:
        String workFlowIdJson = getWorkFlowJson()

        when:
        String workFlowJson = controller.invokeMarketPlaceReport(Vendor.UBEREATS, ReportType.DOWNTIME_REPORT, 15, 3)
        then:
        1 * marketPlaceApplicationService.invokeMarketPlaceReport(_ as MarketPlaceReportCommand) >> { MarketPlaceReportCommand _marketPlaceReportCommand ->
            _marketPlaceReportCommand.vendor == Vendor.UBEREATS
            _marketPlaceReportCommand.reportType == ReportType.DOWNTIME_REPORT
            _marketPlaceReportCommand.startDate == LocalDate.now().minusDays(15)
            _marketPlaceReportCommand.endDate == LocalDate.now().minusDays(3)

            return workFlowIdJson

        }
        assert workFlowJson != null

    }

    def "Test Invoke Report Successfully (ORDER_HISTORY_REPORT)"() {
        given:
        String workFlowIdJson = getWorkFlowJson()

        when:
        String workFlowJson = controller.invokeMarketPlaceReport(Vendor.UBEREATS, ReportType.DOWNTIME_REPORT, 15, 3)
        then:
        1 * marketPlaceApplicationService.invokeMarketPlaceReport(_ as MarketPlaceReportCommand) >> { MarketPlaceReportCommand _marketPlaceReportCommand ->
            _marketPlaceReportCommand.vendor == Vendor.UBEREATS
            _marketPlaceReportCommand.reportType == ReportType.ORDER_HISTORY_REPORT
            _marketPlaceReportCommand.startDate == LocalDate.now().minusDays(15)
            _marketPlaceReportCommand.endDate == LocalDate.now().minusDays(3)

            return workFlowIdJson

        }
        assert workFlowJson != null

    }

    def "Test Invoke Report Successfully (CUSTOMER_AND_DELIVERY_FEEDBACK_REPORT)"() {
        given:
        String workFlowIdJson = getWorkFlowJson()

        when:
        String workFlowJson = controller.invokeMarketPlaceReport(Vendor.UBEREATS, ReportType.DOWNTIME_REPORT, 15, 3)
        then:
        1 * marketPlaceApplicationService.invokeMarketPlaceReport(_ as MarketPlaceReportCommand) >> { MarketPlaceReportCommand _marketPlaceReportCommand ->
            _marketPlaceReportCommand.vendor == Vendor.UBEREATS
            _marketPlaceReportCommand.reportType == ReportType.CUSTOMER_AND_DELIVERY_FEEDBACK_REPORT
            _marketPlaceReportCommand.startDate == LocalDate.now().minusDays(15)
            _marketPlaceReportCommand.endDate == LocalDate.now().minusDays(3)

            return workFlowIdJson

        }
        assert workFlowJson != null

    }

    def "Test Invoke Report Successfully (MENU_ITEM_FEEDBACK_REPORT)"() {
        given:
        String workFlowIdJson = getWorkFlowJson()

        when:
        String workFlowJson = controller.invokeMarketPlaceReport(Vendor.UBEREATS, ReportType.DOWNTIME_REPORT, 15, 3)
        then:
        1 * marketPlaceApplicationService.invokeMarketPlaceReport(_ as MarketPlaceReportCommand) >> { MarketPlaceReportCommand _marketPlaceReportCommand ->
            _marketPlaceReportCommand.vendor == Vendor.UBEREATS
            _marketPlaceReportCommand.reportType == ReportType.MENU_ITEM_FEEDBACK_REPORT
            _marketPlaceReportCommand.startDate == LocalDate.now().minusDays(15)
            _marketPlaceReportCommand.endDate == LocalDate.now().minusDays(3)

            return workFlowIdJson

        }
        assert workFlowJson != null

    }

    def "Test Invoke Report Successfully (ORDER_ERRORS_MENU_ITEM_REPORT)"() {
        given:
        String workFlowIdJson = getWorkFlowJson()

        when:
        String workFlowJson = controller.invokeMarketPlaceReport(Vendor.UBEREATS, ReportType.DOWNTIME_REPORT, 15, 3)
        then:
        1 * marketPlaceApplicationService.invokeMarketPlaceReport(_ as MarketPlaceReportCommand) >> { MarketPlaceReportCommand _marketPlaceReportCommand ->
            _marketPlaceReportCommand.vendor == Vendor.UBEREATS
            _marketPlaceReportCommand.reportType == ReportType.ORDER_ERRORS_MENU_ITEM_REPORT
            _marketPlaceReportCommand.startDate == LocalDate.now().minusDays(15)
            _marketPlaceReportCommand.endDate == LocalDate.now().minusDays(3)

            return workFlowIdJson

        }
        assert workFlowJson != null

    }

    def "Test Invoke Report Successfully (ORDER_ERRORS_TRANSACTION_REPORT)"() {
        given:
        String workFlowIdJson = getWorkFlowJson()

        when:
        String workFlowJson = controller.invokeMarketPlaceReport(Vendor.UBEREATS, ReportType.DOWNTIME_REPORT, 15, 3)
        then:
        1 * marketPlaceApplicationService.invokeMarketPlaceReport(_ as MarketPlaceReportCommand) >> { MarketPlaceReportCommand _marketPlaceReportCommand ->
            _marketPlaceReportCommand.vendor == Vendor.UBEREATS
            _marketPlaceReportCommand.reportType == ReportType.ORDER_ERRORS_TRANSACTION_REPORT
            _marketPlaceReportCommand.startDate == LocalDate.now().minusDays(15)
            _marketPlaceReportCommand.endDate == LocalDate.now().minusDays(3)

            return workFlowIdJson

        }
        assert workFlowJson != null

    }

    def "Test Invoke Report Successfully (PAYMENT_DETAILS_REPORT)"() {
        given:
        String workFlowIdJson = getWorkFlowJson()

        when:
        String workFlowJson = controller.invokeMarketPlaceReport(Vendor.UBEREATS, ReportType.DOWNTIME_REPORT, 15, 3)

        then:
        1 * marketPlaceApplicationService.invokeMarketPlaceReport(_ as MarketPlaceReportCommand) >> { MarketPlaceReportCommand _marketPlaceReportCommand ->
            _marketPlaceReportCommand.vendor == Vendor.UBEREATS
            _marketPlaceReportCommand.reportType == ReportType.PAYMENT_DETAILS_REPORT
            _marketPlaceReportCommand.startDate == LocalDate.now().minusDays(15)
            _marketPlaceReportCommand.endDate == LocalDate.now().minusDays(3)

            return workFlowIdJson

        }
        assert workFlowJson != null

    }

    def "Test Invoke Report Throw OMSBadRequestException"() {
        when:
        String workFlowJson = controller.invokeMarketPlaceReport(Vendor.UBEREATS, ReportType.DOWNTIME_REPORT, 15, 3)
        then:
        1 * marketPlaceApplicationService.invokeMarketPlaceReport(_ as MarketPlaceReportCommand) >> { throw new OMSBadRequestException("Exception") }
        thrown(OMSBadRequestException.class)

    }

    def "Test Invoke Report Throw Exception"() {
        when:
        String workFlowJson = controller.invokeMarketPlaceReport(Vendor.UBEREATS, ReportType.DOWNTIME_REPORT, 15, 3)
        then:
        1 * marketPlaceApplicationService.invokeMarketPlaceReport(_ as MarketPlaceReportCommand) >> { throw new Exception("Exception") }
        thrown(Exception.class)

    }

    def "Test Invoke Report invalid dayToEnd"() {
        given:
        String workFlowIdJson = getWorkFlowJson()

        when:
        String workFlowJson = controller.invokeMarketPlaceReport(Vendor.UBEREATS, ReportType.DOWNTIME_REPORT, 2, 3)
        then:
        0 * marketPlaceApplicationService.invokeMarketPlaceReport(_ as MarketPlaceReportCommand) >> { MarketPlaceReportCommand _marketPlaceReportCommand ->
            _marketPlaceReportCommand.vendor == Vendor.UBEREATS
            _marketPlaceReportCommand.reportType == ReportType.DOWNTIME_REPORT
            _marketPlaceReportCommand.startDate == LocalDate.now().minusDays(2)
            _marketPlaceReportCommand.endDate == LocalDate.now().minusDays(3)

            return workFlowIdJson

        }
        thrown(OMSBadRequestException.class)

    }

    String getWorkFlowJson() {
        return "{ \"workflow_id\": \"818767ad-4035-4aeb-9fb6-ede9db61164b_848c4c6f-1f35-4ebc-af20-189ea63bbefb\" }"
    }
}
