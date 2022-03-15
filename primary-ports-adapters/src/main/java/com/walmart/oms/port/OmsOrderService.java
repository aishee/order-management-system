package com.walmart.oms.port;

import com.walmart.common.domain.type.Tenant;
import com.walmart.common.domain.type.Vertical;
import com.walmart.oms.dto.OmsOrderResponse;

public interface OmsOrderService {

  OmsOrderResponse getOrder(String sourceOrderId, Tenant tenant, Vertical vertical);
}
