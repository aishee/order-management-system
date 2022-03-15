package com.walmart.fms.domain.event.message;

import com.walmart.common.domain.event.processing.Message;
import com.walmart.marketplace.order.domain.entity.type.Vendor;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ItemUnavailabilityMessage implements Message {

  private String vendorOrderId;
  private String storeOrderId;
  private Vendor vendorId;
  private List<String> outOfStockItemIds;
  private String storeId;
}
