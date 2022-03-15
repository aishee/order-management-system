package com.walmart.oms.infrastructure.gateway.orderservice;

import com.walmart.oms.order.domain.entity.OmsOrderItem;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;
import org.springframework.util.ObjectUtils;

@Slf4j
@Mapper
public class OsOrderLineCustomAttributeMapper {

  public static final OsOrderLineCustomAttributeMapper INSTANCE =
      Mappers.getMapper(OsOrderLineCustomAttributeMapper.class);

  /**
   * Maps omsOrderItem to orderLineCustomAttributes.
   *
   * @param omsOrderItem {@link OmsOrderItem}
   * @return Map of orderLineCustomAttributes
   */
  public Map<String, String> createCustomAttributeMap(OmsOrderItem omsOrderItem) {
    if (ObjectUtils.isEmpty(omsOrderItem)) {
      return Collections.emptyMap();
    }
    Map<String, String> customAttributes = new HashMap<>();
    customAttributes.put(
        "order.substituteItem.substitutionOption", omsOrderItem.getSubstitutionOption().name());
    omsOrderItem
        .getSubstitutedItemVendorPrice()
        .ifPresent(
            subItemVendorPrice ->
                customAttributes.put(
                    "order.substituteItem.vendorPrice", String.valueOf(subItemVendorPrice)));
    return customAttributes;
  }
}
