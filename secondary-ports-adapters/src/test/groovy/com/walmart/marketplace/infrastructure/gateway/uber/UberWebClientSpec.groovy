package com.walmart.marketplace.infrastructure.gateway.uber

import com.fasterxml.jackson.databind.ObjectMapper
import com.walmart.common.infrastructure.config.Resilience4jConfig
import com.walmart.common.metrics.MetricService
import com.walmart.marketplace.infrastructure.gateway.uber.dto.response.UberOrder
import com.walmart.marketplace.infrastructure.gateway.uber.dto.response.UberStore
import com.walmart.marketplace.infrastructure.gateway.uber.report.dto.UberReportReq
import com.walmart.marketplace.order.domain.entity.UpdateItemInfo
import com.walmart.marketplace.order.domain.entity.type.Vendor
import com.walmart.marketplace.order.domain.uber.PatchCartInfo
import com.walmart.marketplace.order.domain.uber.ReportType
import com.walmart.oms.domain.error.exception.OMSBadRequestException
import com.walmart.oms.domain.error.exception.OMSThirdPartyException
import io.github.resilience4j.bulkhead.Bulkhead
import io.github.resilience4j.bulkhead.BulkheadRegistry
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry
import io.github.resilience4j.retry.RetryRegistry
import org.apache.commons.lang3.concurrent.BasicThreadFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.client.HttpServerErrorException
import org.springframework.web.reactive.function.client.ClientResponse
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import spock.lang.Specification

import java.lang.reflect.Field
import java.time.LocalDate
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.ThreadFactory
import java.util.function.Function

import static org.junit.jupiter.api.Assertions.assertNotNull

class UberWebClientSpec extends Specification {

    UberServiceConfiguration uberServiceConfiguration = mockUberServiceConfiguration()
    Resilience4jConfig resilience4jConfig = Mock()
    WebClient webClient = Mock()
    UberWebClient uberWebClient
    ObjectMapper jsonObjectMapper = Mock()
    ClientResponse clientResponse = Mock()
    MetricService metricService = Mock()
    ScheduledExecutorService scheduledExecutorService = Mock()

    String externalOrderId = UUID.randomUUID().toString()

    def setup() {

        uberWebClient = new UberWebClient(
                uberConfig: uberServiceConfiguration,
                webClient: webClient,
                jsonObjectMapper: jsonObjectMapper,
                circuitBreakerRegistry: CircuitBreakerRegistry.ofDefaults(),
                bulkheadRegistry: BulkheadRegistry.ofDefaults(),
                retryRegistry: RetryRegistry.ofDefaults(),
                metricService: metricService,
                retryScheduledExecutorService: scheduledExecutorService,
                resilience4jConfig: resilience4jConfig
        )
        Field executorServiceField =
                uberWebClient.getClass().getSuperclass()
                        .getSuperclass()
                        .getSuperclass()
                        .getDeclaredField("executorService")
        executorServiceField.setAccessible(true)
        executorServiceField.set(uberWebClient, mockExecutorService())
    }


    def "Uber WebClient Initialization Successfully"() {
        given:
        Bulkhead bulkhead = uberWebClient.bulkheadRegistry.bulkhead("UBER")

        when:
        uberWebClient.initialize()

        then:
        uberWebClient.webClient != null
        bulkhead != null
    }

    def "Logging Disabled for Initialization"() {
        given:
        !uberServiceConfiguration.isLoggingEnabled()

        when:
        uberWebClient.initialize()

        then:
        0 * uberWebClient.circuitBreakerRegistry.circuitBreaker("UBER")
        0 * uberWebClient.bulkheadRegistry.bulkhead("UBER")
        0 * uberWebClient.retryRegistry.retry("UBER")
    }


    def "When Get Order is Successful"() {
        given:
        webClient.get() >> mockWebClientFor_Valid_GetOrder()

        when:
        UberOrder validUberOrder = uberWebClient.getUberOrder("12345")

        then:
        validUberOrder.displayId == "ABCD"
    }

    def "When  Order is not existent for Get Order"() {
        given:
        webClient.get() >> mockWebClientFor_Invalid_GetOrder()
        when:
        uberWebClient.getUberOrder("12345")
        then:
        thrown OMSThirdPartyException
    }

    def "When get Order throws Bad Request Exception"() {
        given:
        webClient.get() >> mockWebClientFor_Invalid_GetOrder_OMSBadRequestException()

        when:
        uberWebClient.getUberOrder("12345")

        then:
        thrown OMSBadRequestException
    }

    def "When get Order returns null order"() {
        given:
        webClient.get() >> mockWebClientFor_Null_Order()

        when:
        uberWebClient.getUberOrder("12345")

        then:
        thrown OMSThirdPartyException
    }


    def "When  Order is not existent for Get Order InternalServerError"() {
        given:
        webClient.get() >> mockWebClientFor_Invalid_GetOrder_InternalServerError()

        when:
        uberWebClient.getUberOrder("12345")

        then:
        thrown OMSThirdPartyException
    }


    def "When Accept Order is Successful"() {
        given:
        webClient.post() >> mockWebClientFor_Valid_AcceptDenyOrder()

        when:
        boolean orderAccepted = uberWebClient.acceptUberOrder("12345", "accepted")

        then:
        orderAccepted
    }

    def "When Order is not existent for Accept Order"() {
        given:
        webClient.post() >> mockWebClientFor_Invalid_AcceptDenyCancelOrder()

        when:
        uberWebClient.acceptUberOrder("12345", "accepted")

        then:
        thrown OMSThirdPartyException
    }

    def "When Accept Order throws Bad Request Exception"() {
        given:
        webClient.post() >> mockWebClientFor_Invalid_AcceptDenyCancelOrder_OMSBadRequestException()

        when:
        uberWebClient.acceptUberOrder("12345", "accepted")

        then:
        thrown OMSBadRequestException
    }


    def "When Deny Order is Successful"() {
        given:
        webClient.post() >> mockWebClientFor_Valid_AcceptDenyOrder()

        when:
        boolean orderDenied = uberWebClient.denyUberOrder("12345", "failed to submit order", null, null)

        then:
        orderDenied
    }

    def "When Order is not existent for Deny Order"() {
        given:
        webClient.post() >> mockWebClientFor_Invalid_AcceptDenyCancelOrder()

        when:
        uberWebClient.denyUberOrder("12345", "failed to submit order", null, null)

        then:
        thrown OMSThirdPartyException
    }

    def "When Deny Order throws OMSBadRequestException"() {
        given:
        webClient.post() >> mockWebClientFor_Invalid_AcceptDenyCancelOrder_OMSBadRequestException()

        when:
        uberWebClient.denyUberOrder("12345", "failed to submit order", null, null)

        then:
        thrown OMSBadRequestException
    }

    def "When Cancel Order is Successful"() {
        given:
        webClient.post() >> mockWebClientFor_Valid_CancelOrder()

        when:
        boolean orderCancelled = uberWebClient.cancelUberOrder("12345", "OUT_OF_ITEMS", null)

        then:
        orderCancelled
    }

    def "When Order is not existent for Cancel Order"() {
        given:
        webClient.post() >> mockWebClientFor_Invalid_AcceptDenyCancelOrder()

        when:
        uberWebClient.cancelUberOrder("12345", "OUT_OF_ITEMS", null)

        then:
        thrown OMSThirdPartyException
    }

    def "When Cancel Order throws OMSBadRequestException"() {
        given:
        webClient.post() >> mockWebClientFor_Invalid_AcceptDenyCancelOrder_OMSBadRequestException()

        when:
        uberWebClient.cancelUberOrder("12345", "OUT_OF_ITEMS", null)

        then:
        thrown OMSBadRequestException
    }

    def "When Patch Cart for Nil Picks is Successful"() {
        given:
        List<String> instanceId = new ArrayList<>()
        instanceId.add("e2066983-7793-4017-ac06-74785bfeff15")
        instanceId.add("273e9df8-838e-476b-b899-b23795b55b6e")
        PatchCartInfo patchCartInfo = getPatchCartInfo(instanceId, null)
        webClient.patch() >> mockWebClientFor_Valid_PatchCart_UpdateItem()

        when:
        CompletableFuture<Boolean> patchCart = uberWebClient.patchCart(patchCartInfo)

        then:
        patchCart.get()
    }

    def "When Order is not existent for Patch Cart Nil Pick"() {
        given:
        List<String> instanceId = new ArrayList<>()
        instanceId.add("e2066983-7793-4017-ac06-74785bfeff15")
        instanceId.add("273e9df8-838e-476b-b899-b23795b55b6e")
        PatchCartInfo patchCartInfo = getPatchCartInfo(instanceId, null)
        webClient.patch() >> mockWebClientFor_Invalid_PatchCart_UpdateItem()

        when:
        CompletableFuture<Boolean> patchCartCF = uberWebClient.patchCart(patchCartInfo)

        then:
        assertNotNull(patchCartCF)
    }

    def "When Patch Cart throws OMSBadRequestException"() {
        given:
        List<String> instanceId = new ArrayList<>()
        instanceId.add("e2066983-7793-4017-ac06-74785bfeff15")
        instanceId.add("273e9df8-838e-476b-b899-b23795b55b6e")
        PatchCartInfo patchCartInfo = getPatchCartInfo(instanceId, null)
        webClient.patch() >> mockWebClientFor_Invalid_PatchCart_UpdateItem_OMSBadRequestException()

        when:
        CompletableFuture<Boolean> c = uberWebClient.invokeUberPatchCart(patchCartInfo)

        then:
        Thread.sleep(2000)
        c.isCompletedExceptionally()
    }

    def "When Patch Cart for Partial Pick is Successful"() {
        given:
        Map<String, Integer> instanceId = new HashMap<>()
        instanceId.put("e2066983-7793-4017-ac06-74785bfeff15", 1)
        PatchCartInfo patchCartInfo = getPatchCartInfo(null, instanceId)
        webClient.patch() >> mockWebClientFor_Valid_PatchCart_UpdateItem()

        when:
        CompletableFuture<Boolean> patchCart = uberWebClient.patchCart(patchCartInfo)

        then:
        patchCart.get()
    }

    def "When Order is not existent for Patch Cart Partial Pick"() {
        given:
        Map<String, Long> instanceId = new HashMap<>()
        instanceId.put("e2066983-7793-4017-ac06-74785bfeff15", 1)
        PatchCartInfo patchCartInfo = getPatchCartInfo(null, instanceId)
        webClient.patch() >> mockWebClientFor_Invalid_PatchCart_UpdateItem()

        when:
        CompletableFuture<Boolean> patchCart = uberWebClient.patchCart(patchCartInfo)

        then:
        assertNotNull(patchCart)
    }

    def "When Patch Cart for Nil Pick and Partial Pick is Successful"() {
        given:
        List<String> nilPickInstanceId = new ArrayList<>()
        nilPickInstanceId.add("273e9df8-838e-476b-b899-b23795b55b6e")
        Map<String, Integer> partialPickInstanceId = new HashMap<>()
        partialPickInstanceId.put("e2066983-7793-4017-ac06-74785bfeff15", 1)
        PatchCartInfo patchCartInfo = getPatchCartInfo(nilPickInstanceId, partialPickInstanceId)
        webClient.patch() >> mockWebClientFor_Valid_PatchCart_UpdateItem()

        when:
        CompletableFuture<Boolean> patchCart = uberWebClient.patchCart(patchCartInfo)

        then:
        patchCart.get()
    }

    def "When Update Item for out of stock items is Successful"() {
        given:
        String vendorStoreId = "a48c2238-5f8d-48ad-917b-b91e2d577cdc"
        List<String> itemIds = new ArrayList<>()
        itemIds.add("e2066983-7793-4017-ac06-74785bfeff15")
        itemIds.add("273e9df8-838e-476b-b899-b23795b55b6e")
        UpdateItemInfo updateItemInfo = getUpdateItemInfo(vendorStoreId, itemIds)
        webClient.post() >> mockWebClientFor_Valid_PatchCart_UpdateItem()

        when:
        CompletableFuture<Boolean> updateItem = uberWebClient.updateItem(updateItemInfo, vendorStoreId)

        then:
        updateItem.get()
    }

    def "When StoreID is not existent for Update Item"() {
        given:
        String vendorStoreId = "a48c2238-5f8d-48ad-917b-b91e2d577cdc"
        List<String> itemIds = new ArrayList<>()
        itemIds.add("e2066983-7793-4017-ac06-74785bfeff15")
        itemIds.add("273e9df8-838e-476b-b899-b23795b55b6e")
        UpdateItemInfo updateItemInfo = getUpdateItemInfo(vendorStoreId, itemIds)
        webClient.post() >> mockWebClientFor_Invalid_PatchCart_UpdateItem()

        when:
        CompletableFuture<Boolean> updateItem = uberWebClient.updateItem(updateItemInfo, vendorStoreId)

        then:
        assertNotNull(updateItem)
    }

    def "When StoreID is not existent for Update Item OMSBadRequestException"() {
        given:
        String vendorStoreId = "a48c2238-5f8d-48ad-917b-b91e2d577cdc"
        List<String> itemIds = new ArrayList<>()
        itemIds.add("e2066983-7793-4017-ac06-74785bfeff15")
        itemIds.add("273e9df8-838e-476b-b899-b23795b55b6e")
        UpdateItemInfo updateItemInfo = getUpdateItemInfo(vendorStoreId, itemIds)
        webClient.post() >> mockWebClientFor_Invalid_PatchCart_UpdateItem_OMSBadRequestException()

        when:
        CompletableFuture<Boolean> c = uberWebClient.invokeUberUpdateItem(updateItemInfo, vendorStoreId)

        then:
        Thread.sleep(2000)
        c.isCompletedExceptionally()
    }

    def "When Store Response Is Null"() {
        given:
        webClient.get() >> mockWebClientNullStoreResponse()

        when:
        uberWebClient.getUberStore()

        then:
        thrown(OMSThirdPartyException.class)
    }

    def "When Store Response Is Empty"() {
        given:
        webClient.get() >> mockWebClientEmptyStoreResponse()

        when:
        uberWebClient.getUberStore()

        then:
        thrown(OMSBadRequestException.class)
    }

    def "When Store Response Is Success"() {
        given:
        webClient.get() >> mockWebClientSuccessStoreResponse()

        when:
        UberStore uberStore = uberWebClient.getUberStore()

        then:
        uberStore != null
    }

    def "When Store Web Client Error"() {
        given:
        webClient.get() >> mockWebClientStoreApiError()

        when:
        uberWebClient.getUberStore()

        then:
        thrown(OMSThirdPartyException.class)
    }

    def "When Uber Report Response Is Empty"() {
        given:
        UberReportReq reportReq = createUberReportReq()
        webClient.post() >> mockWebClientEmptyReportResponse()

        when:
        uberWebClient.invokeUberReport(reportReq)

        then:
        thrown(OMSThirdPartyException.class)
    }

    def "When Uber Report Response Is Success"() {
        given:
        UberReportReq reportReq = createUberReportReq()
        webClient.post() >> mockWebClientSuccessReportResponse()

        when:
        String workFlowId = uberWebClient.invokeUberReport(reportReq)

        then:
        workFlowId != null
    }

    def "When Uber Report Web Client Error"() {
        given:
        UberReportReq reportReq = createUberReportReq()
        webClient.post() >> mockWebClientReportApiError()

        when:
        uberWebClient.invokeUberReport(reportReq)

        then:
        thrown(OMSThirdPartyException.class)
    }

    def "Custom Exception Filter Response for Bad Request Error"() {

        given:
        clientResponse.statusCode() >> HttpStatus.BAD_REQUEST
        clientResponse.bodyToMono(String.class) >> Mono.just("Order Not Found")

        when:
        Mono<ClientResponse> response = uberWebClient.getResponseProcessorForBadRequestException(clientResponse)

        then:
        StepVerifier.create(response)
                .expectErrorSatisfies(

                        { thr ->
                            thr instanceof Exception
                            thr.getMessage() == ("Error Response httpStatus:400 BAD_REQUEST responseBody :Order Not Found URI:https://api.uber.com")
                        })
                .verify()
    }

    def "Custom Exception Filter Response for Server Error"() {

        given:
        clientResponse.statusCode() >> HttpStatus.INTERNAL_SERVER_ERROR
        clientResponse.bodyToMono(String.class) >> Mono.just("Internal Server Error")

        when:
        Mono<ClientResponse> response = uberWebClient.getResponseProcessorForBadRequestException(clientResponse)

        then:
        StepVerifier.create(response)
                .expectErrorSatisfies(

                        { thr ->
                            thr instanceof Exception
                            thr.getMessage() == ("Error Response httpStatus:500 INTERNAL_SERVER_ERROR responseBody :Internal Server Error URI:https://api.uber.com")
                        })
                .verify()
    }

    UberOrder createSampleUberOrder() {
        return new UberOrder(
                id: "12345",
                displayId: "ABCD"
        )
    }

    private PatchCartInfo getPatchCartInfo(List<String> nilPicks, Map<String, Integer> partialPicks) {
        return PatchCartInfo.builder().vendorOrderId("12345")
                .vendorId(Vendor.UBEREATS)
                .nilPickInstanceIds(nilPicks)
                .partialPickInstanceIds(partialPicks).build()
    }

    private UpdateItemInfo getUpdateItemInfo(String vendorStoreId, List<String> outOfStockItemIds) {
        return UpdateItemInfo.builder()
                .vendorOrderId(externalOrderId)
                .vendorStoreId(vendorStoreId)
                .vendorId(Vendor.UBEREATS)
                .outOfStockItemIds(outOfStockItemIds)
                .suspendUntil(1620757800)
                .reason("OUT_OF_STOCK")
                .build()
    }

    private WebClient.RequestHeadersUriSpec mockWebClientFor_Valid_GetOrder() {
        Mock(WebClient.RequestHeadersUriSpec) {
            uri(_ as Function) >> Mock(WebClient.RequestHeadersSpec) {
                retrieve() >> Mock(WebClient.ResponseSpec) {
                    toEntity(_ as Class) >> {
                        return Mono.just(new ResponseEntity<UberOrder>(createSampleUberOrder(), HttpStatus.OK))
                    }
                }
            }
        }
    }

    private WebClient.RequestHeadersUriSpec mockWebClientFor_Null_Order() {
        Mock(WebClient.RequestHeadersUriSpec) {
            uri(_ as Function) >> Mock(WebClient.RequestHeadersSpec) {
                retrieve() >> Mock(WebClient.ResponseSpec) {
                    toEntity(_ as Class) >> {
                        return Mono.just(new ResponseEntity<UberOrder>(null, HttpStatus.OK))
                    }
                }
            }
        }
    }

    private WebClient.RequestHeadersUriSpec mockWebClientFor_Invalid_GetOrder() {
        Mock(WebClient.RequestHeadersUriSpec) {
            uri(_ as Function) >> Mock(WebClient.RequestHeadersSpec) {
                retrieve() >> Mock(WebClient.ResponseSpec) {
                    toEntity(_ as Class) >> {
                        return Mono.just(new OMSThirdPartyException("Order Not Found"))
                    }
                }
            }
        }
    }

    private WebClient.RequestHeadersUriSpec mockWebClientFor_Invalid_GetOrder_OMSBadRequestException() {
        Mock(WebClient.RequestHeadersUriSpec) {
            uri(_ as Function) >> Mock(WebClient.RequestHeadersSpec) {
                retrieve() >> Mock(WebClient.ResponseSpec) {
                    toEntity(_ as Class) >> {
                        return Mono.error(new OMSBadRequestException(null))
                    }
                }
            }
        }
    }

    private WebClient.RequestHeadersUriSpec mockWebClientFor_Invalid_GetOrder_InternalServerError() {
        Mock(WebClient.RequestHeadersUriSpec) {
            uri(_ as Function) >> Mock(WebClient.RequestHeadersSpec) {
                retrieve() >> Mock(WebClient.ResponseSpec) {
                    toEntity(_ as Class) >> {
                        return Mono.just(new HttpServerErrorException.InternalServerError())
                    }
                }
            }
        }
    }

    private WebClient.RequestHeadersUriSpec mockWebClientFor_Valid_AcceptDenyOrder() {
        Mock(WebClient.RequestBodyUriSpec) {
            uri(_) >> Mock(WebClient.RequestBodySpec) {
                body(_, _) >> Mock(WebClient.RequestHeadersSpec) {
                    retrieve() >> Mock(WebClient.ResponseSpec) {
                        toEntity(_ as Class) >> {
                            return Mono.just(new ResponseEntity<String>(HttpStatus.NO_CONTENT))
                        }
                    }
                }
            }
        }
    }

    private WebClient.RequestHeadersUriSpec mockWebClientFor_Valid_CancelOrder() {
        Mock(WebClient.RequestBodyUriSpec) {
            uri(_) >> Mock(WebClient.RequestBodySpec) {
                body(_, _) >> Mock(WebClient.RequestHeadersSpec) {
                    retrieve() >> Mock(WebClient.ResponseSpec) {
                        toEntity(_ as Class) >> {
                            return Mono.just(new ResponseEntity<String>(HttpStatus.OK))
                        }
                    }
                }
            }
        }
    }

    private WebClient.RequestHeadersUriSpec mockWebClientFor_Invalid_AcceptDenyCancelOrder() {
        Mock(WebClient.RequestBodyUriSpec) {
            uri(_) >> Mock(WebClient.RequestBodySpec) {
                body(_, _) >> Mock(WebClient.RequestHeadersSpec) {
                    retrieve() >> Mock(WebClient.ResponseSpec) {
                        toEntity(_ as Class) >> {
                            return Mono.error(new OMSThirdPartyException("Order Not Found"))
                        }
                    }
                }
            }
        }
    }

    private WebClient.RequestHeadersUriSpec mockWebClientFor_Invalid_AcceptDenyCancelOrder_OMSBadRequestException() {
        Mock(WebClient.RequestBodyUriSpec) {
            uri(_) >> Mock(WebClient.RequestBodySpec) {
                body(_, _) >> Mock(WebClient.RequestHeadersSpec) {
                    retrieve() >> Mock(WebClient.ResponseSpec) {
                        toEntity(_ as Class) >> {
                            return Mono.error(new OMSBadRequestException(null))
                        }
                    }
                }
            }
        }
    }

    private WebClient.RequestHeadersUriSpec mockWebClientFor_Valid_PatchCart_UpdateItem() {
        Mock(WebClient.RequestBodyUriSpec) {
            uri(_) >> Mock(WebClient.RequestBodySpec) {
                body(_, _) >> Mock(WebClient.RequestHeadersSpec) {
                    retrieve() >> Mock(WebClient.ResponseSpec) {
                        toEntity(_ as Class) >> {
                            return Mono.just(new ResponseEntity<String>(HttpStatus.NO_CONTENT))
                        }
                    }
                }
            }
        }
    }

    private WebClient.RequestHeadersUriSpec mockWebClientFor_Invalid_PatchCart_UpdateItem() {
        Mock(WebClient.RequestBodyUriSpec) {
            uri(_) >> Mock(WebClient.RequestBodySpec) {
                body(_, _) >> Mock(WebClient.RequestHeadersSpec) {
                    retrieve() >> Mock(WebClient.ResponseSpec) {
                        toEntity(_ as Class) >> {
                            return Mono.just(new OMSThirdPartyException(null))
                        }
                    }
                }
            }
        }
    }

    private WebClient.RequestHeadersUriSpec mockWebClientFor_Invalid_PatchCart_UpdateItem_OMSBadRequestException() {
        Mock(WebClient.RequestBodyUriSpec) {
            uri(_) >> Mock(WebClient.RequestBodySpec) {
                body(_, _) >> Mock(WebClient.RequestHeadersSpec) {
                    retrieve() >> Mock(WebClient.ResponseSpec) {
                        toEntity(_ as Class) >> {
                            String errorMessage = "Sample message"
                            return Mono.error(new OMSBadRequestException(errorMessage))
                        }
                    }
                }
            }
        }
    }

    private WebClient.RequestHeadersUriSpec mockWebClientNullStoreResponse() {
        Mock(WebClient.RequestHeadersUriSpec) {
            uri(_) >> Mock(WebClient.RequestHeadersSpec) {
                retrieve() >> Mock(WebClient.ResponseSpec) {
                    toEntity(_ as Class) >> {
                        return Mono.just(new ResponseEntity<UberStore>(null, null, HttpStatus.OK))
                    }
                }
            }
        }
    }

    private WebClient.RequestHeadersUriSpec mockWebClientEmptyStoreResponse() {
        Mock(WebClient.RequestHeadersUriSpec) {
            uri(_) >> Mock(WebClient.RequestHeadersSpec) {
                retrieve() >> Mock(WebClient.ResponseSpec) {
                    toEntity(_ as Class) >> {
                        return Mono.just(new ResponseEntity<UberStore>(createEmptyUberStore(), null, HttpStatus.OK))
                    }
                }
            }
        }
    }

    private WebClient.RequestHeadersUriSpec mockWebClientSuccessStoreResponse() {
        Mock(WebClient.RequestHeadersUriSpec) {
            uri(_) >> Mock(WebClient.RequestHeadersSpec) {
                retrieve() >> Mock(WebClient.ResponseSpec) {
                    toEntity(_ as Class) >> {
                        return Mono.just(new ResponseEntity<UberStore>(createSampleUberStore(), null, HttpStatus.OK))
                    }
                }
            }
        }
    }

    private WebClient.RequestHeadersUriSpec mockWebClientStoreApiError() {
        Mock(WebClient.RequestHeadersUriSpec) {
            uri(_) >> Mock(WebClient.RequestHeadersSpec) {
                retrieve() >> Mock(WebClient.ResponseSpec) {
                    toEntity(_ as Class) >> {
                        String errorMessage = "Uber Web Client Custom Exception Filter was not able to handle this exception for report"
                        return Mono.error(new OMSThirdPartyException(errorMessage))
                    }
                }
            }
        }
    }

    private WebClient.RequestHeadersUriSpec mockWebClientSuccessReportResponse() {
        Mock(WebClient.RequestBodyUriSpec) {
            uri(_) >> Mock(WebClient.RequestBodySpec) {
                body(_, _) >> Mock(WebClient.RequestHeadersSpec) {
                    retrieve() >> Mock(WebClient.ResponseSpec) {
                        toEntity(_ as Class) >> {
                            return Mono.just(new ResponseEntity<String>("test", null, HttpStatus.OK))
                        }
                    }
                }
            }
        }
    }

    private WebClient.RequestHeadersUriSpec mockWebClientEmptyReportResponse() {
        Mock(WebClient.RequestBodyUriSpec) {
            uri(_) >> Mock(WebClient.RequestBodySpec) {
                body(_, _) >> Mock(WebClient.RequestHeadersSpec) {
                    retrieve() >> Mock(WebClient.ResponseSpec) {
                        toEntity(_ as Class) >> {
                            return Mono.just(new ResponseEntity<String>(null, null, HttpStatus.OK))
                        }
                    }
                }
            }
        }
    }

    private WebClient.RequestHeadersUriSpec mockWebClientReportApiError() {
        Mock(WebClient.RequestBodyUriSpec) {
            uri(_) >> Mock(WebClient.RequestBodySpec) {
                body(_, _) >> Mock(WebClient.RequestHeadersSpec) {
                    retrieve() >> Mock(WebClient.ResponseSpec) {
                        toEntity(_ as Class) >> {
                            String errorMessage = "Uber Web Client Custom Exception Filter was not able to handle this exception for store request"
                            return Mono.error(new OMSThirdPartyException(errorMessage))
                        }
                    }
                }
            }
        }
    }

    UberStore createSampleUberStore() {
        return new UberStore("test",
                Arrays.asList(new UberStore.Store("test",
                        "a48c2238-5f8d-48ad-917b-b91e2d577cdc",
                        "1234"),
                        new UberStore.Store("test",
                                "73fcf766-07b3-4640-93aa-1d8de5faac08",
                                "1234"))
        )
    }

    UberStore createEmptyUberStore() {
        return new UberStore("test", Arrays.asList())
    }

    UberReportReq createUberReportReq() {
        List<String> storeUUIDs = Arrays.asList("a48c2238-5f8d-48ad-917b-b91e2d577cdc",
                "73fcf766-07b3-4640-93aa-1d8de5faac08")
        return UberReportReq.builder()
                .endDate(LocalDate.now().minusDays(2))
                .startDate(LocalDate.now().minusDays(10))
                .storeUUIDs(storeUUIDs)
                .reportType(ReportType.DOWNTIME_REPORT)
                .build()
    }

    private UberServiceConfiguration mockUberServiceConfiguration() {
        UberServiceConfiguration uberConfig = new UberServiceConfiguration().builder()
                .clientId("uber")
                .scopes(Arrays.asList("eats.order,eats.store,eats.store.orders.read,eats.store.orders.cancel,eats.report"))
                .clientId("DUMMY").clientSecret("DUMMY")
                .accessTokenUri("https://login.uber.com/oauth/v2/token")
                .connTimeout(1000).readTimeout(1000)
                .loggingEnabled(true)
                .uberAcceptOrderUri("/v1/eats/orders/{uber_order_id}/accept_pos_order")
                .uberCancelOrderUri("/v1/eats/orders/{uber_order_id}/cancel")
                .uberDenyOrderUri("/{uber_order_id}/deny_pos_order")
                .uberGetOrderUri("/v2/eats/order/{uber_order_id}")
                .uberPatchCartUri("/v2/eats/orders/{uber_order_id}/cart")
                .uberUpdateItemUri("/v2/eats/stores/{store_id}/menus/items/{item_id}")
                .uberBaseUri("https://api.uber.com")
                .maxConnectionCount(50)
                .pendingAcquireTimeoutMs(30000)
                .idleTimeoutMs(30000)
                .oauth2ClientRegistrationId("uber")
                .uberReportUri("/v1/eats/report")
                .uberStoreUri("/v1/eats/stores")
                .threadPoolSize(10)
                .build()

        return uberConfig
    }

    private static ExecutorService mockExecutorService() {
        return Executors.newFixedThreadPool(10, mockThreadFactory())
    }

    private static ThreadFactory mockThreadFactory() {
        return new BasicThreadFactory.Builder().namingPattern("IRO-thread-factory-%d").build()
    }
}
