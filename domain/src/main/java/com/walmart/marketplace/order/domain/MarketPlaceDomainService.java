package com.walmart.marketplace.order.domain;

import com.walmart.common.domain.event.processing.EventGeneratorService;
import com.walmart.common.domain.messaging.DomainEvent;
import com.walmart.common.domain.messaging.DomainEventPublisher;
import com.walmart.common.domain.type.CancellationSource;
import com.walmart.common.domain.type.Domain;
import com.walmart.common.domain.type.DomainEventType;
import com.walmart.common.domain.valueobject.CancellationDetails;
import com.walmart.marketplace.order.aggregateroot.MarketPlaceOrder;
import com.walmart.marketplace.order.configuration.MarketPlaceOrderConfig;
import com.walmart.marketplace.order.domain.mappers.MarketPlaceDomainToEventMessageMapper;
import com.walmart.marketplace.order.domain.uber.MarketPlaceReportRequest;
import com.walmart.marketplace.order.domain.valueobject.mappers.MarketPlaceOrderToValueObjectMapper;
import com.walmart.marketplace.order.factory.MarketPlaceOrderFactory;
import com.walmart.marketplace.order.gateway.IMarketPlaceGatewayFinder;
import com.walmart.marketplace.order.repository.IMarketPlaceRepository;
import io.strati.configuration.annotation.ManagedConfiguration;
import java.util.Arrays;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class MarketPlaceDomainService {

  private static final String OSN = "OSN";
  private static final String ORDER_CREATED = "ORDER_CREATED";
  private static final String MRK_PLC_ORDER_CREATED_DESC = "A Market place was created";
  private static final String ORDER_CANCELLED = "ORDER_CANCELLED";
  private static final String STORE_BUSY = "STORE_BUSY";
  private static final String MRK_PLC_ORDER_CANCELLED_DESC = "A Market place was cancelled";

  @Autowired
  private IMarketPlaceRepository marketPlaceOrdRepository;

  @Autowired
  private IMarketPlaceGatewayFinder marketPlaceGatewayFinder;

  @Autowired
  private DomainEventPublisher domainEventPublisher;

  @ManagedConfiguration
  private MarketPlaceOrderConfig marketplaceOrderConfig;

  @Autowired
  private EventGeneratorService eventGeneratorService;

  @Autowired
  private MarketPlaceOrderFactory marketPlaceOrderFactory;

  /**
   * @param order
   * @return
   */
  public MarketPlaceOrder processMarketPlaceOrder(MarketPlaceOrder order) {

    order.create();
    int inProgressOrderCount =
        marketPlaceOrdRepository.getInProgressOrderCount(
            Arrays.asList(marketplaceOrderConfig.getInProgressStates().split(",")),
            order.getStoreId());

    validateAndNotifyOrder(order, inProgressOrderCount);
    marketPlaceOrdRepository.save(order);

    if (order.isOrderAccepted()) {
      domainEventPublisher.publish(createMarketPlaceOrderCreatedDomainEvent(order), ORDER_CREATED);
    } else {
      log.warn("Order {} is not accepted, hence no messages to publish", order.getId());
    }
    return order;
  }

  private void validateAndNotifyOrder(MarketPlaceOrder order, int inProgressOrderCount) {
    if (inProgressOrderCount <= marketplaceOrderConfig.getAllowedRunningOrders()) {
      if (isThisATestMarketPlaceOrder(order) || notifiedVendorThatOrderIsAccepted(order)) {
        order.accept();
      }
    } else {
      log.info(
          "InProgress Order Threshold limit of {} reached for Store {}.Rejected VendorOrderId : {}",
          marketplaceOrderConfig.getAllowedRunningOrders(),
          order.getStoreId(),
          order.getVendorId());
      if (isThisATestMarketPlaceOrder(order) || notifiedVendorThatOrderIsRejected(order)) {
        order.reject();
      }
    }
  }

  private boolean notifiedVendorThatOrderIsAccepted(MarketPlaceOrder order) {
    return marketPlaceGatewayFinder.getMarketPlaceGateway(order.getVendorId()).acceptOrder(order);
  }

  private boolean notifiedVendorThatOrderIsRejected(MarketPlaceOrder order) {
    return marketPlaceGatewayFinder
        .getMarketPlaceGateway(order.getVendorId())
        .rejectOrder(order, STORE_BUSY);
  }

  private boolean isThisATestMarketPlaceOrder(MarketPlaceOrder order) {
    return order.isTestOrder();
  }

  private DomainEvent createMarketPlaceOrderCreatedDomainEvent(MarketPlaceOrder order) {
    return new DomainEvent.EventBuilder(DomainEventType.MARKET_PLACE_ORDER_CREATED, MRK_PLC_ORDER_CREATED_DESC)
        .addMessage(MarketPlaceOrderToValueObjectMapper.INSTANCE.modelToValueObject(order))
        .from(Domain.MARKETPLACE)
        .to(Domain.OMS)
        .addHeader(OSN, order.getVendorId().nextOSN())
        .build();
  }

  private DomainEvent createMarketPlaceOrderCancelledDomainEvent(
      MarketPlaceOrder order,
      CancellationDetails cancellationDetails) {
    return new DomainEvent.EventBuilder(DomainEventType.MARKET_PLACE_ORDER_CANCELLED, MRK_PLC_ORDER_CANCELLED_DESC)
        .addMessage(MarketPlaceOrderToValueObjectMapper.INSTANCE.modelToValueObject(order,
            cancellationDetails))
        .from(Domain.MARKETPLACE)
        .to(Domain.OMS)
        .build();
  }

  public MarketPlaceOrder cancelOrder(
      MarketPlaceOrder marketPlaceOrder, CancellationDetails cancellationDetails) {

    marketPlaceOrder.cancel();
    marketPlaceOrdRepository.save(marketPlaceOrder);

    eventGeneratorService.publishApplicationEvent(
        MarketPlaceDomainToEventMessageMapper.mapToMarketPlaceOrderCancelMessage(
            marketPlaceOrder, cancellationDetails));

    if (CancellationSource.VENDOR.equals(cancellationDetails.getCancelledBy())) {
      domainEventPublisher.publish(
          createMarketPlaceOrderCancelledDomainEvent(marketPlaceOrder, cancellationDetails),
          ORDER_CANCELLED);
    }

    return marketPlaceOrder;
  }

  /**
   * @param marketplaceReportRequest {@code The request payload basis on which report will be
   *                                 generated.}
   * @return
   */
  public String invokeMarketPlaceReport(MarketPlaceReportRequest marketplaceReportRequest) {
    log.info(
        "Invoking Marketplace report API, reportType:{}, startDate:{}, endDate:{}",
        marketplaceReportRequest.getReportType(),
        marketplaceReportRequest.getStartDate(),
        marketplaceReportRequest.getEndDate());
    return marketPlaceGatewayFinder
        .getMarketPlaceGateway(marketplaceReportRequest.getVendor())
        .invokeMarketPlaceReport(marketplaceReportRequest);
  }

  public MarketPlaceOrder pickCompleteMarketPlaceOrder(MarketPlaceOrder marketPlaceOrder) {
    marketPlaceOrder.pickCompleteOrder();
    return marketPlaceOrdRepository.save(marketPlaceOrder);
  }
}