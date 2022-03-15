package com.walmart.oms.infrastructure.gateway.price

import com.fasterxml.jackson.databind.ObjectMapper
import com.walmart.common.domain.type.FulfillmentType
import com.walmart.common.metrics.MetricService
import com.walmart.oms.domain.error.exception.OMSBadRequestException
import com.walmart.oms.domain.error.exception.OMSThirdPartyException
import com.walmart.oms.domain.event.messages.OrderCancelledDomainEventMessage
import com.walmart.oms.infrastructure.constants.PYSIPYPConstants
import com.walmart.oms.infrastructure.converter.OmsOrderToPysipypOrderInfoMapper
import com.walmart.oms.infrastructure.gateway.price.dto.*
import com.walmart.oms.order.aggregateroot.OmsOrder
import com.walmart.oms.order.valueobject.OrderPriceInfo
import com.walmart.tax.calculator.dto.Tax
import io.github.resilience4j.bulkhead.Bulkhead
import io.github.resilience4j.bulkhead.BulkheadRegistry
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry
import io.github.resilience4j.retry.RetryRegistry
import org.apache.commons.lang3.concurrent.BasicThreadFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono
import spock.lang.Specification

import java.lang.reflect.Field
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.ThreadFactory

class PYSIPYPWebClientSpec extends Specification {

    PYSIPYPServiceConfiguration pysipypServiceConfiguration = mockPYSIPYPServiceConfiguration()

    WebClient webClient = Mock()
    MetricService metricService = Mock()
    PYSIPYPWebClient pysipypWebClient
    ObjectMapper jsonObjectMapper = Mock()
    OmsOrderToPysipypOrderInfoMapper omsOrderToPYSIPYPOrderInfoMapper = Mock()

    def setup() {

        pysipypWebClient = new PYSIPYPWebClient(
                pysipypServiceConfiguration: pysipypServiceConfiguration,
                webClient: webClient,
                jsonObjectMapper: jsonObjectMapper,
                circuitBreakerRegistry: CircuitBreakerRegistry.ofDefaults(),
                bulkheadRegistry: BulkheadRegistry.ofDefaults(),
                retryRegistry: RetryRegistry.ofDefaults(),
                metricService: metricService,
                omsOrderToPysipypOrderInfoMapper: omsOrderToPYSIPYPOrderInfoMapper,
        )
        Field executorServiceField = pysipypWebClient.getClass().getSuperclass().getSuperclass().getDeclaredField("executorService")
        executorServiceField.setAccessible(true)
        executorServiceField.set(pysipypWebClient, mockExecutorService())

    }


    def "PYSIPYP WebClient Initialization Successfully"() {
        given:
        Bulkhead bulkhead = pysipypWebClient.bulkheadRegistry.bulkhead("PYSIPYP")

        when:
        pysipypWebClient.initialize()

        then:
        pysipypWebClient.webClient != null
        bulkhead != null
    }

    def "When Get price data is Successful"() {
        given:
        webClient.post() >> mockWebClientForValidGetPriceData()
        OmsOrder omsOrder = OmsOrder.builder()
                .orderState("PICK_COMPLETE")
                .id(UUID.randomUUID().toString())
                .pickupLocationId("5755")
                .storeId("5755")
                .authStatus("AUTHENTICATED")
                .sourceOrderId("e2066983-7793-4017-ac06-74785bfeff15")
                .storeOrderId("71615242412")
                .deliveryDate(new Date())
                .fulfillmentType(FulfillmentType.INSTORE_PICKUP)
                .priceInfo(OrderPriceInfo.builder()
                        .carrierBagCharge(0.4)
                        .orderTotal(9.4)
                        .build())
                .build()
        Map<String, Tax> taxInfoMap = new HashMap<>()
        omsOrderToPYSIPYPOrderInfoMapper.buildPysipypRequestforMarketPlaceOrder(omsOrder, taxInfoMap) >> mockPYSIPYPRequest()


        when:

        OrderInformation pysipypResponse = pysipypWebClient.getPriceData(omsOrder, taxInfoMap)

        then:
        pysipypResponse != null
        pysipypResponse.consumerId == "GIF"
    }

    def "When reverse sale is Successful"() {
        given:
        OrderCancelledDomainEventMessage orderCancelledDomainEventMessage =
                mockValidCancelledEventMessage()
        webClient.post() >> mockWebClientForValidGetPriceData()
        omsOrderToPYSIPYPOrderInfoMapper.buildReverseSaleRequest(orderCancelledDomainEventMessage) >> mockPYSIPYPRequest()

        when:
        OrderInformation pysipypResponse = pysipypWebClient.reverseSale(orderCancelledDomainEventMessage)

        then:
        pysipypResponse != null
        pysipypResponse.consumerId == "GIF"
    }

    def "Test RecordSale for when valid OmsOrder is provided and we get a error response from pricing"() {
        given:
        webClient.post() >> mockWebClientForRecordSaleErrorResponse()
        OmsOrder omsOrder = OmsOrder.builder()
                .storeOrderId("32323233")
                .storeId("4401")
                .deliveryDate(new Date())
                .sourceOrderId(UUID.randomUUID().toString())
                .build()

        Map<String, Tax> taxInfoMap = new HashMap<>()
        omsOrderToPYSIPYPOrderInfoMapper.buildPysipypRequestforMarketPlaceOrder(omsOrder, taxInfoMap) >> mockPYSIPYPRequest()

        when:
        pysipypWebClient.getPriceData(omsOrder, taxInfoMap)

        then:
        thrown(OMSThirdPartyException.class)
    }

    def "Test RecordSale for when valid OmsOrder is provided and we get an empty response from pricing"() {
        given:
        webClient.post() >> mockWebClientForRecordSaleEmptyResponse()
        OmsOrder omsOrder = OmsOrder.builder()
                .storeOrderId("32323233")
                .storeId("4401")
                .deliveryDate(new Date())
                .sourceOrderId(UUID.randomUUID().toString())
                .build()

        Map<String, Tax> taxInfoMap = new HashMap<>()
        omsOrderToPYSIPYPOrderInfoMapper.buildPysipypRequestforMarketPlaceOrder(omsOrder, taxInfoMap) >> mockPYSIPYPRequest()

        when:
        pysipypWebClient.getPriceData(omsOrder, taxInfoMap)

        then:
        thrown(OMSThirdPartyException.class)
    }

    def "Test Reversesale for when valid OmsOrder is provided and we get a error response from pricing"() {
        given:
        webClient.post() >> mockWebClientForReverseSaleErrorResponse()
        OrderCancelledDomainEventMessage orderCancelledDomainEventMessage =
                mockValidCancelledEventMessage()
        omsOrderToPYSIPYPOrderInfoMapper.buildReverseSaleRequest(orderCancelledDomainEventMessage) >> mockPYSIPYPRequest()

        when:
        pysipypWebClient.reverseSale(orderCancelledDomainEventMessage)

        then:
        thrown(OMSThirdPartyException.class)
    }

    def "Test Reversesale for when valid OmsOrder is provided and we get a empty response from pricing"() {
        given:
        webClient.post() >> mockWebClientForReverseSaleEmptyResponse()
        OrderCancelledDomainEventMessage orderCancelledDomainEventMessage =
                mockValidCancelledEventMessage()
        omsOrderToPYSIPYPOrderInfoMapper.buildReverseSaleRequest(orderCancelledDomainEventMessage) >> mockPYSIPYPRequest()

        when:
        pysipypWebClient.reverseSale(orderCancelledDomainEventMessage)

        then:
        thrown(OMSThirdPartyException.class)

    }

    def "Timeout from PYSIPYP reverseSale API"() {
        given:
        webClient.post() >> mockWebClientTimeoutError()
        OrderCancelledDomainEventMessage orderCancelledDomainEventMessage =
                mockValidCancelledEventMessage()
        omsOrderToPYSIPYPOrderInfoMapper.buildReverseSaleRequest(orderCancelledDomainEventMessage) >> mockPYSIPYPRequest()

        when:
        pysipypWebClient.reverseSale(orderCancelledDomainEventMessage)
        then:
        thrown(OMSThirdPartyException.class)
    }

    def "Timeout from PYSIPYP record Sale API"() {
        given:
        webClient.post() >> mockWebClientTimeoutError()
        OmsOrder omsOrder = OmsOrder.builder()
                .storeOrderId("32323233")
                .storeId("4401")
                .deliveryDate(new Date())
                .sourceOrderId(UUID.randomUUID().toString())
                .build()

        omsOrderToPYSIPYPOrderInfoMapper.buildPysipypRequestforMarketPlaceOrder(_, _) >> mockPYSIPYPRequest()
        Map<String, Tax> taxInfoMap = new HashMap<>()

        when:
        pysipypWebClient.getPriceData(omsOrder, taxInfoMap)
        then:
        thrown(OMSThirdPartyException.class)
    }

    def "Bad Request in response from PYSIPYP reverseSale API"() {
        given:
        webClient.post() >> mockWebClientBadRequestError()
        OrderCancelledDomainEventMessage orderCancelledDomainEventMessage =
                mockValidCancelledEventMessage()
        omsOrderToPYSIPYPOrderInfoMapper.buildReverseSaleRequest(orderCancelledDomainEventMessage) >> mockPYSIPYPRequest()

        when:
        pysipypWebClient.reverseSale(orderCancelledDomainEventMessage)
        then:
        thrown(OMSBadRequestException.class)
    }


    private WebClient.RequestHeadersUriSpec mockWebClientForValidGetPriceData() {
        Mock(WebClient.RequestBodyUriSpec) {
            uri(_) >> Mock(WebClient.RequestBodySpec) {
                body(_, _) >> Mock(WebClient.RequestHeadersSpec) {
                    header(_, _) >> Mock(WebClient.RequestHeadersSpec) {
                        header(_, _) >> Mock(WebClient.RequestHeadersSpec) {
                            retrieve() >> Mock(WebClient.ResponseSpec) {
                                toEntity(_ as Class) >> {
                                    return Mono.just(new ResponseEntity<OrderInformation>(createSamplePYSIPYPResponse(), null, HttpStatus.OK))
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private WebClient.RequestHeadersUriSpec mockWebClientForReverseSaleErrorResponse() {
        Mock(WebClient.RequestBodyUriSpec) {
            uri(_) >> Mock(WebClient.RequestBodySpec) {
                body(_, _) >> Mock(WebClient.RequestHeadersSpec) {
                    header(_, _) >> Mock(WebClient.RequestHeadersSpec) {
                        header(_, _) >> Mock(WebClient.RequestHeadersSpec) {
                            retrieve() >> Mock(WebClient.ResponseSpec) {
                                toEntity(_ as Class) >>
                                        { return Mono.just(mockErrorReverseSaleResponse()) }
                            }
                        }
                    }
                }
            }
        }
    }

    private WebClient.RequestHeadersUriSpec mockWebClientForRecordSaleErrorResponse() {
        Mock(WebClient.RequestBodyUriSpec) {
            uri(_) >> Mock(WebClient.RequestBodySpec) {
                body(_, _) >> Mock(WebClient.RequestHeadersSpec) {
                    header(_, _) >> Mock(WebClient.RequestHeadersSpec) {
                        header(_, _) >> Mock(WebClient.RequestHeadersSpec) {
                            retrieve() >> Mock(WebClient.ResponseSpec) {
                                toEntity(_ as Class) >> { return Mono.just(mockErrorRecordSaleResponse()) }
                            }
                        }
                    }
                }
            }
        }
    }

    private WebClient.RequestHeadersUriSpec mockWebClientForReverseSaleEmptyResponse() {
        Mock(WebClient.RequestBodyUriSpec) {
            uri(_) >> Mock(WebClient.RequestBodySpec) {
                body(_, _) >> Mock(WebClient.RequestHeadersSpec) {
                    header(_, _) >> Mock(WebClient.RequestHeadersSpec) {
                        header(_, _) >> Mock(WebClient.RequestHeadersSpec) {
                            retrieve() >> Mock(WebClient.ResponseSpec) {
                                toEntity(_ as Class) >> { return Mono.empty() }
                            }
                        }
                    }
                }
            }
        }
    }


    private WebClient.RequestHeadersUriSpec mockWebClientForRecordSaleEmptyResponse() {
        Mock(WebClient.RequestBodyUriSpec) {
            uri(_) >> Mock(WebClient.RequestBodySpec) {
                body(_, _) >> Mock(WebClient.RequestHeadersSpec) {
                    header(_, _) >> Mock(WebClient.RequestHeadersSpec) {
                        header(_, _) >> Mock(WebClient.RequestHeadersSpec) {
                            retrieve() >> Mock(WebClient.ResponseSpec) {
                                toEntity(_ as Class) >> {
                                    return Mono.empty()
                                }
                            }
                        }
                    }
                }
            }
        }
    }


    private OrderInformation mockErrorReverseSaleResponse() {
        OrderInformation orderInformation = mockReverseSaleResponse()
        orderInformation.setTransactionCode("ERROR")

        orderInformation.setError(new ErrorDetail(
                code: "9576",
                description: "failed to perform reversesale",
                type: "test type"
        ))

        return orderInformation
    }

    private OrderInformation mockErrorRecordSaleResponse() {
        OrderInformation orderInformation = mockReverseSaleResponse()
        orderInformation.setTransactionCode("ERROR")

        orderInformation.setError(new ErrorDetail(
                code: "9576",
                description: "failed to perform  record sale",
                type: "test type"
        ))

        return orderInformation
    }

    private WebClient.RequestBodyUriSpec mockWebClientTimeoutError() {
        Mock(WebClient.RequestBodyUriSpec) {
            uri(_) >> Mock(WebClient.RequestBodySpec) {
                body(_, _) >> Mock(WebClient.RequestHeadersSpec) {
                    header(_, _) >> Mock(WebClient.RequestHeadersSpec) {
                        header(_, _) >> Mock(WebClient.RequestHeadersSpec) {
                            retrieve() >> Mock(WebClient.ResponseSpec) {
                                toEntity(_ as Class) >> {
                                    String errorMessage =
                                            String.format(
                                                    "PYSIPYP Web Client Custom Exception Filter was not able to handle this exception for Order")
                                    return Mono.error(new IOException(errorMessage))
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private WebClient.RequestBodyUriSpec mockWebClientBadRequestError() {
        Mock(WebClient.RequestBodyUriSpec) {
            uri(_) >> Mock(WebClient.RequestBodySpec) {
                body(_, _) >> Mock(WebClient.RequestHeadersSpec) {
                    header(_, _) >> Mock(WebClient.RequestHeadersSpec) {
                        header(_, _) >> Mock(WebClient.RequestHeadersSpec) {
                            retrieve() >> Mock(WebClient.ResponseSpec) {
                                toEntity(_ as Class) >> {
                                    String errorMessage =
                                            String.format(
                                                    "PYSIPYP Web Client Custom Exception Filter was not able to handle this exception for Order")
                                    return Mono.error(new OMSBadRequestException(errorMessage))
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private OrderInformation mockPYSIPYPRequest() {
        return OrderInformation.builder()
                .consumerId("GIF")
                .cardTypeUsed("CREDITCARD")
                .header(PYSIPYPHeader.builder()
                        .orderNumber("71615242412")
                        .storeNumber("5755")
                        .accessCode(pysipypServiceConfiguration.accessCode)
                        .messageType(PYSIPYPConstants.ODS_CALC_TOTAL)
                        .countryCode(PYSIPYPConstants.COUNTRY_CODE_GB)
                        .build())
                .orderedItems(Arrays.asList(new OrderedItem.OrderedItemBuilder().cin("1137922")
                        .quantity(3)
                        .priceInfo(PriceInfo.builder().minimumUnitPrice(3.0)
                                .finalAmount(9.0)
                                .rawTotalPrice(9.0).build()).build()
                ))
                .build()
    }

    private OrderInformation createSamplePYSIPYPResponse() {
        return OrderInformation.builder()
                .minBasketChargeAppliedOnPricing(false)
                .colleagueDiscount("N")
                .transactionCode("12345678")
                .consumerId("GIF")
                .cardTypeUsed("CREDITCARD")
                .build()
    }

    private OrderInformation mockReverseSaleResponse() {

        OrderInformation orderInformation = new OrderInformation()

        PYSIPYPHeader header = new PYSIPYPHeader()
        header.setCountryCode("GB")
        //header.setMessageType()
        header.setAccessCode("testAccessCode")
        header.setSaleType("R")
        header.setStoreNumber("4401")
        header.setOrderNumber("32323233")
        orderInformation.setHeader(header)
        orderInformation.setTransactionCode("SUCCESS")
        return orderInformation

    }

    private PYSIPYPServiceConfiguration mockPYSIPYPServiceConfiguration() {
        PYSIPYPServiceConfiguration config = new PYSIPYPServiceConfiguration()
        config.setAccessCode("ahgshagshagshgashgshg")
        config.setConnTimeout(1000)
        config.setLoggingEnabled(true)
        config.setReadTimeout(1000)
        config.setPysipypServiceUriForOrders("http://10.117.144.20:8080/asda-services/rest/orders/")
        config.setPysipypServiceUri("http://10.117.144.20:8080/asda-services/rest/order")
        config.setThreadPoolSize(10)
        return config
    }

    private static OrderCancelledDomainEventMessage mockValidCancelledEventMessage() {
        return new OrderCancelledDomainEventMessage()
                .builder().storeOrderId("12334").storeId("5755").build()
    }

    private static ThreadFactory mockThreadFactory() {
        return new BasicThreadFactory.Builder().namingPattern("IRO-thread-factory-%d").build()
    }

    private static ExecutorService mockExecutorService() {
        return Executors.newFixedThreadPool(10, mockThreadFactory())
    }
}
