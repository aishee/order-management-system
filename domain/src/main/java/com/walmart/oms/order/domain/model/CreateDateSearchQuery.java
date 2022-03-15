package com.walmart.oms.order.domain.model;

import com.walmart.util.DateTimeUtil;
import java.time.LocalDateTime;
import java.util.Date;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CreateDateSearchQuery {
  private final int pageNumber;
  private final int maxFetchLimit;
  private final Date createStartDateTime;
  private final Date createEndDateTime;

  public static class CreateDateSearchQueryBuilder {

    public CreateDateSearchQueryBuilder createStartDateTime(LocalDateTime createStartDateTime) {
      this.createStartDateTime = DateTimeUtil.fromLocalDateTime(createStartDateTime);
      return this;
    }

    public CreateDateSearchQueryBuilder createEndDateTime(LocalDateTime createEndDateTime) {
      this.createEndDateTime = DateTimeUtil.fromLocalDateTime(createEndDateTime);
      return this;
    }
  }
}
