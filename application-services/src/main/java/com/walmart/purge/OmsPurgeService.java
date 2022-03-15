package com.walmart.purge;

import com.walmart.common.domain.valueobject.MetricsValueObject;
import com.walmart.common.metrics.MetricConstants;
import com.walmart.common.metrics.MetricService;
import com.walmart.oms.domain.error.exception.OMSThirdPartyException;
import com.walmart.purge.configuration.OmsPurgeConfig;
import io.strati.configuration.annotation.ManagedConfiguration;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

/**
 * This service class is purging the historical records from OMSCORE DB in Sql Server. UKGRFF-669
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class OmsPurgeService {
  private static final String PURGE_STATUS_MESSAGE =
      "Purge request is accepted. System will execute purge based on input days.";
  private final OmsDomainPurgeService omsDomainPurgeService;
  private final MetricService metricService;
  @Setter @ManagedConfiguration private OmsPurgeConfig omsPurgeConfig;

  /**
   * This method is calling domain purge service parallel to purge historical data from OMSCORE DB.
   *
   * @return {@link Mono of status}
   */
  public Mono<String> purgeHistoricalData() {
    int dayToSub = omsPurgeConfig.getDayToSub();
    log.info("Starting historical data purge with dayToSub:{}", dayToSub);
    Mono<Boolean> egressEventPurgeMono = omsDomainPurgeService.purgeEgressEvents(dayToSub);
    Mono<Boolean> marketPlacePurgeMono = omsDomainPurgeService.purgeMarketPlaceOrder(dayToSub);
    Mono<Boolean> fulfilmentOrderPurgeMono = omsDomainPurgeService.purgeFulfilmentOrder(dayToSub);
    Mono<Boolean> omsOrderPurgeMono = omsDomainPurgeService.purgeOmsOrder(dayToSub);
    Mono<Boolean> marketPlaceEventPurgeMono = omsDomainPurgeService.purgeMarketPlaceEvent(dayToSub);
    long startTime = System.currentTimeMillis();
    return Mono.zip(
            egressEventPurgeMono,
            marketPlacePurgeMono,
            fulfilmentOrderPurgeMono,
            omsOrderPurgeMono,
            marketPlaceEventPurgeMono)
        .onErrorResume(ex -> handlePurgeError(ex, startTime))
        .doOnSuccess(
            statuses -> log.info("Purge completed successfully with dayToSub:{}", dayToSub))
        .thenReturn(PURGE_STATUS_MESSAGE);
  }

  private <T> Mono<T> handlePurgeError(Throwable ex, long startTime) {
    long duration = System.currentTimeMillis() - startTime;
    String exceptionType = ex.getClass().getSimpleName();
    MetricsValueObject metricsValueObject =
        getMetricsValueObject(
            MetricConstants.MetricTypes.OMS_PURGE.getType(),
            duration,
            String.valueOf(HttpStatus.INTERNAL_SERVER_ERROR));
    String message =
        String.format(
            "Exception in historical data purge, message: %s, executionTime: %s",
            ex.getMessage(), metricService.recordExecutionTime(metricsValueObject));
    metricService.incrementExceptionCounterByType(
        MetricConstants.MetricCounters.OMS_PURGE_EXCEPTION_COUNTER.getCounter(),
        MetricConstants.MetricTypes.OMS_PURGE.getType(),
        exceptionType);
    log.error(message, ex);
    throw new OMSThirdPartyException(message, ex);
  }

  private MetricsValueObject getMetricsValueObject(String type, long duration, String status) {
    return MetricsValueObject.builder()
        .counterName(MetricConstants.MetricCounters.OMS_PURGE_EXCEPTION_COUNTER.getCounter())
        .totalExecutionTime(duration)
        .type(type)
        .isSuccess(false)
        .status(status)
        .build();
  }
}
