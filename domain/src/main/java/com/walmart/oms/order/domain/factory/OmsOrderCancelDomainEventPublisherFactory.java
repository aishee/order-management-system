package com.walmart.oms.order.domain.factory;

import com.walmart.common.domain.type.CancellationSource;
import com.walmart.oms.order.domain.DefaultCancelledOrderDomainEventPublisher;
import com.walmart.oms.order.domain.OmsOrderCancelDomainEventPublisher;
import com.walmart.oms.order.domain.OmsStoreCancelledOrderDomainEventPublisher;
import com.walmart.oms.order.domain.OmsVendorCancelledOrderDomainEventPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class OmsOrderCancelDomainEventPublisherFactory {

  private final OmsStoreCancelledOrderDomainEventPublisher omsStoreCancelledOrderDomainEventPublisher;

  private final OmsVendorCancelledOrderDomainEventPublisher omsVendorCancelledOrderDomainEventPublisher;

  private final DefaultCancelledOrderDomainEventPublisher defaultCancelledOrderDomainEventPublisher;

  public OmsOrderCancelDomainEventPublisher getOrderCancelDomainEventPublisher(CancellationSource cancellationSource) {
    if (CancellationSource.STORE == cancellationSource) {
      return omsStoreCancelledOrderDomainEventPublisher;
    } else if (CancellationSource.VENDOR == cancellationSource) {
      return omsVendorCancelledOrderDomainEventPublisher;
    } else {
      return defaultCancelledOrderDomainEventPublisher;
    }
  }
}
