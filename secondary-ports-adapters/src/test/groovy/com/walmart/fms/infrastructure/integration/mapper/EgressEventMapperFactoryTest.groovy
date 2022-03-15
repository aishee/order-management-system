package com.walmart.fms.infrastructure.integration.mapper

import com.walmart.fms.order.gateway.StoreEvents
import spock.lang.Specification

import static com.walmart.fms.mapper.StoreEventsMapperFactory.MappingFunction
import static com.walmart.fms.mapper.StoreEventsMapperFactory.getMapper

class EgressEventMapperFactoryTest extends Specification {


    def "If code is null throw illegal argument Exception"() {
        when:
        getMapper(null)
        then:
        thrown(IllegalArgumentException)
    }

    def "Get Gif mappers"() {
        given:
        MappingFunction mapper
        when:
        mapper = getMapper(mappercode)
        then:
        assert mapper != null
        assert mapper.code != null
        assert mapper.function != null
        assert mapper.code == mappercode
        where:
        mappercode                 | _
        StoreEvents.valueOf("PFO") | _
        StoreEvents.valueOf("UFO") | _
    }
}
