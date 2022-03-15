package com.walmart.common.infrastructure

import spock.lang.Specification

class ApiActionSpec extends Specification {

    def "When Uber patch cart, update item api is called UBER_CART_CIRCUIT_BREAKER is invoked"() {

        when:
        List<ApiAction> apiAction = [ApiAction.PATCH_CART, ApiAction.UPDATE_ITEM]

        then:
        apiAction.forEach({ action ->
            action.getCircuitBreakerName()
                    .equals(ApiAction.CircuitBreaker.UBER_CART_CIRCUIT_BREAKER.name()) })
    }

    def "When Uber get order, accept order, deny order api is called UBER_ORDER_UPDATE_CIRCUIT_BREAKER is invoked"() {

        when:
        List<ApiAction> apiAction = [ApiAction.GET_ORDER, ApiAction.ACCEPT_ORDER, ApiAction.DENY_ORDER]

        then:
        apiAction.forEach({ action ->
            (action.getCircuitBreakerName() == ApiAction.CircuitBreaker.UBER_ORDER_UPDATE_CIRCUIT_BREAKER.name())
        })
    }

    def "When Uber cancel, store, report api is called UBER_BATCH_CIRCUIT_BREAKER is invoked"() {

        when:
        List<ApiAction> apiAction = [ApiAction.CANCEL_ORDER, ApiAction.STORE_API, ApiAction.REPORT_API]

        then:
        apiAction.forEach({ action ->
            (action.getCircuitBreakerName() == ApiAction.CircuitBreaker.UBER_BATCH_CIRCUIT_BREAKER.name())
        })
    }

    def "When TAX, IRO and PYSIPYP api's are called their respective circuit breaker are invoked"() {

        when:
        ApiAction taxApi = ApiAction.TAX
        ApiAction pysipypApi = ApiAction.PYSIPYP
        ApiAction iroApi = ApiAction.IRO

        then:
        assert taxApi.getCircuitBreakerName() == ApiAction.CircuitBreaker.TAX.name()
        assert pysipypApi.getCircuitBreakerName() == ApiAction.CircuitBreaker.PYSIPYP.name()
        assert iroApi.getCircuitBreakerName() == ApiAction.CircuitBreaker.IRO.name()
    }
}
