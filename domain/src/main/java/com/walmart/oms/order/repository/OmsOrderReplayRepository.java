package com.walmart.oms.order.repository;

import java.time.LocalDateTime;

/**
 * This service interface create a contract to fetch all oms order and republish them to Kafka DWH
 * topic using pagination.
 */
public interface OmsOrderReplayRepository {
  /**
   * @param createStartDateTime {@link LocalDateTime} {@code start created date value with time}
   * @param createEndDateTime {@link LocalDateTime} {@code end created date value with time}
   */
  void findAllOrderAndReplayToDwhKafkaTopic(
      LocalDateTime createStartDateTime, LocalDateTime createEndDateTime);
}
