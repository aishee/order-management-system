package com.walmart.oms.infrastructure.gateway.iro

import com.fasterxml.jackson.databind.ObjectMapper
import com.walmart.common.metrics.MetricService
import com.walmart.oms.domain.error.exception.OMSThirdPartyException
import com.walmart.oms.infrastructure.gateway.iro.dto.response.IROResponse
import com.walmart.oms.infrastructure.gateway.iro.dto.response.IRORootItems
import com.walmart.oms.order.valueobject.CatalogItemInfoQuery
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

class IROHttpWebClientTest extends Specification {

    String sourceOrderId = UUID.randomUUID().toString()
    IROHttpWebClient iroHttpClient = Mock()
    WebClient webClient = Mock()
    IROServiceConfiguration iroServiceConfiguration
    ObjectMapper jsonObjectMapper = Mock()
    MetricService metricService = Mock()

    def setup() {
        iroServiceConfiguration = getConfiguration()
        iroHttpClient = new IROHttpWebClient(
                webClient: webClient,
                jsonObjectMapper: jsonObjectMapper,
                iroServiceConfiguration: iroServiceConfiguration,
                circuitBreakerRegistry: CircuitBreakerRegistry.ofDefaults(),
                metricService: metricService,
                retryRegistry: RetryRegistry.ofDefaults()
        )
        Field executorServiceField = iroHttpClient.getClass().getSuperclass().getSuperclass().getDeclaredField("executorService")
        executorServiceField.setAccessible(true)
        executorServiceField.set(iroHttpClient, mockExecutorService())
    }

    private static IROServiceConfiguration getConfiguration() {
        return new IROServiceConfiguration(
                iroServiceUri: "https://iro-service-prod-private.walmart.com/asda-iro-service/catalogitem",
                iroItemBatchSize: 2,
                loggingEnabled: true,
                connTimeout: 1000,
                readTimeout: 2000,
                iroConsumerContract: "ods_store_item",
                iroRequestOrigin: "ods",
                priceDrop: "Price Drop",
                newPriceDropTagEnabled: true,
                threadPoolSize: 20
        )
    }

    def "Null Response from IRO API"() {
        given:
        CatalogItemInfoQuery catalogItemInfoQuery = createCatalogItemInfoQuery()
        webClient.post() >> mockWebClientNullIROResponse()

        when:
        iroHttpClient.retrieveCatalogData(catalogItemInfoQuery)

        then:
        thrown(OMSThirdPartyException.class)
    }

    def "Success Response from IRO API"() {
        given:
        CatalogItemInfoQuery catalogItemInfoQuery = createCatalogItemInfoQuery()
        webClient.post() >> mockWebClientSuccessIROResponse()

        when:
        List<IROResponse> iroResponseList = iroHttpClient.retrieveCatalogData(catalogItemInfoQuery)

        then:
        iroResponseList != null
        iroResponseList.size() == 1
    }

    def "Success Response from IRO API in Batches"() {
        given:
        CatalogItemInfoQuery catalogItemInfoQuery = createCatalogItemInfoQueryBatch()
        webClient.post() >> mockWebClientSuccessIROResponse()

        when:
        List<IROResponse> iroResponseList = iroHttpClient.retrieveCatalogData(catalogItemInfoQuery)

        then:
        iroResponseList != null
        iroResponseList.size() == 2
    }

    def "Invalid Item Response from IRO API"() {
        given:
        CatalogItemInfoQuery catalogItemInfoQuery = createCatalogItemInfoQuery()
        webClient.post() >> mockWebClientInvalidItemIROResponse()

        when:
        iroHttpClient.retrieveCatalogData(catalogItemInfoQuery)

        then:
        thrown(OMSThirdPartyException.class)
    }

    def "Invalid Price Response from IRO API"() {
        given:
        CatalogItemInfoQuery catalogItemInfoQuery = createCatalogItemInfoQuery()
        webClient.post() >> mockWebClientInvalidPriceIROResponse()

        when:
        List<IROResponse> iroResponseList = iroHttpClient.retrieveCatalogData(catalogItemInfoQuery)

        then:
        iroResponseList != null
    }

    def "Timeout from IRO Response API"() {
        given:
        CatalogItemInfoQuery catalogItemInfoQuery = createCatalogItemInfoQuery()
        webClient.post() >> mockWebClientTimeoutError()

        when:
        iroHttpClient.retrieveCatalogData(catalogItemInfoQuery)

        then:
        thrown(OMSThirdPartyException.class)
    }

    CatalogItemInfoQuery createCatalogItemInfoQuery() {

        List<String> itemIdList = new ArrayList<>(["12345"])
        return CatalogItemInfoQuery.builder().itemType("CIN").shipOnDate(new Date()).storeId("5755").storeOrderId("1234").itemIds(itemIdList).build()

    }

    CatalogItemInfoQuery createCatalogItemInfoQueryBatch() {

        List<String> itemIdList = new ArrayList<>(["12345", "54321", "11111"])
        return CatalogItemInfoQuery.builder().itemType("CIN").shipOnDate(new Date()).storeId("5755").storeOrderId("1234").itemIds(itemIdList).build()

    }

    private WebClient.RequestBodyUriSpec mockWebClientNullIROResponse() {
        Mock(WebClient.RequestBodyUriSpec) {
            body(_, _) >> Mock(WebClient.RequestHeadersSpec) {
                retrieve() >> Mock(WebClient.ResponseSpec) {
                    toEntity(_ as Class) >> {
                        return Mono.just(new ResponseEntity<IROResponse>(null, null, HttpStatus.OK))
                    }
                }
            }
        }
    }

    private WebClient.RequestBodyUriSpec mockWebClientSuccessIROResponse() {
        Mock(WebClient.RequestBodyUriSpec) {
            body(_, _) >> Mock(WebClient.RequestHeadersSpec) {
                retrieve() >> Mock(WebClient.ResponseSpec) {
                    toEntity(_ as Class) >> {
                        return Mono.just(new ResponseEntity<IROResponse>(createIROSuccessResponse(), null, HttpStatus.OK))
                    }
                }
            }
        }
    }

    private WebClient.RequestBodyUriSpec mockWebClientInvalidItemIROResponse() {
        Mock(WebClient.RequestBodyUriSpec) {
            body(_, _) >> Mock(WebClient.RequestHeadersSpec) {
                retrieve() >> Mock(WebClient.ResponseSpec) {
                    toEntity(_ as Class) >> {
                        return Mono.just(new ResponseEntity<IROResponse>(createIROInvalidItemResponse(), null, HttpStatus.OK))
                    }
                }
            }
        }
    }

    private WebClient.RequestBodyUriSpec mockWebClientInvalidPriceIROResponse() {
        Mock(WebClient.RequestBodyUriSpec) {
            body(_, _) >> Mock(WebClient.RequestHeadersSpec) {
                retrieve() >> Mock(WebClient.ResponseSpec) {
                    toEntity(_ as Class) >> {
                        return Mono.just(new ResponseEntity<IROResponse>(createIROInvalidPriceResponse(), null, HttpStatus.OK))
                    }
                }
            }
        }
    }

    private WebClient.RequestBodyUriSpec mockWebClientTimeoutError() {
        Mock(WebClient.RequestBodyUriSpec) {
            body(_, _) >> Mock(WebClient.RequestHeadersSpec) {
                retrieve() >> Mock(WebClient.ResponseSpec) {
                    toEntity(_ as Class) >> {
                        String errorMessage =
                                String.format(
                                        "IRO Web Client Custom Exception Filter was not able to handle this exception for Order Id:%s",
                                        sourceOrderId)
                        return Mono.error(new OMSThirdPartyException(errorMessage))
                    }
                }
            }
        }
    }

    IROResponse createIROSuccessResponse() {
        IROResponse iroResponse = new IROResponse()
        IROResponse.Data data = new IROResponse.Data()
        IROResponse.UberItem uberItem = new IROResponse.UberItem()
        IRORootItems iroRootItems = new IRORootItems()
        uberItem.setItems(Collections.singletonList(iroRootItems))
        data.setUberItem(uberItem)
        iroResponse.setData(data)
        return iroResponse
    }

    IROResponse createIROInvalidItemResponse() {
        IROResponse iroResponse = new IROResponse()
        IROResponse.Data data = new IROResponse.Data()
        IROResponse.UberItem uberItem = new IROResponse.UberItem()
        IRORootItems iroRootItems = new IRORootItems()
        uberItem.setItems(Collections.singletonList(iroRootItems))
        IROResponse.OverallInvalidItems invalidItems = new IROResponse.OverallInvalidItems()
        invalidItems.setInvalidItems(Collections.singletonList("12345"))
        uberItem.setInvalidItemIds(invalidItems)
        data.setUberItem(uberItem)
        iroResponse.setData(data)
        return iroResponse
    }

    IROResponse createIROInvalidPriceResponse() {
        IROResponse iroResponse = new IROResponse()
        IROResponse.Data data = new IROResponse.Data()
        IROResponse.UberItem uberItem = new IROResponse.UberItem()
        IRORootItems iroRootItems = new IRORootItems()
        uberItem.setItems(Collections.singletonList(iroRootItems))
        IROResponse.OverallInvalidItems invalidItems = new IROResponse.OverallInvalidItems()
        invalidItems.setInvalidPrices(Collections.singletonList("12345"))
        uberItem.setInvalidItemIds(invalidItems)
        data.setUberItem(uberItem)
        iroResponse.setData(data)
        return iroResponse
    }

    private static ThreadFactory mockThreadFactory() {
        return new BasicThreadFactory.Builder().namingPattern("IRO-thread-factory-%d").build()
    }

    private static ExecutorService mockExecutorService() {
        return Executors.newFixedThreadPool(10, mockThreadFactory())
    }

}
