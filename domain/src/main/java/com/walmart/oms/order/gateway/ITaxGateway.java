package com.walmart.oms.order.gateway;

import com.walmart.oms.order.domain.entity.OmsOrderItem;
import com.walmart.tax.calculator.dto.Tax;
import java.util.List;
import java.util.Map;

public interface ITaxGateway {
  Map<String, Tax> fetchTaxData(List<OmsOrderItem> orderedItemList, String sourceOrderId);
}
