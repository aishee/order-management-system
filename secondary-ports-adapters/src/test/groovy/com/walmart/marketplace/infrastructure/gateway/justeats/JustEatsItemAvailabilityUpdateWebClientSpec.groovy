package com.walmart.marketplace.infrastructure.gateway.justeats

import com.fasterxml.jackson.databind.ObjectMapper
import com.walmart.common.metrics.MetricService
import com.walmart.marketplace.infrastructure.gateway.justeats.config.JustEatsServiceConfiguration
import com.walmart.marketplace.order.domain.entity.UpdateItemInfo
import com.walmart.marketplace.order.domain.entity.type.Vendor
import com.walmart.oms.domain.error.exception.OMSBadRequestException
import com.walmart.oms.domain.error.exception.OMSThirdPartyException
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

class JustEatsItemAvailabilityUpdateWebClientSpec extends Specification {

    JustEatsServiceConfiguration justEatsServiceConfiguration = mockJustEatsServiceConfiguration()
    WebClient webClient = Mock()
    JustEatsItemAvailabilityUpdateClient justEatsWebClient
    ObjectMapper jsonObjectMapper = Mock()
    MetricService metricService = Mock()
    String externalOrderId = UUID.randomUUID().toString()

    def setup() {

        justEatsWebClient = new JustEatsItemAvailabilityUpdateClient(
                justEatsServiceConfiguration: justEatsServiceConfiguration,
                webClient: webClient,
                jsonObjectMapper: jsonObjectMapper,
                circuitBreakerRegistry: CircuitBreakerRegistry.ofDefaults(),
                bulkheadRegistry: BulkheadRegistry.ofDefaults(),
                retryRegistry: RetryRegistry.ofDefaults(),
                metricService: metricService
        )
        Field executorServiceField = justEatsWebClient.getClass().getSuperclass().getSuperclass().getSuperclass().getDeclaredField("executorService")
        executorServiceField.setAccessible(true)
        executorServiceField.set(justEatsWebClient, mockExecutorService())
    }

    def "Logging Disabled for Initialization"() {
        given:
        !justEatsServiceConfiguration.isLoggingEnabled()

        when:
        justEatsWebClient.initialize()

        then:
        0 * justEatsWebClient.circuitBreakerRegistry.circuitBreaker("JUST_EATS")
        0 * justEatsWebClient.bulkheadRegistry.bulkhead("JUST_EATS")
        0 * justEatsWebClient.retryRegistry.retry("JUST_EATS")
    }

    def "Logging Enabled for Initialization"() {
        given:
        justEatsServiceConfiguration.isLoggingEnabled()

        when:
        justEatsWebClient.initialize()

        then:
        0 * justEatsWebClient.circuitBreakerRegistry.circuitBreaker("JUST_EATS")
        0 * justEatsWebClient.bulkheadRegistry.bulkhead("JUST_EATS")
        0 * justEatsWebClient.retryRegistry.retry("JUST_EATS")
    }

    def "When Update Item for out of stock items is Successful"() {
        given:
        String vendorStoreId = "a48c2238-5f8d-48ad-917b-b91e2d577cdc"
        List<String> itemIds = new ArrayList<>()
        itemIds.add("e2066983-7793-4017-ac06-74785bfeff15")
        itemIds.add("273e9df8-838e-476b-b899-b23795b55b6e")
        UpdateItemInfo updateItemInfo = getUpdateItemInfo(vendorStoreId, itemIds)
        webClient.post() >> mockWebClientFor_Valid_UpdateItem()

        when:
        List<Boolean> updateItem = justEatsWebClient.updateItemInfo(updateItemInfo)

        then:
        updateItem.get(0)
    }

    def "When StoreID is not existent for Update Item"() {
        given:
        String vendorStoreId = "a48c2238-5f8d-48ad-917b-b91e2d577cdc"
        List<String> itemIds = new ArrayList<>()
        itemIds.add("e2066983-7793-4017-ac06-74785bfeff15")
        itemIds.add("273e9df8-838e-476b-b899-b23795b55b6e")
        UpdateItemInfo updateItemInfo = getUpdateItemInfo(vendorStoreId, itemIds)
        webClient.post() >> mockWebClientFor_Invalid_UpdateItem()

        when:
        justEatsWebClient.updateItemInfo(updateItemInfo)

        then:
        thrown Exception
    }

    def "When StoreID is not existent for Update Item OMSBadRequestException"() {
        given:
        String vendorStoreId = "a48c2238-5f8d-48ad-917b-b91e2d577cdc"
        List<String> itemIds = new ArrayList<>()
        itemIds.add("e2066983-7793-4017-ac06-74785bfeff15")
        itemIds.add("273e9df8-838e-476b-b899-b23795b55b6e")
        UpdateItemInfo updateItemInfo = getUpdateItemInfo(vendorStoreId, itemIds)
        webClient.post() >> mockWebClientFor_Invalid_UpdateItem_OMSBadRequestException()

        when:
        justEatsWebClient.updateItemInfo(updateItemInfo)

        then:
        thrown OMSBadRequestException
    }

    private static JustEatsServiceConfiguration mockJustEatsServiceConfiguration() {
        JustEatsServiceConfiguration justEatsConfig = new JustEatsServiceConfiguration().builder()
                .connTimeout(1000).readTimeout(1000)
                .loggingEnabled(true)
                .orderStatusUpdateBaseUri("https://api.flytplatform.com")
                .maxConnectionCount(50)
                .pendingAcquireTimeoutMs(30000)
                .idleTimeoutMs(30000)
                .acceptOrderApiUrl("/order/{order_id}/sent-to-pos-success")
                .denyOrderApiUrl("/order/{order_id}/sent-to-pos-failed")
                .itemAvailabilityApiUrl("/item-availability")
                .clientSecret("hdgskVUZRxMaXUUOqlASvZxtpfVAmzyrdMfRR")
                .orderStatusUpdateApiKey("hdgskVUZRxMaXUUOqlASvZxtpfVAmzyrdMfRR")
                .readTimeout(1000).threadPoolSize(10)
                .build()

        return justEatsConfig
    }

    private WebClient.RequestHeadersUriSpec mockWebClientFor_Valid_UpdateItem() {
        Mock(WebClient.RequestBodyUriSpec) {
            uri(_) >> Mock(WebClient.RequestBodySpec) {
                header(_, _) >> Mock(WebClient.RequestBodySpec) {
                    body(_, _) >> Mock(WebClient.RequestHeadersSpec) {
                        retrieve() >> Mock(WebClient.ResponseSpec) {
                            toEntity(_ as Class) >> {
                                return Mono.just(new ResponseEntity<String>(HttpStatus.ACCEPTED))
                            }
                        }
                    }
                }
            }
        }
    }

    private WebClient.RequestHeadersUriSpec mockWebClientFor_Invalid_UpdateItem() {
        Mock(WebClient.RequestBodyUriSpec) {
            uri(_) >> Mock(WebClient.RequestBodySpec) {
                header(_, _) >> Mock(WebClient.RequestBodySpec) {
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
    }

    private WebClient.RequestHeadersUriSpec mockWebClientFor_Invalid_UpdateItem_OMSBadRequestException() {
        Mock(WebClient.RequestBodyUriSpec) {
            uri(_) >> Mock(WebClient.RequestBodySpec) {
                header(_, _) >> Mock(WebClient.RequestBodySpec) {
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

    private static ThreadFactory mockThreadFactory() {
        return new BasicThreadFactory.Builder().namingPattern("IRO-thread-factory-%d").build()
    }

    private static ExecutorService mockExecutorService() {
        return Executors.newFixedThreadPool(10, mockThreadFactory())
    }
}
