package com.walmart.common.domain

import spock.lang.Specification

class BaseEntityTest extends Specification {
    BaseEntity baseEntity

    def "Test Getter for All fields"() {
        when:
        baseEntity = new BaseEntity()
        then:
        baseEntity.getId() == null
        baseEntity.getVersion() == null
        baseEntity.getModifiedDate() == null
        baseEntity.getCreatedDate() == null
    }

    def "Test parameterized constructor for BaseEntity"() {
        when:
        baseEntity = new BaseEntity("id")
        then:
        baseEntity.getId() == "id"
    }
}
