package com.walmart.oms;

import com.google.common.annotations.VisibleForTesting;
import com.walmart.common.ApplicationMetrics;
import com.walmart.common.ExecutorFactory;
import com.walmart.common.domain.valueobject.MetricsValueObject;
import com.walmart.common.metrics.MetricConstants;
import com.walmart.common.metrics.MetricService;
import com.walmart.oms.commands.SearchOmsOrderOnCreateDateCommand;
import com.walmart.oms.domain.error.exception.OMSThirdPartyException;
import com.walmart.oms.order.repository.OmsOrderReplayRepository;
import java.time.LocalDateTime;
import java.util.concurrent.Executor;
import javax.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

/** This service class used to replay(republish) OmsOrders to Kafka Dwh topic. */
@Component
@Slf4j
@RequiredArgsConstructor
public class OmsOrderReplayApplicationService extends ApplicationMetrics {
  private static final String THREAD_FACTORY_NAME_PATTERN = "replay-omsorder-factory-%d";
  private final OmsOrderReplayRepository omsOrderReplayRepository;
  private final MetricService metricService;
  private Executor executor;

  @PostConstruct
  protected void initExecutor() {
    executor = ExecutorFactory.newSingleThreadExecutor(THREAD_FACTORY_NAME_PATTERN);
  }

  /**
   * This method republish OmsOrder to Kafka Dwh topic.
   *
   * @param searchOmsOrderOnCreateDateCommand {@code command object with create start date and
   *     create end date}
   */
  public void executeReplayOmsOrder(
      SearchOmsOrderOnCreateDateCommand searchOmsOrderOnCreateDateCommand) {
    Runnable runnable =
        () ->
            findAllOrderAndReplayToDwhKafkaTopic(
                searchOmsOrderOnCreateDateCommand.getCreateStartDateTime(),
                searchOmsOrderOnCreateDateCommand.getCreateEndDateTime());
    executor.execute(runnable);
  }

  @VisibleForTesting
  private void findAllOrderAndReplayToDwhKafkaTopic(
      LocalDateTime createStartDateTime, LocalDateTime createEndDateTime) {
    long startTime = System.currentTimeMillis();
    try {
      log.info(
          "Starting find all order and replay, createStartDate:{}, createEndDate:{}, requestStartTime:{}",
          createStartDateTime,
          createEndDateTime,
          startTime);
      omsOrderReplayRepository.findAllOrderAndReplayToDwhKafkaTopic(
          createStartDateTime, createEndDateTime);
      doOnSuccess(startTime);
      log.info("Completed find and replay oms order");
    } catch (Exception ex) {
      log.error("Exception in fetching paged OmsOrder from DB", ex);
      handleReplayError(ex, startTime);
    }
  }

  private void doOnSuccess(long startTime) {
    MetricsValueObject metricsValueObject = getMetricsValueObject(startTime, HttpStatus.OK);
    String message =
        String.format(
            "Publishing OmsOrder to Kafka Dwh topic is success, executionTime: %s",
            metricService.recordExecutionTime(metricsValueObject));
    log.info(message);
  }

  private void handleReplayError(Throwable ex, long startTime) {
    String exceptionType = ex.getClass().getSimpleName();
    MetricsValueObject metricsValueObject =
        getMetricsValueObject(startTime, HttpStatus.INTERNAL_SERVER_ERROR);
    String message =
        String.format(
            "Exception in publishing OmsOrder to Kafka Dwh topic, message: %s, executionTime: %s",
            ex.getMessage(), metricService.recordExecutionTime(metricsValueObject));
    metricService.incrementExceptionCounterByType(
        MetricConstants.MetricCounters.OMS_ORDER_REPLAY_EXCEPTION_COUNTER.getCounter(),
        MetricConstants.MetricTypes.OMS_ORDER_REPLAY.getType(),
        exceptionType);
    log.error(message, ex);
    throw new OMSThirdPartyException(message, ex);
  }

  @Override
  protected MetricConstants.MetricCounters getMetricsExceptionCounterName() {
    return MetricConstants.MetricCounters.OMS_ORDER_REPLAY_EXCEPTION_COUNTER;
  }

  @Override
  protected MetricConstants.MetricCounters getMetricsCounterName() {
    return MetricConstants.MetricCounters.OMS_ORDER_REPLAY_INVOCATION_COUNTER;
  }

  @Override
  protected MetricConstants.MetricTypes getMetricsType() {
    return MetricConstants.MetricTypes.OMS_ORDER_REPLAY;
  }
}
