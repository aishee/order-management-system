package com.walmart.oms.infrastructure.gateway.orderservice;

import com.walmart.common.utils.NumberUtils;
import com.walmart.oms.order.aggregateroot.OmsOrder;
import com.walmart.oms.order.domain.entity.OmsOrderBundledItem;
import com.walmart.services.oms.order.common.model.BundleItemReference;
import com.walmart.services.oms.order.common.model.OrderBundleItem;
import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public class OmsOrderBundleItemMapper {

  public static final OmsOrderBundleItemMapper INSTANCE =
      Mappers.getMapper(OmsOrderBundleItemMapper.class);

  /**
   * map bundleItemInfo to the OrderBundleItem if present.
   *
   * @param omsOrder {@link OmsOrder}
   */
  public List<OrderBundleItem> mapToBundleItemInfo(OmsOrder omsOrder) {
    return omsOrder.getBundleItemToOrderItemMap().values().stream()
        .map(this::buildBundleItemInfo)
        .collect(Collectors.toList());
  }

  private List<BundleItemReference> mapToBundleItemReferences(
      List<OmsOrderBundledItem> bundledItemList) {
    return bundledItemList.stream()
        .map(this::buildBundleItemReference)
        .collect(Collectors.toList());
  }

  private BundleItemReference buildBundleItemReference(OmsOrderBundledItem bundledItem) {
    BundleItemReference bundleItemReference = new BundleItemReference();
    bundleItemReference.setQuantityInBundle((int) bundledItem.getItemQuantity());
    bundleItemReference.setSkuId(bundledItem.getOrderItemSkuId());
    return bundleItemReference;
  }

  private OrderBundleItem buildBundleItemInfo(List<OmsOrderBundledItem> bundledItemList) {
    List<BundleItemReference> bundleItemReferences = mapToBundleItemReferences(bundledItemList);
    String id = getBundleItemId(bundledItemList);
    OrderBundleItem orderBundleItem = new OrderBundleItem();
    orderBundleItem.setBundleItemReferences(bundleItemReferences);
    orderBundleItem.setWebItemDescription(getBundleDescription(bundledItemList));
    orderBundleItem.setQuantity(getBundleQuantity(bundledItemList));
    orderBundleItem.setBundleItemCount(getBundleItemCount(bundledItemList));
    orderBundleItem.setConsumerItemNumber(id);
    orderBundleItem.setUnitPrice(getTotalUnitPrice(bundledItemList));
    orderBundleItem.setItemId(id);
    return orderBundleItem;
  }

  private double getTotalUnitPrice(List<OmsOrderBundledItem> bundledItemList) {
    return NumberUtils.getRoundedDouble(bundledItemList.stream()
        .map(OmsOrderBundledItem::getBundleItemTotalPrice)
        .reduce(BigDecimal.ZERO, BigDecimal::add));
  }

  private String getBundleItemId(List<OmsOrderBundledItem> bundledItemList) {
    return bundledItemList.stream()
        .map(OmsOrderBundledItem::getBundleSkuId)
        .findFirst()
        .orElse(null);
  }

  private String getBundleDescription(List<OmsOrderBundledItem> bundledItemList) {
    return bundledItemList.stream()
        .map(OmsOrderBundledItem::getBundleDescription)
        .findFirst()
        .orElse(null);
  }

  private long getBundleQuantity(List<OmsOrderBundledItem> bundledItemList) {
    return bundledItemList.stream()
        .map(OmsOrderBundledItem::getBundleQuantity)
        .findFirst()
        .orElse(0L);
  }

  private int getBundleItemCount(List<OmsOrderBundledItem> bundledItemList) {
    return bundledItemList.stream()
        .map(OmsOrderBundledItem::getItemQuantity)
        .reduce(0L, Long::sum).intValue();
  }
}
