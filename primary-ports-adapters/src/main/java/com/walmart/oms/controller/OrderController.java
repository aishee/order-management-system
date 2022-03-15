package com.walmart.oms.controller;

import com.walmart.common.domain.type.Tenant;
import com.walmart.common.domain.type.Vertical;
import com.walmart.common.metrics.MetricConstants.MetricCounters;
import com.walmart.common.metrics.MetricService;
import com.walmart.oms.OmsOrderApplicationService;
import com.walmart.oms.converter.OmsResponseMapper;
import com.walmart.oms.domain.error.exception.OMSBadRequestException;
import com.walmart.oms.dto.OmsOrderResponse;
import com.walmart.oms.order.aggregateroot.OmsOrder;
import com.walmart.oms.port.OmsOrderService;
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
@RequestMapping("/oms")
@Slf4j
public class OrderController implements OmsOrderService {

  private static final String WM_TENANT_ID = "WM_TENANT_ID";
  private static final String WM_VERTICAL_ID = "WM_VERTICAL_ID";

  @Autowired private OmsResponseMapper omsResponseMapper;
  @Autowired private OmsOrderApplicationService orderApplicationService;
  @Autowired private MetricService metricService;

  @Override
  @GetMapping(path = "/orders/{source-order-id}", produces = MediaType.APPLICATION_JSON_VALUE)
  public OmsOrderResponse getOrder(
      @PathVariable("source-order-id") String sourceOrderId,
      @RequestHeader(WM_TENANT_ID) Tenant tenant,
      @RequestHeader(WM_VERTICAL_ID) Vertical vertical) {
    initMDC("OMS_GET_ORDER", "OMS");
    long startTime = System.currentTimeMillis();
    HttpStatus status = HttpStatus.OK;
    try {
      OmsOrder omsOrder = orderApplicationService.getOrder(sourceOrderId, tenant, vertical);
      return omsResponseMapper.convertToOrderResponse(omsOrder);
    } catch (OMSBadRequestException omsBadRequestException) {
      log.error("Exception occurred while fetching OMS order : ", omsBadRequestException);
      status = HttpStatus.BAD_REQUEST;
      incrementMetricCounter(omsBadRequestException);
      throw omsBadRequestException;
    } catch (Exception exception) {
      log.error("Exception occurred while fetching OMS order : ", exception);
      status = HttpStatus.INTERNAL_SERVER_ERROR;
      incrementMetricCounter(exception);
      throw exception;
    } finally {
      log.info(
          "action=OMS_GET_ORDER Completed, "
              + metricService.recordPrimaryPortMetrics(
                  System.currentTimeMillis() - startTime, String.valueOf(status.value())));
      MDC.clear();
    }
  }

  private void incrementMetricCounter(Exception e) {
    String exceptionType = e.getClass().getSimpleName();
    metricService.incrementCounterByType(
        MetricCounters.PRIMARY_PORT_EXCEPTION_COUNTER.getCounter(), exceptionType);
  }

  void initMDC(String api, String domain) {
    MDC.put("api", api);
    MDC.put("domain", domain);
  }
}
