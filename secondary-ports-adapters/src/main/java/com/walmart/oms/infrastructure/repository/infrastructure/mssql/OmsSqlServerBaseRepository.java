package com.walmart.oms.infrastructure.repository.infrastructure.mssql;

import com.walmart.oms.order.aggregateroot.OmsOrder;
import java.util.Date;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * This is base repository used for searching, sorting, and paging based on the properties of
 * BaseEntity class.
 */
@Repository
public interface OmsSqlServerBaseRepository extends PagingAndSortingRepository<OmsOrder, UUID> {
  @Query(
      "SELECT omsOrder FROM OmsOrder omsOrder WHERE omsOrder.createdDate >= :createStartDate AND omsOrder.createdDate <= :createEndDate")
  Page<OmsOrder> findAllByCreatedDateRange(
      @Param("createStartDate") Date createStartDate,
      @Param("createEndDate") Date createEndDate,
      Pageable pageable);
}
