package com.walmart.oms.infrastructure.gateway.tax

import com.fasterxml.jackson.databind.ObjectMapper
import com.walmart.common.domain.type.Currency
import com.walmart.common.metrics.MetricService
import com.walmart.oms.domain.error.exception.OMSThirdPartyException
import com.walmart.oms.order.aggregateroot.OmsOrder
import com.walmart.oms.order.domain.entity.*
import com.walmart.oms.order.valueobject.ItemPriceInfo
import com.walmart.oms.order.valueobject.Money
import com.walmart.tax.calculator.dto.*
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

class CalculateTaxHttpWebClientTest extends Specification {

    String clientName = "TAX"
    String sourceOrderId = UUID.randomUUID().toString()
    WebClient webClient = Mock()
    ObjectMapper jsonObjectMapper = Mock()
    CalculatorTaxServiceConfiguration calculatorTaxServiceConfiguration
    MetricService metricService = Mock()
    CalculateTaxHttpWebClient calculateTaxHttpWebClient

    def setup() {
        calculatorTaxServiceConfiguration = getConfiguration()
        calculateTaxHttpWebClient = new CalculateTaxHttpWebClient(webClient: webClient,
                jsonObjectMapper: jsonObjectMapper,
                calculatorTaxServiceConfiguration: calculatorTaxServiceConfiguration,
                circuitBreakerRegistry: CircuitBreakerRegistry.ofDefaults(),
                bulkheadRegistry: BulkheadRegistry.ofDefaults(),
                metricService: metricService,
                retryRegistry: RetryRegistry.ofDefaults())
        Field executorServiceField = calculateTaxHttpWebClient.getClass().getSuperclass().getSuperclass().getDeclaredField("executorService")
        executorServiceField.setAccessible(true)
        executorServiceField.set(calculateTaxHttpWebClient, mockExecutorService())
    }

    private static CalculatorTaxServiceConfiguration getConfiguration() {
        return new CalculatorTaxServiceConfiguration(taxServiceUri: "https://api.wal-mart.com/si/boftm/calculation-api/order/tax/calculate",
                isReverseCalculation: true,
                loggingEnabled: true,
                connTimeout: 1000,
                readTimeout: 2000,
                taxClientId: "05e193aa-221e-4ebc-a3c5-79eb6a7af015",
                taxClientSecret: "",
                threadPoolSize: 10)
    }

    def "Calculate Tax WebClient Initialization Successfully"() {
        given:
        Bulkhead bulkhead = calculateTaxHttpWebClient.bulkheadRegistry.bulkhead(clientName)

        when:
        calculateTaxHttpWebClient.initialize()

        then:
        calculateTaxHttpWebClient.webClient != null
        bulkhead != null
    }

    def "Empty OrderedItem list"() {
        given:
        List<OmsOrderItem> orderedItemList = new ArrayList<>()

        when:
        CalculateTaxResponse calculateTaxResponse = calculateTaxHttpWebClient.executeTaxCall(orderedItemList, sourceOrderId)

        then:
        calculateTaxResponse == null
    }

    def "Empty Response from TAX API"() {
        given:
        List<OmsOrderItem> orderedItemList = new ArrayList<>([createOrderedItem()])
        webClient.post() >> mockWebClientEmptyTaxResponse()

        when:
        calculateTaxHttpWebClient.executeTaxCall(orderedItemList, sourceOrderId)

        then:
        noExceptionThrown()
    }

    def "Success Response from TAX API"() {
        given:
        List<OmsOrderItem> orderedItemList = new ArrayList<>([createOrderedItem()])
        webClient.post() >> mockWebClientSuccessTaxResponse()

        when:
        CalculateTaxResponse calculateTaxResponse = calculateTaxHttpWebClient.executeTaxCall(orderedItemList, sourceOrderId)

        then:
        calculateTaxResponse != null
    }

    def "Error Response from TAX API"() {
        given:
        List<OmsOrderItem> orderedItemList = new ArrayList<>([createOrderedItem()])
        webClient.post() >> mockWebClientError()

        when:
        calculateTaxHttpWebClient.executeTaxCall(orderedItemList, sourceOrderId)

        then:
        thrown(OMSThirdPartyException.class)
    }

    def "Test construction of CalculateTaxRequest"() {
        when:
        CalculateTaxRequest actualCalculateTaxRequest = calculateTaxHttpWebClient.buildCalculateTaxRequest([createOrderedItem()])

        then:
        actualCalculateTaxRequest != null
        CalculateTaxRequest expectedCalculateTaxRequest = expectedCalculateTaxRequest()

        actualCalculateTaxRequest.currency == expectedCalculateTaxRequest.currency
        actualCalculateTaxRequest.isReverseCalculation == expectedCalculateTaxRequest.isReverseCalculation

        actualCalculateTaxRequest.order.orderLines.get(0).items.get(0).department == expectedCalculateTaxRequest.order.orderLines.get(0).items.get(0).department
        actualCalculateTaxRequest.order.orderLines.get(0).items.get(0).gtin == expectedCalculateTaxRequest.order.orderLines.get(0).items.get(0).gtin
        actualCalculateTaxRequest.order.orderLines.get(0).items.get(0).number == expectedCalculateTaxRequest.order.orderLines.get(0).items.get(0).number
        actualCalculateTaxRequest.order.orderLines.get(0).items.get(0).quantity == expectedCalculateTaxRequest.order.orderLines.get(0).items.get(0).quantity

        actualCalculateTaxRequest.order.orderLines.get(0).items.get(0).discounts.get(0).amount.type == expectedCalculateTaxRequest.order.orderLines.get(0).items.get(0).discounts.get(0).amount.type
        actualCalculateTaxRequest.order.orderLines.get(0).items.get(0).discounts.get(0).amount.value == expectedCalculateTaxRequest.order.orderLines.get(0).items.get(0).discounts.get(0).amount.value

        actualCalculateTaxRequest.order.orderLines.get(0).items.get(0).fees.get(0).amount.type == expectedCalculateTaxRequest.order.orderLines.get(0).items.get(0).fees.get(0).amount.type
        actualCalculateTaxRequest.order.orderLines.get(0).items.get(0).fees.get(0).amount.value == expectedCalculateTaxRequest.order.orderLines.get(0).items.get(0).fees.get(0).amount.value

        actualCalculateTaxRequest.order.orderLines.get(0).items.get(0).price.type == expectedCalculateTaxRequest.order.orderLines.get(0).items.get(0).price.type
        actualCalculateTaxRequest.order.orderLines.get(0).items.get(0).price.value == expectedCalculateTaxRequest.order.orderLines.get(0).items.get(0).price.value

        actualCalculateTaxRequest.order.orderLines.get(0).shipToNode.address.country == expectedCalculateTaxRequest.order.orderLines.get(0).shipToNode.address.country
        actualCalculateTaxRequest.order.orderLines.get(0).shipToNode.baseDivision == expectedCalculateTaxRequest.order.orderLines.get(0).shipToNode.baseDivision
        actualCalculateTaxRequest.order.orderLines.get(0).shipToNode.id == expectedCalculateTaxRequest.order.orderLines.get(0).shipToNode.id
        actualCalculateTaxRequest.order.orderLines.get(0).shipToNode.type == expectedCalculateTaxRequest.order.orderLines.get(0).shipToNode.type

    }

    def "Test construction of CalculateTaxRequest in case of nil picked item"() {
        when:
        CalculateTaxRequest actualCalculateTaxRequest = calculateTaxHttpWebClient.buildCalculateTaxRequest([createOrderedItemNilPick()])

        then:
        actualCalculateTaxRequest == null
    }

    OmsOrderItem createOrderedItem() {

        OmsOrder omsOrder = new OmsOrder("PICK_COMPLETE")
        OmsOrderItem omsOrderItem = getOmsOrderItem(omsOrder)
        PickedItem pickedItem = getPickedItem(omsOrderItem)
        omsOrderItem.enrichPickedInfoWithPickedItem(pickedItem)

        return omsOrderItem
    }

    OmsOrderItem createOrderedItemNilPick() {

        OmsOrder omsOrder = new OmsOrder("PICK_COMPLETE")
        OmsOrderItem omsOrderItem = getOmsOrderItem(omsOrder)
        PickedItem pickedItem = getNilPickedItem(omsOrderItem)
        omsOrderItem.enrichPickedInfoWithPickedItem(pickedItem)

        return omsOrderItem
    }

    private static PickedItem getNilPickedItem(OmsOrderItem omsOrderItem) {
        return PickedItem.builder()
                .departmentID("68")
                .quantity(0)
                .orderedCin("1234")
                .omsOrderItem(omsOrderItem)
                .pickedItemUpcList(Arrays.asList(PickedItemUpc.builder()
                        .uom("E")
                        .upc("5000168188775")
                        .storeUnitPrice(new Money(BigDecimal.valueOf(2.5), Currency.GBP))
                        .win("50577386")
                        .build())).build()
    }

    private static PickedItem getPickedItem(OmsOrderItem omsOrderItem) {
        PickedItem pickedItem = PickedItem.builder()
                .departmentID("68")
                .quantity(1)
                .orderedCin("1234")
                .omsOrderItem(omsOrderItem)
                .pickedItemUpcList(Collections.singletonList(PickedItemUpc.builder()
                        .uom("E")
                        .upc("5000168188775")
                        .storeUnitPrice(new Money(BigDecimal.valueOf(2.5), Currency.GBP))
                        .win("50577386")
                        .build())).build()
        pickedItem.updateSubstitutedItems(
                Collections.singletonList(SubstitutedItem.builder()
                        .department("68")
                        .consumerItemNumber("123")
                        .description("Substituted item")
                        .walmartItemNumber("1444")
                        .upcs(Collections.singletonList(SubstitutedItemUpc.builder().upc("5000168188776").uom("E").build()))
                        .build()))
        return pickedItem
    }

    private static OmsOrderItem getOmsOrderItem(OmsOrder omsOrder) {
        return OmsOrderItem.builder().quantity(2)
                .itemDescription("test description")
                .cin("1234")
                .omsOrder(omsOrder)
                .itemPriceInfo(new ItemPriceInfo(new Money(BigDecimal.valueOf(2.5), Currency.GBP), new Money(BigDecimal.valueOf(5.0), Currency.GBP)))
                .id(UUID.randomUUID().toString())
                .build()
    }

    CalculateTaxRequest expectedCalculateTaxRequest() {

        CalculateTaxRequest calculateTaxRequest = new CalculateTaxRequest()
        calculateTaxRequest.setCurrency(CalculateTaxRequest.Currency.USD)
        calculateTaxRequest.setIsReverseCalculation(true)
        calculateTaxRequest.setOrder(getTaxOrderDetail())
        calculateTaxRequest.setTransaction(getTaxTransaction())

        return calculateTaxRequest
    }

    Order getTaxOrderDetail() {
        Order order = new Order()
        OrderLine orderLine = new OrderLine()
        List<OrderLine> orderLineList = new ArrayList<>()

        orderLine.setShipToNode(getShipToNode())
        orderLine.setItems(getTaxItems())
        orderLineList.add(orderLine)

        order.setOrderLines(orderLineList)

        return order
    }

    List<Tax> getTaxItems() {
        Item taxItem = new Item()

        Amount taxAmount = new Amount()
        taxAmount.setType(Amount.Type.FIXED)
        taxAmount.setValue(BigDecimal.ZERO)

        List<Discount> taxDiscounts = new ArrayList<>()
        Discount taxDiscount = new Discount()
        taxDiscount.setAmount(taxAmount)
        taxDiscounts.add(taxDiscount)

        List<Fee> taxFees = new ArrayList<>()
        Fee taxFee = new Fee()
        taxFee.setPrice(taxAmount)
        taxFee.setGtin("5000168188775")
        taxFee.setId("String")
        taxFee.setNumber(Long.parseLong("50577386"))
        taxFee.setQuantity(Long.valueOf(1))
        taxFees.add(taxFee)

        taxItem.setDepartment(Long.parseLong("68"))
        taxItem.setDiscounts(taxDiscounts)
        taxItem.setFees(taxFees)
        taxItem.setGtin("5000168188775")
        taxItem.setFees(taxFees)
        taxItem.setPrice(taxAmount)
        taxItem.setQuantity(Long.valueOf(1))
        taxItem.setNumber(Long.parseLong("50577386"))

        List<Tax> taxList = new ArrayList<>()
        taxList.add(taxItem)

        return taxList
    }

    Node getShipToNode() {
        Node shipToNode = new Node()
        Address address = new Address()
        address.setCountry("GB")

        shipToNode.setAddress(address)
        shipToNode.setBaseDivision(1)
        shipToNode.setId(0)
        shipToNode.setType(Node.Type.STORE)
        return shipToNode
    }

    Transaction getTaxTransaction() {
        Transaction transaction = new Transaction()
        transaction.setDate(new Date())
        transaction.setType(Transaction.Type.SALES)
        return transaction
    }

    private WebClient.RequestBodyUriSpec mockWebClientEmptyTaxResponse() {
        Mock(WebClient.RequestBodyUriSpec) {
            headers(_) >> Mock(WebClient.RequestBodySpec) {
                body(_, _) >> Mock(WebClient.RequestHeadersSpec) {
                    retrieve() >> Mock(WebClient.ResponseSpec) {
                        toEntity(_ as Class) >> {
                            return Mono.just(new ResponseEntity<CalculateTaxResponse>(null, null, HttpStatus.OK))
                        }
                    }
                }
            }
        }
    }

    private WebClient.RequestBodyUriSpec mockWebClientSuccessTaxResponse() {
        Mock(WebClient.RequestBodyUriSpec) {
            headers(_) >> Mock(WebClient.RequestBodySpec) {
                body(_, _) >> Mock(WebClient.RequestHeadersSpec) {
                    retrieve() >> Mock(WebClient.ResponseSpec) {
                        toEntity(_ as Class) >> {
                            return Mono.just(new ResponseEntity<CalculateTaxResponse>(createCalculateTaxResponse(), null, HttpStatus.OK))
                        }
                    }
                }
            }
        }
    }

    private WebClient.RequestBodyUriSpec mockWebClientError() {
        Mock(WebClient.RequestBodyUriSpec) {
            headers(_) >> Mock(WebClient.RequestBodySpec) {
                body(_, _) >> Mock(WebClient.RequestHeadersSpec) {
                    retrieve() >> Mock(WebClient.ResponseSpec) {
                        toEntity(_ as Class) >> {
                            String errorMessage =
                                    String.format("TAX Web Client Custom Exception Filter was not able to handle this exception for sourceOrderId: %s",
                                            sourceOrderId)
                            return Mono.error(new OMSThirdPartyException(errorMessage))
                        }
                    }
                }
            }
        }
    }

    CalculateTaxResponse createCalculateTaxResponse() {
        CalculateTaxResponse calculateTaxResponse = new CalculateTaxResponse()

        Amount rate = new Amount()
        rate.setType(Amount.Type.PERCENT)
        rate.setValue(6.4)
        Amount total = new Amount()
        total.setType(Amount.Type.FIXED)
        total.setValue(0)

        Tax tax = new Tax()
        tax.setTaxCode(1)
        tax.setRateId("68")
        tax.setRate(rate)
        tax.setDescription("VAT")
        tax.setTotal(total)

        List<Tax> taxList = new ArrayList<>()
        taxList.add(tax)

        Item item = new Item()
        item.setGtin("5000168188775")
        item.setTaxes(taxList)

        List<Item> itemList = new ArrayList<>()
        itemList.add(item)

        OrderLine orderLine = new OrderLine()
        orderLine.setItems(itemList)

        List<OrderLine> orderLineList = new ArrayList<>()
        orderLineList.add(orderLine)

        Order order = new Order()
        order.setOrderLines(orderLineList)

        calculateTaxResponse.setOrder(order)

        return calculateTaxResponse
    }

    private static ThreadFactory mockThreadFactory() {
        return new BasicThreadFactory.Builder().namingPattern("IRO-thread-factory-%d").build()
    }

    private static ExecutorService mockExecutorService() {
        return Executors.newFixedThreadPool(10, mockThreadFactory())
    }

}
