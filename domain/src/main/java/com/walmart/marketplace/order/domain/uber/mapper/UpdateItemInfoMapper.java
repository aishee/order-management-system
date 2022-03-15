package com.walmart.marketplace.order.domain.uber.mapper;

import com.walmart.fms.domain.event.message.ItemUnavailabilityMessage;
import com.walmart.marketplace.domain.event.messages.MarketPlaceItemAttributes;
import com.walmart.marketplace.domain.event.messages.MarketPlaceOrderCancelMessage;
import com.walmart.marketplace.domain.event.messages.MarketPlacePickCompleteMessage;
import com.walmart.marketplace.order.domain.entity.UpdateItemInfo;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Slf4j
@Mapper
public abstract class UpdateItemInfoMapper {

  public static final UpdateItemInfoMapper INSTANCE = Mappers.getMapper(UpdateItemInfoMapper.class);
  public static final String OUT_OF_STOCK = "OUT_OF_STOCK";

  @Mapping(
      expression = "java(getOutOfStockItemIds(marketPlacePickCompleteMessage))",
      target = "outOfStockItemIds")
  @Mapping(
      expression = "java(getSuspendUntil(marketPlacePickCompleteMessage.getVendorOrderId()))",
      target = "suspendUntil")
  @Mapping(expression = "java(getReason())", target = "reason")
  @Mapping(source = "vendorStoreId", target = "vendorStoreId")
  @Mapping(source = "vendorId", target = "vendorId")
  @Mapping(source = "vendorOrderId", target = "vendorOrderId")
  @Mapping(source = "storeId", target = "storeId")
  public abstract UpdateItemInfo convertToUpdateItemInfo(
      MarketPlacePickCompleteMessage marketPlacePickCompleteMessage);

  @Mapping(
      expression = "java(marketPlaceOrderCancelMessage.getExternalItemIds())",
      target = "outOfStockItemIds")
  @Mapping(
      expression = "java(getSuspendUntil(marketPlaceOrderCancelMessage.getVendorOrderId()))",
      target = "suspendUntil")
  @Mapping(expression = "java(marketPlaceOrderCancelMessage.getVendor())", target = "vendorId")
  @Mapping(expression = "java(getReason())", target = "reason")
  public abstract UpdateItemInfo convertToUpdateItemInfo(
      MarketPlaceOrderCancelMessage marketPlaceOrderCancelMessage);

  @Mapping(source = "outOfStockItemIds", target = "outOfStockItemIds")
  @Mapping(
      expression = "java(getSuspendUntil(itemUnavailabilityMessage.getVendorOrderId()))",
      target = "suspendUntil")
  @Mapping(expression = "java(getReason())", target = "reason")
  @Mapping(source = "storeId", target = "vendorStoreId")
  @Mapping(source = "vendorId", target = "vendorId")
  @Mapping(source = "vendorOrderId", target = "vendorOrderId")
  @Mapping(source = "storeId", target = "storeId")
  public abstract UpdateItemInfo convertToUpdateItemInfo(
      ItemUnavailabilityMessage itemUnavailabilityMessage);

  protected List<String> getOutOfStockItemIds(
      MarketPlacePickCompleteMessage marketPlacePickCompleteMessage) {
    List<MarketPlaceItemAttributes> marketPlaceItemAttributes =
        marketPlacePickCompleteMessage.getMarketPlaceItemAttributes();
    if (!marketPlaceItemAttributes.isEmpty()) {
      return marketPlaceItemAttributes.stream()
          .filter(MarketPlaceItemAttributes::isValidItem)
          .map(MarketPlaceItemAttributes::getExternalItemId)
          .filter(Objects::nonNull)
          .collect(Collectors.toList());
    }
    return Collections.emptyList();
  }

  /**
   * This method will return the time at which the item will be again available for sale, specified
   * as a Unix timestamp in seconds
   *
   * @param vendorOrderId
   * @return
   */
  protected int getSuspendUntil(String vendorOrderId) {
    ZonedDateTime zonedDateTime = LocalDate.now().plusDays(1).atStartOfDay(ZoneId.systemDefault());
    int suspendUntil = (int) zonedDateTime.toInstant().getEpochSecond();
    log.info(
        "Out of stock items for Order {} will be suspended till {}",
        vendorOrderId,
        zonedDateTime.toLocalDateTime().format(DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss")));
    return suspendUntil;
  }

  protected String getReason() {
    return OUT_OF_STOCK;
  }
}
