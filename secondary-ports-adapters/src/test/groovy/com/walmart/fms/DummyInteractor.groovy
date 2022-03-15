package com.walmart.fms

import com.walmart.common.domain.BaseEntity
import com.walmart.common.domain.event.processing.EgressEvent
import com.walmart.common.domain.event.processing.EventResponse
import com.walmart.common.domain.event.processing.Interactor
import com.walmart.fms.infrastructure.integration.gateway.store.dto.orderdownload.PlaceFulfillmentOrderRequest
import com.walmart.fms.order.aggregateroot.FmsOrder
import com.walmart.fms.order.valueobject.events.FmsOrderValueObject

/**
 * A default implementation of {@link Interactor} used for unit testing.
 */

class DummyInteractor {
    static class NoAckInteractor  <T extends BaseEntity, R, W>implements Interactor <T , R, W> {
        @Override
       EventResponse<W> call(EgressEvent<T, R> event) {
            printf("Event received with id={}, name={}, destination={}, payload={}, domain={}",
                    event.getDomainModelId(),
                    event.getName(),
                    event.getDestination(),
                    event.getMessage(),
                    event.getDomain());
            return new EventResponse();
        }
    }

    static class ErrorInteractor <T extends BaseEntity, R, W> implements Interactor <T , R, W> {
        @Override
         EventResponse<W> call(EgressEvent<T, R> event) {
            throw new IllegalStateException("dummy throw");
        }
    }

    static class ACKInteractor <T extends BaseEntity, R, W> implements Interactor <T , R, W> {
        @Override
        <T, R, W> EventResponse<W> call(EgressEvent<T, R> event) {
            return new EventResponse(event, "Hello")
        }
    }

    static class ACKInteractorWithFmsOrder  <T extends BaseEntity, R, W> implements Interactor <T , R, W> {
        @Override
        EventResponse<W> call(EgressEvent<T, R> event) {
            FmsOrderValueObject fmsOrderValueObject = new FmsOrderValueObject()
            EgressEvent<FmsOrder, PlaceFulfillmentOrderRequest> _event = event as EgressEvent<FmsOrder, PlaceFulfillmentOrderRequest>
            fmsOrderValueObject.sourceOrderId = _event.getModel().sourceOrderId
            EventResponse<FmsOrderValueObject> response = new EventResponse(event: event, response: fmsOrderValueObject)
            return response
        }
    }


}
