package com.walmart.common.domain.type

import spock.lang.Specification

class CancellationSourceTest extends Specification {

    def "Test get method for CancellationSource"() {
        when:
        CancellationSource cancellationSource = CancellationSource.get("STORE")
        then:
        cancellationSource == CancellationSource.STORE
        cancellationSource == CancellationSource.valueOf("STORE")
    }
}
