package com.walmart.marketplace.order.domain.valueobject.mappers;

import com.walmart.common.domain.valueobject.CancellationDetails;
import com.walmart.marketplace.order.aggregateroot.MarketPlaceOrder;
import com.walmart.marketplace.order.domain.entity.MarketPlaceItem;
import com.walmart.marketplace.order.domain.valueobject.MarketPlaceOrderValueObject;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper
public abstract class MarketPlaceOrderToValueObjectMapper {

  public static final MarketPlaceOrderToValueObjectMapper INSTANCE =
      Mappers.getMapper(MarketPlaceOrderToValueObjectMapper.class);

  @Mapping(source = "marketPlaceOrderContactInfo.firstName", target = "contactInfo.firstName")
  @Mapping(source = "marketPlaceOrderContactInfo.lastName", target = "contactInfo.lastName")
  @Mapping(source = "marketPlaceOrderPaymentInfo", target = "marketPlaceOrderPaymentInfo")
  @Mapping(source = "marketPlaceItems", target = "items")
  @Mapping(source = "id", target = "sourceOrderId")
  public abstract MarketPlaceOrderValueObject modelToValueObject(MarketPlaceOrder marketPlaceOrder);

  public MarketPlaceOrderValueObject modelToValueObject(MarketPlaceOrder marketPlaceOrder, CancellationDetails cancellationDetails) {
    MarketPlaceOrderValueObject marketPlaceOrderValueObject = modelToValueObject(marketPlaceOrder);
    marketPlaceOrderValueObject.setCancellationDetails(CancellationDetailsValueObjectMapper.INSTANCE.modelToValueObject(cancellationDetails));
    return marketPlaceOrderValueObject;
  }

  @Mapping(source = "marketPlacePriceInfo", target = "itemPriceInfo")
  public abstract MarketPlaceOrderValueObject.Item modelItemToValueObject(
      MarketPlaceItem marketPlaceItem);
}