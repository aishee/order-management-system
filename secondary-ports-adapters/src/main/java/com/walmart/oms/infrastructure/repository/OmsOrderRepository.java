package com.walmart.oms.infrastructure.repository;

import com.walmart.common.domain.type.Tenant;
import com.walmart.common.domain.type.Vertical;
import com.walmart.oms.domain.error.exception.OMSBadRequestException;
import com.walmart.oms.infrastructure.repository.infrastructure.mssql.IOmsOrderSqlServerRepository;
import com.walmart.oms.infrastructure.repository.infrastructure.mssql.OmsSqlServerBaseRepository;
import com.walmart.oms.order.aggregateroot.OmsOrder;
import com.walmart.oms.order.domain.model.CreateDateSearchQuery;
import com.walmart.oms.order.repository.IOmsOrderRepository;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Repository
public class OmsOrderRepository implements IOmsOrderRepository {

  @Autowired private IOmsOrderSqlServerRepository omsOrderSqlServerRepository;
  @Autowired private OmsSqlServerBaseRepository omsOrderIOmsSqlServerBaseRepository;
  @Autowired private OrderUpdateEventPublisher orderUpdateEventPublisher;

  @Override
  public OmsOrder getOrderByMarketPlaceId(
      String marketPlaceOrderId, Tenant tenant, Vertical vertical) {

    return omsOrderSqlServerRepository.findByMarketPlaceInfo_VendorOrderIdAndTenantAndVertical(
        marketPlaceOrderId, tenant, vertical);
  }

  @Override
  public OmsOrder getOrder(String orderId, Tenant tenant, Vertical vertical) {

    OmsOrder existingOrder =
        omsOrderSqlServerRepository.findBySourceOrderIdAndTenantAndVertical(
            orderId, tenant, vertical);
    if (Objects.isNull(existingOrder)) {
      throw new OMSBadRequestException("Unable to find order for id:" + orderId);
    }
    return existingOrder;
  }

  @Override
  public OmsOrder save(OmsOrder omsOrder) {
    omsOrder = omsOrderSqlServerRepository.save(omsOrder);
    orderUpdateEventPublisher.emitOrderUpdateEvent(omsOrder);
    return omsOrder;
  }

  /**
   * This method used to find all OmsOrder based on the CreatedDate.
   *
   * @param searchQuery {@link CreateDateSearchQuery with with query params}
   * @return {@link List of OmsOrder}
   */
  @Override
  @Transactional
  public List<OmsOrder> findAllOrderByCreatedDateRange(CreateDateSearchQuery searchQuery) {
    log.info(
        "Find All OmsOrder pageNumber:{}, pageSize:{}, createStartDate:{}, createEndDate:{}",
        searchQuery.getPageNumber(),
        searchQuery.getMaxFetchLimit(),
        searchQuery.getCreateStartDateTime(),
        searchQuery.getCreateEndDateTime());
    Pageable pageable = PageRequest.of(searchQuery.getPageNumber(), searchQuery.getMaxFetchLimit());
    Page<OmsOrder> omsOrderPage =
        omsOrderIOmsSqlServerBaseRepository.findAllByCreatedDateRange(
            searchQuery.getCreateStartDateTime(), searchQuery.getCreateEndDateTime(), pageable);
    List<OmsOrder> omsOrders = omsOrderPage.getContent();
    omsOrders.forEach(OmsOrder::initializeInnerEntitiesEagerly);
    return omsOrders;
  }

  @Override
  public String getNextIdentity() {
    return UUID.randomUUID().toString();
  }
}
