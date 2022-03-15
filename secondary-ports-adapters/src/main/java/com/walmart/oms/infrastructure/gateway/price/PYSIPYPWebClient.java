package com.walmart.oms.infrastructure.gateway.price;

import com.walmart.common.infrastructure.ApiAction;
import com.walmart.common.infrastructure.webclient.BaseWebClient;
import com.walmart.common.metrics.MetricConstants;
import com.walmart.common.metrics.MetricConstants.MetricCounters;
import com.walmart.oms.domain.error.exception.OMSBadRequestException;
import com.walmart.oms.domain.error.exception.OMSThirdPartyException;
import com.walmart.oms.domain.event.messages.OrderCancelledDomainEventMessage;
import com.walmart.oms.infrastructure.constants.PYSIPYPConstants;
import com.walmart.oms.infrastructure.converter.OmsOrderToPysipypOrderInfoMapper;
import com.walmart.oms.infrastructure.gateway.price.dto.OrderInformation;
import com.walmart.oms.order.aggregateroot.OmsOrder;
import com.walmart.tax.calculator.dto.Tax;
import io.strati.configuration.annotation.ManagedConfiguration;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ThreadFactory;
import javax.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
@Slf4j
public class PYSIPYPWebClient extends BaseWebClient {

  private static final String PYSIPYP = "PYSIPYP";
  private static final String THREAD_FACTORY_NAME = "PYSIPYP-thread-pool-%d";

  @ManagedConfiguration PYSIPYPServiceConfiguration pysipypServiceConfiguration;

  @Autowired OmsOrderToPysipypOrderInfoMapper omsOrderToPysipypOrderInfoMapper;

  @Override
  protected String getResourceBaseUri() {
    return pysipypServiceConfiguration.getPysipypServiceUri();
  }

  @Override
  protected int getReadTimeout() {
    return pysipypServiceConfiguration.getReadTimeout();
  }

  @Override
  protected int getConnTimeout() {
    return pysipypServiceConfiguration.getConnTimeout();
  }

  @Override
  public boolean isLogEnabled() {
    return pysipypServiceConfiguration.isLoggingEnabled();
  }

  @PostConstruct
  @Override
  public void initialize() {
    super.initialize();
  }

  @Override
  protected String getClientName() {
    return PYSIPYP;
  }

  @Override
  protected ThreadFactory getThreadFactory() {
    return new BasicThreadFactory.Builder().namingPattern(THREAD_FACTORY_NAME).build();
  }

  @Override
  protected int getThreadPoolSize() {
    return pysipypServiceConfiguration.getThreadPoolSize();
  }

  @Override
  public String getMetricsCounterName() {
    return MetricCounters.SECONDARY_PORT_INVOCATION.getCounter();
  }

  @Override
  public String getMetricsExceptionCounterName() {
    return MetricCounters.PYSIPYP_EXCEPTION.getCounter();
  }

  public OrderInformation getPriceData(OmsOrder order, Map<String, Tax> taxInfoMap) {
    OrderInformation pysipypRequest =
        omsOrderToPysipypOrderInfoMapper.buildPysipypRequestforMarketPlaceOrder(order, taxInfoMap);
    return invokeRecordSaleApi(pysipypRequest);
  }

  public OrderInformation reverseSale(
      OrderCancelledDomainEventMessage orderCancelledDomainEventMessage) {
    OrderInformation pysipypRequest =
        omsOrderToPysipypOrderInfoMapper.buildReverseSaleRequest(orderCancelledDomainEventMessage);
    return invokeReverseSaleApi(pysipypRequest);
  }

  private OrderInformation invokeReverseSaleApi(OrderInformation orderInformation) {
    String reverseSaleUrl =
        getPysipypEndpoint(PYSIPYPConstants.REVERSE_SALE, orderInformation.getOrderNumber());
    return executeApiWithRetries(
        getPysipypReverseSaleCallable(reverseSaleUrl, orderInformation), ApiAction.PYSIPYP);
  }

  private Callable<OrderInformation> getPysipypReverseSaleCallable(
      String reverseSaleUrl, OrderInformation orderInformation) {
    return () -> {
      logXMLRequest(reverseSaleUrl, orderInformation);
      // Web Client API call
      WebClient.ResponseSpec responseSpec =
          webClient
              .post()
              .uri(reverseSaleUrl)
              .body(Mono.just(orderInformation), OrderInformation.class)
              .header(
                  PYSIPYPConstants.HEADER_PAYLOAD_TYPE, PYSIPYPConstants.ORDER_ITEMS_RETURN_PAYLOAD)
              .header(PYSIPYPConstants.CONTENT_TYPE, PYSIPYPConstants.APPLICATION_XML)
              .retrieve();

      ResponseEntity<OrderInformation> pysipypReverseSaleResponseEntity =
          captureMetrics(
              responseSpec,
              OrderInformation.class,
              MetricConstants.MetricTypes.PYSIPYP_REVERSE_SALE.getType(),
              reverseSaleUrl);

      OrderInformation pysipypReverseSaleResponse = pysipypReverseSaleResponseEntity.getBody();

      return handlePysipypResponse(reverseSaleUrl, pysipypReverseSaleResponse, orderInformation);
    };
  }

  private OrderInformation invokeRecordSaleApi(OrderInformation orderInformation) {
    String recordSaleUrl =
        getPysipypEndpoint(PYSIPYPConstants.ODS_CALC_TOTAL, orderInformation.getOrderNumber());
    return executeApiWithRetries(
        getPysipypRecordSaleCallable(recordSaleUrl, orderInformation), ApiAction.PYSIPYP);
  }

  private Callable<OrderInformation> getPysipypRecordSaleCallable(
      String url, OrderInformation orderInformation) {
    return () -> {
      logXMLRequest(url, orderInformation);
      // Web Client API call
      WebClient.ResponseSpec responseSpec =
          webClient
              .post()
              .uri(url)
              .body(Mono.just(orderInformation), OrderInformation.class)
              .header(
                  PYSIPYPConstants.HEADER_PAYLOAD_TYPE,
                  PYSIPYPConstants.ORDER_PICK_COMPLETE_PAYLOAD)
              .header(PYSIPYPConstants.CONTENT_TYPE, PYSIPYPConstants.APPLICATION_XML)
              .retrieve();

      ResponseEntity<OrderInformation> pysipypResponseEntity =
          captureMetrics(
              responseSpec,
              OrderInformation.class,
              MetricConstants.MetricTypes.PYSIPYP_RECORD_SALE.getType(),
              url);

      OrderInformation pysipypResponse = pysipypResponseEntity.getBody();
      return handlePysipypResponse(url, pysipypResponse, orderInformation);
    };
  }

  private OrderInformation handlePysipypResponse(
      String pysipypUrl, OrderInformation pysipypResponse, OrderInformation pysipypRequest) {
    if (ObjectUtils.isEmpty(pysipypResponse)) {
      String errorMessage =
          String.format(
              "PYSIPYP call returned with empty response for orderNumber: %s",
              pysipypRequest.getOrderNumber());
      log.error(errorMessage);
      throw new OMSThirdPartyException(errorMessage);
    } else if (!ObjectUtils.isEmpty(pysipypResponse.getError())) {
      String errorMessage =
          String.format(
              "PYSIPYP call returned with error for orderNumber : %s errorMessage : %s",
              pysipypRequest.getOrderNumber(), pysipypResponse.getError());
      log.error(errorMessage);
      throw new OMSThirdPartyException(errorMessage);
    }
    logXMLResponse(pysipypUrl, pysipypResponse);
    return pysipypResponse;
  }

  private String getPysipypEndpoint(String messageType, String orderId) {
    switch (messageType) {
      case PYSIPYPConstants.ODS_CALC_TOTAL:
        return pysipypServiceConfiguration.getPysipypServiceUri();
      case PYSIPYPConstants.REVERSE_SALE:
        return pysipypServiceConfiguration.getPysipypServiceUriForOrders()
            + orderId
            + PYSIPYPConstants.SLASH
            + PYSIPYPConstants.REVERSE_SALE;
      default:
        String message =
            String.format(
                "No valid PYSIPYP endpoint found for messageType : %s and storeOrderId : %s",
                messageType, orderId);

        throw new OMSBadRequestException(message);
    }
  }
}
