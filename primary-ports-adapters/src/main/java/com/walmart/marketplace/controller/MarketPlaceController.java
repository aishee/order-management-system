package com.walmart.marketplace.controller;

import static com.walmart.common.constants.CommonConstants.MARKETPLACE;

import com.walmart.common.metrics.MetricConstants.MetricCounters;
import com.walmart.common.metrics.MetricService;
import com.walmart.fms.domain.error.exception.FMSBadRequestException;
import com.walmart.marketplace.MarketPlaceApplicationService;
import com.walmart.marketplace.commands.MarketPlaceCreateOrderCommand;
import com.walmart.marketplace.converter.MarketPlaceResponseMapper;
import com.walmart.marketplace.converter.RequestToCommandMapper;
import com.walmart.marketplace.dto.request.CreateMarketPlaceOrderRequest;
import com.walmart.marketplace.dto.response.MarketPlaceOrderResponse;
import com.walmart.marketplace.order.aggregateroot.MarketPlaceOrder;
import com.walmart.oms.domain.error.exception.OMSBadRequestException;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/marketplace")
public class MarketPlaceController {

  @Autowired private RequestToCommandMapper mapper;

  @Autowired private MarketPlaceResponseMapper responseMapper;

  @Autowired private MarketPlaceApplicationService marketPlaceApplicationService;

  @Autowired private MetricService metricService;

  @PostMapping(path = "/orders", produces = MediaType.APPLICATION_JSON_VALUE)
  public MarketPlaceOrderResponse createOrder(
      @RequestBody CreateMarketPlaceOrderRequest createRequest) {
    initMDC("MARKETPLACE_CREATE_ORDER", MARKETPLACE);
    long startTime = System.currentTimeMillis();
    HttpStatus status = HttpStatus.OK;
    try {
      MarketPlaceCreateOrderCommand createOrderCommand =
          mapper.createMarketPlaceOrderFromRequest(createRequest);
      MarketPlaceOrder marketPlaceOrder =
          marketPlaceApplicationService.createAndProcessMarketPlaceOrder(createOrderCommand);
      return responseMapper.mapToDto(marketPlaceOrder);
    } catch (OMSBadRequestException | FMSBadRequestException exception) {
      log.error("Exception occurred while MarketPlace order creation : ", exception);
      status = HttpStatus.BAD_REQUEST;
      incrementMetricCounter(exception);
      throw exception;
    } catch (Exception exception) {
      log.error("Exception occurred while MarketPlace order creation : ", exception);
      status = HttpStatus.INTERNAL_SERVER_ERROR;
      incrementMetricCounter(exception);
      throw exception;
    } finally {
      log.info(
          "action=MarketPlaceOrderCreation Completed, "
              + metricService.recordPrimaryPortMetrics(
                  System.currentTimeMillis() - startTime, String.valueOf(status.value())));
      MDC.clear();
    }
  }

  @GetMapping(path = "/orders/{vendor-order-id}", produces = MediaType.APPLICATION_JSON_VALUE)
  public MarketPlaceOrderResponse getOrder(@PathVariable("vendor-order-id") String vendorOrderId) {
    initMDC("MARKETPLACE_GET_ORDER", MARKETPLACE);
    long startTime = System.currentTimeMillis();
    HttpStatus status = HttpStatus.OK;
    try {
      MarketPlaceOrder marketPlaceOrder = marketPlaceApplicationService.getOrder(vendorOrderId);
      return responseMapper.mapToDto(marketPlaceOrder);
    } catch (OMSBadRequestException | FMSBadRequestException exception) {
      log.error(
          "Exception occurred while fetching the Marketplace order {} : ",
          vendorOrderId,
          exception);
      status = HttpStatus.BAD_REQUEST;
      incrementMetricCounter(exception);
      throw exception;
    } catch (Exception exception) {
      log.error(
          "Exception occurred while fetching the Marketplace order {} : ",
          vendorOrderId,
          exception);
      status = HttpStatus.INTERNAL_SERVER_ERROR;
      incrementMetricCounter(exception);
      throw exception;
    } finally {
      log.info(
          "action=MarketPlaceOrderGet Completed, "
              + metricService.recordPrimaryPortMetrics(
                  System.currentTimeMillis() - startTime, String.valueOf(status.value())));
      MDC.clear();
    }
  }

  void initMDC(String api, String domain) {
    MDC.put("api", api);
    MDC.put("domain", domain);
  }

  private void incrementMetricCounter(Exception exception) {
    String exceptionType = exception.getClass().getSimpleName();
    metricService.incrementCounterByType(
        MetricCounters.PRIMARY_PORT_EXCEPTION_COUNTER.getCounter(), exceptionType);
  }
}
