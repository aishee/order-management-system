package com.walmart.oms.infrastructure.repository;

import com.walmart.oms.infrastructure.configuration.OmsOrderConfig;
import com.walmart.oms.order.aggregateroot.OmsOrder;
import com.walmart.oms.order.domain.model.CreateDateSearchQuery;
import com.walmart.oms.order.repository.IOmsOrderRepository;
import com.walmart.oms.order.repository.OmsOrderReplayRepository;
import io.strati.configuration.annotation.ManagedConfiguration;
import java.time.LocalDateTime;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

/**
 * This service class is used to fetch all oms order and publish them to Kafka DWH topic using
 * paging.
 */
@Component
@Slf4j
public class OmsOrderReplayRepositoryImpl implements OmsOrderReplayRepository {
  @Autowired private IOmsOrderRepository omsOrderRepository;
  @Autowired private OrderUpdateEventPublisher orderUpdateEventPublisher;
  @ManagedConfiguration private OmsOrderConfig omsOrderConfig;

  /**
   * @param createStartDateTime {@link LocalDateTime} {@code start created date value with time}
   * @param createEndDateTime {@link LocalDateTime} {@code end created date value with time}
   */
  @Override
  public void findAllOrderAndReplayToDwhKafkaTopic(
      LocalDateTime createStartDateTime, LocalDateTime createEndDateTime) {
    int page = 0;
    int orderSize;
    int maxFetchLimit = omsOrderConfig.getMaxOrderFetchLimit();
    do {
      List<OmsOrder> omsOrders =
          findAllOrderByCreatedDateRange(page++, createStartDateTime, createEndDateTime);
      orderSize = omsOrders.size();
      replayOmsOrderToDwhTopic(omsOrders);
    } while (orderSize >= maxFetchLimit);
  }

  private List<OmsOrder> findAllOrderByCreatedDateRange(
      int pageNumber, LocalDateTime createStartDateTime, LocalDateTime createEndDateTime) {
    try {
      log.info(
          "Fetching paged OmsOrder, pageNumber:{}, createStartDate:{}, createEndDate:{}",
          pageNumber,
          createStartDateTime,
          createEndDateTime);
      CreateDateSearchQuery searchQuery =
          CreateDateSearchQuery.builder()
              .createStartDateTime(createStartDateTime)
              .createEndDateTime(createEndDateTime)
              .pageNumber(pageNumber)
              .maxFetchLimit(omsOrderConfig.getMaxOrderFetchLimit())
              .build();
      return omsOrderRepository.findAllOrderByCreatedDateRange(searchQuery);
    } catch (Exception ex) {
      String errMsg =
          String.format(
              "Exception in fetching paged OmsOrder from DB for pageNumber:%d", pageNumber);
      log.error(errMsg, ex);
      throw ex;
    }
  }

  /** @param omsOrders {@link Page of OmsOrder} */
  private void replayOmsOrderToDwhTopic(List<OmsOrder> omsOrders) {
    log.info("Publishing OmsOrder to Kafka Dwh topic, numberOfOrder:{}", omsOrders.size());
    omsOrders.forEach(this::replayOmsOrderToKafkaDwhTopic);
  }

  /**
   * This method republish OmsOrder to Kafka Dwh topic
   *
   * @param omsOrder {@code OmsOrder domain object}
   */
  private void replayOmsOrderToKafkaDwhTopic(OmsOrder omsOrder) {
    try {
      log.info(
          "Republishing OmsOrder to Kafka Dwh topic, StoreOrderId:{}", omsOrder.getStoreOrderId());
      orderUpdateEventPublisher.emitOrderUpdateEvent(omsOrder);
    } catch (Exception ex) {
      String errorMsg =
          String.format(
              "StoreOrderId:%s failed to republish with exception", omsOrder.getSourceOrderId());
      log.error(errorMsg, ex);
    }
  }
}
