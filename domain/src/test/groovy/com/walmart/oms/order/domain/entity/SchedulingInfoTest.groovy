package com.walmart.oms.order.domain.entity

import com.walmart.oms.order.aggregateroot.OmsOrder
import spock.lang.Specification

class SchedulingInfoTest extends Specification {

    def "test scheduling info"() {
        given:
        OmsOrder omsOrder = Mock()
        SchedulingInfo schedulingInfo = SchedulingInfo.builder()
                .id("id").tripId("tripId").doorStepTime(12).vanId("vanId")
                .loadNumber("loadNumber").order(omsOrder)
                .build()

        assert schedulingInfo.getId() == "id"
        assert schedulingInfo.getOrder() != null
        assert schedulingInfo.getTripId() == "tripId"
        assert schedulingInfo.getDoorStepTime() == 12
        assert schedulingInfo.getVanId() == "vanId"
        assert schedulingInfo.getLoadNumber() == "loadNumber"
        assert schedulingInfo.toString() != null
    }
}
