package com.walmart.fms

import com.walmart.common.domain.event.processing.EgressEvent
import com.walmart.fms.infrastructure.integration.gateway.store.GifStoreGateway
import com.walmart.fms.infrastructure.integration.gateway.store.dto.forceordercancellation.UpdateFulfillmentOrderRequest
import com.walmart.fms.infrastructure.integration.gateway.store.dto.orderdownload.PlaceFulfillmentOrderRequest
import com.walmart.fms.mapper.FmsOrderToPFORequestMapper
import com.walmart.fms.mapper.FmsOrderToUFORequestMapper
import com.walmart.fms.order.aggregateroot.FmsOrder
import com.walmart.fms.order.gateway.StoreEvents
import com.walmart.fms.order.valueobject.events.FmsOrderValueObject
import com.walmart.fms.order.valueobject.mappers.FMSOrderToFmsOrderValueObjectMapper

import java.util.function.Function

import static com.walmart.common.domain.event.processing.EgressEvent.EgressStatus.ERROR
import static com.walmart.common.domain.event.processing.EgressEvent.EgressStatus.FAILED
import static com.walmart.common.domain.event.processing.EgressEvent.EgressStatus.PRODUCED
import static com.walmart.common.domain.event.processing.EgressEvent.EgressStatus.READY_TO_PUBLISH
import static com.walmart.fms.FmsMockOrderFactory.give_me_a_valid_market_place_order

class EgressEventsAndMappers {
    public static final String END_POINT = "direct.pfo.topic"
    public static final String FMS = "FMS"

    static Function<String, String> give_me_string_to_string_mapper() {
        return { s -> "'" + s + "'" }
    }

    static Function<FmsOrderValueObject, FmsOrder> map_fms_order_to_FmsOrderValueObject() {
        return { f -> FMSOrderToFmsOrderValueObjectMapper.INSTANCE.convertFmsOrderToFmsOrderValueObject(f) }
    }

    static Function<FmsOrder, PlaceFulfillmentOrderRequest> map_fms_order_to_PFO_request() {
        return { f -> FmsOrderToPFORequestMapper.map(f) }
    }

    static Function<FmsOrder, UpdateFulfillmentOrderRequest> map_fms_order_to_UFO_request() {
        return { f -> FmsOrderToUFORequestMapper.map(f) }
    }

    static EgressEvent<FmsOrder, PlaceFulfillmentOrderRequest> an_egress_event_which_throws_NPE_during_applyMapping() {
        return new EgressEvent.Builder<FmsOrder, PlaceFulfillmentOrderRequest>(StoreEvents.UFO.name(), GifStoreGateway.ORDER_CANCELLATION_MESSAGE_TO_STORE)
                .producedFrom(FMS)
                .toDestination(END_POINT)
                .withModel(give_me_a_valid_market_place_order())
                .withMapper(mapper())
                .asXml()
                .build()
    }

    private static Function<FmsOrder, PlaceFulfillmentOrderRequest> mapper() {
        return { f -> TestMapperThrowingError.map(f) }
    }

    static EgressEvent<FmsOrder, PlaceFulfillmentOrderRequest> event_fms_order_pfo() {
        return new EgressEvent.Builder<FmsOrder, PlaceFulfillmentOrderRequest>(StoreEvents.PFO.name(), GifStoreGateway.ORDER_CONFIRMATION_MESSAGE_TO_STORE)
                .producedFrom(FMS)
                .toDestination(END_POINT)
                .withModel(give_me_a_valid_market_place_order())
                .withMapper(map_fms_order_to_PFO_request())
                .asXml()
                .build()
    }

    static EgressEvent<FmsOrder, PlaceFulfillmentOrderRequest> initial_event_retries_0(String orderId) {
        return new EgressEvent.Builder<FmsOrder, PlaceFulfillmentOrderRequest>(StoreEvents.PFO.name(), GifStoreGateway.ORDER_CONFIRMATION_MESSAGE_TO_STORE)
                .producedFrom(FMS)
                .toDestination(END_POINT)
                .withModel(give_me_a_valid_market_place_order())
                .withMapper(map_fms_order_to_PFO_request())
                .asXml()
                .build()
    }

    static EgressEvent<String, String> an_event_with(String orderId, int tries, EgressEvent.EgressStatus status) {
        EgressEvent<String, String> event = new EgressEvent.Builder<String, String>(StoreEvents.PFO.name(), GifStoreGateway.ORDER_CONFIRMATION_MESSAGE_TO_STORE)
                .producedFrom(FMS)
                .toDestination(END_POINT)
                .withModel(give_me_a_valid_market_place_order())
                .withMapper(give_me_string_to_string_mapper())
                .withRetries(tries)
                .asXml()
                .build()
        switch (status) {
            case PRODUCED: event.markAsProduced()
                break
            case FAILED: event.markAsFailed()
                break
            case READY_TO_PUBLISH:
                event.applyMapping()
                event.createXmlMessage()
                event.markAsReadyToPublish()
                break
            case ERROR: event.markAsError()
                break
        }
        return event
    }

    static EgressEvent<String, String> an_event_with_audit_with(String orderId, int tries, EgressEvent.EgressStatus status) {
        EgressEvent<String, String> event = new EgressEvent.Builder<String, String>(StoreEvents.PFO.name(), GifStoreGateway.ORDER_CONFIRMATION_MESSAGE_TO_STORE)
                .producedFrom(FMS)
                .toDestination(END_POINT)
                .withModel(give_me_a_valid_market_place_order())
                .withMapper(give_me_string_to_string_mapper())
                .withRetries(tries)
                .justAudit()
                .asXml()
                .build()
        switch (status) {
            case PRODUCED: event.markAsProduced()
                break
            case FAILED: event.markAsFailed()
                break
            case READY_TO_PUBLISH:
                event.applyMapping()
                event.createXmlMessage()
                event.markAsReadyToPublish()
                break
            case ERROR: event.markAsError()
                break
        }
        return event
    }

    static EgressEvent<FmsOrder, PlaceFulfillmentOrderRequest> an_egress_event_with_fms_order_and_ufo_request() {
        return new EgressEvent.Builder<FmsOrder, PlaceFulfillmentOrderRequest>(StoreEvents.UFO.name(), GifStoreGateway.ORDER_CANCELLATION_MESSAGE_TO_STORE)
                .producedFrom(FMS)
                .toDestination(END_POINT)
                .withModel(give_me_a_valid_market_place_order())
                .asXml()
                .withMapper(map_fms_order_to_UFO_request())
                .build()
    }

    static EgressEvent<FmsOrder, PlaceFulfillmentOrderRequest> an_egress_event_with_failed_during_publish() {
        return new EgressEvent.Builder<FmsOrder, PlaceFulfillmentOrderRequest>(StoreEvents.UFO.name(), GifStoreGateway.ORDER_CANCELLATION_MESSAGE_TO_STORE)
                .producedFrom(FMS)
                .toDestination(ERROR_ENDPOINT)
                .withModel(give_me_a_valid_market_place_order())
                .withMapper(map_fms_order_to_UFO_request())
                .asXml()
                .build()
    }

    static String ERROR_ENDPOINT = "error.topic"
}
