package com.walmart.marketplace.order.domain.uber.mapper;

import com.walmart.marketplace.domain.event.messages.MarketPlaceItemAttributes;
import com.walmart.marketplace.domain.event.messages.MarketPlacePickCompleteMessage;
import com.walmart.marketplace.order.domain.uber.PatchCartInfo;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper
public abstract class PatchCartInfoMapper {

  public static final PatchCartInfoMapper INSTANCE = Mappers.getMapper(PatchCartInfoMapper.class);

  @Mapping(
      expression = "java(mapNilPicks(marketPlacePickCompleteMessage))",
      target = "nilPickInstanceIds")
  @Mapping(
      expression = "java(mapPartialPicks(marketPlacePickCompleteMessage))",
      target = "partialPickInstanceIds")
  @Mapping(source = "vendorOrderId", target = "vendorOrderId")
  @Mapping(source = "vendorId", target = "vendorId")
  @Mapping(source = "storeId", target = "storeId")
  public abstract PatchCartInfo convertToPatchCartInfo(
      MarketPlacePickCompleteMessage marketPlacePickCompleteMessage);

  protected List<String> mapNilPicks(
      MarketPlacePickCompleteMessage marketPlacePickCompleteMessage) {
    List<MarketPlaceItemAttributes> marketPlaceItem =
        marketPlacePickCompleteMessage.getMarketPlaceItemAttributes();
    return getNilPickInstanceIds(marketPlaceItem);
  }

  private List<String> getNilPickInstanceIds(
      List<MarketPlaceItemAttributes> marketPlaceItemAttributes) {
    return marketPlaceItemAttributes.stream()
        .filter(MarketPlaceItemAttributes::isValidNilPick)
        .map(MarketPlaceItemAttributes::getVendorInstanceId)
        .filter(Objects::nonNull)
        .collect(Collectors.toList());
  }

  protected Map<String, Integer> mapPartialPicks(
      MarketPlacePickCompleteMessage marketPlacePickCompleteMessage) {
    List<MarketPlaceItemAttributes> marketPlaceItem =
        marketPlacePickCompleteMessage.getMarketPlaceItemAttributes();
    return getPartialPickInstanceIds(marketPlaceItem);
  }

  private Map<String, Integer> getPartialPickInstanceIds(
      List<MarketPlaceItemAttributes> marketPlaceItemAttributes) {
    return marketPlaceItemAttributes.stream()
        .filter(MarketPlaceItemAttributes::isValidPartialPick)
        .collect(
            Collectors.toMap(
                MarketPlaceItemAttributes::getVendorInstanceId,
                MarketPlaceItemAttributes::getPickedNonBundledQuantity));
  }
}
