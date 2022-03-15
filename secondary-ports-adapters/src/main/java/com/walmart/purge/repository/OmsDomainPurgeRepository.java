package com.walmart.purge.repository;

import com.walmart.oms.domain.error.exception.OMSThirdPartyException;
import com.walmart.purge.configuration.OmsPurgeConfig;
import io.strati.configuration.annotation.ManagedConfiguration;
import io.strati.libs.google.common.util.concurrent.ThreadFactoryBuilder;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import javax.annotation.PostConstruct;
import javax.persistence.EntityManager;
import javax.persistence.ParameterMode;
import javax.persistence.StoredProcedureQuery;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

/**
 * This repository class uses EntityManager to call the procedures. Procedure are used to purge
 * historical data from OMSCORE DB. UKGRFF-669
 */
@Slf4j
@Repository
public class OmsDomainPurgeRepository {
  private static final ThreadFactory THREAD_FACTORY =
      new ThreadFactoryBuilder()
          .setNameFormat("Oms-Purge-Thread-Pool-%d")
          .setThreadFactory(Executors.defaultThreadFactory())
          .build();
  @Autowired private EntityManager entityManager;
  @ManagedConfiguration private OmsPurgeConfig omsPurgeConfig;
  private Scheduler elasticPool;

  @PostConstruct
  protected void initElasticPool() {
    elasticPool = Schedulers.newElastic(omsPurgeConfig.getTtlSeconds(), THREAD_FACTORY);
  }

  /**
   * @param dayToSub {@code number of day to be subtracted from the current date to form the where
   *     clause.}
   * @return {@code procedure result Mono true/false}
   */
  public Mono<Boolean> purgeEgressEvents(int dayToSub) {
    log.info("Calling Egress Event Procedure with dayToSub : {}", dayToSub);
    return callProcedure(omsPurgeConfig.getEgressEventProcedure(), dayToSub);
  }

  /**
   * @param dayToSub {@code number of day to be subtracted from the current date to form the where
   *     clause.}
   * @return {@code procedure result Mono of true/false}
   */
  public Mono<Boolean> purgeMarketPlaceOrder(int dayToSub) {
    log.info("Calling Market Place Order Procedure with dayToSub : {}", dayToSub);
    return callProcedure(omsPurgeConfig.getMarketplaceOrderProcedure(), dayToSub);
  }

  /**
   * @param dayToSub {@code number of day to be subtracted from the current date to form the where
   *     clause.}
   * @return {@code procedure result Mono true/false}
   */
  public Mono<Boolean> purgeFulfilmentOrder(int dayToSub) {
    log.info("Calling Fulfilment Order Procedure with dayToSub : {}", dayToSub);
    return callProcedure(omsPurgeConfig.getFulfilmentOrderProcedure(), dayToSub);
  }

  /**
   * @param dayToSub {@code number of day to be subtracted from the current date to form the where
   *     clause.}
   * @return {@code procedure result Mono of true/false}
   */
  public Mono<Boolean> purgeOmsOrder(int dayToSub) {
    log.info("Calling OMS Order Procedure with dayToSub : {}", dayToSub);
    return callProcedure(omsPurgeConfig.getOmsOrderProcedure(), dayToSub);
  }

  /**
   * @param dayToSub {@code number of day to be subtracted from the current date to form the where
   *     clause.}
   * @return {@code procedure result Mono of true/false}
   */
  public Mono<Boolean> purgeMarketPlaceEvent(int dayToSub) {
    log.info("Calling Market Place Event Procedure with dayToSub : {}", dayToSub);
    return callProcedure(omsPurgeConfig.getMarketplaceEventProcedure(), dayToSub);
  }

  /**
   * @param procedureName {@code name of the procedure to invoke for purge.}
   * @param dayToSub {@code number of day to be subtracted from the current date to form the where
   *     clause.}
   * @return {@code procedure result true/false}
   */
  private Mono<Boolean> callProcedure(String procedureName, int dayToSub) {
    return Mono.fromCallable(() -> executeCall(procedureName, dayToSub))
        .doOnSuccess(
            status ->
                log.info(
                    "Called procedure, procedureName : {}, with dayToSub : {}, callingStatus: {}",
                    procedureName,
                    dayToSub,
                    status))
        .onErrorResume(ex -> handlePurgeError(ex, procedureName, dayToSub))
        .subscribeOn(elasticPool);
  }

  private boolean executeCall(String procedureName, int dayToSub) {
    StoredProcedureQuery query = this.entityManager.createStoredProcedureQuery(procedureName);
    query.registerStoredProcedureParameter("dayToSubs", Integer.class, ParameterMode.IN);
    query.setParameter("dayToSubs", dayToSub);
    return query.execute();
  }

  private Mono<Boolean> handlePurgeError(Throwable ex, String procedureName, int dayToSub) {
    String message =
        String.format(
            "Exception while calling historical data purge. procedureName: %s, dayToSub: %d message: %s",
            procedureName, dayToSub, ex.getMessage());
    log.error(message, ex);
    throw new OMSThirdPartyException(message, ex);
  }
}
