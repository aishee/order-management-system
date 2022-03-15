package com.walmart.common.domain

import com.walmart.common.domain.type.Tenant
import com.walmart.common.domain.type.Vertical
import spock.lang.Specification

class OrderVOTest extends Specification {
    String sourceOrderId = "sourceOrderId"
    Tenant tenant = Tenant.get("ASDA")
    Vertical vertical = Vertical.get("ASDAGR")

    def "Test Getter for OrderVO"() {
        given:
        when:
        OrderVO orderVO1 = OrderVO.builder().sourceOrderId(sourceOrderId)
                .tenant(tenant).vertical(vertical).build()
        then:
        orderVO1.getSourceOrderId() == sourceOrderId
        orderVO1.getVertical() == Vertical.valueOf("ASDAGR")
        orderVO1.getVertical().getVerticalType() == Vertical.VerticalType.valueOf("GR")
        orderVO1.getTenant() == Tenant.valueOf("ASDA")
        orderVO1.toString().length() > 0
    }
}
