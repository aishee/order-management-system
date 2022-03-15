package com.walmart.util

import com.walmart.services.oms.order.common.model.OmsOrder
import spock.lang.Specification

class JsonConverterUtilSpec extends Specification {

    def "Test json object to String conversion"() {
        when:
        OmsOrder omsOrder = new OmsOrder()
        omsOrder.setContractId("1234")
        omsOrder.setOrderNo("1234")
        omsOrder.setOrder_volume(2.0)
        String json = JsonConverterUtil.convertToString(omsOrder)

        then:
        json != null
    }
}
