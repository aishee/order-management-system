package com.walmart.common.domain.event.processing


import com.walmart.common.domain.BaseEntity
import spock.lang.Specification

import java.util.function.Function

class EgressEventTest extends Specification {
    EgressEvent egressEvent
    String name = "name"
    String description = "description"
    String msg = "msg"
    String source = "source"
    String destination = "destination"

    def setup() {
        egressEvent = new EgressEvent()
    }

    def "Test Copy Method"() {
        given:
        EgressEvent egressEvent1 = new EgressEvent()
        when:
        egressEvent.copy(egressEvent1)
        then:
        noExceptionThrown()
    }

    def "Test EgressEvent Builder"() {
        given:
        String name = "name"
        String description = "description"
        String msg = "msg"
        String source = "source"
        String destination = "destination"
        BaseEntity entity = new BaseEntity()
        EgressEvent.Builder builder = new EgressEvent.Builder(name, description)
        builder = builder.withMessage(msg).withRetries(3).producedFrom(source)
                .justAudit().toDestination(destination).withModel(entity)

        when:
        EgressEvent egressEvent1 = new EgressEvent(builder)
        then:
        egressEvent1.getMessage() != null
        !egressEvent1.isMappingApplied()
    }

    def "Test EgressEvent Validate"() {
        given:
        BaseEntity entity = Mock()
        EgressEvent.Builder builder = new EgressEvent.Builder(name, description)


        when:
        EgressEvent egressEvent1 = builder.withMessage(msg).withRetries(3).producedFrom(source)
                .justAudit().toDestination(destination).withModel(entity).build()
        then:
        2 * entity.getId() >> 5
        egressEvent1.getMessage() != null
    }

    def "Test EgressEvent Validate with Null Message"() {
        given:
        BaseEntity entity = Mock()
        EgressEvent.Builder builder = new EgressEvent.Builder(name, description)
        Function mappingFunction = Mock()


        when:
        EgressEvent egressEvent1 = builder.withMessage(null).withMapper(mappingFunction).withRetries(3).producedFrom(source)
                .justAudit().toDestination(destination).withModel(entity).asJson().build()
        then:
        2 * entity.getId() >> 5
        egressEvent1.getMessage() == null
    }

    def "Test EgressEvent Build asXML"() {
        given:
        BaseEntity entity = Mock()
        EgressEvent.Builder builder = new EgressEvent.Builder(name, description)
        Function mappingFunction = Mock()


        when:
        EgressEvent egressEvent1 = builder.withMessage(null).withMapper(mappingFunction).withRetries(3).producedFrom(source)
                .justAudit().toDestination(destination).withModel(entity).asXml().build()
        then:
        2 * entity.getId() >> 5
        egressEvent1.getMessage() == null
        noExceptionThrown()
    }

    def "Test EgressEvent makeLiteCopy"() {
        given:
        EgressEvent.Builder builder = new EgressEvent.Builder(name, description)
        EgressEvent egressEvent1 = new EgressEvent(builder)
        when:
        EgressEvent copyEvent = egressEvent1.makeLiteCopy()
        then:
        copyEvent.getName() == name
        copyEvent.getDescription() == description
    }

    def "Test CreateXMLMessage"() {
        given:
        BaseEntity entity = Mock()
        EgressEvent.Builder builder = new EgressEvent.Builder(name, description)
        Function mappingFunction = new Function() {
            @Override
            Object apply(Object o) {
                return o
            }
        }
        builder = builder.withModel(entity).withMapper(mappingFunction)
        EgressEvent egressEvent1 = new EgressEvent(builder)
        egressEvent1.applyMapping()

        when:
        egressEvent1.createXmlMessage()
        then:
        noExceptionThrown()
    }

    def "Test CreateJsonMessage"() {
        given:
        BaseEntity entity = Mock()
        EgressEvent.Builder builder = new EgressEvent.Builder(name, description)
        Function mappingFunction = new Function() {
            @Override
            Object apply(Object o) {
                return o
            }
        }
        builder = builder.withModel(entity).withMapper(mappingFunction)
        EgressEvent egressEvent1 = new EgressEvent(builder)
        egressEvent1.applyMapping()

        when:
        egressEvent1.createJsonMessage()
        then:
        noExceptionThrown()
    }

    def 'Test All Getter for EgressEvent'() {
        given:
        BaseEntity entity = Mock()
        EgressEvent.Builder builder = new EgressEvent.Builder(name, description)
        Function mappingFunction = Mock()

        when:
        EgressEvent egressEvent1 = builder.withMessage(msg).withMapper(mappingFunction).withRetries(3).producedFrom(source)
                .justAudit().toDestination(destination).withModel(entity).asJson().build()
        then:
        2 * entity.getId() >> 5
        egressEvent1.getMessage() == msg
        egressEvent1.getDescription() == description
        egressEvent1.getName() == name
        egressEvent1.getDestination() == destination
        egressEvent1.getDomainModelId() == "5"
        egressEvent1.getDomain() != null
        egressEvent1.getMappingFunction() == mappingFunction
        egressEvent1.getModel() == entity
        egressEvent1.getFormat() == EgressEvent.MessageFormat.JSON
        egressEvent1.getFormat() == EgressEvent.MessageFormat.valueOf("JSON")
        egressEvent1.getStatus() == EgressEvent.EgressStatus.INITIAL
        egressEvent1.isJustAudit()
    }

    def 'Test Mark EgressStatus'() {
        given:
        BaseEntity entity = Mock()
        EgressEvent.Builder builder = new EgressEvent.Builder(name, description)
        Function mappingFunction = Mock()
        int retry = 3
        builder = builder.withModel(entity).withMapper(mappingFunction)
                .withRetries(retry).withMessage(msg)
        EgressEvent egressEvent1 = new EgressEvent(builder)

        when:
        egressEvent1.markAsProduced()
        then:
        egressEvent1.getStatus() == EgressEvent.EgressStatus.PRODUCED

        when:
        egressEvent1.markAsError()
        then:
        egressEvent1.markedAsError()

        when:
        egressEvent1.markAsFailed()
        then:
        egressEvent1.isFailed()

        when:
        egressEvent1.markAsInitial()
        then:
        egressEvent1.getStatus() == EgressEvent.EgressStatus.INITIAL

        when:
        egressEvent1.markAsReadyToPublish()
        then:
        egressEvent1.getStatus() == EgressEvent.EgressStatus.READY_TO_PUBLISH
        egressEvent1.getStatus() == EgressEvent.EgressStatus.valueOf("READY_TO_PUBLISH")

        when:
        egressEvent1.tryAgain()
        then:
        egressEvent1.getRetries() == retry + 1

        when:
        boolean result = egressEvent1.isMappingApplied()
        then:
        !result
    }
}
