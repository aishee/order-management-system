package com.walmart.oms.eventprocessors;

import com.walmart.common.domain.messaging.DomainEvent;
import com.walmart.common.domain.messaging.DomainEventPublisher;
import com.walmart.common.domain.type.Domain;
import com.walmart.common.domain.type.DomainEventType;
import com.walmart.oms.commands.PickCompleteCommand;
import com.walmart.oms.domain.error.exception.OMSBadRequestException;
import com.walmart.oms.order.aggregateroot.OmsOrder;
import com.walmart.oms.order.domain.entity.PickedItem;
import com.walmart.oms.order.domain.entity.PickedItemUpc;
import com.walmart.oms.order.domain.entity.SubstitutedItem;
import com.walmart.oms.order.domain.entity.SubstitutedItemUpc;
import com.walmart.oms.order.factory.OmsOrderFactory;
import com.walmart.oms.order.repository.IOmsOrderRepository;
import com.walmart.oms.order.valueobject.mappers.OMSOrderToMarketPlaceOrderValueObjectMapper;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Slf4j
public class OmsPickCompleteCommandService {

  private static final String DESCRIPTION = "An order has been Picked by the store.";
  private static final String DESTINATION = "OMS_ORDER_UPDATES";

  @Autowired
  private OmsOrderFactory omsOrderFactory;
  @Autowired
  private IOmsOrderRepository omsOrderRepository;
  @Autowired
  private DomainEventPublisher omsDomainEventPublisher;

  @Transactional
  public OmsOrder pickCompleteOrder(PickCompleteCommand pickCompleteCommand) {

    OmsOrder omsOrder =
        omsOrderFactory.getOmsOrderBySourceOrder(
            pickCompleteCommand.getSourceOrderId(),
            pickCompleteCommand.getTenant(),
            pickCompleteCommand.getVertical());

    if (omsOrder != null && !omsOrder.isTransientState() && omsOrder.getOrderItemList() != null) {

      Map<String, PickedItem> pickedItemMap = getPickedItemMap(pickCompleteCommand);
      omsOrder.updatePickedItemsFromStore(pickedItemMap);
      if (omsOrder.isOrderStatusUpdatable(OmsOrder.OrderStatus.PICK_COMPLETE.getName())) {
        omsOrder.markOrderAsPickComplete();
        omsOrderRepository.save(omsOrder);
        // domain event generator for communication between OMS/MP/FMS domains using JmsListener
        omsDomainEventPublisher.publish(
            new DomainEvent.EventBuilder(DomainEventType.OMS_ORDER_PICK_COMPLETE, DESCRIPTION)
                .from(Domain.OMS)
                .to(Domain.MARKETPLACE)
                .addMessage(
                    OMSOrderToMarketPlaceOrderValueObjectMapper.INSTANCE
                        .convertOmsOrderToMarketPlaceOrderValueObject(omsOrder))
                .build(),
            DESTINATION);
      } else {
        String message =
            String.format(
                "Received Pick Complete But Order: %s already in %s",
                omsOrder.getSourceOrderId(), omsOrder.getOrderState());
        log.error(message);
      }
      return omsOrder;
    } else {
      String message =
          String.format(
              "Order doesn't exist with source order id : %s",
              pickCompleteCommand.getSourceOrderId());
      log.error(message);
      throw new OMSBadRequestException(message);
    }
  }

  private Map<String, PickedItem> getPickedItemMap(PickCompleteCommand pickCompleteCommand) {
    return pickCompleteCommand.getPickedItems().stream()
        .collect(
            Collectors.toMap(
                PickCompleteCommand.PickedItemInfo::getCin, this::getPickedItemFromCommand));
  }

  private PickedItem getPickedItemFromCommand(PickCompleteCommand.PickedItemInfo pickedItemInfo) {

    List<PickedItemUpc> pickedItemUpcList = new ArrayList<>();
    pickedItemInfo
        .getPickedItemUpcs()
        .forEach(
            pickedItemUpc -> {
              PickedItemUpc pickedUpc = getPickedItemUpc(pickedItemUpc);
              pickedItemUpcList.add(pickedUpc);
            });
    return getPickedItem(pickedItemInfo, pickedItemUpcList);
  }

  private PickedItem getPickedItem(
      PickCompleteCommand.PickedItemInfo pickedItemInfo, List<PickedItemUpc> pickedItemUpcList) {
    PickedItem pickedItem = omsOrderFactory.createPickedItem(
        pickedItemInfo.getPickedItemDescription(),
        pickedItemInfo.getDepartmentId(),
        pickedItemInfo.getCin(),
        pickedItemInfo.getPickedBy(),
        pickedItemUpcList);
    addSubstitutedItems(pickedItem, pickedItemInfo.getSubstitutedItems());
    return pickedItem;
  }

  private void addSubstitutedItems(PickedItem pickedItem,
                                   List<PickCompleteCommand.SubstitutedItemInfo> substitutedItemInfoList) {
    List<SubstitutedItem> substitutedItems = substitutedItemInfoList
        .stream().map(this::buildSubstitutedItem)
        .collect(Collectors.toList());
    pickedItem.updateSubstitutedItems(substitutedItems);
  }

  private List<SubstitutedItemUpc> buildSubstitutedItemUpcList(
      PickCompleteCommand.SubstitutedItemInfo substitutedItemInfo) {
    return substitutedItemInfo.getUpcs().stream()
        .map(upcInfo -> omsOrderFactory
            .buildSubstitutedItemUpcs(upcInfo.getUpc(),
                upcInfo.getUom()))
        .collect(Collectors.toList());
  }

  private SubstitutedItem buildSubstitutedItem(PickCompleteCommand.SubstitutedItemInfo substitutedItemInfo) {
    SubstitutedItem substitutedItem = omsOrderFactory.createSubstitutedItem(
        substitutedItemInfo.getConsumerItemNumber(),
        substitutedItemInfo.getWalmartItemNumber(),
        substitutedItemInfo.getDepartment(),
        substitutedItemInfo.getDescription(),
        substitutedItemInfo.getUnitPrice(),
        substitutedItemInfo.getTotalPrice(),
        substitutedItemInfo.getQuantity(),
        buildSubstitutedItemUpcList(substitutedItemInfo),
        substitutedItemInfo.getWeight());

    substitutedItem.getUpcs()
        .forEach(substitutedItemUpc ->
            substitutedItemUpc.updateSubstitutedItem(substitutedItem));
    return substitutedItem;
  }

  private PickedItemUpc getPickedItemUpc(PickCompleteCommand.PickedItemUpc pickedItemUpc) {
    return omsOrderFactory.createPickedItemUpc(
        pickedItemUpc.getPickedQuantity(),
        pickedItemUpc.getUnitPrice(),
        pickedItemUpc.getUom(),
        pickedItemUpc.getWin(),
        pickedItemUpc.getUpc());
  }
}
