package com.walmart.fms.controller;

import com.walmart.common.domain.type.Domain;
import com.walmart.common.domain.type.Tenant;
import com.walmart.common.domain.type.Vertical;
import com.walmart.common.metrics.MetricConstants.MetricCounters;
import com.walmart.common.metrics.MetricService;
import com.walmart.fms.FmsOrderApplicationService;
import com.walmart.fms.converter.FmsResponseMapper;
import com.walmart.fms.domain.error.exception.FMSBadRequestException;
import com.walmart.fms.dto.FmsOrderResponse;
import com.walmart.fms.order.aggregateroot.FmsOrder;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/fulfillment")
@Slf4j
public class FmsOrderController {

  private static final String WM_TENANT_ID = "WM_TENANT_ID";
  private static final String WM_VERTICAL_ID = "WM_VERTICAL_ID";

  @Autowired private FmsResponseMapper responseMapper;
  @Autowired private FmsOrderApplicationService fmsOrderApplicationService;
  @Autowired private MetricService metricService;

  @GetMapping(path = "/orders/{sourceOrderId}", produces = MediaType.APPLICATION_JSON_VALUE)
  public FmsOrderResponse getOrder(
      @PathVariable(name = "sourceOrderId") String sourceOrderId,
      @RequestHeader(WM_TENANT_ID) Tenant tenant,
      @RequestHeader(WM_VERTICAL_ID) Vertical vertical) {
    initMDC("FMS_GET_ORDER", Domain.FMS.getDomainName());
    long startTime = System.currentTimeMillis();
    HttpStatus status = HttpStatus.OK;

    try {
      FmsOrder fmsOrder = fmsOrderApplicationService.getOrder(sourceOrderId, tenant, vertical);
      return responseMapper.convertToOrderResponse(fmsOrder);
    } catch (FMSBadRequestException ef) {
      status = HttpStatus.BAD_REQUEST;
      String exceptionType = ef.getClass().getSimpleName();
      metricService.incrementCounterByType(
          MetricCounters.PRIMARY_PORT_EXCEPTION_COUNTER.getCounter(), exceptionType);
      log.error(
          "Exception Occurred While fetching order sourceOrderId {} , status {} , error {}",
          sourceOrderId,
          status,
          ef);
      throw ef;
    } catch (Exception e) {
      status = HttpStatus.INTERNAL_SERVER_ERROR;
      String exceptionType = e.getClass().getSimpleName();
      metricService.incrementCounterByType(
          MetricCounters.PRIMARY_PORT_EXCEPTION_COUNTER.getCounter(), exceptionType);
      log.error(
          "Exception Occurred While fetching order sourceOrderId {} , status {} , error {}",
          sourceOrderId,
          status,
          e);
      throw e;
    } finally {
      log.info(
          "action=FMSOrderGet for order {}, Completed, {}",
          sourceOrderId,
          metricService.recordPrimaryPortMetrics(
              System.currentTimeMillis() - startTime, String.valueOf(status.value())));
      MDC.clear();
    }
  }

  void initMDC(String api, String domain) {
    MDC.put("api", api);
    MDC.put("domain", domain);
  }
}
