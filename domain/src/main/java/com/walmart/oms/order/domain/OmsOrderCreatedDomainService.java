package com.walmart.oms.order.domain;

import com.walmart.common.domain.event.processing.EventGeneratorService;
import com.walmart.common.domain.messaging.DomainEvent;
import com.walmart.common.domain.messaging.DomainEventPublisher;
import com.walmart.common.domain.type.Domain;
import com.walmart.common.domain.type.DomainEventType;
import com.walmart.oms.domain.event.messages.OmsOrderEnrichmentFailureEventMessage;
import com.walmart.oms.order.aggregateroot.OmsOrder;
import com.walmart.oms.order.gateway.ICatalogGateway;
import com.walmart.oms.order.repository.IOmsOrderRepository;
import com.walmart.oms.order.valueobject.CatalogItem;
import com.walmart.oms.order.valueobject.CatalogItemInfoQuery;
import com.walmart.oms.order.valueobject.mappers.OMSToFMSValueObjectMapper;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class OmsOrderCreatedDomainService {

  private static final String DESCRIPTION = "An order was created in OMS domain.";
  private static final String DESTINATION = "OMS_ORDER_CREATED";
  private static final String CIN = "CIN";

  private final IOmsOrderRepository omsOrderRepository;
  private final DomainEventPublisher omsDomainEventPublisher;
  private final ICatalogGateway catalogGateway;
  private final OmsOrderDomainService omsOrderDomainService;
  private final EventGeneratorService eventGeneratorService;

  public void enrichSaveAndPublishCreatedOmsOrderToFms(OmsOrder omsOrder) {
    Map<String, CatalogItem> catalogItems;
    try {
      catalogItems = fetchCatalogInfo(omsOrder);
    } catch (Exception e) {
      String message =
          String.format(
              "Exception at IROCatalogGateway for storeOrderId : %s, itemIds: %s",
              omsOrder.getStoreOrderId(), omsOrder.getItemIds());
      log.error(message, e);
      cancelOrder(omsOrder);
      throw e;
    }
    omsOrder.enrichItemsWithCatalogData(catalogItems);
    omsOrder.markOrderAsReadyForStore();
    omsOrderRepository.save(omsOrder);
    omsDomainEventPublisher.publish(
        new DomainEvent.EventBuilder(DomainEventType.OMS_ORDER_CREATED, DESCRIPTION)
            .from(Domain.OMS)
            .to(Domain.FMS)
            .addMessage(
                OMSToFMSValueObjectMapper.INSTANCE.convertOMSOrderToFMSValueObject(omsOrder))
            .build(),
        DESTINATION);
  }

  private void cancelOrder(OmsOrder omsOrder) {
    eventGeneratorService.publishApplicationEvent(OmsOrderEnrichmentFailureEventMessage.builder()
        .sourceOrderId(omsOrder.getSourceOrderId())
        .tenant(omsOrder.getTenant())
        .vertical(omsOrder.getVertical())
        .build());
  }

  private Map<String, CatalogItem> fetchCatalogInfo(OmsOrder omsOrder) {
    return catalogGateway.fetchCatalogData(buildCatalogItemInfoQuery(omsOrder));
  }

  private CatalogItemInfoQuery buildCatalogItemInfoQuery(OmsOrder omsOrder) {
    return CatalogItemInfoQuery.builder()
        .itemIds(omsOrder.getItemIds())
        .itemType(CIN)
        .storeId(omsOrder.getStoreId())
        .shipOnDate(omsOrder.getDeliveryDate())
        .storeOrderId(omsOrder.getStoreOrderId())
        .build();
  }

}