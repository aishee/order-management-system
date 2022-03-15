package com.walmart.oms.infrastructure.gateway.orderservice;

import com.walmart.oms.order.aggregateroot.OmsOrder;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;
import org.springframework.util.ObjectUtils;

/**
 * Map additional order attributes to custom order map.
 */
@Slf4j
@Mapper
public class OsOrderCustomAttributeMapper {

  public static final OsOrderCustomAttributeMapper INSTANCE =
      Mappers.getMapper(OsOrderCustomAttributeMapper.class);

  public static final String MAPPING_VERSION = "3";



  public static final ThreadLocal<SimpleDateFormat> SIMPLE_DATE_FORMAT_THREAD_LOCAL =
      ThreadLocal.withInitial(() -> new SimpleDateFormat("E MMM dd HH:mm:ss z yyyy"));

  /**
   * Maps omsOrder to customAttributes.
   *
   * @param omsOrder {@link OmsOrder}
   * @return Map of customAttributes
   */
  public Map<String, String> createCustomAttributeMap(OmsOrder omsOrder) {
    if (ObjectUtils.isEmpty(omsOrder)) {
      return Collections.emptyMap();
    }
    Map<String, String> customAttributes = new HashMap<>();
    customAttributes.put("mappingVersion", MAPPING_VERSION);
    customAttributes.put("status", omsOrder.getOrderState());
    customAttributes.put("storeId", omsOrder.getStoreId());
    customAttributes.put("vendorId", omsOrder.getVendorId());
    customAttributes.put("vendorOrderId", omsOrder.getVendorOrderId());
    customAttributes.put("webOrderTotal", String.valueOf(omsOrder.getOrderTotal()));
    addDeliveryDate(omsOrder, customAttributes);
    return customAttributes;
  }

  private void addDeliveryDate(OmsOrder omsOrder, Map<String, String> customAttributes) {
    Optional.ofNullable(omsOrder.getDeliveryDate())
        .map(deliveryDate -> SIMPLE_DATE_FORMAT_THREAD_LOCAL.get().format(deliveryDate))
        .ifPresent(
            formattedDeliveryTime -> customAttributes.put("deliveryTime", formattedDeliveryTime));
  }
}
