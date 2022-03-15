package com.walmart.util

import spock.lang.Specification

class JAXBContextUtilTest extends Specification {

    def "Invalid Object for XML Parsing"() {
        when:
        JAXBContextUtil.getJAXBContext(List.class);

        then:
        thrown(AssertionError.class)
    }
}
