package com.walmart.oms.order.valueobject;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.Date;
import java.util.List;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.util.StringUtils;

@Getter
@ToString
@Builder
@Slf4j
public class CatalogItemInfoQuery {

  private final List<String> itemIds;
  private final String itemType;
  private final String storeId;
  private final Date shipOnDate;
  // used only for Logging purpose.
  private final String storeOrderId;

  /**
   * Request validator for Catalog item info.
   *
   * @return Boolean for valid or invalid request.
   */
  @JsonIgnore
  public boolean isValidRequest() {

    if (CollectionUtils.isEmpty(this.getItemIds())) {
      log.debug("Empty/Null itemIds list");
      return false;
    }
    if (StringUtils.isEmpty(this.getItemType())) {
      log.debug("Empty/Null itemType");
      return false;
    }
    if (!"CIN".equalsIgnoreCase(this.getItemType())) {
      log.debug("Invalid itemType");
      return false;
    }

    return true;
  }
}
