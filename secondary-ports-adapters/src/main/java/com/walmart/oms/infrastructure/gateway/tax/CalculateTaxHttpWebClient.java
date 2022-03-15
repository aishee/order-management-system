package com.walmart.oms.infrastructure.gateway.tax;

import static java.util.Objects.requireNonNull;

import com.walmart.common.infrastructure.ApiAction;
import com.walmart.common.infrastructure.webclient.BaseWebClient;
import com.walmart.common.metrics.MetricConstants;
import com.walmart.common.metrics.MetricConstants.MetricCounters;
import com.walmart.oms.order.domain.entity.OmsOrderItem;
import com.walmart.oms.order.domain.entity.PickedItemUpc;
import com.walmart.oms.order.domain.entity.SubstitutedItem;
import com.walmart.tax.calculator.dto.Address;
import com.walmart.tax.calculator.dto.Amount;
import com.walmart.tax.calculator.dto.CalculateTaxRequest;
import com.walmart.tax.calculator.dto.CalculateTaxResponse;
import com.walmart.tax.calculator.dto.Discount;
import com.walmart.tax.calculator.dto.Fee;
import com.walmart.tax.calculator.dto.Item;
import com.walmart.tax.calculator.dto.Node;
import com.walmart.tax.calculator.dto.Order;
import com.walmart.tax.calculator.dto.OrderLine;
import com.walmart.tax.calculator.dto.Transaction;
import io.strati.configuration.annotation.ManagedConfiguration;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.ThreadFactory;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class CalculateTaxHttpWebClient extends BaseWebClient {

  private static final String TAX = "TAX";
  private static final String COUNTRY_CODE = "GB";
  private static final String STRING = "String";
  private static final String HEADER_PAYLOAD_TYPE = "payloadType";
  private static final String TAX_SERVICE_PAYLOAD = "TAX";
  private static final String TAX_HEADER_API_KEY = "WMT-API-KEY";
  private static final String TAX_HEADER_API_SECRET = "WMT-API-SECRET";
  private static final String THREAD_FACTORY_NAME = "IRO-thread-pool-%d";

  @ManagedConfiguration private CalculatorTaxServiceConfiguration calculatorTaxServiceConfiguration;

  @PostConstruct
  @Override
  public void initialize() {
    super.initialize();
  }

  @Override
  protected String getClientName() {
    return TAX;
  }

  @Override
  protected ThreadFactory getThreadFactory() {
    return new BasicThreadFactory.Builder().namingPattern(THREAD_FACTORY_NAME).build();
  }

  @Override
  protected int getThreadPoolSize() {
    return calculatorTaxServiceConfiguration.getThreadPoolSize();
  }

  @Override
  public String getMetricsExceptionCounterName() {
    return MetricCounters.TAX_EXCEPTION.getCounter();
  }

  @Override
  public String getMetricsCounterName() {
    return MetricCounters.SECONDARY_PORT_INVOCATION.getCounter();
  }

  public CalculateTaxResponse executeTaxCall(
      List<OmsOrderItem> orderedItemList, String sourceOrderId) {
    return executeApiWithRetries(
        retrieveCalculateTaxData(orderedItemList, sourceOrderId), ApiAction.TAX);
  }

  public Callable<CalculateTaxResponse> retrieveCalculateTaxData(
      List<OmsOrderItem> orderedItemList, String sourceOrderId) {
    return () -> {
      CalculateTaxRequest calculateTaxRequest = buildCalculateTaxRequest(orderedItemList);
      if (null != calculateTaxRequest) {
        logRequest(calculatorTaxServiceConfiguration.getTaxServiceUri(), calculateTaxRequest);
        // Web Client API call
        WebClient.ResponseSpec responseSpec =
            webClient
                .post()
                .headers(this::addHeaders)
                .body(Mono.just(calculateTaxRequest), CalculateTaxRequest.class)
                .retrieve();
        ResponseEntity<CalculateTaxResponse> calculateTaxResponse =
            captureMetrics(
                responseSpec,
                CalculateTaxResponse.class,
                MetricConstants.MetricTypes.TAX_CALCULATION.getType(),
                calculatorTaxServiceConfiguration.getTaxServiceUri());

        logResponse(
            String.format("TAX API Response for sourceOrderId : %s", sourceOrderId),
            calculateTaxResponse);
        return requireNonNull(calculateTaxResponse).getBody();
      } else {
        return calculateTaxRequestHandling(sourceOrderId);
      }
    };
  }

  private void addHeaders(HttpHeaders httpHeaders) {
    httpHeaders.add(HEADER_PAYLOAD_TYPE, TAX_SERVICE_PAYLOAD);
    httpHeaders.add(TAX_HEADER_API_KEY, calculatorTaxServiceConfiguration.getTaxClientId());
    httpHeaders.add(TAX_HEADER_API_SECRET, calculatorTaxServiceConfiguration.getTaxClientSecret());
  }

  private CalculateTaxResponse calculateTaxRequestHandling(String sourceOrderId) {
    log.info("There are no valid items for invoking TAX API for order : {} ", sourceOrderId);
    return null;
  }

  protected CalculateTaxRequest buildCalculateTaxRequest(List<OmsOrderItem> orderedItemList) {
    Order order = getTaxOrderDetail(orderedItemList);
    return Optional.ofNullable(order).map(this::buildCalculateTaxRequest).orElse(null);
  }

  private CalculateTaxRequest buildCalculateTaxRequest(Order order) {
    CalculateTaxRequest calculateTaxRequest = new CalculateTaxRequest();
    calculateTaxRequest.setCurrency(CalculateTaxRequest.Currency.USD);
    calculateTaxRequest.setIsReverseCalculation(
        calculatorTaxServiceConfiguration.getIsReverseCalculation());
    calculateTaxRequest.setOrder(order);
    calculateTaxRequest.setTransaction(getTaxTransaction());
    return calculateTaxRequest;
  }

  private Order getTaxOrderDetail(List<OmsOrderItem> orderedItemList) {
    List<Item> items = getTaxItems(orderedItemList);
    if (!items.isEmpty()) {
      return getOrderDetails(items);
    }
    return null;
  }

  private Order getOrderDetails(List<Item> items) {
    OrderLine orderLine = new OrderLine();
    List<OrderLine> orderLineList = new ArrayList<>();
    orderLine.setShipToNode(getShipToNode());
    orderLine.setItems(items);
    orderLineList.add(orderLine);
    Order order = new Order();
    order.setOrderLines(orderLineList);
    return order;
  }

  private Node getShipToNode() {
    Node shipToNode = new Node();
    Address address = new Address();
    address.setCountry(COUNTRY_CODE);

    shipToNode.setAddress(address);
    shipToNode.setBaseDivision(1);
    shipToNode.setId(0);
    shipToNode.setType(Node.Type.STORE);
    return shipToNode;
  }

  private List<Item> getTaxItems(List<OmsOrderItem> orderedItemList) {

    List<Item> taxItems = new ArrayList<>();
    orderedItemList.forEach(
        orderedItem -> {
          taxItems.addAll(getItemFromPickedItem(orderedItem));
          taxItems.addAll(getItemFromSubstitutedItem(orderedItem));
        });
    return taxItems;
  }

  private List<Item> getItemFromPickedItem(OmsOrderItem orderedItem) {
    if (orderedItem.isPicked()) {
      return orderedItem.getPickedItemUpcList().stream()
          .map(buildTxItemFromPkdItem(orderedItem))
          .collect(Collectors.toList());
    } else {
      return Collections.emptyList();
    }
  }

  private List<Item> getItemFromSubstitutedItem(OmsOrderItem orderedItem) {
    return orderedItem.getSubstitutedItems().stream()
        .flatMap(substitutedItem -> this.buildTxItemFromSubstitutedItem(substitutedItem).stream())
        .collect(Collectors.toList());
  }

  private Function<PickedItemUpc, Item> buildTxItemFromPkdItem(OmsOrderItem orderItem) {
    return pickedItemUpc ->
        constructTaxCommonReqInfo(
            orderItem.getPickedItemDepartmentId(), pickedItemUpc.getUpc(), pickedItemUpc.getWin());
  }

  private List<Item> buildTxItemFromSubstitutedItem(SubstitutedItem substitutedItem) {

    return substitutedItem.getUpcs().stream()
        .map(
            substitutedItemUpc ->
                constructTaxCommonReqInfo(
                    substitutedItem.getDepartment(),
                    substitutedItemUpc.getUpc(),
                    substitutedItem.getWalmartItemNumber()))
        .collect(Collectors.toList());
  }

  private Item constructTaxCommonReqInfo(String deptNum, String upc, String wmtItemNum) {

    Amount taxAmount = new Amount();
    taxAmount.setType(Amount.Type.FIXED);
    taxAmount.setValue(BigDecimal.ZERO);

    List<Discount> taxDiscounts = new ArrayList<>();
    Discount taxDiscount = new Discount();
    taxDiscount.setAmount(taxAmount);
    taxDiscounts.add(taxDiscount);

    Fee taxFee = new Fee();
    taxFee.setPrice(taxAmount);
    taxFee.setGtin(upc);
    taxFee.setId(STRING);
    taxFee.setNumber(Long.parseLong(wmtItemNum));
    taxFee.setQuantity(1L);
    List<Fee> taxFees = new ArrayList<>();
    taxFees.add(taxFee);

    Item taxItem = new Item();
    taxItem.setDepartment(deptNum != null ? Long.parseLong(deptNum) : null);
    taxItem.setDiscounts(taxDiscounts);
    taxItem.setFees(taxFees);
    taxItem.setGtin(upc);
    taxItem.setFees(taxFees);
    taxItem.setPrice(taxAmount);
    taxItem.setQuantity(1L);
    taxItem.setNumber(Long.parseLong(wmtItemNum));

    return taxItem;
  }

  private Transaction getTaxTransaction() {
    Transaction transaction = new Transaction();
    transaction.setDate(new Date());
    transaction.setType(Transaction.Type.SALES);
    return transaction;
  }

  @Override
  protected int getReadTimeout() {
    return calculatorTaxServiceConfiguration.getReadTimeout();
  }

  @Override
  protected int getConnTimeout() {
    return calculatorTaxServiceConfiguration.getConnTimeout();
  }

  @Override
  public boolean isLogEnabled() {
    return calculatorTaxServiceConfiguration.isLoggingEnabled();
  }

  @Override
  protected String getResourceBaseUri() {
    return calculatorTaxServiceConfiguration.getTaxServiceUri();
  }
}
