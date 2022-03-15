package com.walmart.marketplace.order.domain.uber

import spock.lang.Specification

class ReportTypeSpec extends Specification {
    ReportType reportType

    def "Report type is DOWNTIME REPORT"() {
        given:
        reportType = ReportType.DOWNTIME_REPORT

        when:
        boolean s = reportType.isDownTimeReport()

        then:
        s
    }

    def "Report type is CUSTOMER_AND_DELIVERY_FEEDBACK_REPORT"() {
        given:
        reportType = ReportType.CUSTOMER_AND_DELIVERY_FEEDBACK_REPORT

        when:
        boolean s = reportType.isDownTimeReport()

        then:
        !s
    }

    def "All Report Type Validations"() {
        when:
        List<ReportType> reportTypes = ReportType.values()

        then:
        reportTypes.size() == 9
    }
}
