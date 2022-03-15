package com.walmart.oms.order.valueobject.mappers;

import com.walmart.oms.order.aggregateroot.OmsOrder;
import com.walmart.oms.order.domain.entity.AddressInfo;
import com.walmart.oms.order.domain.entity.OmsOrderItem;
import com.walmart.oms.order.domain.entity.PickedItemUpc;
import com.walmart.oms.order.valueobject.events.FmsAddressInfoValueObject;
import com.walmart.oms.order.valueobject.events.FmsOrderItemUpcValueObject;
import com.walmart.oms.order.valueobject.events.FmsOrderItemvalueObject;
import com.walmart.oms.order.valueobject.events.FmsOrderValueObject;
import com.walmart.oms.order.valueobject.events.FmsPickedItemUpcVo;
import java.util.List;
import org.apache.commons.collections.CollectionUtils;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper
public abstract class OMSToFMSValueObjectMapper {

  public static final OMSToFMSValueObjectMapper INSTANCE =
      Mappers.getMapper(OMSToFMSValueObjectMapper.class);

  @Mapping(source = "contactInfo.phoneNumberOne.number", target = "contactInfo.phoneNumberOne")
  @Mapping(source = "contactInfo.phoneNumberTwo.number", target = "contactInfo.phoneNumberTwo")
  @Mapping(source = "contactInfo.fullName.firstName", target = "contactInfo.firstName")
  @Mapping(source = "contactInfo.fullName.lastName", target = "contactInfo.lastName")
  @Mapping(source = "orderItemList", target = "fmsOrderItemvalueObjectList")
  @Mapping(source = "addresses", target = "addressInfo")
  @Mapping(source = "storeOrderId", target = "storeOrderId")
  @Mapping(source = "pickupLocationId", target = "pickupLocationId")
  public abstract FmsOrderValueObject convertOMSOrderToFMSValueObject(OmsOrder omsOrder);

  @Mapping(source = "itemPriceInfo.unitPrice.amount", target = "unitPrice")
  @Mapping(source = "catalogItem.upcNumbers", target = "upcs")
  public abstract FmsOrderItemvalueObject convertOmsItemToValueObject(OmsOrderItem omsOrderItem);

  public FmsAddressInfoValueObject convertAddress(List<AddressInfo> addresses) {
    if (CollectionUtils.isNotEmpty(addresses)) {
      AddressInfo addressInfo = addresses.get(0);

      return FmsAddressInfoValueObject.builder()
          .addressOne(addressInfo.getAddressOne())
          .addressTwo(addressInfo.getAddressTwo())
          .addressThree(addressInfo.getAddressThree())
          .addressType(addressInfo.getAddressType())
          .city(addressInfo.getCity())
          .country(addressInfo.getCountry())
          .county(addressInfo.getCounty())
          .latitude(addressInfo.getLatitude())
          .longitude(addressInfo.getLongitude())
          .postalCode(addressInfo.getPostalCode())
          .state(addressInfo.getState())
          .build();
    } else {
      return null;
    }
  }

  public FmsOrderItemUpcValueObject convertUpcs(String upc) {
    return FmsOrderItemUpcValueObject.builder().upc(upc).build();
  }

  public FmsPickedItemUpcVo convertOmsPickedItemUPCToFMSVO(PickedItemUpc pickedItemUpc) {
    FmsPickedItemUpcVo fmsPickedItemUpcVo = new FmsPickedItemUpcVo();
    if (pickedItemUpc != null) {
      fmsPickedItemUpcVo.setStoreUnitPrice(pickedItemUpc.getStoreUnitPrice().getAmount());
      fmsPickedItemUpcVo.setQuantity(pickedItemUpc.getQuantity());
      fmsPickedItemUpcVo.setUom(pickedItemUpc.getUom());
      fmsPickedItemUpcVo.setUpc(pickedItemUpc.getUpc());
      fmsPickedItemUpcVo.setWeight(pickedItemUpc.getWeight());
      fmsPickedItemUpcVo.setWin(pickedItemUpc.getWin());
    }
    return fmsPickedItemUpcVo;
  }
}
