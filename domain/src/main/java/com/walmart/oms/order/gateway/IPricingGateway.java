package com.walmart.oms.order.gateway;

import com.walmart.oms.domain.event.messages.OrderCancelledDomainEventMessage;
import com.walmart.oms.order.aggregateroot.OmsOrder;
import com.walmart.oms.order.valueobject.PricingResponse;
import com.walmart.tax.calculator.dto.Tax;
import java.util.Map;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public interface IPricingGateway {

  Optional<PricingResponse> priceOrder(OmsOrder omsOrder, Map<String, Tax> taxInfoMap);

  boolean reverseSale(OrderCancelledDomainEventMessage orderCancelledDomainEventMessage);
}
