package com.walmart.fms.eventprocessors;

import com.walmart.common.domain.event.processing.EventGeneratorService;
import com.walmart.common.domain.messaging.DomainEvent;
import com.walmart.common.domain.messaging.DomainEventPublisher;
import com.walmart.common.domain.type.CancellationSource;
import com.walmart.common.domain.type.Domain;
import com.walmart.common.domain.type.DomainEventType;
import com.walmart.fms.commands.FmsPickCompleteCommand;
import com.walmart.fms.commands.FmsPickCompleteCommand.FmsOrderData;
import com.walmart.fms.commands.FmsPickCompleteCommand.PickedItemUpc;
import com.walmart.fms.domain.error.exception.FMSBadRequestException;
import com.walmart.fms.domain.event.message.ItemUnavailabilityMessage;
import com.walmart.fms.order.aggregateroot.FmsOrder;
import com.walmart.fms.order.domain.entity.FmsPickedItem;
import com.walmart.fms.order.domain.entity.FmsPickedItemUpc;
import com.walmart.fms.order.domain.entity.FmsSubstitutedItem;
import com.walmart.fms.order.domain.entity.FmsSubstitutedItemUpc;
import com.walmart.fms.order.factory.FmsOrderFactory;
import com.walmart.fms.order.repository.IFmsOrderRepository;
import com.walmart.fms.order.valueobject.mappers.FMSOrderToFmsOrderValueObjectMapper;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

@Component
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class FmsPickCompleteCommandService {

  private static final String DESCRIPTION = "An order has completed PICK in FMS domain.";
  private static final String DESTINATION = "FMS_ORDER_UPDATES";
  private static final String CANCEL_ORDER_REASON_CODE = "SUBSTITUTION_OPTION_CANCEL_ENTIRE_ORDER";
  private static final String CANCEL_ORDER_DESCRIPTION =
      "Cancelling the entire order due to nil picks and substitution option CANCEL ENTIRE ORDER";

  private final FmsOrderFactory fmsOrderFactory;

  private final IFmsOrderRepository fmsOrderRepository;

  private final DomainEventPublisher fmsDomainEventPublisher;

  private final EventGeneratorService eventGeneratorService;

  @Transactional
  public FmsOrder pickCompleteOrder(FmsPickCompleteCommand pickCompleteCommand) {
    Assert.notNull(pickCompleteCommand, "Pick Complete Command Cannot be null");
    FmsOrder fmsOrder =
        fmsOrderFactory.getFmsOrderByStoreOrder(pickCompleteCommand.getStoreOrderId());
    if (!fmsOrder.isTransientState() && fmsOrder.isValid()) {
      fmsOrder.updatePickedItems(convertToFmsPickedItem(pickCompleteCommand));
      updateTheOrderStatusAndSendEvent(pickCompleteCommand, fmsOrder);
      return fmsOrder;
    } else {
      log.error(
          "Order doesn't exist with source order id :{}", pickCompleteCommand.getStoreOrderId());
      throw new FMSBadRequestException(
          "Order doesn't exist with store order id " + pickCompleteCommand.getStoreOrderId());
    }
  }

  private void updateTheOrderStatusAndSendEvent(
      FmsPickCompleteCommand pickCompleteCommand, FmsOrder fmsOrder) {
    generateItemOutOfStockEvent(pickCompleteCommand, fmsOrder);
    if (pickCompleteCommand.isOrderCancelled()) {
      pickCompleteOrderCancellationProcessing(pickCompleteCommand, fmsOrder);
      return;
    }
    if (fmsOrder.isEntireOrderCancellationValid()) {
      fmsOrder.cancelOrder(
          CANCEL_ORDER_REASON_CODE, CancellationSource.OMS, CANCEL_ORDER_DESCRIPTION);
      saveOrderAndPublishDomainEvent(fmsOrder, DomainEventType.FMS_ORDER_CANCELLED);
      return;
    }

    if (fmsOrder.isValidOrderStatusSequence(FmsOrder.OrderStatus.PICK_COMPLETE.getName())) {
      fmsOrder.markOrderAsPickComplete();
      saveOrderAndPublishDomainEvent(fmsOrder, DomainEventType.FMS_ORDER_PICK_COMPLETE);
      return;
    }

    throw new FMSBadRequestException(
        "Order:" + fmsOrder.getStoreOrderId() + " already in: " + fmsOrder.getOrderState());
  }

  private void generateItemOutOfStockEvent(
      FmsPickCompleteCommand pickCompleteCommand, FmsOrder fmsOrder) {

    // this condition should cover Order Cancellation (Due to all items nil picked),
    // Nil/Partially/Substituted item scenarios
    if (pickCompleteCommand.isAnyItemNotPickedCompletely()) {

      log.info("Marking items out of stock for Store Order Id : {}", fmsOrder.getStoreOrderId());
      ItemUnavailabilityMessage itemUnavailabilityMessage =
          ItemUnavailabilityMessage.builder()
              .storeOrderId(fmsOrder.getStoreOrderId())
              .storeId(fmsOrder.getStoreId())
              .vendorOrderId(fmsOrder.getVendorOrderId())
              .vendorId(fmsOrder.getVendor())
              .outOfStockItemIds(pickCompleteCommand.getNotFullyPickedItemsCin())
              .build();
      eventGeneratorService.publishApplicationEvent(itemUnavailabilityMessage);
    }
  }

  private void pickCompleteOrderCancellationProcessing(
      FmsPickCompleteCommand pickCompleteCommand, FmsOrder fmsOrder) {
    if (fmsOrder.isValidOrderStatusSequence(FmsOrder.OrderStatus.CANCELLED.getName())) {
      fmsOrder.cancelOrder(
          pickCompleteCommand.getCancelReasonCode(),
          CancellationSource.STORE,
          pickCompleteCommand.getCancelledReasonDescription());
      saveOrderAndPublishDomainEvent(fmsOrder, DomainEventType.FMS_ORDER_CANCELLED);
      return;
    }
    saveFmsOrder(fmsOrder);
    throw new FMSBadRequestException(
        "Order:" + fmsOrder.getStoreOrderId() + " already in: " + fmsOrder.getOrderState());
  }

  private void saveOrderAndPublishDomainEvent(FmsOrder fmsOrder, DomainEventType fmsOrderStatus) {
    fmsOrderRepository.save(fmsOrder);
    fmsDomainEventPublisher.publish(
        new DomainEvent.EventBuilder(fmsOrderStatus, DESCRIPTION)
            .from(Domain.FMS)
            .to(Domain.OMS)
            .addMessage(
                FMSOrderToFmsOrderValueObjectMapper.INSTANCE.convertFmsOrderToFmsOrderValueObject(
                    fmsOrder))
            .build(),
        DESTINATION);
  }

  private void saveFmsOrder(FmsOrder fmsOrder) {
    fmsOrderRepository.save(fmsOrder);
  }

  private FmsPickedItem getPickedItemFromCommand(
      FmsPickCompleteCommand.PickedItemInfo pickedItemInfo) {

    List<FmsPickedItemUpc> pickedItemUpcList =
        pickedItemInfo.getPickedItemUpcs().stream()
            .map(this::getPickedItemUpc)
            .collect(Collectors.toList());

    return getPickedItem(pickedItemInfo, pickedItemUpcList);
  }

  private FmsPickedItem getPickedItem(
      FmsPickCompleteCommand.PickedItemInfo pickedItemInfo,
      List<FmsPickedItemUpc> pickedItemUpcList) {
    FmsPickedItem pickedItem =
        fmsOrderFactory.createPickedItem(
            pickedItemInfo.getPickedItemDescription(),
            pickedItemInfo.getDepartmentId(),
            pickedItemInfo.getCin(),
            pickedItemInfo.getPickedBy(),
            pickedItemUpcList);
    addSubstitutedItems(pickedItem, pickedItemInfo.getSubstitutedItemInfoList());
    return pickedItem;
  }

  private void addSubstitutedItems(
      FmsPickedItem pickedItem,
      List<FmsPickCompleteCommand.SubstitutedItemInfo> substitutedItemInfoList) {
    List<FmsSubstitutedItem> substitutedItems =
        substitutedItemInfoList.stream()
            .map(this::buildSubstitutedItem)
            .collect(Collectors.toList());
    pickedItem.updateSubstitutedItems(substitutedItems);
  }

  private List<FmsSubstitutedItemUpc> buildSubstitutedItemUpcList(
      FmsPickCompleteCommand.SubstitutedItemInfo substitutedItemInfo) {
    return substitutedItemInfo.getUpcs().stream()
        .map(
            upcInfo -> fmsOrderFactory.buildSubstitutedItemUpcs(upcInfo.getUpc(), upcInfo.getUom()))
        .collect(Collectors.toList());
  }

  private FmsSubstitutedItem buildSubstitutedItem(
      FmsPickCompleteCommand.SubstitutedItemInfo substitutedItemInfo) {
    FmsSubstitutedItem substitutedItem =
        fmsOrderFactory.createSubstitutedItem(
            substitutedItemInfo.getConsumerItemNumber(),
            substitutedItemInfo.getWalmartItemNumber(),
            substitutedItemInfo.getDepartment(),
            substitutedItemInfo.getDescription(),
            substitutedItemInfo.getUnitPrice(),
            substitutedItemInfo.getQuantity(),
            buildSubstitutedItemUpcList(substitutedItemInfo),
            substitutedItemInfo.getWeight());

    substitutedItem
        .getUpcs()
        .forEach(substitutedItemUpc -> substitutedItemUpc.updateSubstitutedItem(substitutedItem));
    return substitutedItem;
  }

  private FmsPickedItemUpc getPickedItemUpc(PickedItemUpc pickedItemUpc) {
    return fmsOrderFactory.createPickedItemUpc(
        pickedItemUpc.getPickedQuantity(),
        pickedItemUpc.getUnitPrice(),
        pickedItemUpc.getUom(),
        pickedItemUpc.getWin(),
        pickedItemUpc.getUpc());
  }

  private Map<String, FmsPickedItem> convertToFmsPickedItem(
      FmsPickCompleteCommand pickCompleteCommand) {
    return Optional.ofNullable(pickCompleteCommand)
        .map(FmsPickCompleteCommand::getData)
        .map(FmsOrderData::getPickedItems)
        .filter(CollectionUtils::isNotEmpty)
        .map(
            list ->
                list.stream()
                    .collect(
                        Collectors.toMap(
                            FmsPickCompleteCommand.PickedItemInfo::getCin,
                            this::getPickedItemFromCommand)))
        .orElse(null);
  }
}
