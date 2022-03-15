package com.walmart.marketplace.infrastructure.gateway.justeats

import com.fasterxml.jackson.databind.ObjectMapper
import com.walmart.common.metrics.MetricService
import com.walmart.marketplace.infrastructure.gateway.justeats.config.JustEatsServiceConfiguration
import com.walmart.marketplace.infrastructure.gateway.justeats.dto.request.DenialErrorCode
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

class JustEatsOrderStatusUpdateWebClientSpec extends Specification {

    JustEatsServiceConfiguration justEatsServiceConfiguration = mockJustEatsServiceConfiguration()
    WebClient webClient = Mock()
    JustEatsOrderStatusUpdateClient justEatsWebClient
    ObjectMapper jsonObjectMapper = Mock()
    MetricService metricService = Mock()
    String externalOrderId = UUID.randomUUID().toString()

    def setup() {

        justEatsWebClient = new JustEatsOrderStatusUpdateClient(
                justEatsServiceConfiguration: justEatsServiceConfiguration,
                webClient: webClient,
                jsonObjectMapper: jsonObjectMapper,
                circuitBreakerRegistry: CircuitBreakerRegistry.ofDefaults(),
                bulkheadRegistry: BulkheadRegistry.ofDefaults(),
                retryRegistry: RetryRegistry.ofDefaults(),
                metricService: metricService
        )
        Field executorServiceField = justEatsWebClient.getClass()
                .getSuperclass().getSuperclass()
                .getSuperclass().getDeclaredField("executorService")
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

    def "When Accept Order is Successful"() {
        given:
        webClient.post() >> mockWebClientFor_Valid_AcceptDenyOrder()

        when:
        boolean orderAccepted = justEatsWebClient.acceptOrder("12345")

        then:
        orderAccepted
    }

    def "When Order is not existent for Accept Order"() {
        given:
        webClient.post() >> mockWebClientFor_Invalid_AcceptDenyCancelOrder()

        when:
        justEatsWebClient.acceptOrder("12345")

        then:
        thrown OMSThirdPartyException
    }

    def "When Accept Order throws Bad Request Exception"() {
        given:
        webClient.post() >> mockWebClientFor_Invalid_AcceptDenyCancelOrder_OMSBadRequestException()

        when:
        justEatsWebClient.acceptOrder("12345")

        then:
        thrown OMSBadRequestException
    }

    def "When Deny Order is Successful"() {
        given:
        webClient.post() >> mockWebClientFor_Valid_AcceptDenyOrder()

        when:
        boolean orderAccepted = justEatsWebClient.rejectOrder("12345", DenialErrorCode.IN_USE)

        then:
        orderAccepted
    }

    def "When Order is not existent for Deny Order"() {
        given:
        webClient.post() >> mockWebClientFor_Invalid_AcceptDenyCancelOrder()

        when:
        justEatsWebClient.rejectOrder("12345", DenialErrorCode.IN_USE)

        then:
        thrown OMSThirdPartyException
    }

    def "When Deny Order throws Bad Request Exception"() {
        given:
        webClient.post() >> mockWebClientFor_Invalid_AcceptDenyCancelOrder_OMSBadRequestException()

        when:
        justEatsWebClient.rejectOrder("12345", DenialErrorCode.IN_USE)

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

    private WebClient.RequestHeadersUriSpec mockWebClientFor_Valid_AcceptDenyOrder() {
        Mock(WebClient.RequestBodyUriSpec) {
            uri(_) >> Mock(WebClient.RequestBodySpec) {
                header(_, _) >> Mock(WebClient.RequestBodySpec) {
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
    }

    private WebClient.RequestHeadersUriSpec mockWebClientFor_Invalid_AcceptDenyCancelOrder() {
        Mock(WebClient.RequestBodyUriSpec) {
            uri(_) >> Mock(WebClient.RequestBodySpec) {
                header(_, _) >> Mock(WebClient.RequestBodySpec) {
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
    }

    private WebClient.RequestHeadersUriSpec mockWebClientFor_Invalid_AcceptDenyCancelOrder_OMSBadRequestException() {
        Mock(WebClient.RequestBodyUriSpec) {
            uri(_) >> Mock(WebClient.RequestBodySpec) {
                header(_, _) >> Mock(WebClient.RequestBodySpec) {
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
    }

    private static ThreadFactory mockThreadFactory() {
        return new BasicThreadFactory.Builder().namingPattern("IRO-thread-factory-%d").build()
    }

    private static ExecutorService mockExecutorService() {
        return Executors.newFixedThreadPool(10, mockThreadFactory())
    }

}
