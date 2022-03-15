package com.walmart.marketplace.justeats.request

import spock.lang.Specification

class PriceConversionUtilTest extends Specification {

    def "Price Conversion Success"() {
        when:
        BigDecimal amount = PriceConversionUtil.convertPriceToAmount(1230);

        then:
        amount == BigDecimal.valueOf(12.30)
    }
}
