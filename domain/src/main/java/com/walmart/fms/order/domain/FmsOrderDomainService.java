package com.walmart.fms.order.domain;

import com.walmart.common.domain.messaging.DomainEvent;
import com.walmart.common.domain.messaging.DomainEventPublisher;
import com.walmart.common.domain.type.CancellationSource;
import com.walmart.common.domain.type.Domain;
import com.walmart.common.domain.type.DomainEventType;
import com.walmart.fms.domain.error.exception.FMSBadRequestException;
import com.walmart.fms.order.aggregateroot.FmsOrder;
import com.walmart.fms.order.gateway.IStoreGateway;
import com.walmart.fms.order.repository.IFmsOrderRepository;
import com.walmart.fms.order.valueobject.mappers.FMSOrderToFmsOrderValueObjectMapper;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

@Component
public class FmsOrderDomainService {

  private static final String DESCRIPTION = "An order has cancelled in FMS domain.";
  private static final String DESTINATION = "FMS_ORDER_UPDATES";

  @Autowired
  private IFmsOrderRepository fmsOrderRepository;

  @Autowired
  private IStoreGateway storeGateway;

  @Autowired
  private DomainEventPublisher fmsDomainEventPublisher;

  public FmsOrder processFmsOrder(FmsOrder fmsOrder) {

    Assert.notNull(fmsOrder, "FmsOrder object cannot be null");

    if (fmsOrder.isValid()) {

      fmsOrder.markAsReadyForStore();
      fmsOrderRepository.save(fmsOrder);
      sendOrderToStore(fmsOrder);
      return fmsOrder;
    }
    return fmsOrder;
  }

  private void sendOrderToStore(FmsOrder fmsOrder) {

    storeGateway.sendMarketPlaceOrderDownloadAsync(fmsOrder);
  }

  public FmsOrder cancelFmsOrder(
      FmsOrder fmsOrder, String cancelledReasonCode, CancellationSource cancellationSource, String cancelledReasonDescription) {

    if (fmsOrder.canBeCancelled()) {
      if (cancellationSource != null) {
        fmsOrder.cancelOrder(cancelledReasonCode, cancellationSource, cancelledReasonDescription);
      }
      fmsOrderRepository.save(fmsOrder);

      if (cancellationSource != null && cancellationSource.equals(CancellationSource.VENDOR)) {
        sendCancellationToStore(fmsOrder);
      } else if (cancellationSource != null
          && cancellationSource.equals(CancellationSource.STORE)) {

        fmsDomainEventPublisher.publish(
            new DomainEvent.EventBuilder(DomainEventType.FMS_ORDER_CANCELLED, DESCRIPTION)
                .from(Domain.FMS)
                .to(Domain.OMS)
                .addMessage(
                    FMSOrderToFmsOrderValueObjectMapper.INSTANCE
                        .convertFmsOrderToFmsOrderValueObject(fmsOrder))
                .build(),
            DESTINATION);
      }
    } else {
      throw new FMSBadRequestException("Order cannot be cancelled");
    }
    return fmsOrder;
  }

  private void sendCancellationToStore(FmsOrder fmsOrder) {

    ExecutorService executor = Executors.newFixedThreadPool(1);

    Callable<Boolean> sendCancellationTask =
        () -> storeGateway.sendMarketPlaceForceOrderCancellation(fmsOrder);

    executor.submit(sendCancellationTask);

    executor.shutdown();
  }
}
