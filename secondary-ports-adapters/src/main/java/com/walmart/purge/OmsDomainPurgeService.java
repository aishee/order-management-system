package com.walmart.purge;

import com.walmart.purge.repository.OmsDomainPurgeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

/**
 * This service class is purging the historical records from OMSCORE DB in Sql Server. UKGRFF-669
 */
@Slf4j
@RequiredArgsConstructor
@Service
@Transactional
public class OmsDomainPurgeService {
  private final OmsDomainPurgeRepository omsDomainPurgeRepository;

  /**
   * @param dayToSub {@code number of day to be subtracted from the current date to form the where
   *     clause.}
   * @return {@code procedure result Mono of true/false}
   */
  public Mono<Boolean> purgeEgressEvents(int dayToSub) {
    log.info("Calling Egress Event purge with dayToSub : {}", dayToSub);
    return omsDomainPurgeRepository.purgeEgressEvents(dayToSub);
  }

  /**
   * @param dayToSub {@code number of day to be subtracted from the current date to form the where
   *     clause.}
   * @return {@code procedure result Mono of true/false}
   */
  public Mono<Boolean> purgeMarketPlaceOrder(int dayToSub) {
    log.info("Calling Market Place Order purge with dayToSub : {}", dayToSub);
    return omsDomainPurgeRepository.purgeMarketPlaceOrder(dayToSub);
  }

  /**
   * @param dayToSub {@code number of day to be subtracted from the current date to form the where
   *     clause.}
   * @return {@code procedure result Mono of true/false}
   */
  public Mono<Boolean> purgeFulfilmentOrder(int dayToSub) {
    log.info("Calling Fulfilment Order purge with dayToSub : {}", dayToSub);
    return omsDomainPurgeRepository.purgeFulfilmentOrder(dayToSub);
  }

  /**
   * @param dayToSub {@code number of day to be subtracted from the current date to form the where
   *     clause.}
   * @return {@code procedure result Mono of true/false}
   */
  public Mono<Boolean> purgeOmsOrder(int dayToSub) {
    log.info("Calling OMS Order purge with dayToSub : {}", dayToSub);
    return omsDomainPurgeRepository.purgeOmsOrder(dayToSub);
  }

  /**
   * @param dayToSub {@code number of day to be subtracted from the current date to form the where
   *     clause.}
   * @return {@code procedure result Mono of true/false}
   */
  public Mono<Boolean> purgeMarketPlaceEvent(int dayToSub) {
    log.info("Calling Market Place Event purge with dayToSub : {}", dayToSub);
    return omsDomainPurgeRepository.purgeMarketPlaceEvent(dayToSub);
  }
}
