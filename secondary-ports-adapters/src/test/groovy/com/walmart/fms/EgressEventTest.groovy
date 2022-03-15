package com.walmart.fms

import com.walmart.common.domain.event.processing.EgressEvent
import com.walmart.fms.infrastructure.integration.gateway.store.dto.orderdownload.PlaceFulfillmentOrderRequest
import com.walmart.fms.order.valueobject.events.FmsOrderValueObject
import io.strati.libs.google.gson.JsonObject
import io.strati.libs.google.gson.JsonParser
import io.strati.libs.google.gson.JsonSyntaxException
import spock.lang.Shared
import spock.lang.Specification

import static com.walmart.fms.EgressEventsAndMappers.*
import static com.walmart.fms.FmsMockOrderFactory.give_me_a_valid_market_place_order

class EgressEventTest extends Specification {

    public static final String name = "OrderDownload"
    public static final String desc = "Order confirmation event to GIF"
    public static final String message = "<xml?><body>dummy message</body>"
    @Shared
            stringToStringMapper = give_me_string_to_string_mapper()
    @Shared
            marketPlaceOrder = give_me_a_valid_market_place_order()

    def "Check mandatory parameters: domain,mapper function,domain model,endpoint,name"() {
        when:
        new EgressEvent.Builder<String, String>(eName, description)
                .producedFrom(domain)
                .toDestination(endpoint)
                .withModel(model)
                .withMapper(mapper)
                .asXml()
                .build()
        then:
        thrown(IllegalArgumentException)
        where:
        eName | description | endpoint  | mapper               | domain | model
        null  | null        | null      | null                 | null   | null
        name  | desc        | null      | stringToStringMapper | FMS    | marketPlaceOrder
        name  | desc        | END_POINT | null                 | FMS    | marketPlaceOrder
        name  | desc        | END_POINT | stringToStringMapper | null   | marketPlaceOrder
        name  | desc        | END_POINT | stringToStringMapper | FMS    | null
        null  | desc        | END_POINT | stringToStringMapper | FMS    | marketPlaceOrder

    }

    def "When the applyMapping() method is invoked on the egress event, the mapped object must be valid"() {
        given:
        EgressEvent event = new EgressEvent.Builder(name, desc)
                .producedFrom(FMS)
                .toDestination(END_POINT)
                .withModel(model)
                .withMapper(mapper)
                .asXml()
                .build()
        when:
        event.applyMapping()
        then:
        assert event.isMappingApplied()
        assert event.mappedObject.asType(classtocheck) != null
        where:
        mapper                                 | model            | classtocheck
        map_fms_order_to_FmsOrderValueObject() | marketPlaceOrder | FmsOrderValueObject.class
        give_me_string_to_string_mapper()      | marketPlaceOrder | String.class
        map_fms_order_to_PFO_request()         | marketPlaceOrder | PlaceFulfillmentOrderRequest.class
    }

    def "When createXmlMessage() is invoked, the mapped integration object is marshalled in to an xml string in the message attribute"() {
        given:
        EgressEvent event = new EgressEvent.Builder(name, desc)
                .producedFrom(FMS)
                .toDestination(END_POINT)
                .withModel(model)
                .withMapper(mapper)
                .asXml()
                .build()
        when:
        event.applyMapping()
        event.createXmlMessage()
        then:
        assert event.getMessage() != null
        assert event.getMessage().contains("<?xml")
        where:
        mapper                                 | model
        map_fms_order_to_FmsOrderValueObject() | marketPlaceOrder
        stringToStringMapper                   | marketPlaceOrder
        map_fms_order_to_PFO_request()         | marketPlaceOrder
    }

    def "When createJsonMessage() is invoked, the mapped integration object is marshalled in to an Json string in the message attribute"() {
        given:
        EgressEvent event = new EgressEvent.Builder(name, desc)
                .producedFrom(FMS)
                .toDestination(END_POINT)
                .withModel(model)
                .withMapper(mapper)
                .asJson()
                .build()
        when:
        event.applyMapping()
        event.createJsonMessage()
        then:
        assert event.getMessage() != null
        assertJson(event)
        where:
        mapper                                 | model
        map_fms_order_to_FmsOrderValueObject() | marketPlaceOrder
        map_fms_order_to_PFO_request()         | marketPlaceOrder
    }

    private void assertJson(EgressEvent event) {
        try {
            JsonObject message = new JsonParser().parse(event.getMessage())
        } catch (JsonSyntaxException exception) {
            assert false
        }
        assert true
    }

    def "test the invocations to markAsProduced() is updating the status of the EgressEvent to produced"() {
        when:
        EgressEvent event = new EgressEvent.Builder(name, desc)
                .producedFrom(FMS)
                .toDestination(END_POINT)
                .withModel(marketPlaceOrder)
                .withMapper(stringToStringMapper)
                .asXml()
                .build()
        event.markAsProduced()
        then:
        assert event.status == EgressEvent.EgressStatus.PRODUCED
    }

    def "test the invocations to markAsProduced() is updating the status of the EgressEvent to failed"() {
        when:
        EgressEvent event = new EgressEvent.Builder(name, desc)
                .producedFrom(FMS)
                .toDestination(END_POINT)
                .withModel(marketPlaceOrder)
                .withMapper(stringToStringMapper)
                .asXml()
                .build()
        event.markAsFailed()
        then:
        assert event.status == EgressEvent.EgressStatus.FAILED
    }

    def "test if an event is marked only for audit then the justAudit must be true"() {
        when:
        EgressEvent event = new EgressEvent.Builder(name, desc)
                .producedFrom(FMS)
                .toDestination(END_POINT)
                .withModel(marketPlaceOrder)
                .withMapper(stringToStringMapper)
                .justAudit()
                .asXml()
                .build()
        then:
        assert event.isJustAudit() == true
    }

    def "Test event creation with message"() {
        when:
        EgressEvent event = new EgressEvent.Builder(name, desc)
                .producedFrom(FMS)
                .toDestination(END_POINT)
                .withModel(marketPlaceOrder)
                .withMapper(stringToStringMapper)
                .withMessage(message)
                .asXml()
                .build()
        then:
        assert event.getMessage() != null
        assert event.getMessage() == message
    }

}
