package com.walmart.marketplace.uber.webhook.processors

import com.walmart.common.metrics.MetricService
import com.walmart.marketplace.converter.RequestToCommandMapper
import com.walmart.marketplace.dwh.ReportService
import com.walmart.marketplace.dwh.config.DWHConfig
import com.walmart.marketplace.uber.dto.ReportMetaData
import com.walmart.marketplace.uber.dto.UberWebHookRequest
import com.walmart.oms.domain.error.exception.OMSBadRequestException
import com.walmart.oms.domain.error.exception.OMSThirdPartyException
import spock.lang.Specification

import java.time.Instant

class ReportDownloadEventProcessorTest extends Specification {
    ReportDownloadEventProcessor reportDownloadEventProcessor

    private RequestToCommandMapper mapper = Mock()
    private ReportService reportService = Mock()
    private MetricService metricService = Mock()

    def setup() {
        reportDownloadEventProcessor = new ReportDownloadEventProcessor(
                mapper: mapper,
                reportService: reportService,
                metricService: metricService
        )
    }

    def "Test Uber Webhook event on Uber Report Enabled Empty Url"() {
        given:
        String eventId = UUID.randomUUID().toString()
        String eventType = "eats.report.success"
        String resourceUrl = "http://localhost:8080"

        UberWebHookRequest webHookRequest = new UberWebHookRequest()
        webHookRequest.setEventId(eventId)
        webHookRequest.setEventType(eventType)
        webHookRequest.setResourceHref(resourceUrl)
        webHookRequest.setEventTime(Instant.now().toEpochMilli())

        when:
        reportDownloadEventProcessor.processWebhookRequest(webHookRequest)

        then:
        thrown OMSBadRequestException
    }

    def "Uber Webhook event on Uber Report throwing Exception"() {
        given:
        String resourceUrl = "http://localhost:8080"
        ReportMetaData.Section section = new ReportMetaData.Section()
        section.setDownloadUrl("test")
        List<ReportMetaData.Section> sectionList = new ArrayList<>()
        sectionList.add(section)
        ReportMetaData reportMetaData = new ReportMetaData()
        reportMetaData.setSections(sectionList)
        UberWebHookRequest webHookRequest = new UberWebHookRequest()
        webHookRequest.setResourceHref(resourceUrl)
        webHookRequest.setReportMetaData(reportMetaData)
        mapper.createReportWebHookCommand(_ as UberWebHookRequest) >> { throw new OMSThirdPartyException("Mapping failure") }

        when:
        reportDownloadEventProcessor.processWebhookRequest(webHookRequest)

        then:
        thrown OMSThirdPartyException
    }

    def "Test Uber Webhook event on Uber Report Enabled"() {
        given:
        String eventId = UUID.randomUUID().toString()
        String eventType = "eats.report.success"
        String resourceUrl = "http://localhost:8080"
        ReportMetaData.Section section = new ReportMetaData.Section()
        section.setDownloadUrl("test")
        List<ReportMetaData.Section> sectionList = new ArrayList<>()
        sectionList.add(section)
        ReportMetaData reportMetaData = new ReportMetaData()
        reportMetaData.setSections(sectionList)

        UberWebHookRequest webHookRequest = new UberWebHookRequest()
        webHookRequest.setEventId(eventId)
        webHookRequest.setEventType(eventType)
        webHookRequest.setResourceHref(resourceUrl)
        webHookRequest.setEventTime(Instant.now().toEpochMilli())
        webHookRequest.setReportMetaData(reportMetaData)

        when:
        boolean result = reportDownloadEventProcessor.processWebhookRequest(webHookRequest)

        then:
        result
    }
}
