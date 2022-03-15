package com.walmart.marketplace.infrastructure.gateway.uber.report.dto

import com.walmart.marketplace.order.domain.uber.ReportType
import com.walmart.oms.domain.error.exception.OMSBadRequestException
import spock.lang.Specification

import java.time.LocalDate

class UberReportReqSpec extends Specification {

    def "InValid End Date in Uber Report Request"() {
        given:
        UberReportReq uberReportReq = new UberReportReq(ReportType.ORDER_HISTORY_REPORT, Collections.singletonList("1234"), LocalDate.now(), LocalDate.now().minusDays(1));

        when:
        uberReportReq.validateReportEndDate(2);

        then:
        thrown(OMSBadRequestException.class)
    }

    def "Valid End Date in Uber Report Request"() {
        given:
        UberReportReq uberReportReq = new UberReportReq(ReportType.ORDER_HISTORY_REPORT, Collections.singletonList("1234"), LocalDate.now(), LocalDate.now().minusDays(2));

        when:
        uberReportReq.validateReportEndDate(1);

        then:
        noExceptionThrown()
    }
}
