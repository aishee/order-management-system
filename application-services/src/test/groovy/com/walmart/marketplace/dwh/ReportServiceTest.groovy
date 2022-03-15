package com.walmart.marketplace.dwh

import com.jcraft.jsch.ChannelSftp
import com.jcraft.jsch.Session

import com.walmart.common.metrics.MetricService
import com.walmart.marketplace.commands.DownloadReportEventCommand

import com.walmart.marketplace.dwh.config.DWHConfig
import com.walmart.marketplace.order.domain.uber.ReportType
import com.walmart.oms.domain.error.exception.OMSThirdPartyException
import io.github.resilience4j.retry.Retry
import io.github.resilience4j.retry.RetryRegistry
import spock.lang.Specification

class ReportServiceTest extends Specification {
    ReportClient reportClient = Mock()
    RetryRegistry retryRegistry = Mock()
    DWHConfig dwhConfig = Mock()
    MetricService metricService = Mock()

    Session session
    ChannelSftp sftpChannel

    ReportService reportService
    Retry retry = RetryRegistry.ofDefaults().retry("UBER")

    def setup() {
        session = Mock()
        sftpChannel = Mock()

        reportService = new ReportServiceImpl(reportClient: reportClient,
                retryRegistry: retryRegistry,
                dwhConfig: dwhConfig,
                metricService: metricService
        )
    }

    def "Upload History Report without Retries"() {
        DownloadReportEventCommand command = getWebhookEventCommand(ReportType.ORDER_HISTORY_REPORT)

        given:
        reportClient.getSession(_, _, _, _) >> session
        reportClient.getChannel(session) >> sftpChannel
        retryRegistry.retry("UBER") >> retry

        when:
        reportService.uploadReport(command)

        then:
        1 * reportClient.downloadReport(_, _)
        1 * reportClient.uploadReport(_, _, _)
        1 * reportClient.closeConnection(_, _)
    }

    def "Upload Downtime Report without Retries"() {
        DownloadReportEventCommand command = getWebhookEventCommand(ReportType.DOWNTIME_REPORT)

        given:
        reportClient.getSession(_, _, _, _) >> session
        reportClient.getChannel(session) >> sftpChannel
        retryRegistry.retry("UBER") >> retry

        when:
        reportService.uploadReport(command)

        then:
        1 * reportClient.downloadReport(_, _)
        1 * reportClient.uploadReport(_, _, _)
        1 * reportClient.closeConnection(_, _)
    }

    def "Upload Report with Retry"() {
        DownloadReportEventCommand command = getWebhookEventCommand(ReportType.ORDER_HISTORY_REPORT)

        given:
        reportClient.getSession(_, _, _, _) >> session
        reportClient.getChannel(session) >> sftpChannel
        retryRegistry.retry("UBER") >> retry
        reportClient.uploadReport(_, _, _) >> { throw new OMSThirdPartyException() }

        when:
        reportService.uploadReport(command)

        then:
        3 * reportClient.downloadReport(_, _)
        3 * reportClient.closeConnection(_, _)
    }

    private DownloadReportEventCommand getWebhookEventCommand(ReportType reportType) {
        String url = "https://tbs-static.uber.com/fecb3ad5-1070-43f7-aef9-93626b8effb9_order_history_local_2021-06-05_2021-06-05.csv"
        List<String> urlList = new ArrayList<>()
        urlList.add(url)
        return DownloadReportEventCommand.builder()
                .reportUrls(urlList)
                .reportType(reportType)
                .build()
    }
}
