package com.walmart.oms.controller;

import com.walmart.common.mdc.MDCRecorder;
import com.walmart.common.metrics.MetricConstants;
import com.walmart.common.metrics.MetricService;
import com.walmart.oms.OmsOrderReplayApplicationService;
import com.walmart.oms.commands.SearchOmsOrderOnCreateDateCommand;
import com.walmart.oms.converter.OmsOrderReplayRequestToSearchCommandMapper;
import com.walmart.oms.domain.error.exception.OMSBadRequestException;
import com.walmart.oms.dto.OmsOrderReplayRequest;
import com.walmart.oms.dto.response.OmsOrderRepublishResponse;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/** This controller class used to replay(republish) OmsOrders to Kafka Dwh topic. */
@RestController
@RequestMapping("/oms")
@Slf4j
@RequiredArgsConstructor
public class OmsOrderReplayController {
  private final MetricService metricService;
  private final OmsOrderReplayRequestToSearchCommandMapper orderReplayRequestToSearchCommandMapper;
  private final OmsOrderReplayApplicationService omsOrderReplayApplicationService;

  @PostMapping(value = "/orders/republish-to-kafka-dwh")
  @ResponseStatus(HttpStatus.ACCEPTED)
  public OmsOrderRepublishResponse replayOmsOrderToKafkaDwhTopic(
      @RequestBody @Valid OmsOrderReplayRequest omsOrderReplayRequest) {
    MDCRecorder.initMDC("REPUBLISH_TO_KAFKA_DWH", "OMS");
    long startTime = System.currentTimeMillis();
    HttpStatus status = HttpStatus.ACCEPTED;
    try {
      log.info("Starting replay oms order with requestPayload:{}", omsOrderReplayRequest);
      omsOrderReplayRequest.validate();
      SearchOmsOrderOnCreateDateCommand omsOrderOnCreateDateCommand =
          orderReplayRequestToSearchCommandMapper.mapToSearchOmsOrderOnCreateDate(
              omsOrderReplayRequest);
      omsOrderReplayApplicationService.executeReplayOmsOrder(omsOrderOnCreateDateCommand);
      return OmsOrderRepublishResponse.accepted();
    } catch (OMSBadRequestException ex) {
      String errMsg =
          String.format(
              "Exception occurred while republishing OMS order to Kafka DWH topic, createStartDateTime:%s, createEndDateTime:%s",
              omsOrderReplayRequest.getCreateStartDateTime(),
              omsOrderReplayRequest.getCreateEndDateTime());
      log.error(errMsg, ex);
      status = HttpStatus.BAD_REQUEST;
      incrementMetricCounter(ex);
      throw ex;
    } catch (Exception ex) {
      String errMsg =
          String.format(
              "Exception occurred while republishing OMS order to Kafka DWH topic, createStartDateTime:%s, createEndDateTime:%s",
              omsOrderReplayRequest.getCreateStartDateTime(),
              omsOrderReplayRequest.getCreateEndDateTime());
      log.error(errMsg, ex);
      status = HttpStatus.INTERNAL_SERVER_ERROR;
      incrementMetricCounter(ex);
      throw ex;
    } finally {
      log.info(
          "action=republish-to-kafka-dwh Completed, metrics:{}̵",
          metricService.recordPrimaryPortMetrics(
              System.currentTimeMillis() - startTime, String.valueOf(status.value())));
      MDCRecorder.clear();
    }
  }

  private void incrementMetricCounter(Exception e) {
    String exceptionType = e.getClass().getSimpleName();
    metricService.incrementCounterByType(
        MetricConstants.MetricCounters.OMS_ORDER_REPLAY_EXCEPTION_COUNTER.getCounter(),
        exceptionType);
  }
}
