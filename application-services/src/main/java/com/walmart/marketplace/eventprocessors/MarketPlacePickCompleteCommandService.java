package com.walmart.marketplace.eventprocessors;

import com.walmart.common.domain.event.processing.EventGeneratorService;
import com.walmart.marketplace.commands.MarketPlacePickCompleteCommand;
import com.walmart.marketplace.commands.mapper.MarketPlacePickCompleteCommandToDomainMessageMapper;
import com.walmart.marketplace.domain.event.messages.MarketPlacePickCompleteMessage;
import com.walmart.marketplace.mappers.SubstituteItemCommandToMarketPlaceItemMapper;
import com.walmart.marketplace.order.aggregateroot.MarketPlaceOrder;
import com.walmart.marketplace.order.domain.MarketPlaceDomainService;
import com.walmart.marketplace.order.factory.MarketPlaceOrderFactory;
import com.walmart.oms.domain.error.exception.OMSBadRequestException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class MarketPlacePickCompleteCommandService {

  private final EventGeneratorService eventGeneratorService;
  private final MarketPlaceOrderFactory marketPlaceOrderFactory;
  private final MarketPlaceDomainService marketPlaceDomainService;
  private final SubstituteItemCommandToMarketPlaceItemMapper substituteItemCommandToMarketPlaceItemMapper;

  @Transactional
  public MarketPlaceOrder pickCompleteOrder(
      MarketPlacePickCompleteCommand marketPlacePickCompleteCommand) {
    MarketPlaceOrder marketPlaceOrder =
        savePickCompleteMarketplaceOrder(marketPlacePickCompleteCommand.getData());
    MarketPlacePickCompleteMessage marketPlacePickCompleteMessage =
        MarketPlacePickCompleteCommandToDomainMessageMapper.mapToMarketPlacePickCompleteMessage(
            marketPlacePickCompleteCommand, marketPlaceOrder);
    eventGeneratorService.publishApplicationEvent(marketPlacePickCompleteMessage);
    return marketPlaceOrder;
  }

  /**
   * Fetch the MarketPlaceOrder from SourceOrderId,
   * update substitutedItem details
   * and order state to PICK_COMPLETE
   *
   * @param pickCompleteCommandData pick complete details
   * @return MarketPlaceOrder
   */
  private MarketPlaceOrder savePickCompleteMarketplaceOrder(
      MarketPlacePickCompleteCommand.MarketPlacePickCompleteCommandData pickCompleteCommandData) {
    return marketPlaceOrderFactory
        .getOrder(pickCompleteCommandData.getSourceOrderId())
        .map(marketPlaceOrder -> {
          mapSubstitutedItems(pickCompleteCommandData, marketPlaceOrder);
          return marketPlaceDomainService.pickCompleteMarketPlaceOrder(marketPlaceOrder);
        })
        .orElseThrow(() -> handleException(pickCompleteCommandData.getSourceOrderId()));
  }

  private void mapSubstitutedItems(
      MarketPlacePickCompleteCommand.MarketPlacePickCompleteCommandData pickCompleteCommand,
      MarketPlaceOrder marketPlaceOrder) {
    substituteItemCommandToMarketPlaceItemMapper.mapToSubstitutedItemEntityList(marketPlaceOrder
        .getMarketPlaceItems(), pickCompleteCommand.getMarketplacePickCompleteItemCommands());
  }

  private OMSBadRequestException handleException(String sourceOrderId) {
    log.error("Order doesn't exist with source order id :{}", sourceOrderId);
    return new OMSBadRequestException("Order doesn't exist with order id :" + sourceOrderId);
  }
}
